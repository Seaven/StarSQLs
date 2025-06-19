/* Main query to analyze customer behavior and product performance */WITH 
customer_segments AS (/* Identify customer segments based on purchase behavior */
    SELECT 
        c.id , 
        c.name ,/* Calculate total spending and frequency */ 
        SUM(o.amount) AS total_spent , 
        COUNT(DISTINCT o.id) AS order_count ,/* Segment customers based on spending patterns */ 
        CASE 
            WHEN SUM(o.amount) > 10000 THEN 'VIP'
            WHEN SUM(o.amount) > 5000 THEN 'Regular'
            ELSE 'Basic'
        END AS segment ,/* Track customer preferences */ 
        ARRAY_AGG(DISTINCT p.category) AS preferred_categories
    FROM customers c
        LEFT JOIN orders o
        ON c.id = o.customer_id
        LEFT JOIN order_items oi
        ON o.id = oi.order_id
        LEFT JOIN products p
        ON oi.product_id = p.id
    WHERE o.order_date >= DATE'2024-01-01'
    GROUP BY 
        c.id , 
        c.name
) , 
product_analysis AS (/* Analyze product performance metrics */
    SELECT 
        p.id , 
        p.name , 
        p.category ,/* Calculate sales metrics */ 
        COUNT(DISTINCT o.id) AS order_count , 
        SUM(oi.quantity) AS total_quantity , 
        SUM(oi.quantity * oi.price) AS total_revenue ,/* Calculate average order value */ 
        AVG(oi.quantity * oi.price) OVER (PARTITION BY p.category) AS category_avg ,/* Track product popularity */ 
        RANK() OVER (ORDER BY SUM(oi.quantity) DESC) AS popularity_rank ,/* Store detailed metrics */ 
        MAP{'min_price':MIN(oi.price) , 'max_price':MAX(oi.price) , 'avg_price':AVG(oi.price)} AS price_metrics
    FROM products p
        JOIN order_items oi
        ON p.id = oi.product_id
        JOIN orders o
        ON oi.order_id = o.id
    WHERE o.order_date >= DATE'2024-01-01'
    GROUP BY 
        p.id , 
        p.name , 
        p.category
) , 
inventory_status AS (/* Monitor inventory levels and movements */
    SELECT 
        i.product_id , 
        i.warehouse_id ,/* Calculate current stock levels */ 
        SUM(i.quantity) AS current_stock ,/* Track stock movements */ 
        SUM(CASE 
            WHEN i.movement_type = 'IN' THEN i.quantity
            ELSE 0
        END) AS stock_in , 
        SUM(CASE 
            WHEN i.movement_type = 'OUT' THEN i.quantity
            ELSE 0
        END) AS stock_out ,/* Calculate stock turnover */ 
        COUNT(DISTINCT i.movement_date) AS turnover_days ,/* Store warehouse details */ 
        NAMED_STRUCT('name' , 
                     w.name , 
                     'location' , 
                     w.location) AS warehouse_info
    FROM inventory_movements i
        JOIN warehouses w
        ON i.warehouse_id = w.id
    WHERE i.movement_date >= DATE'2024-01-01'
    GROUP BY 
        i.product_id , 
        i.warehouse_id , 
        w.name , 
        w.location
)/* Main query to combine all analyses */
SELECT/* Customer information */ 
    cs.name AS customer_name , 
    cs.segment AS customer_segment , 
    cs.total_spent , 
    cs.order_count AS customer_orders , 
    cs.preferred_categories[1] AS top_category ,/* Product information */ 
    pa.name AS product_name , 
    pa.category AS product_category , 
    pa.order_count AS product_orders , 
    pa.total_quantity , 
    pa.total_revenue , 
    pa.category_avg , 
    pa.popularity_rank , 
    pa.price_metrics['avg_price'] AS avg_price ,/* Inventory information */ 
    ins.current_stock , 
    ins.stock_in , 
    ins.stock_out , 
    ins.turnover_days , 
    ins.warehouse_info.name AS warehouse_name ,/* Additional metrics */ 
    (/* Calculate customer-product relationship metrics */
        SELECT 
            json_object('customer_id' , 
                        c.id , 
                        'product_id' , 
                        p.id , 
                        'metrics' , 
                        json_object('purchase_count' , 
                                    COUNT(*) , 
                                    'last_purchase' , 
                                    MAX(o.order_date) , 
                                    'avg_quantity' , 
                                    AVG(oi.quantity)))
        FROM customers c
            JOIN orders o
            ON c.id = o.customer_id
            JOIN order_items oi
            ON o.id = oi.order_id
            JOIN products p
            ON oi.product_id = p.id
        WHERE c.id = cs.id
            AND p.id = pa.id
        GROUP BY 
            c.id , 
            p.id
    ) AS customer_product_metrics ,/* Historical data */ 
    (/* Get historical order data */
        SELECT 
            ARRAY_AGG(NAMED_STRUCT('date' , 
                                   o.order_date , 
                                   'quantity' , 
                                   oi.quantity , 
                                   'amount' , 
                                   oi.quantity * oi.price))
        FROM orders o
            JOIN order_items oi
            ON o.id = oi.order_id
        WHERE o.customer_id = cs.id
            AND oi.product_id = pa.id
            AND o.order_date >= DATE'2024-01-01'
    ) AS order_history
FROM customer_segments cs
    CROSS JOIN product_analysis pa
    
    LEFT JOIN inventory_status ins
    ON pa.id = ins.product_id/* Join with customer preferences */
    LEFT JOIN LATERAL (
        SELECT 
            ARRAY_AGG(DISTINCT p.category) AS categories , 
            MAP{'count':COUNT(*) , 'avg_price':AVG(p.price)} AS category_metrics
        FROM products p
            JOIN order_items oi
            ON p.id = oi.product_id
            JOIN orders o
            ON oi.order_id = o.id
        WHERE o.customer_id = cs.id
    ) cp
    ON true
WHERE/* Filter conditions */ cs.total_spent > 1000
    AND pa.total_revenue > pa.category_avg
    AND ins.current_stock > 0
    AND EXISTS (/* Check for recent orders */
        SELECT 
            1
        FROM orders o
            JOIN order_items oi
            ON o.id = oi.order_id
        WHERE o.customer_id = cs.id
            AND oi.product_id = pa.id
            AND o.order_date >= DATE'2024-01-01'
    )
GROUP BY/* Group by all non-aggregated columns */ 
    cs.id , 
    cs.name , 
    cs.segment , 
    cs.total_spent , 
    cs.order_count , 
    cs.preferred_categories , 
    pa.id , 
    pa.name , 
    pa.category , 
    pa.order_count , 
    pa.total_quantity , 
    pa.total_revenue , 
    pa.category_avg , 
    pa.popularity_rank , 
    pa.price_metrics , 
    ins.product_id , 
    ins.warehouse_id , 
    ins.current_stock , 
    ins.stock_in , 
    ins.stock_out , 
    ins.turnover_days , 
    ins.warehouse_info
HAVING/* Additional filtering conditions */ COUNT(DISTINCT ins.warehouse_id) > 0
AND cp.category_metrics['count'] > 0
ORDER BY/* Sort by most important metrics */ 
    cs.total_spent DESC , 
    pa.total_revenue DESC
LIMIT 50;