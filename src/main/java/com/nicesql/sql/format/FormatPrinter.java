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

package com.nicesql.sql.format;

import com.nicesql.sql.parser.GenericSQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class FormatPrinter extends FormatPrinterBase {

    public FormatPrinter(FormatOptions options) {
        this.options = options;
    }

    protected Void visitList(List<? extends ParserRuleContext> contexts, String splitStr) {
        for (int i = 0; i < contexts.size(); i++) {
            visit(contexts.get(i));
            if (i != contexts.size() - 1) {
                append(splitStr);
            }
        }
        return null;
    }

    @Override
    public Void visit(ParseTree tree) {
        if (tree != null) {
            return super.visit(tree);
        }
        return null;
    }

    @Override
    public Void visitStatement(GenericSQLParser.StatementContext ctx) {
        currentSQL = new StringBuilder();
        visit(ctx.queryStatement());
        if (ctx.SEMICOLON() != null) {
            append(ctx.SEMICOLON().getText());
        }
        if (ctx.EOF() != null) {
            append(ctx.EOF().getText());
        }
        visit(ctx.emptyStatement());
        formatSQLs.add(currentSQL);
        return null;
    }

    @Override
    public Void visitEmptyStatement(GenericSQLParser.EmptyStatementContext ctx) {
        append(ctx.SEMICOLON().getText());
        return null;
    }

    @Override
    public Void visitSubfieldName(GenericSQLParser.SubfieldNameContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitNestedFieldName(GenericSQLParser.NestedFieldNameContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitWithClause(GenericSQLParser.WithClauseContext ctx) {
        appendIndent();
        appendKey(ctx.WITH());
        visitList(ctx.commonTableExpression(), comma() + newLine());
        appendNewLine();
        return null;
    }

    @Override
    public Void visitQueryNoWith(GenericSQLParser.QueryNoWithContext ctx) {
        visit(ctx.queryPrimary());
        if (ctx.ORDER() != null) {
            appendKey(ctx.ORDER());
            appendKey(ctx.BY());
            appendBreak(options.breakOrderBy);
            visitList(ctx.sortItem(), comma());
        }
        if (ctx.limitElement() != null) {
            appendBreak(options.breakLimit);
            visit(ctx.limitElement());
        }
        return null;
    }

    @Override
    public Void visitQueryPeriod(GenericSQLParser.QueryPeriodContext ctx) {
        appendKey(ctx.FOR());
        visit(ctx.periodType());
        if (ctx.BETWEEN() != null) {
            appendKey(ctx.BETWEEN());
            visit(ctx.expression(0));
            appendKey(ctx.AND());
            visit(ctx.expression(1));
        } else if (ctx.FROM() != null) {
            appendKey(ctx.FROM());
            visit(ctx.expression(0));
            appendKey(ctx.TO());
            visit(ctx.expression(1));
        } else if (ctx.ALL() != null) {
            appendKey(ctx.ALL());
        } else if (ctx.AS() != null && ctx.end != null) {
            appendKey(ctx.AS());
            appendKey(ctx.OF());
            visit(ctx.end);
        }
        return null;
    }

    @Override
    public Void visitPeriodType(GenericSQLParser.PeriodTypeContext ctx) {
        appendKey(ctx.getText());
        return null;
    }


    @Override
    public Void visitSetOperation(GenericSQLParser.SetOperationContext ctx) {
        visit(ctx.left);
        appendKey(ctx.operator.getText());
        visit(ctx.setQuantifier());
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitSubquery(GenericSQLParser.SubqueryContext ctx) {
        intoParentheses(() -> visit(ctx.queryRelation()));
        return null;
    }

    @Override
    public Void visitRowConstructor(GenericSQLParser.RowConstructorContext ctx) {
        intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitSortItem(GenericSQLParser.SortItemContext ctx) {
        visit(ctx.expression());
        if (ctx.ordering != null) {
            appendKey(ctx.ordering.getText(), true, false);
        }
        if (ctx.nullOrdering != null) {
            appendKey(ctx.NULLS());
            appendKey(ctx.nullOrdering.getText());
        }
        return null;
    }

    @Override
    public Void visitLimitElement(GenericSQLParser.LimitElementContext ctx) {
        appendKey(ctx.LIMIT().getText());
        if (ctx.OFFSET() != null) {
            append(ctx.limit.getText());
            appendKey(ctx.OFFSET().getText());
            append(ctx.offset.getText());
        } else if (ctx.offset != null) {
            append(ctx.offset.getText());
            append(comma());
            append(ctx.limit.getText());
        } else {
            append(ctx.limit.getText());
        }
        return null;
    }

    @Override
    public Void visitQuerySpecification(GenericSQLParser.QuerySpecificationContext ctx) {
        appendKey(ctx.SELECT().getText(), false, true);
        visit(ctx.setQuantifier());
        visitList(ctx.selectItem(), comma());
        appendNewLine();
        visit(ctx.fromClause());
        if (ctx.where != null) {
            appendNewLine();
            appendKey(ctx.WHERE());
            visit(ctx.where);
        }
        if (ctx.groupingElement() != null) {
            appendNewLine();
            appendKey(ctx.GROUP()).appendKey(ctx.BY());
            visit(ctx.groupingElement());
        }
        if (ctx.having != null) {
            appendNewLine();
            appendKey(ctx.HAVING());
            visit(ctx.having);
        }
        if (ctx.QUALIFY() != null) {
            appendNewLine();
            appendKey(ctx.QUALIFY());
            visit(ctx.qualifyFunction);
            appendKey(ctx.comparisonOperator().getText());
            append(ctx.limit.getText());
        }
        return null;
    }

    @Override
    public Void visitFrom(GenericSQLParser.FromContext ctx) {
        appendKey(ctx.FROM());
        visit(ctx.relations());
        visit(ctx.pivotClause());
        return null;
    }

    @Override
    public Void visitDual(GenericSQLParser.DualContext ctx) {
        return super.visitDual(ctx);
    }

    @Override
    public Void visitRollup(GenericSQLParser.RollupContext ctx) {
        return super.visitRollup(ctx);
    }

    @Override
    public Void visitCube(GenericSQLParser.CubeContext ctx) {
        return super.visitCube(ctx);
    }

    @Override
    public Void visitMultipleGroupingSets(GenericSQLParser.MultipleGroupingSetsContext ctx) {
        return super.visitMultipleGroupingSets(ctx);
    }

    @Override
    public Void visitSingleGroupingSet(GenericSQLParser.SingleGroupingSetContext ctx) {
        return super.visitSingleGroupingSet(ctx);
    }

    @Override
    public Void visitGroupingSet(GenericSQLParser.GroupingSetContext ctx) {
        return super.visitGroupingSet(ctx);
    }

    @Override
    public Void visitCommonTableExpression(GenericSQLParser.CommonTableExpressionContext ctx) {
        appendSpace(ctx.name.getText());
        if (ctx.columnAliases() != null) {
            visit(ctx.columnAliases());
        }
        appendSpace("AS");
        intoParentheses(() -> appendNewLine().intoLevel(() -> visit(ctx.queryRelation())));
        return null;
    }

    @Override
    public Void visitSetQuantifier(GenericSQLParser.SetQuantifierContext ctx) {
        appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitSelectSingle(GenericSQLParser.SelectSingleContext ctx) {
        visit(ctx.expression());
        if (ctx.AS() != null) {
            appendKey(ctx.AS());
            if (ctx.identifier() != null) {
                append(ctx.identifier().getText());
            }
            if (ctx.string() != null) {
                append(ctx.string().getText());
            }
        }
        return null;
    }

    @Override
    public Void visitSelectAll(GenericSQLParser.SelectAllContext ctx) {
        if (ctx.qualifiedName() != null) {
            append(ctx.qualifiedName().getText());
            append(".");
        }
        append(ctx.ASTERISK_SYMBOL().getText());
        if (ctx.excludeClause() != null) {
            visit(ctx.excludeClause());
        }
        return null;
    }

    @Override
    public Void visitExcludeClause(GenericSQLParser.ExcludeClauseContext ctx) {
        appendKey(ctx.getChild(0).getText());
        intoParentheses(() -> visitList(ctx.identifier(), comma()));
        return null;
    }

    @Override
    public Void visitRelations(GenericSQLParser.RelationsContext ctx) {
        visit(ctx.relation(0));
        for (int i = 1; i < ctx.relation().size(); i++) {
            append(comma());
            if (ctx.LATERAL(i) != null) {
                appendKey(ctx.LATERAL(i));
            }
            visit(ctx.relation(i));
        }
        return null;
    }

    @Override
    public Void visitNonBracketsRelation(GenericSQLParser.NonBracketsRelationContext ctx) {
        visit(ctx.relationPrimary());
        return visitList(ctx.joinRelation(), "");
    }

    @Override
    public Void visitBracketsRelation(GenericSQLParser.BracketsRelationContext ctx) {
        intoParentheses(() -> {
            visit(ctx.relationPrimary());
            visitList(ctx.joinRelation(), "");
        });
        visit(ctx.relationPrimary());
        return null;
    }

    @Override
    public Void visitTableAtom(GenericSQLParser.TableAtomContext ctx) {
        append(ctx.qualifiedName().getText());
        visit(ctx.queryPeriod());
        visit(ctx.partitionNames());
        visit(ctx.tabletList());
        visit(ctx.replicaList());
        if (ctx.alias != null) {
            appendKey(ctx.AS());
            append(ctx.alias.getText());
        }
        visit(ctx.bracketHint());
        if (ctx.BEFORE() != null) {
            appendKey(ctx.BEFORE());
            append(ctx.ts.getText());
        }
        return null;
    }

    @Override
    public Void visitInlineTable(GenericSQLParser.InlineTableContext ctx) {
        intoParentheses(() -> {
            appendKey(ctx.VALUES().getText());
            visitList(ctx.rowConstructor(), comma());
        });
        if (ctx.AS() != null) {
            appendKey(ctx.AS());
            append(ctx.alias.getText());
        }
        if (ctx.columnAliases() != null) {
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitSubqueryWithAlias(GenericSQLParser.SubqueryWithAliasContext ctx) {
        if (ctx.ASSERT_ROWS() != null) {
            appendKey(ctx.ASSERT_ROWS());
        }
        visit(ctx.subquery());
        if (ctx.alias != null) {
            appendKey(ctx.AS());
            append(ctx.alias.getText());
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitTableFunction(GenericSQLParser.TableFunctionContext ctx) {
        append(ctx.qualifiedName().getText());
        intoParentheses(() -> visit(ctx.expressionList()));
        if (ctx.AS() != null) {
            appendKey(ctx.AS());
            append(ctx.alias.getText());
        }
        if (ctx.columnAliases() != null) {
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitNormalizedTableFunction(GenericSQLParser.NormalizedTableFunctionContext ctx) {
        appendKey(ctx.TABLE().getText());
        intoParentheses(() -> {
            append(ctx.qualifiedName().getText());
            intoParentheses(() -> visit(ctx.argumentList()));
        });
        if (ctx.AS() != null) {
            appendKey(ctx.AS());
            append(ctx.alias.getText());
        }
        if (ctx.columnAliases() != null) {
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitParenthesizedRelation(GenericSQLParser.ParenthesizedRelationContext ctx) {
        intoParentheses(() -> visit(ctx.relations()));
        return null;
    }

    @Override
    public Void visitPivotClause(GenericSQLParser.PivotClauseContext ctx) {
        appendKey(ctx.PIVOT().getText());
        intoParentheses(() -> {
            visitList(ctx.pivotAggregationExpression(), comma());
            appendKey(ctx.FOR().getText());
            if (ctx.identifier() != null) {
                append(ctx.identifier().getText());
            } else if (ctx.identifierList() != null) {
                visit(ctx.identifierList());
            }
            appendKey(ctx.IN().getText());
            intoParentheses(() -> visitList(ctx.pivotValue(), comma()));
        });
        return null;
    }

    @Override
    public Void visitPivotAggregationExpression(GenericSQLParser.PivotAggregationExpressionContext ctx) {
        visit(ctx.functionCall());
        if (ctx.AS() != null) {
            appendKey(ctx.AS());
            if (ctx.identifier() != null) {
                append(ctx.identifier().getText());
            }
            if (ctx.string() != null) {
                append(ctx.string().getText());
            }
        }
        return null;
    }

    @Override
    public Void visitPivotValue(GenericSQLParser.PivotValueContext ctx) {
        if (ctx.literalExpression() != null) {
            visit(ctx.literalExpression());
        } else if (ctx.literalExpressionList() != null) {
            visit(ctx.literalExpressionList());
        }
        if (ctx.AS() != null) {
            appendKey(ctx.AS());
            if (ctx.identifier() != null) {
                append(ctx.identifier().getText());
            }
            if (ctx.string() != null) {
                append(ctx.string().getText());
            }
        }
        return null;
    }

    @Override
    public Void visitArgumentList(GenericSQLParser.ArgumentListContext ctx) {
        if (ctx.expressionList() != null) {
            visit(ctx.expressionList());
        } else if (ctx.namedArgumentList() != null) {
            visit(ctx.namedArgumentList());
        }
        return null;
    }

    @Override
    public Void visitNamedArgumentList(GenericSQLParser.NamedArgumentListContext ctx) {
        visitList(ctx.namedArgument(), comma());
        return null;
    }

    @Override
    public Void visitNamedArguments(GenericSQLParser.NamedArgumentsContext ctx) {
        append(ctx.identifier().getText());
        append(" => ");
        visit(ctx.expression());
        return null;
    }

    @Override
    public Void visitJoinRelation(GenericSQLParser.JoinRelationContext ctx) {
        if (ctx.crossOrInnerJoinType() != null) {
            visit(ctx.crossOrInnerJoinType());
        } else if (ctx.outerAndSemiJoinType() != null) {
            visit(ctx.outerAndSemiJoinType());
        }
        if (ctx.bracketHint() != null) {
            visit(ctx.bracketHint());
        }
        appendKey(ctx.LATERAL());
        visit(ctx.rightRelation);
        visit(ctx.joinCriteria());
        return null;
    }

//    @Override
//    public Void visitCrossOrInnerJoinType(GenericSQLParser.CrossOrInnerJoinTypeContext ctx) {
//        appendKey(ctx.getText());
//        return null;
//    }
//
//    @Override
//    public Void visitOuterAndSemiJoinType(GenericSQLParser.OuterAndSemiJoinTypeContext ctx) {
//        appendKey(ctx.getText());
//        visitChildren(ctx);
//        return null;
//    }

    @Override
    public Void visitBracketHint(GenericSQLParser.BracketHintContext ctx) {
        append("[");
        visitList(ctx.identifier(), comma());
        if (ctx.primaryExpression() == null) {
            append("|");
            visit(ctx.primaryExpression());
            visit(ctx.literalExpressionList());
        }
        append("]");
        return null;
    }

    @Override
    public Void visitJoinCriteria(GenericSQLParser.JoinCriteriaContext ctx) {
        if (ctx.ON() != null) {
            appendKey(ctx.ON());
            visit(ctx.expression());
        }
        if (ctx.USING() != null) {
            appendKey(ctx.USING());
            intoParentheses(() -> visitList(ctx.identifier(), comma()));
        }
        return null;
    }

    @Override
    public Void visitColumnAliases(GenericSQLParser.ColumnAliasesContext ctx) {
        intoParentheses(() -> appendItem(ctx.identifier().stream().map(RuleContext::toString).toArray(String[]::new)));
        return null;
    }

    @Override
    public Void visitPartitionNames(GenericSQLParser.PartitionNamesContext ctx) {
        return super.visitPartitionNames(ctx);
    }

    @Override
    public Void visitKeyPartitionList(GenericSQLParser.KeyPartitionListContext ctx) {
        appendKey(ctx.PARTITION().getText());
        intoParentheses(() -> visitList(ctx.keyPartition(), comma()));
        return null;
    }

    @Override
    public Void visitTabletList(GenericSQLParser.TabletListContext ctx) {
        appendKey(ctx.TABLET().getText());
        intoParentheses(() -> {
            for (int i = 0; i < ctx.INTEGER_VALUE().size(); i++) {
                append(ctx.INTEGER_VALUE(i).getText());
                if (i < ctx.INTEGER_VALUE().size() - 1) {
                    append(comma());
                }
            }
        });
        return null;
    }

    @Override
    public Void visitReplicaList(GenericSQLParser.ReplicaListContext ctx) {
        appendKey(ctx.REPLICA());
        intoParentheses(() -> {
            for (int i = 0; i < ctx.INTEGER_VALUE().size(); i++) {
                append(ctx.INTEGER_VALUE(i).getText());
                if (i < ctx.INTEGER_VALUE().size() - 1) {
                    append(comma());
                }
            }
        });
        return null;
    }

    @Override
    public Void visitMapExpressionList(GenericSQLParser.MapExpressionListContext ctx) {
        visitList(ctx.mapExpression(), comma());
        return null;
    }

    @Override
    public Void visitMapExpression(GenericSQLParser.MapExpressionContext ctx) {
        visit(ctx.key);
        append(":");
        visit(ctx.value);
        return null;
    }

    @Override
    public Void visitExpressionDefault(GenericSQLParser.ExpressionDefaultContext ctx) {
        appendKey(ctx.BINARY());
        return visit(ctx.booleanExpression());
    }

    @Override
    public Void visitLogicalNot(GenericSQLParser.LogicalNotContext ctx) {
        appendKey(ctx.NOT());
        return visit(ctx.expression());
    }

    @Override
    public Void visitLogicalBinary(GenericSQLParser.LogicalBinaryContext ctx) {
        visit(ctx.left);
        appendKey(ctx.operator.getText());
        return visit(ctx.right);
    }

    @Override
    public Void visitExpressionList(GenericSQLParser.ExpressionListContext ctx) {
        return visitList(ctx.expression(), comma());
    }

    @Override
    public Void visitComparison(GenericSQLParser.ComparisonContext ctx) {
        visit(ctx.left);
        appendKey(ctx.comparisonOperator().getText());
        visit(ctx.right);
        return null;
    }

    //    @Override
    //    public Void visitBooleanExpressionDefault(GenericSQLParser.BooleanExpressionDefaultContext ctx) {
    //        return super.visitBooleanExpressionDefault(ctx);
    //    }

    @Override
    public Void visitIsNull(GenericSQLParser.IsNullContext ctx) {
        visit(ctx.booleanExpression());
        appendKey(ctx.IS()).appendKey(ctx.NOT()).appendKey(ctx.NULL());
        return null;
    }

    @Override
    public Void visitScalarSubquery(GenericSQLParser.ScalarSubqueryContext ctx) {
        visit(ctx.booleanExpression());
        appendKey(ctx.comparisonOperator().getText());
        intoParentheses(() -> intoLevel(() -> visit(ctx.queryRelation())));
        return null;
    }

    @Override
    public Void visitPredicate(GenericSQLParser.PredicateContext ctx) {
        if (ctx.predicateOperations() != null) {
            return visit(ctx.predicateOperations());
        } else if (ctx.tupleInSubquery() != null) {
            return visit(ctx.tupleInSubquery());
        } else {
            return visit(ctx.valueExpression());
        }
    }

    @Override
    public Void visitTupleInSubquery(GenericSQLParser.TupleInSubqueryContext ctx) {
        intoParentheses(() -> visitList(ctx.expression(), comma()));
        appendKey(ctx.NOT()).appendKey(ctx.IN());
        intoParentheses(() -> intoLevel(() -> visit(ctx.queryRelation())));
        return null;
    }

    @Override
    public Void visitInSubquery(GenericSQLParser.InSubqueryContext ctx) {
        visit(ctx.value);
        appendKey(ctx.NOT()).appendKey(ctx.IN());
        intoParentheses(() -> intoLevel(() -> visit(ctx.queryRelation())));
        return null;
    }

    @Override
    public Void visitInList(GenericSQLParser.InListContext ctx) {
        visit(ctx.value);
        appendKey(ctx.NOT()).appendKey(ctx.IN());
        intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitBetween(GenericSQLParser.BetweenContext ctx) {
        visit(ctx.value);
        appendKey(ctx.NOT());
        appendKey(ctx.BETWEEN());
        visit(ctx.lower);
        appendKey(ctx.AND());
        return visit(ctx.upper);
    }

    @Override
    public Void visitLike(GenericSQLParser.LikeContext ctx) {
        visit(ctx.value);
        appendKey(ctx.NOT());
        appendKey(ctx.op.getText());
        return visit(ctx.pattern);
    }

    @Override
    public Void visitArithmeticBinary(GenericSQLParser.ArithmeticBinaryContext ctx) {
        visit(ctx.left);
        append(ctx.operator.getText());
        return visit(ctx.right);
    }

    @Override
    public Void visitDereference(GenericSQLParser.DereferenceContext ctx) {
        visit(ctx.base);
        if (ctx.DOT_IDENTIFIER() != null) {
            append(ctx.DOT_IDENTIFIER().getText());
        } else {
            append(".");
        }
        append(ctx.fieldName.getText());
        return null;
    }

    @Override
    public Void visitSimpleCase(GenericSQLParser.SimpleCaseContext ctx) {
        appendKey(ctx.CASE().getText(), false, true);
        visit(ctx.caseExpr);
        visitList(ctx.whenClause(), " ");
        if (ctx.ELSE() != null) {
            appendKey(ctx.ELSE());
            visit(ctx.elseExpression);
        }
        appendKey(ctx.END().getText(), true, false);
        return null;
    }

    @Override
    public Void visitArrowExpression(GenericSQLParser.ArrowExpressionContext ctx) {
        return super.visitArrowExpression(ctx);
    }

    @Override
    public Void visitOdbcFunctionCallExpression(GenericSQLParser.OdbcFunctionCallExpressionContext ctx) {
        return super.visitOdbcFunctionCallExpression(ctx);
    }

    @Override
    public Void visitMatchExpr(GenericSQLParser.MatchExprContext ctx) {
        return super.visitMatchExpr(ctx);
    }

    @Override
    public Void visitColumnRef(GenericSQLParser.ColumnRefContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSystemVariableExpression(GenericSQLParser.SystemVariableExpressionContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitConvert(GenericSQLParser.ConvertContext ctx) {
        appendKey(ctx.CONVERT());
        intoParentheses(() -> {
            visit(ctx.expression());
            append(comma());
            visit(ctx.type());
        });
        return null;
    }

    @Override
    public Void visitConcat(GenericSQLParser.ConcatContext ctx) {
        visit(ctx.left);
        append("||");
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitSubqueryExpression(GenericSQLParser.SubqueryExpressionContext ctx) {
        visit(ctx.subquery());
        return null;
    }

    @Override
    public Void visitLambdaFunctionExpr(GenericSQLParser.LambdaFunctionExprContext ctx) {
        if (ctx.identifier() != null) {
            append(ctx.identifier().getText());
        } else if (ctx.identifierList() != null) {
            visit(ctx.identifierList());
        }
        append("->");
        visit(ctx.expression());
        return null;
    }

    @Override
    public Void visitCollectionSubscript(GenericSQLParser.CollectionSubscriptContext ctx) {
        visit(ctx.value);
        append("[");
        visit(ctx.index);
        append("]");
        return null;
    }

    @Override
    public Void visitLiteral(GenericSQLParser.LiteralContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitCast(GenericSQLParser.CastContext ctx) {
        appendKey(ctx.CAST().getText(), true, false);
        intoParentheses(() -> {
            visit(ctx.expression());
            appendKey(ctx.AS());
            visit(ctx.type());
        });
        return null;
    }

    @Override
    public Void visitCollate(GenericSQLParser.CollateContext ctx) {
        visit(ctx.primaryExpression());
        appendKey(ctx.COLLATE());
        if (ctx.identifier() != null) {
            append(ctx.identifier().getText());
        } else if (ctx.string() != null) {
            append(ctx.string().getText());
        }
        return null;
    }

    @Override
    public Void visitParenthesizedExpression(GenericSQLParser.ParenthesizedExpressionContext ctx) {
        intoParentheses(() -> visit(ctx.expression()));
        return null;
    }

    @Override
    public Void visitUserVariableExpression(GenericSQLParser.UserVariableExpressionContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitArrayConstructor(GenericSQLParser.ArrayConstructorContext ctx) {
        if (ctx.arrayType() != null) {
            visit(ctx.arrayType());
        }
        append("[");
        if (ctx.expressionList() != null) {
            visit(ctx.expressionList());
        }
        append("]");
        return null;
    }

    @Override
    public Void visitMapConstructor(GenericSQLParser.MapConstructorContext ctx) {
        if (ctx.mapType() != null) {
            visit(ctx.mapType());
        } else if (ctx.MAP() != null) {
            appendKey(ctx.MAP());
        }
        append("{");
        if (ctx.mapExpressionList() != null) {
            visit(ctx.mapExpressionList());
        }
        append("}");
        return null;
    }

    @Override
    public Void visitArraySlice(GenericSQLParser.ArraySliceContext ctx) {
        visit(ctx.primaryExpression());
        append("[");
        if (ctx.start != null) {
            append(ctx.start.getText());
        }
        append(":");
        if (ctx.end != null) {
            append(ctx.end.getText());
        }
        append("]");
        return null;
    }

    @Override
    public Void visitFunctionCallExpression(GenericSQLParser.FunctionCallExpressionContext ctx) {
        visit(ctx.functionCall());
        return null;
    }

    @Override
    public Void visitExists(GenericSQLParser.ExistsContext ctx) {
        appendKey(ctx.EXISTS());
        intoParentheses(() -> visit(ctx.queryRelation()));
        return null;
    }

    @Override
    public Void visitSearchedCase(GenericSQLParser.SearchedCaseContext ctx) {
        appendKey(ctx.CASE());
        visitList(ctx.whenClause(), " ");
        if (ctx.ELSE() != null) {
            appendKey(ctx.ELSE());
            visit(ctx.elseExpression);
        }
        appendKey(ctx.END());
        return null;
    }

    @Override
    public Void visitArithmeticUnary(GenericSQLParser.ArithmeticUnaryContext ctx) {
        append(ctx.operator.getText());
        visit(ctx.primaryExpression());
        return null;
    }

    @Override
    public Void visitNullLiteral(GenericSQLParser.NullLiteralContext ctx) {
        appendKey(ctx.NULL());
        return null;
    }

    @Override
    public Void visitBooleanLiteral(GenericSQLParser.BooleanLiteralContext ctx) {
        appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitNumericLiteral(GenericSQLParser.NumericLiteralContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDateLiteral(GenericSQLParser.DateLiteralContext ctx) {
        appendKey(ctx.DATE());
        appendKey(ctx.DATETIME());
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitStringLiteral(GenericSQLParser.StringLiteralContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIntervalLiteral(GenericSQLParser.IntervalLiteralContext ctx) {
        visit(ctx.interval());
        return null;
    }

    @Override
    public Void visitUnitBoundaryLiteral(GenericSQLParser.UnitBoundaryLiteralContext ctx) {
        appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitBinaryLiteral(GenericSQLParser.BinaryLiteralContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitParameter(GenericSQLParser.ParameterContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitExtract(GenericSQLParser.ExtractContext ctx) {
        appendKey(ctx.EXTRACT());
        intoParentheses(() -> {
            append(ctx.identifier().getText());
            appendKey(ctx.FROM());
            visit(ctx.valueExpression());
        });
        return null;
    }

    @Override
    public Void visitInformationFunction(GenericSQLParser.InformationFunctionContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialDateTime(GenericSQLParser.SpecialDateTimeContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialFunction(GenericSQLParser.SpecialFunctionContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitAggregationFunctionCall(GenericSQLParser.AggregationFunctionCallContext ctx) {
        visit(ctx.aggregationFunction());
        if (ctx.over() != null) {
            visit(ctx.over());
        }
        return null;
    }

    @Override
    public Void visitWindowFunctionCall(GenericSQLParser.WindowFunctionCallContext ctx) {
        visit(ctx.windowFunction());
        visit(ctx.over());
        return null;
    }

    @Override
    public Void visitTranslateFunctionCall(GenericSQLParser.TranslateFunctionCallContext ctx) {
        appendKey(ctx.TRANSLATE());
        intoParentheses(() -> visitList(ctx.expression(), comma()));
        return null;
    }

    @Override
    public Void visitSimpleFunctionCall(GenericSQLParser.SimpleFunctionCallContext ctx) {
        append(ctx.qualifiedName().getText());
        intoParentheses(() -> visitList(ctx.expression(), comma()));
        if (ctx.over() != null) {
            visit(ctx.over());
        }
        return null;
    }

    @Override
    public Void visitAggregationFunction(GenericSQLParser.AggregationFunctionContext ctx) {
        append(ctx.name.getText());
        intoParentheses(() -> {
            if (ctx.name.getType() == GenericSQLParser.AVG || ctx.name.getType() == GenericSQLParser.MAX
                    || ctx.name.getType() == GenericSQLParser.MIN || ctx.name.getType() == GenericSQLParser.SUM) {
                visit(ctx.setQuantifier());
                visit(ctx.expression(0));
            } else if (ctx.name.getType() == GenericSQLParser.COUNT) {
                if (ctx.ASTERISK_SYMBOL() != null) {
                    append(ctx.ASTERISK_SYMBOL().getText());
                }
                if (ctx.setQuantifier() != null) {
                    visit(ctx.setQuantifier());
                    visit(ctx.bracketHint());
                }
                visitList(ctx.expression(), comma());
            } else if (ctx.name.getType() == GenericSQLParser.ARRAY_AGG) {
                visit(ctx.setQuantifier());
                visit(ctx.expression(0));
                appendKey(ctx.ORDER());
                appendKey(ctx.BY());
                visitList(ctx.sortItem(), comma());
            } else if (ctx.name.getType() == GenericSQLParser.ARRAY_AGG_DISTINCT) {
                visit(ctx.expression(0));
                appendKey(ctx.ORDER());
                appendKey(ctx.BY());
                visitList(ctx.sortItem(), comma());
            } else if (ctx.name.getType() == GenericSQLParser.GROUP_CONCAT) {
                visit(ctx.setQuantifier());
                visitList(ctx.expression().subList(0, ctx.expression().size() - 1), comma());
                if (ctx.ORDER() != null) {
                    appendKey(ctx.ORDER());
                    appendKey(ctx.BY());
                    visitList(ctx.sortItem(), comma());
                }
                if (ctx.SEPARATOR() != null) {
                    appendKey(ctx.SEPARATOR());
                    visit(ctx.expression(ctx.expression().size() - 1));
                }
            }
        });
        return null;
    }

    @Override
    public Void visitUserVariable(GenericSQLParser.UserVariableContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSystemVariable(GenericSQLParser.SystemVariableContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitColumnReference(GenericSQLParser.ColumnReferenceContext ctx) {
        append(ctx.identifier().getText());
        return null;
    }

    @Override
    public Void visitInformationFunctionExpression(GenericSQLParser.InformationFunctionExpressionContext ctx) {
        appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialDateTimeExpression(GenericSQLParser.SpecialDateTimeExpressionContext ctx) {
        appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialFunctionExpression(GenericSQLParser.SpecialFunctionExpressionContext ctx) {
        appendKey(ctx.name.getText());
        intoParentheses(() -> {
            append(ctx.unitIdentifier().getText());
            append(comma());
            visitList(ctx.expression(), comma());
        });
        return null;
    }

    @Override
    public Void visitWindowFunction(GenericSQLParser.WindowFunctionContext ctx) {
        append(ctx.name.getText());
        intoParentheses(() -> {
            if (ctx.expression() != null) {
                visit(ctx.expression(0));
                if (ctx.null1 != null) {
                    appendKey(ctx.null1.getText());
                }
                if (ctx.expression().size() > 1) {
                    visitList(ctx.expression().subList(1, ctx.expression().size()), comma());
                }
            }
        });
        if (ctx.null2 != null) {
            appendKey(ctx.null2.getText());
        }
        return null;

    }

    @Override
    public Void visitWhenClause(GenericSQLParser.WhenClauseContext ctx) {
        appendKey(ctx.WHEN());
        visit(ctx.condition);
        appendKey(ctx.THEN());
        visit(ctx.result);
        return null;
    }

    @Override
    public Void visitOver(GenericSQLParser.OverContext ctx) {
        appendKey(ctx.OVER());
        intoParentheses(() -> {
            if (ctx.bracketHint() != null) {
                visit(ctx.bracketHint());
            }
            if (ctx.PARTITION() != null) {
                appendKey(ctx.PARTITION());
                appendKey(ctx.BY(0));
                visitList(ctx.partition, comma());
            }
            if (ctx.ORDER() != null) {
                appendKey(ctx.ORDER());
                appendKey(ctx.BY(0));
                visitList(ctx.sortItem(), comma());
            }
            if (ctx.windowFrame() != null) {
                visit(ctx.windowFrame());
            }
        });
        return null;
    }

    @Override
    public Void visitIgnoreNulls(GenericSQLParser.IgnoreNullsContext ctx) {
        appendKey(ctx.IGNORE());
        appendKey(ctx.NULLS());
        return null;
    }

    @Override
    public Void visitWindowFrame(GenericSQLParser.WindowFrameContext ctx) {
        appendKey(ctx.frameType.getText());
        if (ctx.BETWEEN() != null) {
            appendKey(ctx.BETWEEN());
            visit(ctx.start);
            appendKey(ctx.AND());
            visit(ctx.end);
        } else {
            visit(ctx.start);
        }
        return null;
    }

    @Override
    public Void visitUnboundedFrame(GenericSQLParser.UnboundedFrameContext ctx) {
        appendKey(ctx.UNBOUNDED());
        appendKey(ctx.boundType.getText());
        return null;
    }

    @Override
    public Void visitCurrentRowBound(GenericSQLParser.CurrentRowBoundContext ctx) {
        appendKey(ctx.CURRENT());
        appendKey(ctx.ROW());
        return null;
    }

    @Override
    public Void visitBoundedFrame(GenericSQLParser.BoundedFrameContext ctx) {
        visit(ctx.expression());
        appendKey(ctx.boundType.getText());
        return null;
    }

    @Override
    public Void visitExplainDesc(GenericSQLParser.ExplainDescContext ctx) {
        appendKey(ctx.EXPLAIN().getText());
        appendKey(ctx.level.getText());
        appendBreak(options.breakExplain);
        return null;
    }

    @Override
    public Void visitLiteralExpressionList(GenericSQLParser.LiteralExpressionListContext ctx) {
        intoParentheses(() -> {
            for (int i = 0; i < ctx.literalExpression().size(); i++) {
                visit(ctx.literalExpression(i));
                if (i < ctx.literalExpression().size() - 1) {
                    append(comma());
                }
            }
        });
        return null;
    }

    @Override
    public Void visitKeyPartition(GenericSQLParser.KeyPartitionContext ctx) {
        append(ctx.partitionColName.getText());
        append("=");
        visit(ctx.partitionColValue);
        return null;
    }

    @Override
    public Void visitInterval(GenericSQLParser.IntervalContext ctx) {
        appendKey(ctx.INTERVAL().getText());
        visit(ctx.value);
        visit(ctx.from);
        return null;
    }

    @Override
    public Void visitArrayType(GenericSQLParser.ArrayTypeContext ctx) {
        appendKey(ctx.ARRAY().getText(), true, false);
        if (ctx.type() != null) {
            append("<");
            visit(ctx.type());
            append(">");
        }
        return null;
    }

    @Override
    public Void visitMapType(GenericSQLParser.MapTypeContext ctx) {
        appendKey(ctx.MAP().getText(), true, false);
        if (ctx.type() != null) {
            append("<");
            visitList(ctx.type(), comma());
            append(">");
        }
        return null;
    }

    @Override
    public Void visitSubfieldDescs(GenericSQLParser.SubfieldDescsContext ctx) {
        visitList(ctx.subfieldDesc(), comma());
        return null;
    }

    @Override
    public Void visitStructType(GenericSQLParser.StructTypeContext ctx) {
        appendKey(ctx.STRUCT().getText(), true, false);
        append("<");
        visit(ctx.subfieldDescs());
        append(">");
        return null;
    }

    @Override
    public Void visitTypeParameter(GenericSQLParser.TypeParameterContext ctx) {
        intoParentheses(() -> append(ctx.INTEGER_VALUE().getText()));
        return null;
    }

    @Override
    public Void visitDecimalType(GenericSQLParser.DecimalTypeContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitUnquotedIdentifier(GenericSQLParser.UnquotedIdentifierContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDigitIdentifier(GenericSQLParser.DigitIdentifierContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitBackQuotedIdentifier(GenericSQLParser.BackQuotedIdentifierContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIdentifierList(GenericSQLParser.IdentifierListContext ctx) {
        visitList(ctx.identifier(), comma());
        return null;
    }

    @Override
    public Void visitIdentifierOrString(GenericSQLParser.IdentifierOrStringContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDecimalValue(GenericSQLParser.DecimalValueContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDoubleValue(GenericSQLParser.DoubleValueContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIntegerValue(GenericSQLParser.IntegerValueContext ctx) {
        append(ctx.getText());
        return null;
    }

    @Override
    public Void visitType(GenericSQLParser.TypeContext ctx) {
        appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == GenericSQLParser.EOF) {
            return null; // Ignore EOF token
        }
        appendKey(node.getText());
        return null;
    }

    @Override
    public Void visitQualifiedName(GenericSQLParser.QualifiedNameContext ctx) {
        visit(ctx.getChild(0));
        for (int i = 1; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof GenericSQLParser.IdentifierContext) {
                append(".");
                visit(ctx.getChild(i));
            } else {
                visit(ctx.getChild(i));
            }
        }
        return super.visitQualifiedName(ctx);
    }
}
