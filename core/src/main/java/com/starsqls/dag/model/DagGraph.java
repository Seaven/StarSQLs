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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a complete SQL DAG (Directed Acyclic Graph).
 * Contains all nodes and edges that make up the query execution plan.
 */
public class DagGraph {
    /**
     * List of all nodes in the graph
     */
    private List<DagNode> nodes;
    
    /**
     * List of all edges in the graph
     */
    private List<DagEdge> edges;
    
    public DagGraph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }
    
    // Node operations
    
    /**
     * Add a node to the graph
     */
    public void addNode(DagNode node) {
        if (node != null && !hasNode(node.getId())) {
            this.nodes.add(node);
        }
    }
    
    /**
     * Check if a node with the given ID exists
     */
    public boolean hasNode(String nodeId) {
        return nodes.stream().anyMatch(n -> n.getId().equals(nodeId));
    }
    
    /**
     * Find a node by ID
     */
    public DagNode findNode(String nodeId) {
        return nodes.stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
    
    // Edge operations
    
    /**
     * Add an edge to the graph
     */
    public void addEdge(DagEdge edge) {
        if (edge != null) {
            // Auto-generate ID if not provided
            if (edge.getId() == null) {
                edge.setId("edge_" + edges.size());
            }
            this.edges.add(edge);
        }
    }
    
    /**
     * Add an edge between two nodes
     */
    public void addEdge(DagNode source, DagNode target) {
        addEdge(source, target, EdgeType.DATAFLOW);
    }
    
    /**
     * Add an edge between two nodes with specified type
     */
    public void addEdge(DagNode source, DagNode target, EdgeType type) {
        if (source != null && target != null) {
            DagEdge edge = new DagEdge(source.getId(), target.getId(), type);
            addEdge(edge);
        }
    }
    
    /**
     * Get all outgoing edges from a node
     */
    public List<DagEdge> getOutgoingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getSource().equals(nodeId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all incoming edges to a node
     */
    public List<DagEdge> getIncomingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getTarget().equals(nodeId))
                .collect(Collectors.toList());
    }
    
    /**
     * Find leaf nodes (nodes with no outgoing edges)
     */
    public List<DagNode> getLeafNodes() {
        return nodes.stream()
                .filter(node -> getOutgoingEdges(node.getId()).isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Find root nodes (nodes with no incoming edges)
     */
    public List<DagNode> getRootNodes() {
        return nodes.stream()
                .filter(node -> getIncomingEdges(node.getId()).isEmpty())
                .collect(Collectors.toList());
    }
    
    // Getters and Setters
    
    public List<DagNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<DagNode> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }
    
    public List<DagEdge> getEdges() {
        return edges;
    }
    
    public void setEdges(List<DagEdge> edges) {
        this.edges = edges != null ? edges : new ArrayList<>();
    }
    
    /**
     * Clear all nodes and edges
     */
    public void clear() {
        this.nodes.clear();
        this.edges.clear();
    }
    
    /**
     * Get statistics about the graph
     */
    public String getStats() {
        return String.format("DagGraph{nodes=%d, edges=%d}", nodes.size(), edges.size());
    }
    
    /**
     * Get the DAG structure as a formatted string
     */
    public String toDagString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DAG Structure ===\n");
        sb.append(getStats()).append("\n\n");
        
        // Print nodes by type
        sb.append("--- Nodes by Type ---\n");
        nodes.stream()
            .collect(Collectors.groupingBy(DagNode::getType))
            .forEach((type, nodeList) -> {
                sb.append(type).append(": ").append(nodeList.size()).append("\n");
                nodeList.forEach(node -> {
                    sb.append("  - ").append(node.getId()).append(": ").append(node.getLabel()).append("\n");
                });
            });
        sb.append("\n");
        
        // Print edges
        sb.append("--- Edges ---\n");
        edges.forEach(edge -> {
            String label = edge.getLabel() != null ? " (" + edge.getLabel() + ")" : "";
            sb.append("  ").append(edge.getSource()).append(" -> ").append(edge.getTarget()).append(label).append("\n");
        });
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Get DAG as a tree structure string starting from leaf nodes (bottom-up: RESULT at top, SCAN at bottom)
     */
    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DAG Tree Structure (Bottom-Up) ===\n");
        
        List<DagNode> leaves = getLeafNodes();
        
        if (leaves.isEmpty()) {
            sb.append("No leaf nodes found (possible cycle or empty graph)\n");
            return sb.toString();
        }
        
        leaves.forEach(leaf -> buildNodeTreeBottomUp(leaf, "", true, new java.util.HashSet<>(), sb));
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Recursively build node tree structure string from bottom to top (leaf to root)
     */
    private void buildNodeTreeBottomUp(DagNode node, String prefix, boolean isLast, 
                                       java.util.Set<String> visited, StringBuilder sb) {
        if (visited.contains(node.getId())) {
            sb.append(prefix).append(isLast ? "└── " : "├── ")
              .append(node.getLabel()).append(" (").append(node.getId()).append(")")
              .append(" (already visited)\n");
            return;
        }
        
        visited.add(node.getId());
        
        // Append current node with format: label (id)
        sb.append(prefix).append(isLast ? "└── " : "├── ")
          .append(node.getLabel()).append(" (").append(node.getId()).append(")\n");
        
        // Get parents (nodes connected by incoming edges)
        List<DagEdge> incomingEdges = getIncomingEdges(node.getId());
        
        for (int i = 0; i < incomingEdges.size(); i++) {
            DagEdge edge = incomingEdges.get(i);
            DagNode parentNode = findNode(edge.getSource());
            
            if (parentNode != null) {
                boolean isLastParent = (i == incomingEdges.size() - 1);
                String parentPrefix = prefix + (isLast ? "    " : "│   ");
                buildNodeTreeBottomUp(parentNode, parentPrefix, isLastParent, visited, sb);
            }
        }
    }
    
    @Override
    public String toString() {
        return getStats();
    }
}
