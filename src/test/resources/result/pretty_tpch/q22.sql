SELECT 
    cntrycode , 
    COUNT(*) AS numcust , 
    SUM(c_acctbal) AS totacctbal
FROM (
        SELECT 
            substring(c_phone , 
                      1 , 
                      2) AS cntrycode , 
            c_acctbal
        FROM customer
        WHERE substring(c_phone , 
                        1 , 
                        2) IN 
            ('21' , '28' , '24' , '32' , '35' , '34' , '37')
            AND c_acctbal > (
                SELECT 
                    AVG(c_acctbal)
                FROM customer
                WHERE c_acctbal > 0.00
                    AND substring(c_phone , 
                                  1 , 
                                  2) IN 
                    ('21' , '28' , '24' , '32' , '35' , '34' , '37')
            )
            AND NOT EXISTS (
                SELECT 
                    *
                FROM orders
                WHERE o_custkey = c_custkey
            )
    ) AS custsale
GROUP BY 
    cntrycode
ORDER BY 
    cntrycode;