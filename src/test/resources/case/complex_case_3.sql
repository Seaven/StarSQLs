-- complex_case_3.sql
-- Complex SQL: deep nesting, multiple window/aggregate functions, all join types, advanced expressions
WITH user_activity AS (
    SELECT u.id AS user_id, COUNT(a.id) AS activity_count
    FROM users u
    LEFT JOIN activities a ON u.id = a.user_id
    WHERE a.timestamp > CURRENT_DATE - INTERVAL '90' DAY
    GROUP BY u.id
),
latest_orders AS (
    SELECT o.id, o.customer_id, o.created_at
    FROM orders o
    WHERE o.created_at = (SELECT MAX(created_at) FROM orders WHERE customer_id = o.customer_id)
)
SELECT
    u.user_id,
    u.activity_count,
    lo.id AS last_order_id,
    lo.created_at AS last_order_date,
    SUM(p.price * oi.quantity) AS total_order_value,
    MIN(p.price) OVER (PARTITION BY u.user_id) AS min_price,
    MAX(p.price) OVER (PARTITION BY u.user_id) AS max_price,
    COUNT(*) OVER () AS total_rows,
    CASE WHEN u.activity_count > 50 THEN 'active' ELSE 'inactive' END AS activity_level,
    ARRAY_AGG(DISTINCT p.category) AS categories,
    EXISTS (SELECT 1 FROM reviews r WHERE r.user_id = u.user_id AND r.rating = 5) AS has_5star_review,
    (SELECT COUNT(*) FROM wishlist w WHERE w.user_id = u.user_id) AS wishlist_count
FROM
    user_activity u
    LEFT JOIN latest_orders lo ON u.user_id = lo.customer_id
    RIGHT JOIN order_items oi ON lo.id = oi.order_id
    FULL OUTER JOIN products p ON oi.product_id = p.id
    LEFT SEMI JOIN premium_users pu ON pu.user_id = u.user_id
    LEFT ANTI JOIN banned_users bu ON bu.user_id = u.user_id
WHERE
    u.activity_count IS NOT NULL
    AND p.price > 0
GROUP BY
    u.user_id, u.activity_count, lo.id, lo.created_at
ORDER BY
    total_order_value DESC, u.user_id
LIMIT 200;

