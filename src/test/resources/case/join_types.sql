-- join_types.sql
-- Various JOIN types
SELECT a.id, b.name FROM (SELECT 1 AS id) a LEFT JOIN (SELECT 1 AS id, 'foo' AS name) b ON a.id = b.id RIGHT JOIN (SELECT 2 AS id) c ON b.id = c.id;

