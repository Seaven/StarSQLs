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
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class FormatPrinter extends FormatPrinterBase {

    public FormatPrinter(FormatOptions options) {
        this.options = options;
    }

    protected Void visitList(List<? extends ParserRuleContext> contexts, String splitStr) {
        for (int i = 0; i < contexts.size(); i++) {
            visit(contexts.get(i));
            if (i != contexts.size() - 1) {
                sql.append(splitStr);
            }
        }
        return null;
    }

    protected void visitListAutoBreak(List<? extends ParserRuleContext> contexts, String splitStr) {
        for (int i = 0; i < contexts.size(); i++) {
            final int j = i;
            sql.intoAutoBreak(() -> {
                visit(contexts.get(j));
                if (j != contexts.size() - 1) {
                    sql.append(splitStr);
                }
            });
        }
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
        sql = new SQLBuilder(options);
        visit(ctx.queryStatement());
        if (ctx.SEMICOLON() != null) {
            sql.append(ctx.SEMICOLON().getText());
        }
        if (ctx.EOF() != null) {
            sql.append(ctx.EOF().getText());
        }
        visit(ctx.emptyStatement());
        formatSQLs.add(sql);
        return null;
    }

    @Override
    public Void visitEmptyStatement(GenericSQLParser.EmptyStatementContext ctx) {
        sql.append(ctx.SEMICOLON().getText());
        return null;
    }

    @Override
    public Void visitSubfieldName(GenericSQLParser.SubfieldNameContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitNestedFieldName(GenericSQLParser.NestedFieldNameContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitWithClause(GenericSQLParser.WithClauseContext ctx) {
        sql.appendKey(ctx.WITH());
        sql.appendBreak(options.breakCTE);
        visitList(ctx.commonTableExpression(), commaBreak(options.breakCTE));
        sql.appendNewLine();
        return null;
    }

    @Override
    public Void visitQueryNoWith(GenericSQLParser.QueryNoWithContext ctx) {
        visit(ctx.queryPrimary());
        if (ctx.ORDER() != null) {
            sql.appendNewLine();
            sql.appendKey(ctx.ORDER());
            sql.appendKey(ctx.BY());
            sql.intoLevel(() -> {
                sql.appendBreak(options.breakOrderBy);
                visitListAutoBreak(ctx.sortItem(), commaBreak(options.breakOrderBy));
            });
        }
        if (ctx.limitElement() != null) {
            sql.appendNewLine();
            visit(ctx.limitElement());
        }
        return null;
    }

    @Override
    public Void visitQueryPeriod(GenericSQLParser.QueryPeriodContext ctx) {
        sql.appendKey(ctx.FOR());
        visit(ctx.periodType());
        if (ctx.BETWEEN() != null) {
            sql.appendKey(ctx.BETWEEN());
            visit(ctx.expression(0));
            sql.appendKey(ctx.AND());
            visit(ctx.expression(1));
        } else if (ctx.FROM() != null) {
            sql.appendKey(ctx.FROM());
            visit(ctx.expression(0));
            sql.appendKey(ctx.TO());
            visit(ctx.expression(1));
        } else if (ctx.ALL() != null) {
            sql.appendKey(ctx.ALL());
        } else if (ctx.AS() != null && ctx.end != null) {
            sql.appendKey(ctx.AS());
            sql.appendKey(ctx.OF());
            visit(ctx.end);
        }
        return null;
    }

    @Override
    public Void visitPeriodType(GenericSQLParser.PeriodTypeContext ctx) {
        sql.appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitSetOperation(GenericSQLParser.SetOperationContext ctx) {
        visit(ctx.left);
        sql.appendNewLine();
        sql.appendKey(ctx.operator.getText());
        visit(ctx.setQuantifier());
        sql.appendNewLine();
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitSubquery(GenericSQLParser.SubqueryContext ctx) {
        sql.intoParentheses(() -> {
            sql.intoLevel(() -> {
                sql.appendNewLine();
                visit(ctx.queryRelation());
            });
            sql.appendNewLine();
        });
        return null;
    }

    @Override
    public Void visitRowConstructor(GenericSQLParser.RowConstructorContext ctx) {
        sql.intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitSortItem(GenericSQLParser.SortItemContext ctx) {
        visit(ctx.expression());
        if (ctx.ordering != null) {
            sql.appendKey(ctx.ordering.getText(), true, false);
        }
        if (ctx.nullOrdering != null) {
            sql.appendKey(ctx.NULLS());
            sql.appendKey(ctx.nullOrdering.getText());
        }
        return null;
    }

    @Override
    public Void visitLimitElement(GenericSQLParser.LimitElementContext ctx) {
        sql.appendKey(ctx.LIMIT().getText());
        if (ctx.OFFSET() != null) {
            sql.append(ctx.limit.getText());
            sql.appendKey(ctx.OFFSET().getText());
            sql.append(ctx.offset.getText());
        } else if (ctx.offset != null) {
            sql.append(ctx.offset.getText());
            sql.append(comma());
            sql.append(ctx.limit.getText());
        } else {
            sql.append(ctx.limit.getText());
        }
        return null;
    }

    @Override
    public Void visitQuerySpecification(GenericSQLParser.QuerySpecificationContext ctx) {
        sql.appendKey(ctx.SELECT().getText(), false, true);
        visit(ctx.setQuantifier());
        sql.intoLevel(() -> {
            sql.appendBreak(options.breakSelectItems);
            visitList(ctx.selectItem(), commaBreak(options.breakSelectItems));
        });
        sql.appendNewLine();
        sql.intoLevel(() -> visit(ctx.fromClause()));
        if (ctx.where != null) {
            sql.appendNewLine();
            sql.appendKey(ctx.WHERE());
            sql.intoLevel(() -> visit(ctx.where));
        }
        if (ctx.groupingElement() != null) {
            sql.appendNewLine();
            sql.appendKey(ctx.GROUP()).appendKey(ctx.BY());
            sql.intoLevel(() -> {
                sql.appendBreak(options.breakGroupByItems);
                visit(ctx.groupingElement());
            });
        }
        if (ctx.having != null) {
            sql.appendNewLine();
            sql.appendKey(ctx.HAVING());
            visit(ctx.having);
        }
        if (ctx.QUALIFY() != null) {
            sql.appendNewLine();
            sql.appendKey(ctx.QUALIFY());
            visit(ctx.qualifyFunction);
            sql.appendKey(ctx.comparisonOperator().getText());
            sql.append(ctx.limit.getText());
        }
        return null;
    }

    @Override
    public Void visitFrom(GenericSQLParser.FromContext ctx) {
        sql.appendKey(ctx.FROM());
        visit(ctx.relations());
        visit(ctx.pivotClause());
        return null;
    }

    @Override
    public Void visitCommonTableExpression(GenericSQLParser.CommonTableExpressionContext ctx) {
        sql.append(ctx.name.getText()).append(" ");
        visit(ctx.columnAliases());
        sql.appendKey(ctx.AS());
        sql.intoParentheses(() -> sql.intoLevel(() -> {
            sql.appendNewLine();
            visit(ctx.queryRelation());
        }));
        return null;
    }

    @Override
    public Void visitSetQuantifier(GenericSQLParser.SetQuantifierContext ctx) {
        sql.appendKey(ctx.getText(), false, true);
        return null;
    }

    @Override
    public Void visitSelectSingle(GenericSQLParser.SelectSingleContext ctx) {
        sql.intoAutoBreak(() -> {
            visit(ctx.expression());
                sql.appendKey(ctx.AS());
                if (ctx.identifier() != null) {
                    sql.append(ctx.identifier().getText(), true, false);
                }
                if (ctx.string() != null) {
                    sql.append(ctx.string().getText(), true, false);
                }
        });
        return null;
    }

    @Override
    public Void visitSelectAll(GenericSQLParser.SelectAllContext ctx) {
        sql.intoAutoBreak(() -> {
            if (ctx.qualifiedName() != null) {
                sql.append(ctx.qualifiedName().getText());
                sql.append(".");
            }
            sql.append(ctx.ASTERISK_SYMBOL().getText());
            if (ctx.excludeClause() != null) {
                visit(ctx.excludeClause());
            }
        });
        return null;
    }

    @Override
    public Void visitExcludeClause(GenericSQLParser.ExcludeClauseContext ctx) {
        sql.appendKey(ctx.getChild(0).getText());
        sql.intoParentheses(() -> visitList(ctx.identifier(), comma()));
        return null;
    }

    @Override
    public Void visitRelations(GenericSQLParser.RelationsContext ctx) {
        sql.intoAutoBreak(() -> super.visitRelations(ctx));
        return null;
    }

    @Override
    public Void visitNonBracketsRelation(GenericSQLParser.NonBracketsRelationContext ctx) {
        visit(ctx.relationPrimary());
        if (ctx.joinRelation() == null || ctx.joinRelation().isEmpty()) {
            return null;
        }
        if (options.breakJoinRelations) {
            sql.appendBreak(true);
            visitList(ctx.joinRelation(), sql.newBreak(options.breakJoinRelations));
        } else {
            sql.intoLevel(() -> visitList(ctx.joinRelation(), newLine()));
        }
        return null;
    }

    @Override
    public Void visitBracketsRelation(GenericSQLParser.BracketsRelationContext ctx) {
        sql.intoParentheses(() -> {
            visit(ctx.relationPrimary());
            if (ctx.joinRelation() == null || ctx.joinRelation().isEmpty()) {
                return;
            }
            if (options.breakJoinRelations) {
                sql.appendBreak(true);
                visitList(ctx.joinRelation(), sql.newBreak(options.breakJoinRelations));
            } else {
                sql.intoLevel(() -> visitList(ctx.joinRelation(), newLine()));
            }
        });
        return null;
    }

    @Override
    public Void visitTableAtom(GenericSQLParser.TableAtomContext ctx) {
        sql.append(ctx.qualifiedName().getText());
        visit(ctx.queryPeriod());
        visit(ctx.partitionNames());
        visit(ctx.tabletList());
        visit(ctx.replicaList());
        if (ctx.alias != null) {
            sql.appendKey(ctx.AS());
            sql.append(ctx.alias.getText(), true, false);
        }
        visit(ctx.bracketHint());
        if (ctx.BEFORE() != null) {
            sql.appendKey(ctx.BEFORE());
            sql.append(ctx.ts.getText());
        }
        return null;
    }

    @Override
    public Void visitInlineTable(GenericSQLParser.InlineTableContext ctx) {
        sql.intoParentheses(() -> {
            sql.appendKey(ctx.VALUES().getText());
            visitList(ctx.rowConstructor(), comma());
        });
        if (ctx.alias != null) {
            sql.appendKey(ctx.AS());
            sql.append(ctx.alias.getText(), true, false);
        }
        if (ctx.columnAliases() != null) {
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitSubqueryWithAlias(GenericSQLParser.SubqueryWithAliasContext ctx) {
        if (ctx.ASSERT_ROWS() != null) {
            sql.appendKey(ctx.ASSERT_ROWS());
        }
        visit(ctx.subquery());
        if (ctx.alias != null) {
            sql.appendKey(ctx.AS());
            sql.append(ctx.alias.getText(), true, false);
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitTableFunction(GenericSQLParser.TableFunctionContext ctx) {
        visit(ctx.qualifiedName());
        sql.append("(");
        visit(ctx.expressionList());
        sql.append(")");
        if (ctx.alias != null) {
            visit(ctx.AS());
            sql.append(ctx.alias.getText(), true, false);
            visit(ctx.columnAliases());
        }
        return null;
    }

    @Override
    public Void visitNormalizedTableFunction(GenericSQLParser.NormalizedTableFunctionContext ctx) {
        visit(ctx.TABLE());
        sql.intoParentheses(() -> {
            visit(ctx.qualifiedName());
            sql.intoParentheses(() -> visit(ctx.argumentList()));
        });
        visit(ctx.AS());
        sql.append(ctx.alias.getText(), true, false);
        visit(ctx.columnAliases());
        return null;
    }

    @Override
    public Void visitParenthesizedRelation(GenericSQLParser.ParenthesizedRelationContext ctx) {
        sql.intoParentheses(() -> visit(ctx.relations()));
        return null;
    }

    @Override
    public Void visitPivotClause(GenericSQLParser.PivotClauseContext ctx) {
        sql.appendKey(ctx.PIVOT().getText());
        sql.intoParentheses(() -> {
            visitList(ctx.pivotAggregationExpression(), comma());
            sql.appendKey(ctx.FOR().getText());
            if (ctx.identifier() != null) {
                sql.append(ctx.identifier().getText());
            } else if (ctx.identifierList() != null) {
                visit(ctx.identifierList());
            }
            sql.appendKey(ctx.IN().getText());
            sql.intoParentheses(() -> visitList(ctx.pivotValue(), comma()));
        });
        return null;
    }

    @Override
    public Void visitPivotAggregationExpression(GenericSQLParser.PivotAggregationExpressionContext ctx) {
        visit(ctx.functionCall());
        sql.appendKey(ctx.AS());
        if (ctx.identifier() != null) {
            sql.append(ctx.identifier().getText(), true, false);
        }
        if (ctx.string() != null) {
            sql.append(ctx.string().getText(), true, false);
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
        sql.appendKey(ctx.AS());
        if (ctx.identifier() != null) {
            sql.append(ctx.identifier().getText(), true, false);
        }
        if (ctx.string() != null) {
            sql.append(ctx.string().getText(), true, false);
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
        sql.append(ctx.identifier().getText());
        sql.append(" => ");
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
        visit(ctx.bracketHint());
        sql.appendKey(ctx.LATERAL());
        visit(ctx.rightRelation);
        sql.appendBreak(options.breakJoinOn);
        visit(ctx.joinCriteria());
        return null;
    }

    @Override
    public Void visitBracketHint(GenericSQLParser.BracketHintContext ctx) {
        sql.appendKey("[");
        visitList(ctx.identifier(), comma());
        if (ctx.primaryExpression() == null) {
            sql.append("|");
            visit(ctx.primaryExpression());
            visit(ctx.literalExpressionList());
        }
        sql.appendKey("]");
        return null;
    }

    @Override
    public Void visitJoinCriteria(GenericSQLParser.JoinCriteriaContext ctx) {
        if (ctx.ON() != null) {
            sql.appendKey(ctx.ON());
            visit(ctx.expression());
        }
        if (ctx.USING() != null) {
            sql.appendKey(ctx.USING());
            sql.intoParentheses(() -> visitList(ctx.identifier(), comma()));
        }
        return null;
    }

    @Override
    public Void visitColumnAliases(GenericSQLParser.ColumnAliasesContext ctx) {
        sql.intoParentheses(() -> visitList(ctx.identifier(), comma()));
        return null;
    }

    @Override
    public Void visitPartitionNames(GenericSQLParser.PartitionNamesContext ctx) {
        return super.visitPartitionNames(ctx);
    }

    @Override
    public Void visitKeyPartitionList(GenericSQLParser.KeyPartitionListContext ctx) {
        sql.appendKey(ctx.PARTITION().getText());
        sql.intoParentheses(() -> visitList(ctx.keyPartition(), comma()));
        return null;
    }

    @Override
    public Void visitTabletList(GenericSQLParser.TabletListContext ctx) {
        sql.appendKey(ctx.TABLET().getText());
        sql.intoParentheses(() -> {
            for (int i = 0; i < ctx.INTEGER_VALUE().size(); i++) {
                sql.append(ctx.INTEGER_VALUE(i).getText());
                if (i < ctx.INTEGER_VALUE().size() - 1) {
                    sql.append(comma());
                }
            }
        });
        return null;
    }

    @Override
    public Void visitReplicaList(GenericSQLParser.ReplicaListContext ctx) {
        sql.appendKey(ctx.REPLICA());
        sql.intoParentheses(() -> {
            for (int i = 0; i < ctx.INTEGER_VALUE().size(); i++) {
                sql.append(ctx.INTEGER_VALUE(i).getText());
                if (i < ctx.INTEGER_VALUE().size() - 1) {
                    sql.append(comma());
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
        sql.append(":");
        visit(ctx.value);
        return null;
    }

    @Override
    public Void visitExpressionDefault(GenericSQLParser.ExpressionDefaultContext ctx) {
        sql.appendKey(ctx.BINARY());
        return visit(ctx.booleanExpression());
    }

    @Override
    public Void visitLogicalNot(GenericSQLParser.LogicalNotContext ctx) {
        sql.appendKey(ctx.NOT());
        return visit(ctx.expression());
    }

    @Override
    public Void visitLogicalBinary(GenericSQLParser.LogicalBinaryContext ctx) {
        visit(ctx.left);
        sql.appendBreak(options.breakAndOr);
        sql.appendKey(ctx.operator.getText());
        return visit(ctx.right);
    }

    @Override
    public Void visitExpressionList(GenericSQLParser.ExpressionListContext ctx) {
        return visitList(ctx.expression(), comma());
    }

    @Override
    public Void visitComparison(GenericSQLParser.ComparisonContext ctx) {
        visit(ctx.left);
        sql.appendKey(ctx.comparisonOperator().getText());
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitIsNull(GenericSQLParser.IsNullContext ctx) {
        visit(ctx.booleanExpression());
        sql.appendKey(ctx.IS()).appendKey(ctx.NOT()).appendKey(ctx.NULL());
        return null;
    }

    @Override
    public Void visitScalarSubquery(GenericSQLParser.ScalarSubqueryContext ctx) {
        visit(ctx.booleanExpression());
        sql.appendKey(ctx.comparisonOperator().getText());
        sql.intoParentheses(() -> sql.intoLevel(() -> {
            sql.appendNewLine();
            visit(ctx.queryRelation());
        }));
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
        sql.intoParentheses(() -> visitList(ctx.expression(), comma()));
        sql.appendKey(ctx.NOT()).appendKey(ctx.IN());
        sql.intoParentheses(() -> {
            sql.intoLevel(() -> {
                sql.appendNewLine();
                visit(ctx.queryRelation());
            });
            sql.appendNewLine();
        });
        return null;
    }

    @Override
    public Void visitInList(GenericSQLParser.InListContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT()).appendKey(ctx.IN());
        sql.appendBreak(options.breakInList);
        sql.intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitInSubquery(GenericSQLParser.InSubqueryContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT()).appendKey(ctx.IN());
        sql.intoParentheses(() -> {
            sql.intoLevel(() -> {
                sql.appendNewLine();
                visit(ctx.queryRelation());
            });
            sql.appendNewLine();
        });
        return null;
    }

    @Override
    public Void visitBetween(GenericSQLParser.BetweenContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT());
        sql.appendKey(ctx.BETWEEN());
        visit(ctx.lower);
        sql.appendKey(ctx.AND());
        visit(ctx.upper);
        return null;
    }

    @Override
    public Void visitLike(GenericSQLParser.LikeContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT());
        sql.appendKey(ctx.op.getText());
        return visit(ctx.pattern);
    }

    @Override
    public Void visitArithmeticBinary(GenericSQLParser.ArithmeticBinaryContext ctx) {
        visit(ctx.left);
        if (options.isCompact) {
            sql.append(ctx.operator.getText());
        } else {
            sql.appendKey(ctx.operator.getText());
        }
        return visit(ctx.right);
    }

    @Override
    public Void visitDereference(GenericSQLParser.DereferenceContext ctx) {
        visit(ctx.base);
        if (ctx.DOT_IDENTIFIER() != null) {
            sql.append(ctx.DOT_IDENTIFIER().getText());
        } else {
            sql.append(".");
        }
        sql.append(ctx.fieldName.getText());
        return null;
    }

    @Override
    public Void visitSimpleCase(GenericSQLParser.SimpleCaseContext ctx) {
        sql.appendKey(ctx.CASE().getText(), false, true);
        visit(ctx.caseExpr);
        sql.intoLevel(() -> {
            sql.appendBreak(options.breakCaseWhen);
            visitList(ctx.whenClause(), options.breakCaseWhen ? newLine() : " ");
            if (ctx.ELSE() != null) {
                sql.appendBreak(options.breakCaseWhen);
                sql.appendKey(ctx.ELSE());
                visit(ctx.elseExpression);
            }
        });
        sql.appendBreak(options.breakCaseWhen);
        sql.appendKey(ctx.END().getText(), true, false);
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
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSystemVariableExpression(GenericSQLParser.SystemVariableExpressionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitConvert(GenericSQLParser.ConvertContext ctx) {
        sql.appendKey(ctx.CONVERT(), false, false);
        sql.intoParentheses(() -> {
            visit(ctx.expression());
            sql.append(comma());
            visit(ctx.type());
        });
        return null;
    }

    @Override
    public Void visitConcat(GenericSQLParser.ConcatContext ctx) {
        visit(ctx.left);
        sql.append("||");
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
            sql.append(ctx.identifier().getText());
        } else if (ctx.identifierList() != null) {
            visit(ctx.identifierList());
        }
        sql.append("->");
        visit(ctx.expression());
        return null;
    }

    @Override
    public Void visitCollectionSubscript(GenericSQLParser.CollectionSubscriptContext ctx) {
        visit(ctx.value);
        sql.append("[");
        visit(ctx.index);
        sql.append("]");
        return null;
    }

    @Override
    public Void visitLiteral(GenericSQLParser.LiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitCast(GenericSQLParser.CastContext ctx) {
        sql.appendKey(ctx.CAST(), false, false);
        sql.intoParentheses(() -> {
            visit(ctx.expression());
            sql.appendKey(ctx.AS());
            visit(ctx.type());
        });
        return null;
    }

    @Override
    public Void visitCollate(GenericSQLParser.CollateContext ctx) {
        visit(ctx.primaryExpression());
        sql.appendKey(ctx.COLLATE());
        if (ctx.identifier() != null) {
            sql.append(ctx.identifier().getText());
        } else if (ctx.string() != null) {
            sql.append(ctx.string().getText());
        }
        return null;
    }

    @Override
    public Void visitParenthesizedExpression(GenericSQLParser.ParenthesizedExpressionContext ctx) {
        sql.intoParentheses(() -> sql.intoLevel(() -> visit(ctx.expression())));
        return null;
    }

    @Override
    public Void visitUserVariableExpression(GenericSQLParser.UserVariableExpressionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitArrayConstructor(GenericSQLParser.ArrayConstructorContext ctx) {
        if (ctx.arrayType() != null) {
            visit(ctx.arrayType());
        }
        sql.append("[");
        if (ctx.expressionList() != null) {
            visit(ctx.expressionList());
        }
        sql.append("]");
        return null;
    }

    @Override
    public Void visitMapConstructor(GenericSQLParser.MapConstructorContext ctx) {
        if (ctx.mapType() != null) {
            visit(ctx.mapType());
        } else if (ctx.MAP() != null) {
            sql.appendKey(ctx.MAP(), false, false);
        }
        sql.append("{");
        if (ctx.mapExpressionList() != null) {
            visit(ctx.mapExpressionList());
        }
        sql.append("}");
        return null;
    }

    @Override
    public Void visitArraySlice(GenericSQLParser.ArraySliceContext ctx) {
        visit(ctx.primaryExpression());
        sql.append("[");
        if (ctx.start != null) {
            sql.append(ctx.start.getText());
        }
        sql.append(":");
        if (ctx.end != null) {
            sql.append(ctx.end.getText());
        }
        sql.append("]");
        return null;
    }

    @Override
    public Void visitFunctionCallExpression(GenericSQLParser.FunctionCallExpressionContext ctx) {
        visit(ctx.functionCall());
        return null;
    }

    @Override
    public Void visitExists(GenericSQLParser.ExistsContext ctx) {
        sql.appendKey(ctx.EXISTS());
        sql.intoParentheses(() -> sql.intoLevel(() -> {
            sql.appendNewLine();
            visit(ctx.queryRelation());
        }));
        return null;
    }

    @Override
    public Void visitSearchedCase(GenericSQLParser.SearchedCaseContext ctx) {
        sql.appendKey(ctx.CASE(), false, true);
        sql.intoLevel(() -> {
            sql.appendBreak(options.breakCaseWhen);
            visitList(ctx.whenClause(), options.breakCaseWhen ? newLine() : " ");
            if (ctx.ELSE() != null) {
                sql.appendBreak(options.breakCaseWhen);
                sql.appendKey(ctx.ELSE());
                visit(ctx.elseExpression);
            }
        });
        sql.appendBreak(options.breakCaseWhen);
        sql.appendKey(ctx.END(), true, false);
        return null;
    }

    @Override
    public Void visitArithmeticUnary(GenericSQLParser.ArithmeticUnaryContext ctx) {
        sql.append(ctx.operator.getText());
        visit(ctx.primaryExpression());
        return null;
    }

    @Override
    public Void visitNullLiteral(GenericSQLParser.NullLiteralContext ctx) {
        sql.appendKey(ctx.NULL());
        return null;
    }

    @Override
    public Void visitBooleanLiteral(GenericSQLParser.BooleanLiteralContext ctx) {
        sql.appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitNumericLiteral(GenericSQLParser.NumericLiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDateLiteral(GenericSQLParser.DateLiteralContext ctx) {
        sql.appendKey(ctx.DATE());
        sql.appendKey(ctx.DATETIME());
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitStringLiteral(GenericSQLParser.StringLiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIntervalLiteral(GenericSQLParser.IntervalLiteralContext ctx) {
        visit(ctx.interval());
        return null;
    }

    @Override
    public Void visitUnitBoundaryLiteral(GenericSQLParser.UnitBoundaryLiteralContext ctx) {
        sql.appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitBinaryLiteral(GenericSQLParser.BinaryLiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitParameter(GenericSQLParser.ParameterContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitExtract(GenericSQLParser.ExtractContext ctx) {
        sql.appendKey(ctx.EXTRACT(), false, false);
        sql.intoParentheses(() -> {
            sql.append(ctx.identifier().getText());
            sql.appendKey(ctx.FROM());
            visit(ctx.valueExpression());
        });
        return null;
    }

    @Override
    public Void visitInformationFunction(GenericSQLParser.InformationFunctionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialDateTime(GenericSQLParser.SpecialDateTimeContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialFunction(GenericSQLParser.SpecialFunctionContext ctx) {
        sql.append(ctx.getText());
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
        sql.appendKey(ctx.TRANSLATE(), false, false);
        sql.intoParentheses(() -> visitList(ctx.expression(), comma()));
        return null;
    }

    @Override
    public Void visitSimpleFunctionCall(GenericSQLParser.SimpleFunctionCallContext ctx) {
        sql.append(ctx.qualifiedName().getText());
        if (options.breakFunctionArgs) {
            sql.intoParentheses(() -> visitList(ctx.expression(), commaBreak(true)));
        } else if (options.alignFunctionArgs) {
            sql.intoParentheses(() -> sql.intoPrefix(() -> sql.intoAutoBreak(() -> visitList(ctx.expression(), comma()))));
        }
        if (ctx.over() != null) {
            visit(ctx.over());
        }
        return null;
    }

    @Override
    public Void visitAggregationFunction(GenericSQLParser.AggregationFunctionContext ctx) {
        sql.appendKey(ctx.name.getText(), false, false);
        sql.intoParentheses(() -> {
            if (ctx.name.getType() == GenericSQLParser.AVG || ctx.name.getType() == GenericSQLParser.MAX
                    || ctx.name.getType() == GenericSQLParser.MIN || ctx.name.getType() == GenericSQLParser.SUM) {
                visit(ctx.setQuantifier());
                visit(ctx.expression(0));
            } else if (ctx.name.getType() == GenericSQLParser.COUNT) {
                if (ctx.ASTERISK_SYMBOL() != null) {
                    sql.append(ctx.ASTERISK_SYMBOL().getText());
                } else {
                    visit(ctx.setQuantifier());
                    visit(ctx.bracketHint());
                    visitList(ctx.expression(), comma());
                }
            } else if (ctx.name.getType() == GenericSQLParser.ARRAY_AGG
                    || ctx.name.getType() == GenericSQLParser.ARRAY_AGG_DISTINCT) {
                visit(ctx.setQuantifier());
                visit(ctx.expression(0));
                sql.appendKey(ctx.ORDER());
                sql.appendKey(ctx.BY());
                visitList(ctx.sortItem(), comma());
            } else if (ctx.name.getType() == GenericSQLParser.GROUP_CONCAT) {
                visit(ctx.setQuantifier());
                visitList(ctx.expression().subList(0, ctx.expression().size() - 1), comma());
                if (ctx.ORDER() != null) {
                    sql.appendKey(ctx.ORDER());
                    sql.appendKey(ctx.BY());
                    visitList(ctx.sortItem(), comma());
                }
                if (ctx.SEPARATOR() != null) {
                    sql.appendKey(ctx.SEPARATOR());
                    visit(ctx.expression(ctx.expression().size() - 1));
                }
            }
        });
        return null;
    }

    @Override
    public Void visitUserVariable(GenericSQLParser.UserVariableContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSystemVariable(GenericSQLParser.SystemVariableContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitColumnReference(GenericSQLParser.ColumnReferenceContext ctx) {
        sql.append(ctx.identifier().getText());
        return null;
    }

    @Override
    public Void visitInformationFunctionExpression(GenericSQLParser.InformationFunctionExpressionContext ctx) {
        sql.appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitSpecialDateTimeExpression(GenericSQLParser.SpecialDateTimeExpressionContext ctx) {
        sql.appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitSpecialFunctionExpression(GenericSQLParser.SpecialFunctionExpressionContext ctx) {
        sql.appendKey(ctx.name.getText(), false, false);
        sql.intoParentheses(() -> {
            sql.append(ctx.unitIdentifier().getText());
            sql.append(comma());
            visitList(ctx.expression(), comma());
        });
        return null;
    }

    @Override
    public Void visitWindowFunction(GenericSQLParser.WindowFunctionContext ctx) {
        sql.appendKey(ctx.name.getText(), false, false);
        sql.intoParentheses(() -> {
            if (ctx.expression() != null) {
                visit(ctx.expression(0));
                if (ctx.null1 != null) {
                    sql.appendKey(ctx.null1.getText());
                }
                if (ctx.expression().size() > 1) {
                    visitList(ctx.expression().subList(1, ctx.expression().size()), comma());
                }
            }
        });
        if (ctx.null2 != null) {
            sql.appendKey(ctx.null2.getText());
        }
        return null;
    }

    @Override
    public Void visitWhenClause(GenericSQLParser.WhenClauseContext ctx) {
        sql.appendKey(ctx.WHEN());
        visit(ctx.condition);
        sql.appendKey(ctx.THEN());
        visit(ctx.result);
        return null;
    }

    @Override
    public Void visitOver(GenericSQLParser.OverContext ctx) {
        sql.appendKey(ctx.OVER());
        sql.intoParentheses(() -> {
            if (ctx.bracketHint() != null) {
                visit(ctx.bracketHint());
            }
            if (ctx.PARTITION() != null) {
                sql.appendKey(ctx.PARTITION());
                sql.appendKey(ctx.BY(0));
                visitList(ctx.partition, comma());
            }
            if (ctx.ORDER() != null) {
                sql.appendKey(ctx.ORDER());
                sql.appendKey(ctx.BY(0));
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
        sql.appendKey(ctx.IGNORE());
        sql.appendKey(ctx.NULLS());
        return null;
    }

    @Override
    public Void visitWindowFrame(GenericSQLParser.WindowFrameContext ctx) {
        sql.appendKey(ctx.frameType.getText());
        if (ctx.BETWEEN() != null) {
            sql.appendKey(ctx.BETWEEN());
            visit(ctx.start);
            sql.appendKey(ctx.AND());
            visit(ctx.end);
        } else {
            visit(ctx.start);
        }
        return null;
    }

    @Override
    public Void visitUnboundedFrame(GenericSQLParser.UnboundedFrameContext ctx) {
        sql.appendKey(ctx.UNBOUNDED());
        sql.appendKey(ctx.boundType.getText());
        return null;
    }

    @Override
    public Void visitCurrentRowBound(GenericSQLParser.CurrentRowBoundContext ctx) {
        sql.appendKey(ctx.CURRENT());
        sql.appendKey(ctx.ROW());
        return null;
    }

    @Override
    public Void visitBoundedFrame(GenericSQLParser.BoundedFrameContext ctx) {
        visit(ctx.expression());
        sql.appendKey(ctx.boundType.getText());
        return null;
    }

    @Override
    public Void visitExplainDesc(GenericSQLParser.ExplainDescContext ctx) {
        sql.appendKey(ctx.EXPLAIN().getText());
        sql.appendKey(ctx.level.getText());
        sql.appendBreak(options.breakExplain);
        return null;
    }

    @Override
    public Void visitLiteralExpressionList(GenericSQLParser.LiteralExpressionListContext ctx) {
        sql.intoParentheses(() -> {
            for (int i = 0; i < ctx.literalExpression().size(); i++) {
                visit(ctx.literalExpression(i));
                if (i < ctx.literalExpression().size() - 1) {
                    sql.append(comma());
                }
            }
        });
        return null;
    }

    @Override
    public Void visitKeyPartition(GenericSQLParser.KeyPartitionContext ctx) {
        sql.append(ctx.partitionColName.getText());
        sql.append("=");
        visit(ctx.partitionColValue);
        return null;
    }

    @Override
    public Void visitInterval(GenericSQLParser.IntervalContext ctx) {
        sql.appendKey(ctx.INTERVAL().getText());
        visit(ctx.value);
        visit(ctx.from);
        return null;
    }

    @Override
    public Void visitArrayType(GenericSQLParser.ArrayTypeContext ctx) {
        sql.appendKey(ctx.ARRAY().getText(), false, false);
        if (ctx.type() != null) {
            sql.append("<");
            visit(ctx.type());
            sql.append(">");
        }
        return null;
    }

    @Override
    public Void visitMapType(GenericSQLParser.MapTypeContext ctx) {
        sql.appendKey(ctx.MAP().getText(), false, false);
        if (ctx.type() != null) {
            sql.append("<");
            visitList(ctx.type(), comma());
            sql.append(">");
        }
        return null;
    }

    @Override
    public Void visitSubfieldDescs(GenericSQLParser.SubfieldDescsContext ctx) {
        visitList(ctx.subfieldDesc(), comma());
        return null;
    }

    @Override
    public Void visitSubfieldDesc(GenericSQLParser.SubfieldDescContext ctx) {
        sql.append(ctx.identifier().getText());
        sql.append(" ");
        visit(ctx.type());
        return null;
    }

    @Override
    public Void visitStructType(GenericSQLParser.StructTypeContext ctx) {
        sql.appendKey(ctx.STRUCT().getText(), true, false);
        if (ctx.subfieldDescs() != null) {
            sql.append("<");
            visit(ctx.subfieldDescs());
            sql.append(">");
        }
        return null;
    }

    @Override
    public Void visitTypeParameter(GenericSQLParser.TypeParameterContext ctx) {
        sql.intoParentheses(() -> sql.append(ctx.INTEGER_VALUE().getText()));
        return null;
    }

    @Override
    public Void visitDecimalType(GenericSQLParser.DecimalTypeContext ctx) {
        sql.appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitUnquotedIdentifier(GenericSQLParser.UnquotedIdentifierContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDigitIdentifier(GenericSQLParser.DigitIdentifierContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitBackQuotedIdentifier(GenericSQLParser.BackQuotedIdentifierContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIdentifierList(GenericSQLParser.IdentifierListContext ctx) {
        visitList(ctx.identifier(), comma());
        return null;
    }

    @Override
    public Void visitIdentifierOrString(GenericSQLParser.IdentifierOrStringContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDecimalValue(GenericSQLParser.DecimalValueContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDoubleValue(GenericSQLParser.DoubleValueContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIntegerValue(GenericSQLParser.IntegerValueContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitBaseType(GenericSQLParser.BaseTypeContext ctx) {
        sql.appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitQualifiedName(GenericSQLParser.QualifiedNameContext ctx) {
        visit(ctx.getChild(0));
        for (int i = 1; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof GenericSQLParser.IdentifierContext) {
                sql.append(".");
                visit(ctx.getChild(i));
            } else {
                visit(ctx.getChild(i));
            }
        }
        return null;
    }

    @Override
    public Void visitRollup(GenericSQLParser.RollupContext ctx) {
        sql.appendKey(ctx.ROLLUP());
        sql.intoParentheses(() -> visitList(ctx.expressionList().expression(), commaBreak(options.breakGroupByItems)));
        return null;
    }

    @Override
    public Void visitCube(GenericSQLParser.CubeContext ctx) {
        sql.appendKey(ctx.CUBE());
        sql.intoParentheses(() -> visitList(ctx.expressionList().expression(), commaBreak(options.breakGroupByItems)));
        return null;
    }

    @Override
    public Void visitSingleGroupingSet(GenericSQLParser.SingleGroupingSetContext ctx) {
        visitListAutoBreak(ctx.expressionList().expression(), commaBreak(options.breakGroupByItems));
        return null;
    }

    @Override
    public Void visitGroupingSet(GenericSQLParser.GroupingSetContext ctx) {
        sql.intoParentheses(() -> visitListAutoBreak(ctx.expression(), commaBreak(options.breakGroupByItems)));
        return null;
    }

    @Override
    public Void visitMultipleGroupingSets(GenericSQLParser.MultipleGroupingSetsContext ctx) {
        sql.appendKey(ctx.GROUPING()).appendKey(ctx.SETS());
        sql.intoParentheses(() -> visitList(ctx.groupingSet(), commaBreak(options.breakGroupByItems)));
        return null;
    }
}
