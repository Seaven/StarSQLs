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

import com.starsqls.dag.model.DagGraph;
import com.starsqls.dag.model.DagNode;
import com.starsqls.dag.model.NodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for SQLDagAnalyzer
 */
public class SQLDagAnalyzerTest {
    
    private final SQLDagAnalyzer analyzer = new SQLDagAnalyzer();
    
    @Test
    public void testSimpleScan() {
        String sql = "SELECT * FROM users";
        DagGraph graph = analyzer.analyze(sql);
        
        assertNotNull(graph);
        assertTrue(graph.getNodes().size() >= 2); // At least SCAN + RESULT
        
        // Check for SCAN node
        boolean hasScan = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.SCAN);
        assertTrue(hasScan, "Should have SCAN node");
        
        // Check for RESULT node
        boolean hasResult = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.RESULT);
        assertTrue(hasResult, "Should have RESULT node");
        
        System.out.println("Simple scan test passed: " + graph.getStats());
    }
    
    @Test
    public void testScanWithWhere() {
        String sql = "SELECT * FROM users WHERE active = true";
        DagGraph graph = analyzer.analyze(sql);
        
        assertNotNull(graph);
        
        // Check for SCAN node
        boolean hasScan = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.SCAN);
        assertTrue(hasScan, "Should have SCAN node");
        
        // Check for FILTER node
        boolean hasFilter = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.FILTER);
        assertTrue(hasFilter, "Should have FILTER node");
        
        System.out.println("Scan with WHERE test passed: " + graph.getStats());
    }
    
    @Test
    public void testProjection() {
        String sql = "SELECT name, age * 2 AS double_age FROM users WHERE active = true";
        DagGraph graph = analyzer.analyze(sql);
        
        assertNotNull(graph);
        
        // Check for PROJECT node (due to expression age * 2)
        boolean hasProject = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.PROJECT);
        assertTrue(hasProject, "Should have PROJECT node for expressions");
        
        System.out.println("Projection test passed: " + graph.getStats());
    }
    
    @Test
    public void testNodeDetails() {
        String sql = "SELECT name FROM users WHERE status = 'active'";
        DagGraph graph = analyzer.analyze(sql);
        
        // Find FILTER node and check details
        DagNode filterNode = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.FILTER)
            .findFirst()
            .orElse(null);
        
        assertNotNull(filterNode, "Should have FILTER node");
        assertNotNull(filterNode.getDetails(), "Filter node should have details");
        assertFalse(filterNode.getDetails().isEmpty(), "Filter details should not be empty");
        
        System.out.println("Filter node details: " + filterNode.getDetails());
        System.out.println("Filter SQL fragment: " + filterNode.getSqlFragment());
    }
    
    @Test
    public void testEdgeCreation() {
        String sql = "SELECT * FROM users WHERE active = true";
        DagGraph graph = analyzer.analyze(sql);
        
        // Should have edges connecting nodes
        assertTrue(graph.getEdges().size() >= 2, "Should have at least 2 edges");
        
        // Find SCAN node
        DagNode scanNode = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.SCAN)
            .findFirst()
            .orElse(null);
        
        assertNotNull(scanNode);
        
        // SCAN should have outgoing edge
        assertFalse(graph.getOutgoingEdges(scanNode.getId()).isEmpty(), 
                    "SCAN node should have outgoing edge");
        
        System.out.println("Edge creation test passed: " + graph.getEdges().size() + " edges");
    }
    
    @Test
    public void testSimpleJoin() {
        String sql = "SELECT u.name, o.amount FROM users u INNER JOIN orders o ON u.id = o.user_id";
        DagGraph graph = analyzer.analyze(sql);
        
        assertNotNull(graph);
        
        // Should have 2 SCAN nodes (users, orders)
        long scanCount = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.SCAN)
            .count();
        assertEquals(2, scanCount, "Should have 2 SCAN nodes");
        
        // Should have 1 JOIN node
        boolean hasJoin = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.JOIN);
        assertTrue(hasJoin, "Should have JOIN node");
        
        // Find JOIN node
        DagNode joinNode = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.JOIN)
            .findFirst()
            .orElse(null);
        
        assertNotNull(joinNode);
        assertEquals("INNER", joinNode.getData().get("joinType"), "Should be INNER JOIN");
        
        // JOIN node should have 2 incoming edges (from both tables)
        assertEquals(2, graph.getIncomingEdges(joinNode.getId()).size(),
                    "JOIN should have 2 incoming edges");
        
        System.out.println("Simple JOIN test passed: " + graph.getStats());
        System.out.println("JOIN condition: " + joinNode.getData().get("condition"));
    }
    
    @Test
    public void testMultipleJoins() {
        String sql = "SELECT * FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id " +
                     "LEFT JOIN products p ON o.product_id = p.id";
        DagGraph graph = analyzer.analyze(sql);
        
        assertNotNull(graph);
        
        // Should have 3 SCAN nodes
        long scanCount = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.SCAN)
            .count();
        assertEquals(3, scanCount, "Should have 3 SCAN nodes");
        
        // Should have 2 JOIN nodes
        long joinCount = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.JOIN)
            .count();
        assertEquals(2, joinCount, "Should have 2 JOIN nodes");
        
        System.out.println("Multiple JOINs test passed: " + graph.getStats());
    }
    
    @Test
    public void testJoinWithWhere() {
        String sql = "SELECT u.name, o.amount FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id " +
                     "WHERE o.amount > 100";
        DagGraph graph = analyzer.analyze(sql);
        
        assertNotNull(graph);
        
        // Should have JOIN and FILTER nodes
        boolean hasJoin = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.JOIN);
        boolean hasFilter = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.FILTER);
        
        assertTrue(hasJoin, "Should have JOIN node");
        assertTrue(hasFilter, "Should have FILTER node");
        
        // FILTER should come after JOIN (JOIN -> FILTER)
        DagNode joinNode = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.JOIN)
            .findFirst().orElse(null);
        DagNode filterNode = graph.getNodes().stream()
            .filter(n -> n.getType() == NodeType.FILTER)
            .findFirst().orElse(null);
        
        assertNotNull(joinNode);
        assertNotNull(filterNode);
        
        // Check that JOIN has outgoing edge to FILTER
        boolean joinToFilter = graph.getOutgoingEdges(joinNode.getId()).stream()
            .anyMatch(e -> e.getTarget().equals(filterNode.getId()));
        assertTrue(joinToFilter, "JOIN should connect to FILTER");
        
        System.out.println("JOIN with WHERE test passed: " + graph.getStats());
    }
    
    /**
     * Main method for manual testing and debugging
     */
    public static void main(String[] args) {
        SQLDagAnalyzer analyzer = new SQLDagAnalyzer();
        
        // Test Case 1: Simple SELECT
        System.out.println("========================================");
        System.out.println("Test 1: Simple SELECT");
        System.out.println("========================================");
        testSQL(analyzer, "SELECT id, name FROM users WHERE age > 18");
        
        // Test Case 2: JOIN
        System.out.println("\n========================================");
        System.out.println("Test 2: JOIN Query");
        System.out.println("========================================");
        testSQL(analyzer, 
            "SELECT u.id, u.name, o.order_id " +
            "FROM users u " +
            "JOIN orders o ON u.id = o.user_id " +
            "WHERE u.age > 18"
        );
        
        // Test Case 3: Aggregation
        System.out.println("\n========================================");
        System.out.println("Test 3: Aggregation");
        System.out.println("========================================");
        testSQL(analyzer,
            "SELECT department, COUNT(*) as cnt, AVG(salary) as avg_sal " +
            "FROM employees " +
            "GROUP BY department " +
            "HAVING COUNT(*) > 5"
        );
        
        // Test Case 4: CTE (Common Table Expression)
        System.out.println("\n========================================");
        System.out.println("Test 4: CTE");
        System.out.println("========================================");
        testSQL(analyzer,
            "WITH user_stats AS ( " +
            "  SELECT user_id, COUNT(*) as order_count " +
            "  FROM orders " +
            "  GROUP BY user_id " +
            ") " +
            "SELECT u.name, us.order_count " +
            "FROM users u " +
            "JOIN user_stats us ON u.id = us.user_id"
        );
        
        // Test Case 5: Subquery
        System.out.println("\n========================================");
        System.out.println("Test 5: Subquery");
        System.out.println("========================================");
        testSQL(analyzer,
            "SELECT * FROM users " +
            "WHERE id IN (SELECT user_id FROM orders WHERE amount > 1000)"
        );
    }
    
    private static void testSQL(SQLDagAnalyzer analyzer, String sql) {
        System.out.println("SQL: " + sql);
        System.out.println();
        
        try {
            DagGraph dag = analyzer.analyze(sql);
            
            // Print DAG structure
            System.out.println(dag.toDagString());
            
            // Print tree structure
            System.out.println(dag.toTreeString());
            
        } catch (Exception e) {
            System.err.println("Error analyzing SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

