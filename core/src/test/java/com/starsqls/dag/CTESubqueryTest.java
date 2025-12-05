package com.starsqls.dag;

import com.starsqls.dag.model.DagGraph;
import org.junit.jupiter.api.Test;

/**
 * Test CTE with subquery in WHERE clause
 */
public class CTESubqueryTest {
    
    private final SQLDagAnalyzer analyzer = new SQLDagAnalyzer();
    
    @Test
    public void testCTEWithSubqueryInWhere() {
        String sql = """
                WITH latest_pay_amount AS (
                    SELECT dt, role_id, SUM(amount) AS current_amount
                    FROM event_log
                    WHERE dt > (SELECT MAX(dt) FROM latest_load_time)
                    GROUP BY 1, 2
                )
                SELECT * FROM latest_pay_amount
                """;
        
        DagGraph graph = analyzer.analyze(sql);
        System.out.println("\n=== CTE with Subquery in WHERE ===");
        System.out.println(graph.toTreeString());
        System.out.println("\n=== All Nodes ===");
        graph.getNodes().forEach(n -> System.out.println(n.getId() + ": " + n.getLabel()));
        System.out.println("\n=== All Edges ===");
        graph.getEdges().forEach(e -> System.out.println(e.getSource() + " -> " + e.getTarget()));
    }
}
