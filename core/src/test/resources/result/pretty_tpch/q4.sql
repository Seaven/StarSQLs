SELECT 
    o_orderpriority , 
    COUNT(*) AS order_count
FROM orders
WHERE o_orderdate >= date'1994-09-01'
    AND o_orderdate < date'1994-12-01'
    AND EXISTS (
        SELECT 
            *
        FROM lineitem
        WHERE l_orderkey = o_orderkey
            AND l_receiptdate > l_commitdate
    )
GROUP BY 
    o_orderpriority
ORDER BY 
    o_orderpriority;