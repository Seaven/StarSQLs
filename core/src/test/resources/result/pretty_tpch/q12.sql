SELECT 
    l_shipmode , 
    SUM(CASE 
        WHEN o_orderpriority = '1-URGENT'
        OR o_orderpriority = '2-HIGH' THEN CAST(1 AS BIGINT)
        ELSE CAST(0 AS BIGINT)
    END) AS high_line_count , 
    SUM(CASE 
        WHEN o_orderpriority <> '1-URGENT'
        AND o_orderpriority <> '2-HIGH' THEN CAST(1 AS BIGINT)
        ELSE CAST(0 AS BIGINT)
    END) AS low_line_count
FROM orders , lineitem
WHERE o_orderkey = l_orderkey
    AND l_shipmode IN 
    ('REG AIR' , 'MAIL')
    AND l_commitdate < l_receiptdate
    AND l_shipdate < l_commitdate
    AND l_receiptdate >= date'1997-01-01'
    AND l_receiptdate < date'1998-01-01'
GROUP BY 
    l_shipmode
ORDER BY 
    l_shipmode;