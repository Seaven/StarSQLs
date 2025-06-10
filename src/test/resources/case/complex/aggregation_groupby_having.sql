-- aggregation_groupby_having.sql
-- Aggregation, GROUP BY and HAVING
SELECT col1, COUNT(*), SUM(col2) FROM (VALUES (1, 10), (2, 20), (1, 30)) AS t(col1, col2) GROUP BY col1 HAVING SUM(col2) > 20;

