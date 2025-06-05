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

import com.google.common.collect.Lists;
import com.ibm.icu.util.CaseInsensitiveString;
import com.nicesql.sql.parser.CaseInsensitiveStream;
import com.nicesql.sql.parser.GenericLex;
import com.nicesql.sql.parser.GenericSQLBaseVisitor;
import com.nicesql.sql.parser.GenericSQLLexer;
import com.nicesql.sql.parser.GenericSQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FormatPrinterBase extends GenericSQLBaseVisitor<Void> {
    protected FormatOptions options;

    protected List<StringBuilder> formatSQLs = Lists.newArrayList();

    protected StringBuilder currentSQL = new StringBuilder();

    protected int indentLevel = 0;

    protected Set<String> keywords = HashSet.newHashSet(12);

    protected void nextLevel() {
    }

    protected void intoLevel(Runnable func) {
        indentLevel++;
        func.run();
        indentLevel--;
    }

    protected void intoParentheses(Runnable func) {
        append("(");
        func.run();
        append(") ");
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
        if (key == null) {
            return this;
        }
        if (options.upperCaseKeyWords) {
            key = key.toUpperCase();
        }
        keywords.add(key);
        currentSQL.append(key).append(" ");
        return this;
    }

    protected FormatPrinterBase appendKey(TerminalNode node) {
        if (node == null) {
            return this;
        }
        return appendKey(node.getText());
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

    protected FormatPrinterBase appendFnArgs(String func, String... args) {
        currentSQL.append(func).append('(');
        currentSQL.append(String.join(comma(), args));
        currentSQL.append(')');
        return this;
    }

    //    protected FormatPrinterBase strip(String str, boolean stripBreak) {
    //        int len = str.length();
    //        if (stripBreak) {
    //            len -= 1;
    //        } else {
    //            len -= str.length();
    //        }
    //        currentSQL.delete(len, currentSQL.length());
    //        return this;
    //    }
    protected String comma() {return ",";}

    protected String newLineComma() {
        return options.newLine + comma();
    }

    protected String commaNewLine() {
        return comma() + options.newLine;
    }

    private GenericSQLParser.SqlStatementsContext parse(String sql) {
        GenericSQLLexer lexer = new GenericSQLLexer(new CaseInsensitiveStream(CharStreams.fromString(sql)));
        GenericSQLParser parser = new GenericSQLParser(new CommonTokenStream(lexer));
        return parser.sqlStatements();
    }

    public String format(String sql) {
        GenericSQLParser.SqlStatementsContext context = parse(sql);
        for (GenericSQLParser.StatementContext stmt : context.statement()) {
            stmt.accept(this);
            formatSQLs.add(currentSQL);
            currentSQL = new StringBuilder();
        }
        return String.join("\n", formatSQLs);
    }
}
