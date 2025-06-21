WITH 
sales_data AS (
    SELECT 
        'retail' AS channel , 
        s.store_id , 
        s.product_id , 
        s.sale_date , 
        s.quantity , 
        s.amount , 
        RANK() OVER (PARTITION BY s.store_id ORDER BY s.amount DESC) AS store_rank
    FROM retail_sales s
    WHERE s.sale_date >= DATE'2024-01-01'
    UNION ALL 
    SELECT 
        'online' AS channel , 
        o.store_id , 
        o.product_id , 
        o.order_date AS sale_date , 
        o.quantity , 
        o.amount , 
        RANK() OVER (PARTITION BY o.store_id ORDER BY o.amount DESC) AS store_rank
    FROM online_orders o
    WHERE o.order_date >= DATE'2024-01-01'
    UNION ALL 
    SELECT 
        'wholesale' AS channel , 
        w.client_id AS store_id , 
        w.product_id , 
        w.order_date AS sale_date , 
        w.quantity , 
        w.amount , 
        RANK() OVER (PARTITION BY w.client_id ORDER BY w.amount DESC) AS store_rank
    FROM wholesale_orders w
    WHERE w.order_date >= DATE'2024-01-01'
) , 
product_metrics AS (
    SELECT 
        p.id , 
        p.name , 
        p.category , 
        COUNT(DISTINCT sd.store_id) AS store_count , 
        SUM(sd.quantity) AS total_quantity , 
        SUM(sd.amount) AS total_amount , 
        AVG(sd.amount) OVER (PARTITION BY p.category) AS category_avg , 
        ARRAY_AGG(NAMED_STRUCT('channel' , 
                               sd.channel , 
                               'amount' , 
                               sd.amount)) AS channel_metrics
    FROM products p
        JOIN sales_data sd
        ON p.id = sd.product_id
    GROUP BY 
        p.id , 
        p.name , 
        p.category
) , 
store_performance AS (
    SELECT 
        s.id , 
        s.name , 
        s.region , 
        COUNT(DISTINCT sd.product_id) AS product_count , 
        SUM(sd.amount) AS total_sales , 
        AVG(sd.amount) OVER (PARTITION BY s.region) AS region_avg , 
        MAP{'retail':SUM(CASE 
            WHEN sd.channel = 'retail' THEN sd.amount
            ELSE 0
        END) , 'online':SUM(CASE 
            WHEN sd.channel = 'online' THEN sd.amount
            ELSE 0
        END) , 'wholesale':SUM(CASE 
            WHEN sd.channel = 'wholesale' THEN sd.amount
            ELSE 0
        END)} AS channel_sales
    FROM stores s
        JOIN sales_data sd
        ON s.id = sd.store_id
    GROUP BY 
        s.id , 
        s.name , 
        s.region
)
SELECT 
    pm.name AS product_name , 
    pm.category , 
    pm.store_count , 
    pm.total_quantity , 
    pm.total_amount , 
    pm.category_avg , 
    pm.channel_metrics[1].channel AS top_channel , 
    sp.name AS top_store , 
    sp.region AS store_region , 
    sp.product_count AS store_products , 
    sp.total_sales AS store_sales , 
    sp.region_avg , 
    sp.channel_sales['retail'] AS retail_sales , 
    sp.channel_sales['online'] AS online_sales , 
    sp.channel_sales['wholesale'] AS wholesale_sales , 
    (
        SELECT 
            json_object('product_id' , 
                        p.id , 
                        'name' , 
                        p.name , 
                        'category' , 
                        p.category , 
                        'metrics' , 
                        json_object('stores' , 
                                    COUNT(DISTINCT sd.store_id) , 
                                    'quantity' , 
                                    SUM(sd.quantity) , 
                                    'amount' , 
                                    SUM(sd.amount)))
        FROM products p
            JOIN sales_data sd
            ON p.id = sd.product_id
        WHERE p.id = pm.id
        GROUP BY 
            p.id , 
            p.name , 
            p.category
    ) AS product_details , 
    (
        SELECT 
            ARRAY_AGG(NAMED_STRUCT('date' , 
                                   sd.sale_date , 
                                   'channel' , 
                                   sd.channel , 
                                   'amount' , 
                                   sd.amount))
        FROM sales_data sd
        WHERE sd.product_id = pm.id
            AND sd.sale_date >= DATE'2024-01-01'
    ) AS sales_history
FROM product_metrics pm
    LEFT JOIN store_performance sp
    ON pm.id = sp.id
    LEFT JOIN LATERAL (
        SELECT 
            ARRAY_AGG(DISTINCT c.id) AS customer_ids , 
            MAP{'total':COUNT(*) , 'avg_amount':AVG(o.amount)} AS customer_metrics
        FROM customers c
            JOIN orders o
            ON c.id = o.customer_id
            JOIN order_items oi
            ON o.id = oi.order_id
        WHERE oi.product_id = pm.id
    ) c
    ON true
WHERE pm.total_amount > pm.category_avg
    AND sp.total_sales > (
        SELECT 
            AVG(total_sales)
        FROM store_performance
    )
    AND EXISTS (
        SELECT 
            1
        FROM sales_data sd
        WHERE sd.product_id = pm.id
            AND sd.amount > 0
    )
GROUP BY 
    pm.id , 
    pm.name , 
    pm.category , 
    pm.store_count , 
    pm.total_quantity , 
    pm.total_amount , 
    pm.category_avg , 
    pm.channel_metrics , 
    sp.id , 
    sp.name , 
    sp.region , 
    sp.product_count , 
    sp.total_sales , 
    sp.region_avg , 
    sp.channel_sales
HAVING COUNT(DISTINCT c.customer_ids) > 0
AND c.customer_metrics['total'] > 0
ORDER BY 
    pm.total_amount DESC , 
    sp.total_sales DESC
LIMIT 50;