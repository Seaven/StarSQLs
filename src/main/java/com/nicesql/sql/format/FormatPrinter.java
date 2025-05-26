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
    public Void visitSqlStatements(GenericSQLParser.SqlStatementsContext ctx) {
        return super.visitSqlStatements(ctx);
    }

    @Override
    public Void visitStatement(GenericSQLParser.StatementContext ctx) {
        return super.visitStatement(ctx);
    }

    @Override
    public Void visitEmptyStatement(GenericSQLParser.EmptyStatementContext ctx) {
        return super.visitEmptyStatement(ctx);
    }

    @Override
    public Void visitSubfieldName(GenericSQLParser.SubfieldNameContext ctx) {
        return super.visitSubfieldName(ctx);
    }

    @Override
    public Void visitNestedFieldName(GenericSQLParser.NestedFieldNameContext ctx) {
        return super.visitNestedFieldName(ctx);
    }

    @Override
    public Void visitWithClause(GenericSQLParser.WithClauseContext ctx) {
        appendIndent();
        appendKey(ctx.WITH().getText());
        visitList(ctx.commonTableExpression(), commaNewLine());
        appendNewLine();
        return null;
    }

    @Override
    public Void visitQueryNoWith(GenericSQLParser.QueryNoWithContext ctx) {
        visit(ctx.queryPrimary());
        if (ctx.ORDER() != null) {
            appendKey(ctx.ORDER().getText());
            appendKey(ctx.BY().getText());
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
        return super.visitQueryPeriod(ctx);
    }

    @Override
    public Void visitPeriodType(GenericSQLParser.PeriodTypeContext ctx) {
        return super.visitPeriodType(ctx);
    }

    @Override
    public Void visitQueryWithParentheses(GenericSQLParser.QueryWithParenthesesContext ctx) {
        return super.visitQueryWithParentheses(ctx);
    }

    @Override
    public Void visitSetOperation(GenericSQLParser.SetOperationContext ctx) {
        return super.visitSetOperation(ctx);
    }

    //    @Override
    //    public Void visitQueryPrimaryDefault(GenericSQLParser.QueryPrimaryDefaultContext ctx) {
    //        return super.visitQueryPrimaryDefault(ctx);
    //    }

    @Override
    public Void visitSubquery(GenericSQLParser.SubqueryContext ctx) {
        return super.visitSubquery(ctx);
    }

    @Override
    public Void visitRowConstructor(GenericSQLParser.RowConstructorContext ctx) {
        return super.visitRowConstructor(ctx);
    }

    @Override
    public Void visitSortItem(GenericSQLParser.SortItemContext ctx) {
        visit(ctx.expression());
        if (ctx.ordering != null) {
            appendKey(ctx.ordering.getText());
        }
        if (ctx.nullOrdering != null) {
            appendKey(ctx.NULLS().getText());
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
        appendKey(ctx.SELECT());
        appendKey(ctx.setQuantifier().getText());
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
        appendSpace(comma()).appendBreak(options.breakCTE);
        return null;
    }

    @Override
    public Void visitSetQuantifier(GenericSQLParser.SetQuantifierContext ctx) {
        return super.visitSetQuantifier(ctx);
    }

    @Override
    public Void visitSelectSingle(GenericSQLParser.SelectSingleContext ctx) {
        return super.visitSelectSingle(ctx);
    }

    @Override
    public Void visitSelectAll(GenericSQLParser.SelectAllContext ctx) {
        return super.visitSelectAll(ctx);
    }

    @Override
    public Void visitExcludeClause(GenericSQLParser.ExcludeClauseContext ctx) {
        return super.visitExcludeClause(ctx);
    }

    @Override
    public Void visitRelations(GenericSQLParser.RelationsContext ctx) {
        assert ctx.relation().size() == ctx.LATERAL().size();
        visit(ctx.relation(0));
        for (int i = 1; i < ctx.relation().size(); i++) {
            append(comma());
            if (ctx.LATERAL(i) != null) {
                appendKey(ctx.LATERAL(i));
            }
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
        if (ctx.AS() != null) {
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
        return super.visitInlineTable(ctx);
    }

    @Override
    public Void visitSubqueryWithAlias(GenericSQLParser.SubqueryWithAliasContext ctx) {
        return super.visitSubqueryWithAlias(ctx);
    }

    @Override
    public Void visitTableFunction(GenericSQLParser.TableFunctionContext ctx) {
        return super.visitTableFunction(ctx);
    }

    @Override
    public Void visitNormalizedTableFunction(GenericSQLParser.NormalizedTableFunctionContext ctx) {
        return super.visitNormalizedTableFunction(ctx);
    }

    @Override
    public Void visitParenthesizedRelation(GenericSQLParser.ParenthesizedRelationContext ctx) {
        return super.visitParenthesizedRelation(ctx);
    }

    @Override
    public Void visitPivotClause(GenericSQLParser.PivotClauseContext ctx) {
        return super.visitPivotClause(ctx);
    }

    @Override
    public Void visitPivotAggregationExpression(GenericSQLParser.PivotAggregationExpressionContext ctx) {
        return super.visitPivotAggregationExpression(ctx);
    }

    @Override
    public Void visitPivotValue(GenericSQLParser.PivotValueContext ctx) {
        return super.visitPivotValue(ctx);
    }

    @Override
    public Void visitArgumentList(GenericSQLParser.ArgumentListContext ctx) {
        return super.visitArgumentList(ctx);
    }

    @Override
    public Void visitNamedArgumentList(GenericSQLParser.NamedArgumentListContext ctx) {
        return super.visitNamedArgumentList(ctx);
    }

    @Override
    public Void visitNamedArguments(GenericSQLParser.NamedArgumentsContext ctx) {
        return super.visitNamedArguments(ctx);
    }

    @Override
    public Void visitJoinRelation(GenericSQLParser.JoinRelationContext ctx) {
        return super.visitJoinRelation(ctx);
    }

    @Override
    public Void visitCrossOrInnerJoinType(GenericSQLParser.CrossOrInnerJoinTypeContext ctx) {
        return super.visitCrossOrInnerJoinType(ctx);
    }

    @Override
    public Void visitOuterAndSemiJoinType(GenericSQLParser.OuterAndSemiJoinTypeContext ctx) {
        return super.visitOuterAndSemiJoinType(ctx);
    }

    @Override
    public Void visitBracketHint(GenericSQLParser.BracketHintContext ctx) {
        return super.visitBracketHint(ctx);
    }

    @Override
    public Void visitJoinCriteria(GenericSQLParser.JoinCriteriaContext ctx) {
        return super.visitJoinCriteria(ctx);
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
        return super.visitKeyPartitionList(ctx);
    }

    @Override
    public Void visitTabletList(GenericSQLParser.TabletListContext ctx) {
        return super.visitTabletList(ctx);
    }

    @Override
    public Void visitReplicaList(GenericSQLParser.ReplicaListContext ctx) {
        return super.visitReplicaList(ctx);
    }

    @Override
    public Void visitMapExpressionList(GenericSQLParser.MapExpressionListContext ctx) {
        return super.visitMapExpressionList(ctx);
    }

    @Override
    public Void visitMapExpression(GenericSQLParser.MapExpressionContext ctx) {
        return super.visitMapExpression(ctx);
    }

    @Override
    public Void visitExpressionDefault(GenericSQLParser.ExpressionDefaultContext ctx) {
        if (ctx.BINARY() != null) {
            appendKey(ctx.BINARY().getText());
        }
        return visit(ctx.booleanExpression());
    }

    @Override
    public Void visitLogicalNot(GenericSQLParser.LogicalNotContext ctx) {
        if (ctx.NOT() != null) {
            appendKey(ctx.NOT().getText());
        }
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
        appendKey(ctx.NOT()).appendKey(ctx.IN());
        intoParentheses(() -> intoLevel(() -> visit(ctx.queryRelation())));
        return null;
    }

    @Override
    public Void visitInList(GenericSQLParser.InListContext ctx) {
        appendKey(ctx.NOT()).appendKey(ctx.IN());
        intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitBetween(GenericSQLParser.BetweenContext ctx) {
        appendKey(ctx.NOT()).appendKey(ctx.BETWEEN());
        visit(ctx.lower);
        appendKey(ctx.AND());
        return visit(ctx.upper);
    }

    @Override
    public Void visitLike(GenericSQLParser.LikeContext ctx) {
        appendKey(ctx.NOT());
        appendKey(ctx.op.getText());
        return visit(ctx.pattern);
    }

    //    @Override
    //    public Void visitValueExpressionDefault(GenericSQLParser.ValueExpressionDefaultContext ctx) {
    //        return super.visitValueExpressionDefault(ctx);
    //    }

    @Override
    public Void visitArithmeticBinary(GenericSQLParser.ArithmeticBinaryContext ctx) {
        visit(ctx.left);
        appendKey(ctx.operator.getText());
        return visit(ctx.right);
    }

    @Override
    public Void visitDereference(GenericSQLParser.DereferenceContext ctx) {
        visit(ctx.base);
        if (ctx.DOT_IDENTIFIER() != null) {
            appendKey(ctx.DOT_IDENTIFIER());
        } else {
            appendKey(".");
        }
        append(ctx.fieldName.getText());
        return null;
    }

    @Override
    public Void visitSimpleCase(GenericSQLParser.SimpleCaseContext ctx) {
        appendKey(ctx.CASE());
        visit(ctx.caseExpr);
        visitList(ctx.whenClause(), " ");
        if (ctx.ELSE() != null) {
            appendKey(ctx.ELSE());
            visit(ctx.elseExpression);
        }
        appendKey(ctx.END());
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
        return super.visitColumnRef(ctx);
    }

    @Override
    public Void visitSystemVariableExpression(GenericSQLParser.SystemVariableExpressionContext ctx) {
        return super.visitSystemVariableExpression(ctx);
    }

    @Override
    public Void visitConvert(GenericSQLParser.ConvertContext ctx) {
        return super.visitConvert(ctx);
    }

    @Override
    public Void visitConcat(GenericSQLParser.ConcatContext ctx) {
        return super.visitConcat(ctx);
    }

    @Override
    public Void visitSubqueryExpression(GenericSQLParser.SubqueryExpressionContext ctx) {
        return super.visitSubqueryExpression(ctx);
    }

    @Override
    public Void visitLambdaFunctionExpr(GenericSQLParser.LambdaFunctionExprContext ctx) {
        return super.visitLambdaFunctionExpr(ctx);
    }

    @Override
    public Void visitCollectionSubscript(GenericSQLParser.CollectionSubscriptContext ctx) {
        return super.visitCollectionSubscript(ctx);
    }

    @Override
    public Void visitLiteral(GenericSQLParser.LiteralContext ctx) {
        return super.visitLiteral(ctx);
    }

    @Override
    public Void visitCast(GenericSQLParser.CastContext ctx) {
        return super.visitCast(ctx);
    }

    @Override
    public Void visitCollate(GenericSQLParser.CollateContext ctx) {
        return super.visitCollate(ctx);
    }

    @Override
    public Void visitParenthesizedExpression(GenericSQLParser.ParenthesizedExpressionContext ctx) {
        return super.visitParenthesizedExpression(ctx);
    }

    @Override
    public Void visitUserVariableExpression(GenericSQLParser.UserVariableExpressionContext ctx) {
        return super.visitUserVariableExpression(ctx);
    }

    @Override
    public Void visitArrayConstructor(GenericSQLParser.ArrayConstructorContext ctx) {
        return super.visitArrayConstructor(ctx);
    }

    @Override
    public Void visitMapConstructor(GenericSQLParser.MapConstructorContext ctx) {
        return super.visitMapConstructor(ctx);
    }

    @Override
    public Void visitArraySlice(GenericSQLParser.ArraySliceContext ctx) {
        return super.visitArraySlice(ctx);
    }

    @Override
    public Void visitFunctionCallExpression(GenericSQLParser.FunctionCallExpressionContext ctx) {
        return super.visitFunctionCallExpression(ctx);
    }

    @Override
    public Void visitExists(GenericSQLParser.ExistsContext ctx) {
        return super.visitExists(ctx);
    }

    @Override
    public Void visitSearchedCase(GenericSQLParser.SearchedCaseContext ctx) {
        return super.visitSearchedCase(ctx);
    }

    @Override
    public Void visitArithmeticUnary(GenericSQLParser.ArithmeticUnaryContext ctx) {
        return super.visitArithmeticUnary(ctx);
    }

    @Override
    public Void visitNullLiteral(GenericSQLParser.NullLiteralContext ctx) {
        return super.visitNullLiteral(ctx);
    }

    @Override
    public Void visitBooleanLiteral(GenericSQLParser.BooleanLiteralContext ctx) {
        return super.visitBooleanLiteral(ctx);
    }

    @Override
    public Void visitNumericLiteral(GenericSQLParser.NumericLiteralContext ctx) {
        return super.visitNumericLiteral(ctx);
    }

    @Override
    public Void visitDateLiteral(GenericSQLParser.DateLiteralContext ctx) {
        return super.visitDateLiteral(ctx);
    }

    @Override
    public Void visitStringLiteral(GenericSQLParser.StringLiteralContext ctx) {
        return super.visitStringLiteral(ctx);
    }

    @Override
    public Void visitIntervalLiteral(GenericSQLParser.IntervalLiteralContext ctx) {
        return super.visitIntervalLiteral(ctx);
    }

    @Override
    public Void visitUnitBoundaryLiteral(GenericSQLParser.UnitBoundaryLiteralContext ctx) {
        return super.visitUnitBoundaryLiteral(ctx);
    }

    @Override
    public Void visitBinaryLiteral(GenericSQLParser.BinaryLiteralContext ctx) {
        return super.visitBinaryLiteral(ctx);
    }

    @Override
    public Void visitParameter(GenericSQLParser.ParameterContext ctx) {
        return super.visitParameter(ctx);
    }

    @Override
    public Void visitExtract(GenericSQLParser.ExtractContext ctx) {
        return super.visitExtract(ctx);
    }

    @Override
    public Void visitInformationFunction(GenericSQLParser.InformationFunctionContext ctx) {
        return super.visitInformationFunction(ctx);
    }

    @Override
    public Void visitSpecialDateTime(GenericSQLParser.SpecialDateTimeContext ctx) {
        return super.visitSpecialDateTime(ctx);
    }

    @Override
    public Void visitSpecialFunction(GenericSQLParser.SpecialFunctionContext ctx) {
        return super.visitSpecialFunction(ctx);
    }

    @Override
    public Void visitAggregationFunctionCall(GenericSQLParser.AggregationFunctionCallContext ctx) {
        return super.visitAggregationFunctionCall(ctx);
    }

    @Override
    public Void visitWindowFunctionCall(GenericSQLParser.WindowFunctionCallContext ctx) {
        return super.visitWindowFunctionCall(ctx);
    }

    @Override
    public Void visitTranslateFunctionCall(GenericSQLParser.TranslateFunctionCallContext ctx) {
        return super.visitTranslateFunctionCall(ctx);
    }

    @Override
    public Void visitSimpleFunctionCall(GenericSQLParser.SimpleFunctionCallContext ctx) {
        return super.visitSimpleFunctionCall(ctx);
    }

    @Override
    public Void visitAggregationFunction(GenericSQLParser.AggregationFunctionContext ctx) {
        return super.visitAggregationFunction(ctx);
    }

    @Override
    public Void visitUserVariable(GenericSQLParser.UserVariableContext ctx) {
        return super.visitUserVariable(ctx);
    }

    @Override
    public Void visitSystemVariable(GenericSQLParser.SystemVariableContext ctx) {
        return super.visitSystemVariable(ctx);
    }

    @Override
    public Void visitColumnReference(GenericSQLParser.ColumnReferenceContext ctx) {
        return super.visitColumnReference(ctx);
    }

    @Override
    public Void visitInformationFunctionExpression(GenericSQLParser.InformationFunctionExpressionContext ctx) {
        return super.visitInformationFunctionExpression(ctx);
    }

    @Override
    public Void visitSpecialDateTimeExpression(GenericSQLParser.SpecialDateTimeExpressionContext ctx) {
        return super.visitSpecialDateTimeExpression(ctx);
    }

    @Override
    public Void visitSpecialFunctionExpression(GenericSQLParser.SpecialFunctionExpressionContext ctx) {
        return super.visitSpecialFunctionExpression(ctx);
    }

    @Override
    public Void visitWindowFunction(GenericSQLParser.WindowFunctionContext ctx) {
        return super.visitWindowFunction(ctx);
    }

    @Override
    public Void visitWhenClause(GenericSQLParser.WhenClauseContext ctx) {
        return super.visitWhenClause(ctx);
    }

    @Override
    public Void visitOver(GenericSQLParser.OverContext ctx) {
        return super.visitOver(ctx);
    }

    @Override
    public Void visitIgnoreNulls(GenericSQLParser.IgnoreNullsContext ctx) {
        return super.visitIgnoreNulls(ctx);
    }

    @Override
    public Void visitWindowFrame(GenericSQLParser.WindowFrameContext ctx) {
        return super.visitWindowFrame(ctx);
    }

    @Override
    public Void visitUnboundedFrame(GenericSQLParser.UnboundedFrameContext ctx) {
        return super.visitUnboundedFrame(ctx);
    }

    @Override
    public Void visitCurrentRowBound(GenericSQLParser.CurrentRowBoundContext ctx) {
        return super.visitCurrentRowBound(ctx);
    }

    @Override
    public Void visitBoundedFrame(GenericSQLParser.BoundedFrameContext ctx) {
        return super.visitBoundedFrame(ctx);
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
        return null;
    }

    @Override
    public Void visitKeyPartition(GenericSQLParser.KeyPartitionContext ctx) {
        return super.visitKeyPartition(ctx);
    }
    //
    //    @Override
    //    public Void visitVarType(GenericSQLParser.VarTypeContext ctx) {
    //        return super.visitVarType(ctx);
    //    }

    //    @Override
    //    public Void visitString(GenericSQLParser.StringContext ctx) {
    //        return super.visitString(ctx);
    //    }

    //    @Override
    //    public Void visitBinary(GenericSQLParser.BinaryContext ctx) {
    //        return super.visitBinary(ctx);
    //    }

    //    @Override
    //    public Void visitComparisonOperator(GenericSQLParser.ComparisonOperatorContext ctx) {
    //        return super.visitComparisonOperator(ctx);
    //    }
    //
    //    @Override
    //    public Void visitBooleanValue(GenericSQLParser.BooleanValueContext ctx) {
    //        return super.visitBooleanValue(ctx);
    //    }

    @Override
    public Void visitInterval(GenericSQLParser.IntervalContext ctx) {
        return super.visitInterval(ctx);
    }

    //    @Override
    //    public Void visitUnitIdentifier(GenericSQLParser.UnitIdentifierContext ctx) {
    //        return super.visitUnitIdentifier(ctx);
    //    }
    //
    //    @Override
    //    public Void visitUnitBoundary(GenericSQLParser.UnitBoundaryContext ctx) {
    //        return super.visitUnitBoundary(ctx);
    //    }

    @Override
    public Void visitType(GenericSQLParser.TypeContext ctx) {
        return super.visitType(ctx);
    }

    @Override
    public Void visitArrayType(GenericSQLParser.ArrayTypeContext ctx) {
        return super.visitArrayType(ctx);
    }

    @Override
    public Void visitMapType(GenericSQLParser.MapTypeContext ctx) {
        return super.visitMapType(ctx);
    }

    @Override
    public Void visitSubfieldDesc(GenericSQLParser.SubfieldDescContext ctx) {
        return super.visitSubfieldDesc(ctx);
    }

    @Override
    public Void visitSubfieldDescs(GenericSQLParser.SubfieldDescsContext ctx) {
        return super.visitSubfieldDescs(ctx);
    }

    @Override
    public Void visitStructType(GenericSQLParser.StructTypeContext ctx) {
        return super.visitStructType(ctx);
    }

    @Override
    public Void visitTypeParameter(GenericSQLParser.TypeParameterContext ctx) {
        return super.visitTypeParameter(ctx);
    }

    @Override
    public Void visitBaseType(GenericSQLParser.BaseTypeContext ctx) {
        return super.visitBaseType(ctx);
    }

    @Override
    public Void visitDecimalType(GenericSQLParser.DecimalTypeContext ctx) {
        return super.visitDecimalType(ctx);
    }

//    @Override
//    public Void visitQualifiedName(GenericSQLParser.QualifiedNameContext ctx) {
//        return super.visitQualifiedName(ctx);
//    }

    @Override
    public Void visitUnquotedIdentifier(GenericSQLParser.UnquotedIdentifierContext ctx) {
        return super.visitUnquotedIdentifier(ctx);
    }

    @Override
    public Void visitDigitIdentifier(GenericSQLParser.DigitIdentifierContext ctx) {
        return super.visitDigitIdentifier(ctx);
    }

    @Override
    public Void visitBackQuotedIdentifier(GenericSQLParser.BackQuotedIdentifierContext ctx) {
        return super.visitBackQuotedIdentifier(ctx);
    }

    @Override
    public Void visitIdentifierList(GenericSQLParser.IdentifierListContext ctx) {
        return super.visitIdentifierList(ctx);
    }

    @Override
    public Void visitIdentifierOrString(GenericSQLParser.IdentifierOrStringContext ctx) {
        return super.visitIdentifierOrString(ctx);
    }

    @Override
    public Void visitDecimalValue(GenericSQLParser.DecimalValueContext ctx) {
        return super.visitDecimalValue(ctx);
    }

    @Override
    public Void visitDoubleValue(GenericSQLParser.DoubleValueContext ctx) {
        return super.visitDoubleValue(ctx);
    }

    @Override
    public Void visitIntegerValue(GenericSQLParser.IntegerValueContext ctx) {
        return super.visitIntegerValue(ctx);
    }
}
