-- subquery_in_exists.sql
-- Subquery, IN and EXISTS
SELECT * FROM (SELECT 1 AS x) t WHERE x IN (SELECT 1) AND EXISTS (SELECT 1 WHERE 1=1);

