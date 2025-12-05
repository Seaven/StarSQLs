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

import com.starsqls.dag.model.*;
import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;
import com.starsqls.format.Printer;
import com.starsqls.parser.StarRocksBaseVisitor;
import com.starsqls.parser.StarRocksLexer;
import com.starsqls.parser.StarRocksParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes SQL statements and generates a DAG (Directed Acyclic Graph) representation.
 * This class traverses the ANTLR parse tree and extracts logical operation nodes.
 */
public class SQLDagAnalyzer extends StarRocksBaseVisitor<DagNode> {
    private final Printer printer;
    private final DagGraph graph;
    private final Stack<DagNode> nodeStack;
    private final Map<String, DagNode> cteMap;
    private int nodeCounter;
    
    public SQLDagAnalyzer() {
        this.graph = new DagGraph();
        this.nodeStack = new Stack<>();
        this.cteMap = new HashMap<>();
        this.nodeCounter = 0;
        FormatOptions options = FormatOptions.defaultOptions();
        options.keyWordStyle = FormatOptions.KeyWordStyle.UPPER_CASE;
        this.printer = Printer.create(options);
    }
    
    /**
     * Analyze a SQL statement and generate its DAG representation
     * 
     * @param sql SQL statement to analyze
     * @return DagGraph containing nodes and edges
     */
    public DagGraph analyze(String sql) {
        try {
            // Reset state
            graph.clear();
            nodeStack.clear();
            cteMap.clear();
            nodeCounter = 0;
            
            // Parse SQL
            StarRocksLexer lexer = new StarRocksLexer(CharStreams.fromString(sql));
            StarRocksParser parser = new StarRocksParser(new CommonTokenStream(lexer));
            StarRocksParser.SqlStatementsContext tree = parser.sqlStatements();
            
            // Visit parse tree
            visit(tree);
            
            // Add RESULT node at the end
            addResultNode();
            
            return graph;
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze SQL DAG: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate next unique node ID
     */
    private String nextNodeId() {
        return String.valueOf(nodeCounter++);
    }
    
    /**
     * Add RESULT node and connect all leaf nodes to it
     */
    private void addResultNode() {
        DagNode resultNode = new DagNode()
            .setId("result")
            .setType(NodeType.RESULT)
            .setLabel("RESULT")
            .setSqlFragment("Query Result");
        
        graph.addNode(resultNode);
        
        // Connect all leaf nodes (nodes with no outgoing edges) to RESULT
        for (DagNode node : graph.getLeafNodes()) {
            if (!node.getId().equals("result")) {
                graph.addEdge(node, resultNode, EdgeType.DATAFLOW);
            }
        }
    }
    
    /**
     * Truncate text for display (max 50 characters)
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
    
    // ==================== Query Structure ====================
    @Override
    public DagNode visitQuerySpecification(StarRocksParser.QuerySpecificationContext ctx) {
        DagNode currentNode = null;

        // 1. FROM clause - SCAN nodes
        if (ctx.fromClause() != null) {
            currentNode = visit(ctx.fromClause());
        }
        
        // 2. WHERE clause - FILTER node
        if (ctx.where != null) {
            String condition = printer.format(ctx.where);
            String shortCondition = truncate(condition, 50);
            
            DagNode filterNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.FILTER)
                .setLabel("FILTER (WHERE)")
                .addDetail(shortCondition)
                .setSqlFragment("WHERE " + condition);
            
            graph.addNode(filterNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, filterNode, EdgeType.DATAFLOW);
            }
            
            // Process any subqueries in WHERE condition and connect them to this filter
            processSubqueriesInExpression(ctx.where, filterNode);
            
            currentNode = filterNode;
        }
        
        // 3. GROUP BY - AGGREGATE node
        if (ctx.groupingElement() != null) {
            List<String> groupByColumns = extractGroupByColumns(ctx.groupingElement());
            List<String> aggFunctions = extractAggFunctions(ctx.selectItem());
            
            String groupByStr = "GROUP BY: " + String.join(", ", groupByColumns);
            String aggStr = aggFunctions.isEmpty() ? "" : "AGG: " + String.join(", ", aggFunctions);
            
            DagNode aggNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.AGGREGATE)
                .setLabel("AGGREGATE")
                .addDetail(truncate(groupByStr, 50))
                .setSqlFragment("GROUP BY " + String.join(", ", groupByColumns) + 
                               (aggFunctions.isEmpty() ? "" : "\nFunctions: " + String.join(", ", aggFunctions)));
            
            if (!aggStr.isEmpty()) {
                aggNode.addDetail(truncate(aggStr, 50));
            }
            
            graph.addNode(aggNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, aggNode, EdgeType.DATAFLOW);
            }
            
            // Process subqueries in GROUP BY expressions
            if (ctx.groupingElement() instanceof StarRocksParser.SingleGroupingSetContext) {
                StarRocksParser.SingleGroupingSetContext single = (StarRocksParser.SingleGroupingSetContext) ctx.groupingElement();
                if (single.expressionList() != null && single.expressionList().expression() != null) {
                    for (StarRocksParser.ExpressionContext expr : single.expressionList().expression()) {
                        processSubqueriesInExpression(expr, aggNode);
                    }
                }
            }
            
            currentNode = aggNode;
        }
        
        // 4. HAVING clause - FILTER node
        if (ctx.having != null) {
            String condition = printer.format(ctx.having);
            String shortCondition = truncate(condition, 50);
            
            DagNode havingNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.FILTER)
                .setLabel("FILTER (HAVING)")
                .addDetail(shortCondition)
                .setSqlFragment("HAVING " + condition);
            
            graph.addNode(havingNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, havingNode, EdgeType.DATAFLOW);
            }
            
            // Process subqueries in HAVING condition
            processSubqueriesInExpression(ctx.having, havingNode);
            
            currentNode = havingNode;
        }
        
        // 5. DISTINCT
        if (ctx.setQuantifier() != null && ctx.setQuantifier().DISTINCT() != null) {
            DagNode distinctNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.DISTINCT)
                .setLabel("DISTINCT")
                .setSqlFragment("SELECT DISTINCT");
            
            graph.addNode(distinctNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, distinctNode, EdgeType.DATAFLOW);
            }
            currentNode = distinctNode;
        }
        
        // 6. SELECT clause - PROJECT node (only if has expressions/calculations)
        if (hasComplexSelectItems(ctx.selectItem())) {
            List<String> columns = extractSelectColumns(ctx.selectItem());
            String columnsStr = String.join(", ", columns);
            String shortColumns = truncate(columnsStr, 50);
            
            DagNode projectNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.PROJECT)
                .setLabel("PROJECT")
                .addDetail(shortColumns)
                .setSqlFragment("SELECT " + columnsStr);
            
            graph.addNode(projectNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, projectNode, EdgeType.DATAFLOW);
            }
            
            // Process subqueries in SELECT list
            for (StarRocksParser.SelectItemContext item : ctx.selectItem()) {
                if (item instanceof StarRocksParser.SelectSingleContext) {
                    StarRocksParser.SelectSingleContext single = (StarRocksParser.SelectSingleContext) item;
                    processSubqueriesInExpression(single.expression(), projectNode);
                }
            }
            
            currentNode = projectNode;
        }
        
        // 7. Window functions
        if (hasWindowFunctions(ctx.selectItem())) {
            DagNode windowNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.WINDOW)
                .setLabel("WINDOW")
                .setSqlFragment("Window Functions");
            
            graph.addNode(windowNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, windowNode, EdgeType.DATAFLOW);
            }
            currentNode = windowNode;
        }
        
        // Connect to parent node if in a subquery or CTE
        if (!nodeStack.isEmpty() && currentNode != null) {
            graph.addEdge(currentNode, nodeStack.peek(), EdgeType.DATAFLOW);
        }
        
        return currentNode;
    }
    
    @Override
    public DagNode visitQueryNoWith(StarRocksParser.QueryNoWithContext ctx) {
        // Process main query
        DagNode currentNode = visit(ctx.queryPrimary());

        if (ctx.ORDER() != null && ctx.limitElement() != null) {
            String sortStr = ctx.sortItem().stream().map(printer::format).collect(Collectors.joining(", "));
            String limit = printer.format(ctx.limitElement());

            DagNode sortNode = new DagNode()
                    .setId(nextNodeId())
                    .setType(NodeType.TOP_N)
                    .setLabel("TOP_N")
                    .addDetail(sortStr)
                    .setSqlFragment("ORDER BY " + sortStr + "\n" + limit);

            graph.addNode(sortNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, sortNode, EdgeType.DATAFLOW);
            }
            currentNode = sortNode;
        }

        // ORDER BY - SORT node
        if (ctx.ORDER() != null) {
            String sortStr = ctx.sortItem().stream().map(printer::format).collect(Collectors.joining(", "));
            
            DagNode sortNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.SORT)
                .setLabel("SORT")
                .addDetail(sortStr)
                .setSqlFragment("ORDER BY " + sortStr);
            
            graph.addNode(sortNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, sortNode, EdgeType.DATAFLOW);
            }
            
            // Process subqueries in ORDER BY expressions
            for (StarRocksParser.SortItemContext sortItem : ctx.sortItem()) {
                processSubqueriesInExpression(sortItem.expression(), sortNode);
            }
            
            currentNode = sortNode;
        }
        
        // LIMIT/OFFSET - LIMIT node
        if (ctx.limitElement() != null) {
            String limit = printer.format(ctx.limitElement());
            
            DagNode limitNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.LIMIT)
                .setLabel("LIMIT")
                .addDetail(limit)
                .setSqlFragment(limit);
            
            graph.addNode(limitNode);
            if (currentNode != null) {
                graph.addEdge(currentNode, limitNode, EdgeType.DATAFLOW);
            }
            currentNode = limitNode;
        }
        
        return currentNode;
    }
    
    @Override
    public DagNode visitFrom(StarRocksParser.FromContext ctx) {
        if (ctx.relations().relation().size() == 1) {
            return visit(ctx.relations().relation(0));
        } else {
            // Multiple relations (comma-separated) - create JOIN nodes
            DagNode currentNode = null;
            for (StarRocksParser.RelationContext relationCtx : ctx.relations().relation()) {
                DagNode nextNode = visit(relationCtx);

                if (currentNode == null) {
                    currentNode = nextNode;
                } else {
                    // Create CROSS JOIN node
                    DagNode joinNode = new DagNode()
                        .setId(nextNodeId())
                        .setType(NodeType.JOIN)
                        .setLabel("JOIN (CROSS)")
                        .setSqlFragment("CROSS JOIN");

                    graph.addNode(joinNode);
                    graph.addEdge(currentNode, joinNode, EdgeType.DATAFLOW);
                    graph.addEdge(nextNode, joinNode, EdgeType.DATAFLOW);

                    currentNode = joinNode;
                }
            }
            return currentNode;
        }
    }
    
    // ==================== CTE (WITH clause) ====================
    
    @Override
    public DagNode visitWithClause(StarRocksParser.WithClauseContext ctx) {
        for (StarRocksParser.CommonTableExpressionContext cte : ctx.commonTableExpression()) {
            String cteName = cte.name.getText();
            
            DagNode cteNode = new DagNode()
                .setId("cte_" + cteName)
                .setType(NodeType.CTE)
                .setLabel("CTE")
                .addDetail(cteName)
                .setSqlFragment("WITH " + cteName + " AS (...)");

            graph.addNode(cteNode);
            cteMap.put(cteName, cteNode);
            
            // Recursively analyze CTE body
            nodeStack.push(cteNode);
            visit(cte.queryRelation());
            nodeStack.pop();
        }
        return null;
    }
    
    // ==================== Subquery ====================
    
    @Override
    public DagNode visitSubqueryWithAlias(StarRocksParser.SubqueryWithAliasContext ctx) {
        String alias = ctx.alias != null ? ctx.alias.getText() : "subquery";
        
        DagNode subqueryNode = new DagNode()
            .setId(nextNodeId())
            .setType(NodeType.SUBQUERY)
            .setLabel("SUBQUERY")
            .addDetail(alias)
            .setSqlFragment("(SELECT ...) AS " + alias);
        
        graph.addNode(subqueryNode);
        
        // Recursively analyze subquery
        nodeStack.push(subqueryNode);
        visit(ctx.subquery().queryRelation());
        nodeStack.pop();
        
        return subqueryNode;
    }
    
    // ==================== UNION ====================
    
    @Override
    public DagNode visitSetOperation(StarRocksParser.SetOperationContext ctx) {
        DagNode leftNode = visit(ctx.left);
        DagNode rightNode = visit(ctx.right);
        
        String unionType = ctx.operator.getText().toUpperCase();
        if (ctx.setQuantifier() != null && ctx.setQuantifier().ALL() != null) {
            unionType += " ALL";
        }
        
        DagNode unionNode = new DagNode()
            .setId(nextNodeId())
            .setType(NodeType.UNION)
            .setLabel("UNION")
            .addDetail(unionType)
            .setSqlFragment(unionType);
        
        graph.addNode(unionNode);
        
        if (leftNode != null) {
            graph.addEdge(leftNode, unionNode, EdgeType.DATAFLOW);
        }
        if (rightNode != null) {
            graph.addEdge(rightNode, unionNode, EdgeType.DATAFLOW);
        }
        
        return unionNode;
    }
    
    @Override
    public DagNode visitRelation(StarRocksParser.RelationContext ctx) {
        // Handle primary relation (first table/subquery)
        DagNode primaryNode = visit(ctx.relationPrimary());
        
        // Handle JOINs
        if (ctx.joinRelation() != null && !ctx.joinRelation().isEmpty()) {
            DagNode currentNode = primaryNode;
            
            for (StarRocksParser.JoinRelationContext joinCtx : ctx.joinRelation()) {
                DagNode rightNode = visit(joinCtx.rightRelation);
                
                // Extract JOIN type and condition
                String joinType = extractJoinType(joinCtx);
                String condition = extractJoinCondition(joinCtx);
                String shortCondition = truncate(condition, 50);
                
                // Create JOIN node
                DagNode joinNode = new DagNode()
                    .setId(nextNodeId())
                    .setType(NodeType.JOIN)
                    .setLabel("" + joinType + " JOIN")
                    .addDetail(shortCondition)
                    .setSqlFragment("JOIN CONDITION: " + condition);
                
                graph.addNode(joinNode);
                
                // Connect left and right tables to JOIN node
                graph.addEdge(currentNode, joinNode, EdgeType.DATAFLOW);
                graph.addEdge(rightNode, joinNode, EdgeType.DATAFLOW);
                
                // Process subqueries in JOIN condition
                if (joinCtx.joinCriteria() != null && joinCtx.joinCriteria().expression() != null) {
                    processSubqueriesInExpression(joinCtx.joinCriteria().expression(), joinNode);
                }
                
                currentNode = joinNode;
            }
            
            return currentNode;
        }
        
        return primaryNode;
    }
    
    // ==================== Table Scan ====================
    
    @Override
    public DagNode visitTableAtom(StarRocksParser.TableAtomContext ctx) {
        String tableName = ctx.qualifiedName().getText();
        String alias = ctx.alias != null ? ctx.alias.getText() : tableName;
        
        // Check if this is a CTE reference
        if (cteMap.containsKey(tableName)) {
            DagNode cteNode = cteMap.get(tableName);
            
            // Create SCAN node for CTE reference
            DagNode scanNode = new DagNode()
                .setId(nextNodeId())
                .setType(NodeType.SCAN)
                .setLabel("SCAN")
                .addDetail(tableName + " (" + alias + ")")
                .setSqlFragment("FROM " + tableName + " AS " + alias);
            
            graph.addNode(scanNode);
            
            // Create CTE reference edge (dashed line)
            DagEdge cteEdge = new DagEdge(cteNode.getId(), scanNode.getId(), EdgeType.CTE_REFERENCE)
                .setStyle("dashed");
            
            graph.addEdge(cteEdge);
            
            return scanNode;
        }
        
        // Regular table scan
        DagNode scanNode = new DagNode()
            .setId(nextNodeId())
            .setType(NodeType.SCAN)
            .setLabel("SCAN")
            .addDetail(tableName + (alias.equals(tableName) ? "" : " (" + alias + ")"))
            .setSqlFragment("FROM " + tableName + (alias.equals(tableName) ? "" : " AS " + alias));
        
        graph.addNode(scanNode);
        return scanNode;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Check if SELECT items contain complex expressions (not just simple column refs)
     */
    private boolean hasComplexSelectItems(List<StarRocksParser.SelectItemContext> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        for (StarRocksParser.SelectItemContext item : items) {
            if (item instanceof StarRocksParser.SelectSingleContext single) {

                // If it has an alias, consider it complex (requires projection)
                if (single.identifier() != null || single.string() != null) {
                    return true;
                }
                
                // Check if expression text contains functions, operators, etc.
                String exprText = single.expression().getText();
                // Simple heuristics: contains parentheses (functions) or operators
                if (exprText.contains("(") || exprText.contains("+") || 
                    exprText.contains("-") || exprText.contains("*") || 
                    exprText.contains("/") || exprText.contains("||")) {
                    return true;
                }
            }
            // SELECT * is not complex
            if (item instanceof StarRocksParser.SelectAllContext) {
                continue;
            }
        }
        
        return false;
    }
    
    /**
     * Extract SELECT column names/expressions
     */
    private List<String> extractSelectColumns(List<StarRocksParser.SelectItemContext> items) {
        List<String> columns = new ArrayList<>();
        
        if (items == null) {
            return columns;
        }
        
        for (StarRocksParser.SelectItemContext item : items) {
            if (item instanceof StarRocksParser.SelectSingleContext single) {
                String expr = printer.format(single.expression());
                
                if (single.identifier() != null) {
                    expr += " AS " + single.identifier().getText();
                } else if (single.string() != null) {
                    expr += " AS " + single.string().getText();
                }
                
                columns.add(expr);
            } else if (item instanceof StarRocksParser.SelectAllContext) {
                StarRocksParser.SelectAllContext all = (StarRocksParser.SelectAllContext) item;
                if (all.qualifiedName() != null) {
                    columns.add(all.qualifiedName().getText() + ".*");
                } else {
                    columns.add("*");
                }
            }
        }
        
        return columns;
    }
    
    /**
     * Extract JOIN type from join relation context
     */
    private String extractJoinType(StarRocksParser.JoinRelationContext ctx) {
        String joinType = "INNER"; // Default
        if (ctx.crossOrInnerJoinType() != null) {
            String type = ctx.crossOrInnerJoinType().getText().toUpperCase();
            // Handle CROSS JOIN, INNER JOIN, or just JOIN
            if (type.contains("CROSS")) {
                joinType = "CROSS";
            } else if (type.contains("INNER")) {
                joinType = "INNER";
            } else {
                joinType = "INNER"; // Default JOIN is INNER
            }
        } else if (ctx.outerAndSemiJoinType() != null) {
            joinType = ctx.outerAndSemiJoinType().getText().toUpperCase();
        }
        joinType = joinType.trim();
        if (joinType.trim().equals("JOIN")) {
            joinType = "INNER";
        }
        if (joinType.endsWith("JOIN")) {
            joinType = joinType.substring(0, joinType.length() - 4).trim();
        }
        return joinType;
    }
    
    /**
     * Extract JOIN condition from join criteria
     */
    private String extractJoinCondition(StarRocksParser.JoinRelationContext ctx) {
        if (ctx.joinCriteria() == null) {
            return "";
        }
        
        StarRocksParser.JoinCriteriaContext criteria = ctx.joinCriteria();
        
        if (criteria.ON() != null && criteria.expression() != null) {
            return printer.format(criteria.expression());
        } else if (criteria.USING() != null && criteria.identifier() != null) {
            List<String> columns = new ArrayList<>();
            for (StarRocksParser.IdentifierContext id : criteria.identifier()) {
                columns.add(id.getText());
            }
            return "USING (" + String.join(", ", columns) + ")";
        }
        
        return "";
    }
    
    /**
     * Extract GROUP BY columns
     */
    private List<String> extractGroupByColumns(StarRocksParser.GroupingElementContext ctx) {
        List<String> columns = new ArrayList<>();
        
        if (ctx instanceof StarRocksParser.SingleGroupingSetContext) {
            StarRocksParser.SingleGroupingSetContext single = (StarRocksParser.SingleGroupingSetContext) ctx;
            if (single.expressionList() != null && single.expressionList().expression() != null) {
                for (StarRocksParser.ExpressionContext expr : single.expressionList().expression()) {
                    columns.add(printer.format(expr));
                }
            }
        }
        
        return columns;
    }
    
    /**
     * Extract aggregate functions from SELECT items
     */
    private List<String> extractAggFunctions(List<StarRocksParser.SelectItemContext> items) {
        List<String> functions = new ArrayList<>();
        
        if (items == null) {
            return functions;
        }
        
        for (StarRocksParser.SelectItemContext item : items) {
            if (item instanceof StarRocksParser.SelectSingleContext) {
                StarRocksParser.SelectSingleContext single = (StarRocksParser.SelectSingleContext) item;
                String expr = single.expression().getText();
                
                // Simple detection of aggregate functions
                if (expr.matches(".*(?i)(COUNT|SUM|AVG|MIN|MAX|ARRAY_AGG|GROUP_CONCAT)\\s*\\(.*")) {
                    // Extract function name
                    String funcName = expr.replaceAll("(?i)^.*(COUNT|SUM|AVG|MIN|MAX|ARRAY_AGG|GROUP_CONCAT)\\s*\\(.*", "$1").toUpperCase();
                    functions.add(funcName + "(...)");
                }
            }
        }
        
        return functions;
    }
    
    /**
     * Extract ORDER BY columns
     */
    private List<String> extractSortColumns(List<StarRocksParser.SortItemContext> items) {
        List<String> columns = new ArrayList<>();
        if (items == null) {
            return columns;
        }
        for (StarRocksParser.SortItemContext item : items) {
            String column = item.expression().getText();
            if (item.ordering != null) {
                column += " " + item.ordering.getText().toUpperCase();
            }
            columns.add(column);
        }
        
        return columns;
    }
    
    /**
     * Check if SELECT items contain window functions
     */
    private boolean hasWindowFunctions(List<StarRocksParser.SelectItemContext> items) {
        if (items == null) {
            return false;
        }
        
        for (StarRocksParser.SelectItemContext item : items) {
            if (item instanceof StarRocksParser.SelectSingleContext) {
                StarRocksParser.SelectSingleContext single = (StarRocksParser.SelectSingleContext) item;
                String expr = single.expression().getText();
                
                // Check for OVER clause
                if (expr.matches(".*(?i)OVER\\s*\\(.*")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Process subqueries within an expression (e.g., in WHERE clause)
     * This visitor traverses the expression tree and creates DAG nodes for any subqueries found
     * 
     * @param ctx Expression context to search for subqueries
     * @param parentNode The node that uses this expression (e.g., FILTER node for WHERE clause)
     */
    private void processSubqueriesInExpression(StarRocksParser.ExpressionContext ctx, DagNode parentNode) {
        if (ctx == null) {
            return;
        }
        
        final SQLDagAnalyzer analyzer = this;
        
        // Use a custom visitor to find all subqueries in the expression tree
        ctx.accept(new StarRocksBaseVisitor<Void>() {
            @Override
            public Void visitSubqueryExpression(StarRocksParser.SubqueryExpressionContext ctx) {
                // Create a SUBQUERY node for this subquery
                DagNode subqueryNode = new DagNode()
                    .setId(nextNodeId())
                    .setType(NodeType.SUBQUERY)
                    .setLabel("SUBQUERY")
                    .setSqlFragment(printer.format(ctx));
                
                graph.addNode(subqueryNode);
                
                // Connect subquery to parent node (the filter that uses it)
                if (parentNode != null) {
                    graph.addEdge(subqueryNode, parentNode, EdgeType.DATAFLOW);
                }
                
                // Recursively analyze the subquery content
                // Push the SUBQUERY node to stack to capture the subquery's result
                // The visitQuerySpecification will automatically connect the result to this subqueryNode
                nodeStack.push(subqueryNode);
                analyzer.visit(ctx.subquery().queryRelation());
                nodeStack.pop();
                
                // Don't call super.visit to avoid duplicate processing
                return null;
            }
            
            @Override
            public Void visitScalarSubquery(StarRocksParser.ScalarSubqueryContext ctx) {
                // Handle scalar subquery (e.g., col = (SELECT ...))
                DagNode subqueryNode = new DagNode()
                    .setId(nextNodeId())
                    .setType(NodeType.SUBQUERY)
                    .setLabel("SUBQUERY (SCALAR)")
                    .setSqlFragment(printer.format(ctx.queryRelation()));
                
                graph.addNode(subqueryNode);
                
                // Connect subquery to parent node (the filter that uses it)
                if (parentNode != null) {
                    graph.addEdge(subqueryNode, parentNode, EdgeType.DATAFLOW);
                }
                
                // Recursively analyze the subquery content
                // Push the SUBQUERY node to stack to capture the subquery's result
                // The visitQuerySpecification will automatically connect the result to this subqueryNode
                nodeStack.push(subqueryNode);
                analyzer.visit(ctx.queryRelation());
                nodeStack.pop();
                
                // Don't call super.visit to avoid duplicate processing
                return null;
            }
            
            @Override
            public Void visitInSubquery(StarRocksParser.InSubqueryContext ctx) {
                // Handle IN subquery (e.g., col IN (SELECT ...))
                DagNode subqueryNode = new DagNode()
                    .setId(nextNodeId())
                    .setType(NodeType.SUBQUERY)
                    .setLabel("SUBQUERY (IN)")
                    .setSqlFragment(printer.format(ctx.queryRelation()));
                
                graph.addNode(subqueryNode);
                
                // Connect subquery to parent node (the filter that uses it)
                if (parentNode != null) {
                    graph.addEdge(subqueryNode, parentNode, EdgeType.DATAFLOW);
                }
                
                // Recursively analyze the subquery content
                // Push the SUBQUERY node to stack to capture the subquery's result
                // The visitQuerySpecification will automatically connect the result to this subqueryNode
                nodeStack.push(subqueryNode);
                analyzer.visit(ctx.queryRelation());
                nodeStack.pop();
                
                // Don't call super.visit to avoid duplicate processing
                return null;
            }
            
            @Override
            public Void visitExists(StarRocksParser.ExistsContext ctx) {
                // Handle EXISTS subquery
                DagNode subqueryNode = new DagNode()
                    .setId(nextNodeId())
                    .setType(NodeType.SUBQUERY)
                    .setLabel("SUBQUERY (EXISTS)")
                    .setSqlFragment(printer.format(ctx.queryRelation()));
                
                graph.addNode(subqueryNode);
                
                // Connect subquery to parent node (the filter that uses it)
                if (parentNode != null) {
                    graph.addEdge(subqueryNode, parentNode, EdgeType.DATAFLOW);
                }
                
                // Recursively analyze the subquery content
                // Push the SUBQUERY node to stack to capture the subquery's result
                // The visitQuerySpecification will automatically connect the result to this subqueryNode
                nodeStack.push(subqueryNode);
                analyzer.visit(ctx.queryRelation());
                nodeStack.pop();
                
                // Don't call super.visit to avoid duplicate processing
                return null;
            }
            
            @Override
            protected Void aggregateResult(Void aggregate, Void nextResult) {
                return null;
            }
        });
    }
}
