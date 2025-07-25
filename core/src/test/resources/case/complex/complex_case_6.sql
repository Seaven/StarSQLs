WITH inventory_analysis AS (
    SELECT 
        p.id,
        p.name,
        p.category,
        SUM(i.quantity) as total_stock,
        AVG(i.quantity) OVER (PARTITION BY p.category) as category_avg,
        PERCENT_RANK() OVER (ORDER BY SUM(i.quantity)) as stock_rank,
        ARRAY_AGG(DISTINCT i.warehouse_id) as warehouse_ids,
        MAP{
            'min': MIN(i.quantity),
            'max': MAX(i.quantity),
            'avg': AVG(i.quantity)
        } as stock_metrics
    FROM products p
    JOIN inventory i ON p.id = i.product_id
    WHERE i.last_updated >= DATE '2024-01-01'
    GROUP BY p.id, p.name, p.category
),
supplier_performance AS (
    SELECT 
        s.id,
        s.name,
        COUNT(DISTINCT p.id) as product_count,
        SUM(p.price * i.quantity) as total_value,
        LEAD(SUM(p.price * i.quantity)) OVER (ORDER BY s.id) as next_supplier_value,
        LAG(SUM(p.price * i.quantity)) OVER (ORDER BY s.id) as prev_supplier_value,
        FIRST_VALUE(s.name) OVER (
            PARTITION BY p.category 
            ORDER BY SUM(p.price * i.quantity) DESC
        ) as top_supplier_by_category
    FROM suppliers s
    JOIN products p ON s.id = p.supplier_id
    JOIN inventory i ON p.id = i.product_id
    GROUP BY s.id, s.name, p.category
),
category_metrics AS (
    SELECT 
        c.id,
        c.name,
        COUNT(DISTINCT p.id) as product_count,
        SUM(p.price * i.quantity) as total_value,
        ARRAY_AGG(
            NAMED_STRUCT(
                'id', p.id,
                'name', p.name,
                'price', p.price
            )
        ) as top_products
    FROM categories c
    JOIN products p ON c.id = p.category_id
    JOIN inventory i ON p.id = i.product_id
    GROUP BY c.id, c.name
)
SELECT 
    ia.name as product_name,
    ia.category,
    ia.total_stock,
    ia.category_avg,
    ia.stock_rank,
    ia.warehouse_ids[1] as primary_warehouse,
    ia.stock_metrics['min'] as min_stock,
    ia.stock_metrics['max'] as max_stock,
    sp.name as supplier_name,
    sp.product_count as supplier_products,
    sp.total_value as supplier_value,
    sp.next_supplier_value,
    sp.prev_supplier_value,
    sp.top_supplier_by_category,
    cm.product_count as category_products,
    cm.total_value as category_value,
    cm.top_products[1].name as top_product,
    (
        SELECT json_object(
            'product_id', p.id,
            'name', p.name,
            'price', p.price,
            'stock', i.quantity,
            'warehouse', w.name
        )
        FROM products p
        JOIN inventory i ON p.id = i.product_id
        JOIN warehouses w ON i.warehouse_id = w.id
        WHERE p.id = ia.id
        ORDER BY i.quantity DESC
        LIMIT 1
    ) as stock_details,
    (
        SELECT ARRAY_AGG(
            NAMED_STRUCT(
                'date', h.date,
                'quantity', h.quantity,
                'price', h.price
            )
        )
        FROM inventory_history h
        WHERE h.product_id = ia.id
        AND h.date >= DATE '2024-01-01'
    ) as historical_data
FROM 
    inventory_analysis ia
    LEFT JOIN supplier_performance sp ON ia.id = sp.id
    LEFT JOIN category_metrics cm ON ia.category = cm.name
    LEFT JOIN TABLE(flatten(ia.warehouse_ids)) w ON true
    LEFT JOIN LATERAL (
        SELECT 
            ARRAY_AGG(DISTINCT o.id) as order_ids,
            MAP{
                'total': COUNT(*),
                'avg_amount': AVG(o.amount)
            } as order_metrics
        FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        WHERE oi.product_id = ia.id
    ) o ON true
WHERE 
    ia.total_stock > ia.category_avg
    AND sp.total_value > (
        SELECT AVG(total_value) 
        FROM supplier_performance
    )
    AND EXISTS (
        SELECT 1 
        FROM inventory_history h 
        WHERE h.product_id = ia.id 
        AND h.quantity > 0
    )
GROUP BY 
    ia.id, ia.name, ia.category, ia.total_stock, ia.category_avg, ia.stock_rank,
    ia.warehouse_ids, ia.stock_metrics,
    sp.id, sp.name, sp.product_count, sp.total_value, sp.next_supplier_value,
    sp.prev_supplier_value, sp.top_supplier_by_category,
    cm.id, cm.name, cm.product_count, cm.total_value, cm.top_products
HAVING 
    COUNT(DISTINCT w) > 0
    AND o.order_metrics['total'] > 0
ORDER BY 
    ia.stock_rank,
    sp.total_value DESC
LIMIT 50; 