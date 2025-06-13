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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nicesql.sql.parser.GenericSQLBaseVisitor;
import com.nicesql.sql.parser.GenericSQLLexer;
import com.nicesql.sql.parser.GenericSQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FormatPrinterBase extends GenericSQLBaseVisitor<Void> {
    private static final Logger logger = LogManager.getLogger(FormatPrinterBase.class);

    protected FormatOptions options;

    protected List<StringBuilder> formatSQLs = Lists.newArrayList();

    protected StringBuilder currentSQL;

    protected int indentLevel = 0;

    protected Set<String> keywords = HashSet.newHashSet(12);

    private final Map<Long, String> comments = Maps.newLinkedHashMap();

    protected void intoLevel(Runnable func) {
        indentLevel++;
        func.run();
        indentLevel--;
    }

    protected void intoParentheses(Runnable func) {
        append("(");
        func.run();
        append(")");
    }

    protected FormatPrinterBase append(String str) {
        currentSQL.append(str);
        return this;
    }

    protected FormatPrinterBase appendSpace(String str) {
        currentSQL.append(str);
        currentSQL.append(" ");
        return this;
    }

    protected FormatPrinterBase appendKey(String key) {
        return appendKey(key, true, true);
    }

    protected FormatPrinterBase appendKey(TerminalNode node) {
        if (node == null) {
            return this;
        }
        return appendKey(node.getText(), true, true);
    }

    protected FormatPrinterBase appendKey(TerminalNode node, boolean prefixSpace, boolean suffixSpace) {
        if (node == null) {
            return this;
        }
        return appendKey(node.getText(), prefixSpace, suffixSpace);
    }

    protected FormatPrinterBase appendKey(String key, boolean prefixSpace, boolean suffixSpace) {
        if (key == null) {
            return this;
        }
        key = key.trim();
        if (options.upperCaseKeyWords) {
            key = key.toUpperCase();
        }

        if (prefixSpace && !currentSQL.isEmpty()) {
            char l = currentSQL.charAt(currentSQL.length() - 1);
            if (' ' != l && l != '\n' && l != '\r' && l != '\t') {
                currentSQL.append(' ');
            }
        }
        keywords.add(key);
        currentSQL.append(key);
        if (suffixSpace) {
            currentSQL.append(" ");
        }
        return this;
    }

    protected FormatPrinterBase appendIndent() {
        return this;
    }

    protected FormatPrinterBase appendNewLine() {
        return this;
    }

    protected FormatPrinterBase appendBreak(boolean isBreak) {
        return this;
    }

    protected FormatPrinterBase appendItem(String... args) {
        currentSQL.append(String.join(comma(), args));
        return this;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == GenericSQLParser.EOF) {
            return null; // Ignore EOF token
        }
        if (",".equals(node.getText().trim())) {
            append(comma());
            return null;
        }
        appendKey(node.getText());
        return null;
    }

    protected String comma() {return ",";}

    protected String newLine() {
        return options.newLine;
    }

    private GenericSQLParser.SqlStatementsContext parse(String sql) {
        logger.debug("Parsing SQL: {}", sql);
        GenericSQLLexer lexer = new GenericSQLLexer(CharStreams.fromString(sql));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        initComments(tokenStream);
        GenericSQLParser parser = new GenericSQLParser(tokenStream);

        SQLSyntaxErrorListener errorListener = new SQLSyntaxErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        GenericSQLParser.SqlStatementsContext context = parser.sqlStatements();

        if (errorListener.hasErrors()) {
            String errorMsg = String.join("\n", errorListener.getErrors());
            logger.warn("SQL parsing error: {}", errorMsg);
        }

        return context;
    }

    private void initComments(CommonTokenStream tokenStream) {
        tokenStream.fill();
        long index = 0;
        for (Token t : tokenStream.getTokens()) {
            if (t.getChannel() != Token.HIDDEN_CHANNEL) {
                index += t.getText().chars().filter(c -> !Character.isWhitespace(c)).count();
            } else {
                // replace -- to /* */, because -- will comment the real sql
                final String c = t.getText().startsWith("--") ? "/*" + t.getText().substring(2).trim() + "*/\n"
                        : t.getText();
                comments.compute(index, (k, s) -> Strings.nullToEmpty(s) + c);
            }
        }
    }

    private String insertComments(String sql) {
        if (comments.isEmpty()) {
            return sql;
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int start = 0;
        for (var entry : comments.entrySet()) {
            int index = entry.getKey().intValue();
            String comment = entry.getValue();
            // find index
            int find = start;
            while (find < sql.length() && count < index) {
                char c = sql.charAt(find);
                if (!Character.isWhitespace(c)) {
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

    public String format(String sql) {
        GenericSQLParser.SqlStatementsContext context = parse(sql);
        context.accept(this);
        String formatSQL = formatSQLs.stream()
                .map(StringBuilder::toString)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
        return insertComments(formatSQL);
    }
}

