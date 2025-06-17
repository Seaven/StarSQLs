WITH 
hierarchy AS (
    SELECT 
        id , 
        parent_id , 
        name , 
        1 AS level
    FROM departments
    WHERE parent_id IS NULL 
    UNION ALL 
    SELECT 
        d.id , 
        d.parent_id , 
        d.name , 
        h.level+1
    FROM departmentsd
        JOIN hierarchyh
        ON d.parent_id = h.id
    ) , 
latest_salary AS (
    SELECT 
        employee_id , 
        MAX(salary) AS max_salary
    FROM salaries
    GROUP BY 
        employee_id
    )
SELECT 
    e.id AS emp_id , 
    e.name , 
    h.name AS dept_name , 
    h.level , 
    ls.max_salary , 
    AVG(ls.max_salary) OVER (PARTITION BY h.level) AS avg_salary_by_level , 
    COUNT(*) OVER () AS total_employees , 
    CASE 
        WHEN ls.max_salary > 10000 THEN 'high'
        ELSE 'normal'
    END AS salary_level , 
    ARRAY<BIGINT>[ls.max_salary , 10000] AS salary_arr , 
    EXISTS (SELECT 
                1
            FROM awardsa
            WHERE a.employee_id = e.id
            ) AS has_award , 
    (SELECT 
         COUNT(*)
     FROM projectsp
     WHERE p.leader_id = e.id
     ) AS project_count
FROM employeese
    LEFT JOIN hierarchyh
    ON e.dept_id = h.id
    RIGHT JOIN latest_salaryls
    ON e.id = ls.employee_id
    FULL OUTER JOIN managersm
    ON e.manager_id = m.id
    LEFT SEMI JOIN mentorsmt
    ON mt.mentee_id = e.id
    LEFT ANTI JOIN blacklistb
    ON b.employee_id = e.id
WHERE e.status = 'active'
    AND h.level <= 5
GROUP BY 
    e.id , 
    e.name , 
    h.name , 
    h.level , 
    ls.max_salary
UNION 
SELECT 
    *
FROM (SELECT 
          1 , 
          'dummy' , 
          'dummy' , 
          0 , 
          0 , 
          0 , 
          0 , 
          'dummy' , 
          ARRAY<TINYINT>[0] , 
          row(0 , 
          '') , 
          FALSE , 
          0
      
      ) AS dummy
ORDER BY 
    max_salary DESC , 
    e.name
LIMIT 100;