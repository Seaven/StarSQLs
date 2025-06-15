package com.nicesql.sql.format;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.Set;

public class SQLBuilder {
    private final FormatOptions options;

    private final String comma;
    private final String newLine;

    private final StringBuilder sql;
    private final Set<String> keywords = HashSet.newHashSet(12);
    private int indentLevel = 0;

    public SQLBuilder(FormatOptions options) {
        this.options = options;
        this.sql = new StringBuilder();

        String temp = ",";
        if (options.spaceAfterComma) {
            temp += " ";
        }
        if (options.spaceBeforeComma) {
            temp = " " + temp;
        }
        this.comma = temp;
        this.newLine = options.isCompact ? "" : "\n";
    }

    public void intoLevel(Runnable func) {
        indentLevel++;
        func.run();
        indentLevel--;
    }

    public void intoParentheses(Runnable func) {
        append("(");
        func.run();
        append(")");
    }

    public SQLBuilder append(String str) {
        sql.append(str);
        return this;
    }

    public SQLBuilder appendSpace(String str) {
        sql.append(str);
        sql.append(" ");
        return this;
    }

    public SQLBuilder appendKey(String key) {
        return appendKey(key, true, true);
    }

    public SQLBuilder appendKey(TerminalNode node) {
        if (node == null) {
            return this;
        }
        return appendKey(node.getText(), true, true);
    }

    public SQLBuilder appendKey(TerminalNode node, boolean prefixSpace, boolean suffixSpace) {
        if (node == null) {
            return this;
        }
        return appendKey(node.getText(), prefixSpace, suffixSpace);
    }

    public SQLBuilder appendKey(String key, boolean prefixSpace, boolean suffixSpace) {
        if (key == null) {
            return this;
        }
        key = key.trim();
        if (options.upperCaseKeyWords) {
            key = key.toUpperCase();
        } else if (options.lowerCaseKeyWords) {
            key = key.toLowerCase();
        }
        if (prefixSpace && !sql.isEmpty()) {
            char l = sql.charAt(sql.length() - 1);
            if (!Character.isWhitespace(l) && l != '(') {
                sql.append(' ');
            }
        }
        keywords.add(key);
        sql.append(key);
        if (suffixSpace) {
            sql.append(" ");
        }
        return this;
    }

    public SQLBuilder appendNewLine() {
        append(newLine());
        return this;
    }

    public SQLBuilder appendBreak(boolean isBreak) {
        if (isBreak) {
            appendNewLine();
        }
        return this;
    }

    public SQLBuilder appendItem(String... args) {
        sql.append(String.join(comma(), args));
        return this;
    }

    public String comma() {return comma;}

    public String newBreak(boolean isBreak) {
        return isBreak ? newLine() : "";
    }

    public String newLine() {
        return newLine + Strings.repeat(options.indent, indentLevel);
    }

    @Override
    public String toString() {
        return sql.toString();
    }
}
