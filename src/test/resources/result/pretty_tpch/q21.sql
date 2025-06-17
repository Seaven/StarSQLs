SELECT 
    s_name , 
    COUNT(*) AS numwait
FROM supplier , lineiteml1 , orders , nation
WHERE s_suppkey = l1.l_suppkey
    AND o_orderkey = l1.l_orderkey
    AND o_orderstatus = 'F'
    AND l1.l_receiptdate > l1.l_commitdate
    AND EXISTS (SELECT 
                    *
                FROM lineiteml2
                WHERE l2.l_orderkey = l1.l_orderkey
                    AND l2.l_suppkey <> l1.l_suppkey
                )
    AND NOT EXISTS (SELECT 
                        *
                    FROM lineiteml3
                    WHERE l3.l_orderkey = l1.l_orderkey
                        AND l3.l_suppkey <> l1.l_suppkey
                        AND l3.l_receiptdate > l3.l_commitdate
                    )
    AND s_nationkey = n_nationkey
    AND n_name = 'CANADA'
GROUP BY 
    s_name
ORDER BY 
    numwait DESC , 
    s_name
LIMIT 100;