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

package com.starsqls.format;

import com.google.common.base.Preconditions;
import com.starsqls.parser.StarRocksParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

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
    public Void visitSingleStatement(StarRocksParser.SingleStatementContext ctx) {
        sql = new SQLBuilder(options);
        visit(ctx.statement());
        if (ctx.SEMICOLON() != null) {
            sql.append(ctx.SEMICOLON().getText());
        }
        visit(ctx.emptyStatement());
        formatSQLs.add(sql);
        return null;
    }

    @Override
    public Void visitEmptyStatement(StarRocksParser.EmptyStatementContext ctx) {
        sql.append(ctx.SEMICOLON().getText());
        return null;
    }

    @Override
    public Void visitSubfieldName(StarRocksParser.SubfieldNameContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitNestedFieldName(StarRocksParser.NestedFieldNameContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitWithClause(StarRocksParser.WithClauseContext ctx) {
        sql.appendKey(ctx.WITH());
        sql.appendBreak(options.breakCTE);
        visitList(ctx.commonTableExpression(), commaBreak(options.breakCTE));
        sql.appendNewLine();
        return null;
    }

    @Override
    public Void visitQueryNoWith(StarRocksParser.QueryNoWithContext ctx) {
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
    public Void visitQueryPeriod(StarRocksParser.QueryPeriodContext ctx) {
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
    public Void visitPeriodType(StarRocksParser.PeriodTypeContext ctx) {
        sql.appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitSetOperation(StarRocksParser.SetOperationContext ctx) {
        visit(ctx.left);
        sql.appendNewLine();
        sql.appendKey(ctx.operator.getText());
        visit(ctx.setQuantifier());
        sql.appendNewLine();
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitSubquery(StarRocksParser.SubqueryContext ctx) {
        visitSubqueryImpl(ctx.queryRelation());
        return null;
    }

    @Override
    public Void visitRowConstructor(StarRocksParser.RowConstructorContext ctx) {
        sql.intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitSortItem(StarRocksParser.SortItemContext ctx) {
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
    public Void visitLimitElement(StarRocksParser.LimitElementContext ctx) {
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
    public Void visitQuerySpecification(StarRocksParser.QuerySpecificationContext ctx) {
        sql.appendKey(ctx.SELECT().getText(), false, true);
        visit(ctx.setQuantifier());
        sql.intoLevel(() -> {
            sql.appendBreak(options.breakSelectItems);
            visitList(ctx.selectItem(), commaBreak(options.breakSelectItems));
        });
        if (!StringUtils.isBlank(ctx.fromClause().getText())) {
            sql.appendNewLine();
            sql.intoLevel(() -> visit(ctx.fromClause()));
        }
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
    public Void visitFrom(StarRocksParser.FromContext ctx) {
        sql.appendKey(ctx.FROM());
        visit(ctx.relations());
        visit(ctx.pivotClause());
        return null;
    }

    @Override
    public Void visitCommonTableExpression(StarRocksParser.CommonTableExpressionContext ctx) {
        sql.append(ctx.name.getText()).append(" ");
        visit(ctx.columnAliases());
        sql.appendKey(ctx.AS());
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
    public Void visitSetQuantifier(StarRocksParser.SetQuantifierContext ctx) {
        sql.appendKey(ctx.getText(), false, true);
        return null;
    }

    @Override
    public Void visitSelectSingle(StarRocksParser.SelectSingleContext ctx) {
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
    public Void visitSelectAll(StarRocksParser.SelectAllContext ctx) {
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
    public Void visitExcludeClause(StarRocksParser.ExcludeClauseContext ctx) {
        sql.appendKey(ctx.getChild(0).getText());
        sql.intoParentheses(() -> visitList(ctx.identifier(), comma()));
        return null;
    }

    @Override
    public Void visitRelations(StarRocksParser.RelationsContext ctx) {
        sql.intoAutoBreak(() -> super.visitRelations(ctx));
        return null;
    }

    @Override
    public Void visitRelation(StarRocksParser.RelationContext ctx) {
        if (ctx.getChild(0) instanceof TerminalNode) {
            Preconditions.checkState(ctx.getChild(ctx.getChildCount() - 1) instanceof TerminalNode);
            sql.intoParentheses(() -> visitNonBracketsRelation(ctx));

        }
        return visitNonBracketsRelation(ctx);
    }

    public Void visitNonBracketsRelation(StarRocksParser.RelationContext ctx) {
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
    public Void visitTableAtom(StarRocksParser.TableAtomContext ctx) {
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
    public Void visitInlineTable(StarRocksParser.InlineTableContext ctx) {
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
    public Void visitSubqueryWithAlias(StarRocksParser.SubqueryWithAliasContext ctx) {
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
    public Void visitTableFunction(StarRocksParser.TableFunctionContext ctx) {
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
    public Void visitNormalizedTableFunction(StarRocksParser.NormalizedTableFunctionContext ctx) {
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
    public Void visitParenthesizedRelation(StarRocksParser.ParenthesizedRelationContext ctx) {
        sql.intoParentheses(() -> visit(ctx.relations()));
        return null;
    }

    @Override
    public Void visitPivotClause(StarRocksParser.PivotClauseContext ctx) {
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
    public Void visitPivotAggregationExpression(StarRocksParser.PivotAggregationExpressionContext ctx) {
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
    public Void visitPivotValue(StarRocksParser.PivotValueContext ctx) {
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
    public Void visitArgumentList(StarRocksParser.ArgumentListContext ctx) {
        if (ctx.expressionList() != null) {
            visit(ctx.expressionList());
        } else if (ctx.namedArgumentList() != null) {
            visit(ctx.namedArgumentList());
        }
        return null;
    }

    @Override
    public Void visitNamedArgumentList(StarRocksParser.NamedArgumentListContext ctx) {
        visitList(ctx.namedArgument(), comma());
        return null;
    }

    @Override
    public Void visitNamedArguments(StarRocksParser.NamedArgumentsContext ctx) {
        sql.append(ctx.identifier().getText());
        sql.append(" => ");
        visit(ctx.expression());
        return null;
    }

    @Override
    public Void visitJoinRelation(StarRocksParser.JoinRelationContext ctx) {
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
    public Void visitBracketHint(StarRocksParser.BracketHintContext ctx) {
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
    public Void visitJoinCriteria(StarRocksParser.JoinCriteriaContext ctx) {
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
    public Void visitColumnAliases(StarRocksParser.ColumnAliasesContext ctx) {
        sql.intoParentheses(() -> visitList(ctx.identifier(), comma()));
        return null;
    }

    @Override
    public Void visitPartitionNames(StarRocksParser.PartitionNamesContext ctx) {
        return super.visitPartitionNames(ctx);
    }

    @Override
    public Void visitKeyPartitionList(StarRocksParser.KeyPartitionListContext ctx) {
        sql.appendKey(ctx.PARTITION().getText());
        sql.intoParentheses(() -> visitList(ctx.keyPartition(), comma()));
        return null;
    }

    @Override
    public Void visitTabletList(StarRocksParser.TabletListContext ctx) {
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
    public Void visitReplicaList(StarRocksParser.ReplicaListContext ctx) {
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
    public Void visitMapExpressionList(StarRocksParser.MapExpressionListContext ctx) {
        visitList(ctx.mapExpression(), comma());
        return null;
    }

    @Override
    public Void visitMapExpression(StarRocksParser.MapExpressionContext ctx) {
        visit(ctx.key);
        sql.append(":");
        visit(ctx.value);
        return null;
    }

    @Override
    public Void visitExpressionDefault(StarRocksParser.ExpressionDefaultContext ctx) {
        sql.appendKey(ctx.BINARY());
        return visit(ctx.booleanExpression());
    }

    @Override
    public Void visitLogicalNot(StarRocksParser.LogicalNotContext ctx) {
        sql.appendKey(ctx.NOT());
        return visit(ctx.expression());
    }

    @Override
    public Void visitLogicalBinary(StarRocksParser.LogicalBinaryContext ctx) {
        visit(ctx.left);
        sql.appendBreak(options.breakAndOr);
        sql.appendKey(ctx.operator.getText());
        return visit(ctx.right);
    }

    @Override
    public Void visitExpressionList(StarRocksParser.ExpressionListContext ctx) {
        return visitList(ctx.expression(), comma());
    }

    @Override
    public Void visitComparison(StarRocksParser.ComparisonContext ctx) {
        visit(ctx.left);
        sql.appendKey(ctx.comparisonOperator().getText());
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitIsNull(StarRocksParser.IsNullContext ctx) {
        visit(ctx.booleanExpression());
        sql.appendKey(ctx.IS()).appendKey(ctx.NOT()).appendKey(ctx.NULL());
        return null;
    }

    public void visitSubqueryImpl(StarRocksParser.QueryRelationContext ctx) {
        sql.intoParentheses(() -> {
            if (!options.formatSubquery) {
                FormatPrinter unformat = new FormatPrinter(new FormatOptions());
                sql.append(unformat.format(ctx));
                return;
            }
            sql.intoLevel(() -> {
                sql.appendNewLine();
                visit(ctx);
            });
            sql.appendNewLine();
        });
    }

    @Override
    public Void visitScalarSubquery(StarRocksParser.ScalarSubqueryContext ctx) {
        visit(ctx.booleanExpression());
        sql.appendKey(ctx.comparisonOperator().getText());
        visitSubqueryImpl(ctx.queryRelation());
        return null;
    }

    @Override
    public Void visitPredicate(StarRocksParser.PredicateContext ctx) {
        if (ctx.predicateOperations() != null) {
            return visit(ctx.predicateOperations());
        } else if (ctx.tupleInSubquery() != null) {
            return visit(ctx.tupleInSubquery());
        } else {
            return visit(ctx.valueExpression());
        }
    }

    @Override
    public Void visitTupleInSubquery(StarRocksParser.TupleInSubqueryContext ctx) {
        sql.intoParentheses(() -> visitList(ctx.expression(), comma()));
        sql.appendKey(ctx.NOT()).appendKey(ctx.IN());
        visitSubqueryImpl(ctx.queryRelation());
        return null;
    }

    @Override
    public Void visitInList(StarRocksParser.InListContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT()).appendKey(ctx.IN());
        sql.appendBreak(options.breakInList);
        sql.intoParentheses(() -> visit(ctx.expressionList()));
        return null;
    }

    @Override
    public Void visitInSubquery(StarRocksParser.InSubqueryContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT()).appendKey(ctx.IN());
        visitSubqueryImpl(ctx.queryRelation());
        return null;
    }

    @Override
    public Void visitBetween(StarRocksParser.BetweenContext ctx) {
        visit(ctx.value);
        sql.appendKey(ctx.NOT());
        sql.appendKey(ctx.BETWEEN());
        visit(ctx.lower);
        sql.appendKey(ctx.AND());
        visit(ctx.upper);
        return null;
    }

    @Override
    public Void visitLike(StarRocksParser.LikeContext ctx) {
        visit(ctx.value);
        return super.visitLike(ctx);
    }

    @Override
    public Void visitArithmeticBinary(StarRocksParser.ArithmeticBinaryContext ctx) {
        visit(ctx.left);
        if (options.isMinify) {
            sql.append(ctx.operator.getText());
        } else {
            sql.appendKey(ctx.operator.getText());
        }
        return visit(ctx.right);
    }

    @Override
    public Void visitDereference(StarRocksParser.DereferenceContext ctx) {
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
    public Void visitSimpleCase(StarRocksParser.SimpleCaseContext ctx) {
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
    public Void visitArrowExpression(StarRocksParser.ArrowExpressionContext ctx) {
        return super.visitArrowExpression(ctx);
    }

    @Override
    public Void visitOdbcFunctionCallExpression(StarRocksParser.OdbcFunctionCallExpressionContext ctx) {
        return super.visitOdbcFunctionCallExpression(ctx);
    }

    @Override
    public Void visitMatchExpr(StarRocksParser.MatchExprContext ctx) {
        return super.visitMatchExpr(ctx);
    }

    @Override
    public Void visitColumnRef(StarRocksParser.ColumnRefContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSystemVariableExpression(StarRocksParser.SystemVariableExpressionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitConvert(StarRocksParser.ConvertContext ctx) {
        sql.appendKey(ctx.CONVERT(), false, false);
        sql.intoParentheses(() -> {
            visit(ctx.expression());
            sql.append(comma());
            visit(ctx.type());
        });
        return null;
    }

    @Override
    public Void visitConcat(StarRocksParser.ConcatContext ctx) {
        visit(ctx.left);
        sql.append("||");
        visit(ctx.right);
        return null;
    }

    @Override
    public Void visitSubqueryExpression(StarRocksParser.SubqueryExpressionContext ctx) {
        visit(ctx.subquery());
        return null;
    }

    @Override
    public Void visitLambdaFunctionExpr(StarRocksParser.LambdaFunctionExprContext ctx) {
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
    public Void visitCollectionSubscript(StarRocksParser.CollectionSubscriptContext ctx) {
        visit(ctx.value);
        sql.append("[");
        visit(ctx.index);
        sql.append("]");
        return null;
    }

    @Override
    public Void visitLiteral(StarRocksParser.LiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitCast(StarRocksParser.CastContext ctx) {
        sql.appendKey(ctx.CAST(), false, false);
        sql.intoParentheses(() -> {
            visit(ctx.expression());
            sql.appendKey(ctx.AS());
            visit(ctx.type());
        });
        return null;
    }

    @Override
    public Void visitCollate(StarRocksParser.CollateContext ctx) {
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
    public Void visitParenthesizedExpression(StarRocksParser.ParenthesizedExpressionContext ctx) {
        sql.intoParentheses(() -> sql.intoLevel(() -> visit(ctx.expression())));
        return null;
    }

    @Override
    public Void visitUserVariableExpression(StarRocksParser.UserVariableExpressionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitArrayConstructor(StarRocksParser.ArrayConstructorContext ctx) {
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
    public Void visitMapConstructor(StarRocksParser.MapConstructorContext ctx) {
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
    public Void visitArraySlice(StarRocksParser.ArraySliceContext ctx) {
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
    public Void visitFunctionCallExpression(StarRocksParser.FunctionCallExpressionContext ctx) {
        visit(ctx.functionCall());
        return null;
    }

    @Override
    public Void visitExists(StarRocksParser.ExistsContext ctx) {
        sql.appendKey(ctx.EXISTS());
        visitSubqueryImpl(ctx.queryRelation());
        return null;
    }

    @Override
    public Void visitSearchedCase(StarRocksParser.SearchedCaseContext ctx) {
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
    public Void visitArithmeticUnary(StarRocksParser.ArithmeticUnaryContext ctx) {
        sql.append(ctx.operator.getText());
        visit(ctx.primaryExpression());
        return null;
    }

    @Override
    public Void visitNullLiteral(StarRocksParser.NullLiteralContext ctx) {
        sql.appendKey(ctx.NULL());
        return null;
    }

    @Override
    public Void visitBooleanLiteral(StarRocksParser.BooleanLiteralContext ctx) {
        sql.appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitNumericLiteral(StarRocksParser.NumericLiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDateLiteral(StarRocksParser.DateLiteralContext ctx) {
        sql.appendKey(ctx.DATE());
        sql.appendKey(ctx.DATETIME());
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitStringLiteral(StarRocksParser.StringLiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIntervalLiteral(StarRocksParser.IntervalLiteralContext ctx) {
        visit(ctx.interval());
        return null;
    }

    @Override
    public Void visitUnitBoundaryLiteral(StarRocksParser.UnitBoundaryLiteralContext ctx) {
        sql.appendKey(ctx.getText());
        return null;
    }

    @Override
    public Void visitBinaryLiteral(StarRocksParser.BinaryLiteralContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitParameter(StarRocksParser.ParameterContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitExtract(StarRocksParser.ExtractContext ctx) {
        sql.appendKey(ctx.EXTRACT(), false, false);
        sql.intoParentheses(() -> {
            sql.append(ctx.identifier().getText());
            sql.appendKey(ctx.FROM());
            visit(ctx.valueExpression());
        });
        return null;
    }

    @Override
    public Void visitInformationFunction(StarRocksParser.InformationFunctionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialDateTime(StarRocksParser.SpecialDateTimeContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSpecialFunction(StarRocksParser.SpecialFunctionContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitAggregationFunctionCall(StarRocksParser.AggregationFunctionCallContext ctx) {
        visit(ctx.aggregationFunction());
        if (ctx.over() != null) {
            visit(ctx.over());
        }
        return null;
    }

    @Override
    public Void visitWindowFunctionCall(StarRocksParser.WindowFunctionCallContext ctx) {
        visit(ctx.windowFunction());
        visit(ctx.over());
        return null;
    }

    @Override
    public Void visitTranslateFunctionCall(StarRocksParser.TranslateFunctionCallContext ctx) {
        sql.appendKey(ctx.TRANSLATE(), false, false);
        sql.intoParentheses(() -> visitList(ctx.expression(), comma()));
        return null;
    }

    @Override
    public Void visitSimpleFunctionCall(StarRocksParser.SimpleFunctionCallContext ctx) {
        sql.append(ctx.qualifiedName().getText());
        if (options.breakFunctionArgs) {
            if (options.alignFunctionArgs) {
                sql.intoParentheses(() -> sql.intoPrefix(() -> visitList(ctx.expression(), commaBreak(true))));
            } else {
                sql.intoParentheses(() -> visitList(ctx.expression(), commaBreak(true)));
            }
        } else if (options.alignFunctionArgs) {
            sql.intoParentheses(
                    () -> sql.intoPrefix(() -> sql.intoAutoBreak(() -> visitList(ctx.expression(), comma()))));
        } else {
            sql.intoParentheses(() -> visitList(ctx.expression(), comma()));
        }
        if (ctx.over() != null) {
            visit(ctx.over());
        }
        return null;
    }

    @Override
    public Void visitAggregationFunction(StarRocksParser.AggregationFunctionContext ctx) {
        Preconditions.checkState(ctx.getChild(0) instanceof TerminalNode);
        TerminalNode name = (TerminalNode) ctx.getChild(0);
        int func = name.getSymbol().getType();
        sql.appendKey(name.getText(), false, false);
        sql.intoParentheses(() -> {
            if (func == StarRocksParser.AVG || func == StarRocksParser.MAX
                    || func == StarRocksParser.MIN || func == StarRocksParser.SUM) {
                visit(ctx.setQuantifier());
                visit(ctx.expression(0));
            } else if (func == StarRocksParser.COUNT) {
                if (ctx.ASTERISK_SYMBOL() != null) {
                    sql.append(ctx.ASTERISK_SYMBOL().getText());
                } else {
                    visit(ctx.setQuantifier());
                    visit(ctx.bracketHint());
                    visitList(ctx.expression(), comma());
                }
            } else if (func == StarRocksParser.ARRAY_AGG
                    || func == StarRocksParser.ARRAY_AGG_DISTINCT) {
                visit(ctx.setQuantifier());
                visit(ctx.expression(0));
                sql.appendKey(ctx.ORDER());
                sql.appendKey(ctx.BY());
                visitList(ctx.sortItem(), comma());
            } else if (func == StarRocksParser.GROUP_CONCAT) {
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
    public Void visitUserVariable(StarRocksParser.UserVariableContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitSystemVariable(StarRocksParser.SystemVariableContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitColumnReference(StarRocksParser.ColumnReferenceContext ctx) {
        sql.append(ctx.identifier().getText());
        return null;
    }

    @Override
    public Void visitInformationFunctionExpression(StarRocksParser.InformationFunctionExpressionContext ctx) {
        sql.appendKey(ctx.getChild(0).getText(), false, false);
        for (int i = 0; i < ctx.getChildCount(); i++) {
            visit(ctx.getChild(i));
        }
        return null;
    }

    @Override
    public Void visitSpecialDateTimeExpression(StarRocksParser.SpecialDateTimeExpressionContext ctx) {
        sql.appendKey(ctx.getChild(0).getText(), false, false);
        for (int i = 0; i < ctx.getChildCount(); i++) {
            visit(ctx.getChild(i));
        }
        return null;
    }

    @Override
    public Void visitSpecialFunctionExpression(StarRocksParser.SpecialFunctionExpressionContext ctx) {
        sql.appendKey(ctx.getChild(0).getText(), false, false);
        if (options.breakFunctionArgs) {
            if (options.alignFunctionArgs) {
                sql.intoParentheses(() -> sql.intoPrefix(() -> visitList(ctx.expression(), commaBreak(true))));
            } else {
                sql.intoParentheses(() -> visitList(ctx.expression(), commaBreak(true)));
            }
        } else if (options.alignFunctionArgs) {
            sql.intoParentheses(
                    () -> sql.intoPrefix(() -> sql.intoAutoBreak(() -> visitList(ctx.expression(), comma()))));
        } else {
            sql.intoParentheses(() -> visitList(ctx.expression(), comma()));
        }
        return null;
    }

    @Override
    public Void visitWindowFunction(StarRocksParser.WindowFunctionContext ctx) {
        sql.appendKey(ctx.name.getText(), false, false);
        for (int i = 1; i < ctx.getChildCount(); i++) {
            visit(ctx.getChild(i));
        }
        return null;
    }

    @Override
    public Void visitWhenClause(StarRocksParser.WhenClauseContext ctx) {
        sql.appendKey(ctx.WHEN());
        visit(ctx.condition);
        sql.appendKey(ctx.THEN());
        visit(ctx.result);
        return null;
    }

    @Override
    public Void visitOver(StarRocksParser.OverContext ctx) {
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
    public Void visitIgnoreNulls(StarRocksParser.IgnoreNullsContext ctx) {
        sql.appendKey(ctx.IGNORE());
        sql.appendKey(ctx.NULLS());
        return null;
    }

    @Override
    public Void visitWindowFrame(StarRocksParser.WindowFrameContext ctx) {
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
    public Void visitUnboundedFrame(StarRocksParser.UnboundedFrameContext ctx) {
        sql.appendKey(ctx.UNBOUNDED());
        sql.appendKey(ctx.boundType.getText());
        return null;
    }

    @Override
    public Void visitCurrentRowBound(StarRocksParser.CurrentRowBoundContext ctx) {
        sql.appendKey(ctx.CURRENT());
        sql.appendKey(ctx.ROW());
        return null;
    }

    @Override
    public Void visitBoundedFrame(StarRocksParser.BoundedFrameContext ctx) {
        visit(ctx.expression());
        sql.appendKey(ctx.boundType.getText());
        return null;
    }

    @Override
    public Void visitExplainDesc(StarRocksParser.ExplainDescContext ctx) {
        super.visitExplainDesc(ctx);
        sql.appendBreak(options.breakExplain);
        return null;
    }

    @Override
    public Void visitLiteralExpressionList(StarRocksParser.LiteralExpressionListContext ctx) {
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
    public Void visitKeyPartition(StarRocksParser.KeyPartitionContext ctx) {
        sql.append(ctx.partitionColName.getText());
        sql.append("=");
        visit(ctx.partitionColValue);
        return null;
    }

    @Override
    public Void visitInterval(StarRocksParser.IntervalContext ctx) {
        sql.appendKey(ctx.INTERVAL().getText());
        visit(ctx.value);
        visit(ctx.from);
        return null;
    }

    @Override
    public Void visitArrayType(StarRocksParser.ArrayTypeContext ctx) {
        sql.appendKey(ctx.ARRAY().getText(), false, false);
        if (ctx.type() != null) {
            sql.append("<");
            visit(ctx.type());
            sql.append(">");
        }
        return null;
    }

    @Override
    public Void visitMapType(StarRocksParser.MapTypeContext ctx) {
        sql.appendKey(ctx.MAP().getText(), false, false);
        if (ctx.type() != null) {
            sql.append("<");
            visitList(ctx.type(), comma());
            sql.append(">");
        }
        return null;
    }

    @Override
    public Void visitSubfieldDescs(StarRocksParser.SubfieldDescsContext ctx) {
        visitList(ctx.subfieldDesc(), comma());
        return null;
    }

    @Override
    public Void visitSubfieldDesc(StarRocksParser.SubfieldDescContext ctx) {
        sql.append(ctx.identifier().getText());
        sql.append(" ");
        visit(ctx.type());
        return null;
    }

    @Override
    public Void visitStructType(StarRocksParser.StructTypeContext ctx) {
        sql.appendKey(ctx.STRUCT().getText(), true, false);
        if (ctx.subfieldDescs() != null) {
            sql.append("<");
            visit(ctx.subfieldDescs());
            sql.append(">");
        }
        return null;
    }

    @Override
    public Void visitTypeParameter(StarRocksParser.TypeParameterContext ctx) {
        sql.intoParentheses(() -> sql.append(ctx.INTEGER_VALUE().getText()));
        return null;
    }

    @Override
    public Void visitDecimalType(StarRocksParser.DecimalTypeContext ctx) {
        sql.appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitUnquotedIdentifier(StarRocksParser.UnquotedIdentifierContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDigitIdentifier(StarRocksParser.DigitIdentifierContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitBackQuotedIdentifier(StarRocksParser.BackQuotedIdentifierContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIdentifierList(StarRocksParser.IdentifierListContext ctx) {
        visitList(ctx.identifier(), comma());
        return null;
    }

    @Override
    public Void visitIdentifierOrString(StarRocksParser.IdentifierOrStringContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDecimalValue(StarRocksParser.DecimalValueContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitDoubleValue(StarRocksParser.DoubleValueContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitIntegerValue(StarRocksParser.IntegerValueContext ctx) {
        sql.append(ctx.getText());
        return null;
    }

    @Override
    public Void visitBaseType(StarRocksParser.BaseTypeContext ctx) {
        sql.appendKey(ctx.getText(), false, false);
        return null;
    }

    @Override
    public Void visitQualifiedName(StarRocksParser.QualifiedNameContext ctx) {
        visit(ctx.getChild(0));
        for (int i = 1; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof StarRocksParser.IdentifierContext) {
                sql.append(".");
                visit(ctx.getChild(i));
            } else {
                visit(ctx.getChild(i));
            }
        }
        return null;
    }

    @Override
    public Void visitRollup(StarRocksParser.RollupContext ctx) {
        sql.appendKey(ctx.ROLLUP());
        sql.intoParentheses(() -> visitList(ctx.expressionList().expression(), commaBreak(options.breakGroupByItems)));
        return null;
    }

    @Override
    public Void visitCube(StarRocksParser.CubeContext ctx) {
        sql.appendKey(ctx.CUBE());
        sql.intoParentheses(() -> visitList(ctx.expressionList().expression(), commaBreak(options.breakGroupByItems)));
        return null;
    }

    @Override
    public Void visitSingleGroupingSet(StarRocksParser.SingleGroupingSetContext ctx) {
        visitListAutoBreak(ctx.expressionList().expression(), commaBreak(options.breakGroupByItems));
        return null;
    }

    @Override
    public Void visitGroupingSet(StarRocksParser.GroupingSetContext ctx) {
        sql.intoParentheses(() -> visitListAutoBreak(ctx.expression(), commaBreak(options.breakGroupByItems)));
        return null;
    }

    @Override
    public Void visitMultipleGroupingSets(StarRocksParser.MultipleGroupingSetsContext ctx) {
        sql.appendKey(ctx.GROUPING()).appendKey(ctx.SETS());
        sql.intoParentheses(() -> visitList(ctx.groupingSet(), commaBreak(options.breakGroupByItems)));
        return null;
    }
}
