-- nested_select_scalar_subquery.sql
-- Nested SELECT and scalar subquery
SELECT (SELECT MAX(x) FROM (VALUES (1), (2)) AS t2(x)) AS max_x FROM (SELECT 1) t1;

