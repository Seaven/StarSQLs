-- cte_window_function.sql
-- WITH/CTE and window function
WITH t1 AS (SELECT 1 AS id, 100 AS val), t2 AS (SELECT 2 AS id, 200 AS val)
SELECT id, val, ROW_NUMBER() OVER (ORDER BY val) rn FROM t1 UNION ALL SELECT * FROM t2;

