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

package com.starsqls.dag.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an edge (connection) between two nodes in the SQL DAG.
 * Each edge represents data flow or dependency between operations.
 */
public class DagEdge {
    /**
     * Unique identifier for the edge
     */
    private String id;
    
    /**
     * Source node ID
     */
    private String source;
    
    /**
     * Target node ID
     */
    private String target;
    
    /**
     * Type of the edge (e.g., DATAFLOW, CTE_REFERENCE)
     */
    private EdgeType type;
    
    /**
     * Display label for the edge
     */
    private String label;
    
    /**
     * Line style (e.g., "solid", "dashed")
     */
    private String style;
    
    /**
     * User-provided estimated row count
     */
    private Integer rows;
    
    /**
     * Additional data associated with the edge
     */
    private Map<String, Object> data;
    
    public DagEdge() {
        this.data = new HashMap<>();
        this.style = "solid";
    }
    
    public DagEdge(String source, String target, EdgeType type) {
        this();
        this.source = source;
        this.target = target;
        this.type = type;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public DagEdge setId(String id) {
        this.id = id;
        return this;
    }
    
    public String getSource() {
        return source;
    }
    
    public DagEdge setSource(String source) {
        this.source = source;
        return this;
    }
    
    public String getTarget() {
        return target;
    }
    
    public DagEdge setTarget(String target) {
        this.target = target;
        return this;
    }
    
    public EdgeType getType() {
        return type;
    }
    
    public DagEdge setType(EdgeType type) {
        this.type = type;
        return this;
    }
    
    public String getLabel() {
        return label;
    }
    
    public DagEdge setLabel(String label) {
        this.label = label;
        return this;
    }
    
    public String getStyle() {
        return style;
    }
    
    public DagEdge setStyle(String style) {
        this.style = style;
        return this;
    }
    
    public Integer getRows() {
        return rows;
    }
    
    public DagEdge setRows(Integer rows) {
        this.rows = rows;
        if (rows != null) {
            this.label = rows + " rows";
        }
        return this;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public DagEdge setData(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
        return this;
    }
    
    public DagEdge addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
    
    @Override
    public String toString() {
        return "DagEdge{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", type=" + type +
                '}';
    }
}
