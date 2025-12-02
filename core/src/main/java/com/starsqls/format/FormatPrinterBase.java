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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.starsqls.parser.StarRocksBaseVisitor;
import com.starsqls.parser.StarRocksLexer;
import com.starsqls.parser.StarRocksParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormatPrinterBase extends StarRocksBaseVisitor<Void> implements Printer {
    protected FormatOptions options;

    protected SQLBuilder sql;

    protected List<SQLBuilder> formatSQLs = Lists.newArrayList();

    private final Map<Long, String> comments = Maps.newLinkedHashMap();

    protected String comma() {return sql.comma();}

    protected String commaBreak(boolean isBreak) {
        return comma() + sql.newBreak(isBreak);
    }

    protected String newLine() {
        return sql.newLine();
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == StarRocksParser.EOF) {
            return null; // Ignore EOF token
        }
        String text = node.getText();
        // Optimize: avoid multiple trim() calls
        if (",".equals(text) || (text.length() > 0 && text.trim().equals(","))) {
            sql.append(comma());
            return null;
        }
        char firstChar = text.length() > 0 ? text.charAt(0) : 0;
        if (firstChar == '(' || firstChar == ')') {
            sql.append(String.valueOf(firstChar));
            return null;
        }
        sql.appendKey(text);
        return null;
    }

    private StarRocksParser.SqlStatementsContext parse(String sql) {
        StarRocksLexer lexer = new StarRocksLexer(CharStreams.fromString(sql));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        initComments(tokenStream);
        StarRocksParser parser = new StarRocksParser(tokenStream);

        SQLSyntaxErrorListener errorListener = new SQLSyntaxErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        StarRocksParser.SqlStatementsContext context = parser.sqlStatements();

        if (errorListener.hasErrors()) {
            String errorMsg = String.join("\n", errorListener.getErrors());
            throw new IllegalArgumentException(errorMsg);
        }

        return context;
    }

    private void initComments(CommonTokenStream tokenStream) {
        tokenStream.fill();
        long index = 0;
        List<Token> tokens = tokenStream.getTokens();
        for (Token t : tokens) {
            String tokenText = t.getText();
            if (t.getChannel() != Token.HIDDEN_CHANNEL) {
                // Optimize: count non-whitespace chars more efficiently
                for (int i = 0; i < tokenText.length(); i++) {
                    if (!Character.isWhitespace(tokenText.charAt(i))) {
                        index++;
                    }
                }
            } else {
                if (tokenText.startsWith("/*+")) {
                    // Optimizer hint comments, save them as comments
                    comments.compute(index, (k, s) -> Strings.nullToEmpty(s) + tokenText);
                } else if (!options.ignoreComment) {
                    // replace -- to /* */, because -- will comment the real sql
                    final String c = tokenText.startsWith("--") 
                            ? "/*" + tokenText.substring(2).trim() + "*/"
                            : tokenText;
                    comments.compute(index, (k, s) -> Strings.nullToEmpty(s) + c);
                }
            }
        }
    }

    private String insertComments(String sql) {
        if (comments.isEmpty()) {
            return sql;
        }
        StringBuilder sb = new StringBuilder(sql.length() + comments.size() * 20); // Pre-allocate
        int count = 0;
        int start = 0;
        for (var entry : comments.entrySet()) {
            int index = entry.getKey().intValue();
            String comment = entry.getValue();
            // find index - optimize to avoid repeated charAt calls
            int find = start;
            while (find < sql.length() && count < index) {
                if (!Character.isWhitespace(sql.charAt(find))) {
                    count++;
                }
                find++;
            }
            sb.append(sql, start, find);
            sb.append(comment);
            start = find;
        }
        sb.append(sql, start, sql.length());
        return sb.toString();
    }

    @Override
    public String format(String sql) {
        StarRocksParser.SqlStatementsContext context = parse(sql);
        context.accept(this);
        String formatSQL = formatSQLs.stream()
                .map(SQLBuilder::toString)
                .collect(Collectors.joining("\n"));
        return insertComments(formatSQL);
    }

    public String format(ParseTree tree) {
        this.sql = new SQLBuilder(options);
        tree.accept(this);
        String formatSQL = sql.toString();
        return insertComments(formatSQL);
    }
}

