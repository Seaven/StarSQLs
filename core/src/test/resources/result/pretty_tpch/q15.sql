SELECT 
    s_suppkey , 
    s_name , 
    s_address , 
    s_phone , 
    total_revenue
FROM supplier , (
        SELECT 
            l_suppkey AS supplier_no , 
            SUM(l_extendedprice * (1 - l_discount)) AS total_revenue
        FROM lineitem
        WHERE l_shipdate >= date'1995-07-01'
            AND l_shipdate < date'1995-10-01'
        GROUP BY 
            l_suppkey
    ) a
WHERE s_suppkey = supplier_no
    AND total_revenue = (
        SELECT 
            MAX(total_revenue)
        FROM (
                SELECT 
                    l_suppkey AS supplier_no , 
                    SUM(l_extendedprice * (1 - l_discount)) AS total_revenue
                FROM lineitem
                WHERE l_shipdate >= date'1995-07-01'
                    AND l_shipdate < date'1995-10-01'
                GROUP BY 
                    l_suppkey
            ) b
    )
ORDER BY 
    s_suppkey;