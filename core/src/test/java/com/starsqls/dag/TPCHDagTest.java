// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starsqls.dag;

import com.starsqls.PrinterTestBase;
import com.starsqls.dag.model.DagGraph;
import com.starsqls.dag.model.DagNode;
import com.starsqls.dag.model.NodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for SQLDagAnalyzer
 */
public class TPCHDagTest extends PrinterTestBase {
    
    private final SQLDagAnalyzer analyzer = new SQLDagAnalyzer();
    
    @Test
    public void testJoinWithWhere() {
        String sql = sql("tpch/q2.sql");
        DagGraph graph = analyzer.analyze(sql);
        System.out.println(graph.toTreeString());
    }
    
    @Test
    public void testSubqueriesInAllPlaces() {
        String sql = """
                SELECT
                    name,
                    (SELECT MAX(salary) FROM employees e2 WHERE e2.dept_id = e.dept_id) as max_dept_salary,
                    salary
                FROM employees e
                WHERE salary > (SELECT AVG(salary) FROM employees)
                GROUP BY dept_id, name, salary
                HAVING COUNT(*) > (SELECT MIN(cnt) FROM dept_stats)
                ORDER BY (SELECT rank FROM dept_ranking WHERE dept_id = e.dept_id)
                """;

        DagGraph graph = analyzer.analyze(sql);
        System.out.println("\n=== Test Subqueries in All Places ===");
        System.out.println(graph.toTreeString());

        // Verify that subqueries were extracted
        long subqueryCount = graph.getNodes().stream()
                .filter(n -> n.getType() == NodeType.SUBQUERY)
                .count();

        System.out.println("\nTotal SUBQUERY nodes found: " + subqueryCount);
        assertTrue(subqueryCount >= 4, "Should find at least 4 subqueries (SELECT, WHERE, HAVING, ORDER BY)");
    }
    
        
    @Test
    public void testComplexSQLDag() {
        String sql = sql("xx.sql");
        DagGraph graph = analyzer.analyze(sql);
        System.out.println(graph.toTreeString());
    }
}

