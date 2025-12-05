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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a node in the SQL DAG (Directed Acyclic Graph).
 * Each node represents a logical operation in the SQL execution plan.
 */
public class DagNode {
    /**
     * Unique identifier for the node
     */
    private String id;
    
    /**
     * Type of the node (e.g., SCAN, FILTER, JOIN)
     */
    private NodeType type;
    
    /**
     * Display label for the node (first line)
     */
    private String label;
    
    /**
     * Additional detail lines to display on the node
     */
    private List<String> details;
    
    /**
     * Complete SQL fragment for tooltip display
     */
    private String sqlFragment;
    
    /**
     * Additional data associated with the node
     */
    private Map<String, Object> data;
    
    public DagNode() {
        this.details = new ArrayList<>();
        this.data = new HashMap<>();
    }
    
    public DagNode(String id, NodeType type, String label) {
        this();
        this.id = id;
        this.type = type;
        this.label = label;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public DagNode setId(String id) {
        this.id = id;
        return this;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public DagNode setType(NodeType type) {
        this.type = type;
        return this;
    }
    
    public String getLabel() {
        return label;
    }
    
    public DagNode setLabel(String label) {
        this.label = label;
        return this;
    }
    
    public List<String> getDetails() {
        return details;
    }
    
    public DagNode setDetails(List<String> details) {
        this.details = details != null ? details : new ArrayList<>();
        return this;
    }
    
    public DagNode addDetail(String detail) {
        this.details.add(detail);
        return this;
    }
    
    public String getSqlFragment() {
        return sqlFragment;
    }
    
    public DagNode setSqlFragment(String sqlFragment) {
        this.sqlFragment = sqlFragment;
        return this;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public DagNode setData(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
        return this;
    }
    
    public DagNode addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
    
    @Override
    public String toString() {
        return "DagNode{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", label='" + label + '\'' +
                '}';
    }
}
