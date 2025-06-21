SELECT 
    c_count , 
    COUNT(*) AS custdist
FROM (
        SELECT 
            c_custkey , 
            COUNT(o_orderkey) AS c_count
        FROM customer
            LEFT OUTER JOIN orders
            ON c_custkey = o_custkey
            AND o_comment NOT LIKE '%unusual%deposits%'
        GROUP BY 
            c_custkey
    ) a
GROUP BY 
    c_count
ORDER BY 
    custdist DESC , 
    c_count DESC;