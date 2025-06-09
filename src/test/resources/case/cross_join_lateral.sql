-- cross_join_lateral.sql
-- CROSS JOIN and LATERAL
SELECT * FROM (VALUES (1)) t1 CROSS JOIN LATERAL (SELECT t1.column1 + 1 AS y) t2;
