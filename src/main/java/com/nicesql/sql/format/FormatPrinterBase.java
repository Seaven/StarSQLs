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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormatPrinterBase extends GenericSQLBaseVisitor<Void> {
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
        if (node.getSymbol().getType() == GenericSQLParser.EOF) {
            return null; // Ignore EOF token
        }
        if (",".equals(node.getText().trim())) {
            sql.append(comma());
            return null;
        }
        sql.appendKey(node.getText());
        return null;
    }

    private GenericSQLParser.SqlStatementsContext parse(String sql) {
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
            throw new IllegalArgumentException(errorMsg);
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
                .map(SQLBuilder::toString)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
        return insertComments(formatSQL);
    }
}

