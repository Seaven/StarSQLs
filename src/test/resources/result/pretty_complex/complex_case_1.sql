WITH 
sales_cte AS (
    SELECT 
        s.seller_id , 
        SUM(s.amount) AS total_sales
    FROM saless
    WHERE s.date BETWEEN DATE'2024-01-01' AND DATE'2024-12-31'
    GROUP BY 
        s.seller_id
    HAVING SUM(s.amount) > 10000
    ) , 
ranked_customers AS (
    SELECT 
        c.id , 
        c.name , 
        RANK() OVER (ORDER BY c.created_at DESC) AS rnk
    FROM customersc
    )
SELECT 
    c.id AS customer_id , 
    c.name , 
    o.id AS order_id , 
    o.status , 
    o.created_at , 
    p.name AS product_name , 
    p.price , 
    COALESCE(o.discount , 
    0) AS discount , 
    CASE 
        WHEN o.status = 'shipped' THEN TRUE
        ELSE FALSE
    END AS shipped_flag , 
    ARRAY<INT>[1 , 2 , 3] AS arr , 
    MAP{'a':1 , 'b':2} AS mp , 
    s.total_sales , 
    SUM(oi.quantity*p.price) OVER (PARTITION BY c.id) AS total_spent , 
    EXISTS (SELECT 
                1
            FROM returnsr
            WHERE r.order_id = o.id
            ) AS has_return , 
    (SELECT 
         MAX(amount)
     FROM payments
     WHERE payments.order_id = o.id
     ) AS max_payment
FROM ranked_customersc
    LEFT JOIN orderso
    ON c.id = o.customer_id
    LEFT SEMI JOIN sales_ctes
    ON s.seller_id = o.seller_id
    RIGHT ANTI JOIN blacklistb
    ON b.customer_id = c.id
    INNER JOIN order_itemsoi
    ON o.id = oi.order_id
    JOIN productsp
    ON oi.product_id = p.id
WHERE c.rnk <= 100
    AND o.status IN 
    ('shipped' , 'delivered')
    AND p.price BETWEEN 10 AND 1000
    AND o.created_at > DATE'2024-01-01'
ORDER BY 
    total_spent DESC , 
    c.name
LIMIT 50 OFFSET 10;