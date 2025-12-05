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

package com.starsqls.web.controller;

import com.starsqls.dag.SQLDagAnalyzer;
import com.starsqls.dag.model.DagGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for SQL DAG analysis
 */
@RestController
@RequestMapping("/api/dag")
public class DagController {
    
    private static final Logger logger = LoggerFactory.getLogger(DagController.class);
    
    /**
     * Analyze SQL and generate DAG
     * 
     * POST /api/dag/analyze
     * Request body: { "sql": "SELECT * FROM users" }
     * Response: { "success": true, "graph": { "nodes": [...], "edges": [...] } }
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyzeSql(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String sql = request.get("sql");
            
            if (sql == null || sql.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "SQL statement is required");
                return response;
            }
            
            logger.info("Analyzing SQL DAG for: {}", sql.substring(0, Math.min(100, sql.length())));
            
            // Analyze SQL
            SQLDagAnalyzer analyzer = new SQLDagAnalyzer();
            DagGraph graph = analyzer.analyze(sql);
            
            // Build response
            response.put("success", true);
            response.put("graph", convertGraphToMap(graph));
            
            logger.info("DAG analysis completed: {}", graph.getStats());
            
        } catch (Exception e) {
            logger.error("Failed to analyze SQL DAG", e);
            response.put("success", false);
            response.put("error", "Failed to analyze SQL: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Convert DagGraph to Map for JSON serialization
     */
    private Map<String, Object> convertGraphToMap(DagGraph graph) {
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", graph.getNodes());
        result.put("edges", graph.getEdges());
        return result;
    }
}
