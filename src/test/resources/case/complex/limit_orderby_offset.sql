-- limit_orderby_offset.sql
-- LIMIT, ORDER BY and OFFSET
SELECT * FROM (VALUES (1), (2), (3)) AS t(x) ORDER BY x DESC LIMIT 2 OFFSET 1;

