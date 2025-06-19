WITH 
employee_hierarchy AS (
    SELECT 
        id , 
        name , 
        manager_id , 
        1 AS level , 
        ARRAY<BIGINT>[id] AS path
    FROM employees
    WHERE manager_id IS NULL 
    UNION ALL 
    SELECT 
        e.id , 
        e.name , 
        e.manager_id , 
        eh.level + 1 , 
        array_append(eh.path , 
                     e.id)
    FROM employees e
        JOIN employee_hierarchy eh
        ON e.manager_id = eh.id
) , 
sales_metrics AS (
    SELECT 
        s.salesperson_id , 
        COUNT(*) AS total_orders , 
        SUM(s.amount) AS total_sales , 
        AVG(s.amount) OVER (PARTITION BY s.region_id) AS region_avg , 
        RANK() OVER (ORDER BY SUM(s.amount) DESC) AS sales_rank
    FROM sales s
    WHERE s.date >= DATE'2024-01-01'
    GROUP BY 
        s.salesperson_id , 
        s.region_id
) , 
customer_segments AS (
    SELECT 
        c.id , 
        c.name , 
        CASE 
            WHEN SUM(o.amount) > 10000 THEN 'VIP'
            WHEN SUM(o.amount) > 5000 THEN 'Regular'
            ELSE 'Basic'
        END AS segment , 
        MAP{'purchases':COUNT(*) , 'last_order':MAX(o.date)} AS metrics
    FROM customers c
        LEFT JOIN orders o
        ON c.id = o.customer_id
    GROUP BY 
        c.id , 
        c.name
)
SELECT 
    e.name AS employee_name , 
    e.level AS hierarchy_level , 
    sm.total_orders , 
    sm.total_sales , 
    sm.region_avg , 
    sm.sales_rank , 
    cs.segment AS customer_segment , 
    cs.metrics['purchases'] AS customer_purchases , 
    ARRAY_AGG(DISTINCT p.name) AS premium_products , 
    NAMED_STRUCT('id' , 
                 e.id , 
                 'name' , 
                 e.name , 
                 'revenue' , 
                 sm.total_sales) AS employee_profile , 
    EXISTS (
        SELECT 
            1
        FROM projects pr
        WHERE pr.leader_id = e.id
            AND pr.status = 'active'
    ) AS is_project_leader , 
    (
        SELECT 
            json_arrayagg(json_object('project_id' , 
                                      p.id , 
                                      'budget' , 
                                      p.budget , 
                                      'team_size' , 
                                      (
                                          SELECT 
                                              COUNT(*)
                                          FROM project_members pm
                                          WHERE pm.project_id = p.id
                                      )))
        FROM projects p
        WHERE p.leader_id = e.id
    ) AS project_details
FROM employee_hierarchy e
    LEFT JOIN sales_metrics sm
    ON e.id = sm.salesperson_id
    LEFT JOIN customer_segments cs
    ON e.id = cs.id
    LEFT JOIN TABLE (flatten(ARRAY<INT>[1 , 2 , 3])) t
    ON true
    LEFT JOIN LATERAL (
        SELECT 
            ARRAY_AGG(product_id) AS product_ids
        FROM order_items
        WHERE order_id IN (
                SELECT 
                    id
                FROM orders
                WHERE salesperson_id = e.id
            )
    ) oi
    ON true
    LEFT JOIN products p
    ON p.id = ANY(oi.product_ids)
WHERE e.level <= 5
    AND sm.total_sales > (
        SELECT 
            AVG(total_sales)
        FROM sales_metrics
    )
GROUP BY 
    e.id , 
    e.name , 
    e.level , 
    sm.total_orders , 
    sm.total_sales , 
    sm.region_avg , 
    sm.sales_rank , 
    cs.segment , 
    cs.metrics
HAVING COUNT(DISTINCT p.id) > 0
ORDER BY 
    sm.sales_rank , 
    e.level
LIMIT 100;