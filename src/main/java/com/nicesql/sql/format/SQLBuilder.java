package com.nicesql.sql.format;

import com.google.common.base.Strings;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class SQLBuilder {
    private final FormatOptions options;

    private final String comma;
    private final String newLine;
    private final String prefixUnit;

    private final StringBuilder sql;
    private final Set<String> keywords = HashSet.newHashSet(12);
    private int indentLevel = 0;
    private String linePrefix = "";
    private int lastBreakPoint = 0;

    public SQLBuilder(FormatOptions options) {
        this.options = options;
        this.sql = new StringBuilder();
        this.prefixUnit = options.isCompact ? "" : " ";

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

    public void intoPrefix(Runnable func) {
        int oldIndentLevel = indentLevel;
        indentLevel = 0;
        int count = sql.length() - sql.lastIndexOf("\n") - 1;
        linePrefix = Strings.repeat(prefixUnit, count);
        func.run();
        linePrefix = "";
        indentLevel = oldIndentLevel;
    }

    public void intoParentheses(Runnable func) {
        append("(");
        func.run();
        append(")");
    }

    public void intoAutoBreak(Runnable func) {
        lastBreakPoint = sql.length(); // Record current position as a possible break point
        func.run();
        lastBreakPoint = 0; // Reset after the function execution
    }

    public SQLBuilder append(String str) {
        sql.append(str);
        breakMaxLength();
        return this;
    }

    public SQLBuilder append(String str, boolean prefixSpace, boolean suffixSpace) {
        if (str == null) {
            return this;
        }
        str = str.trim();
        if (prefixSpace && !sql.isEmpty()) {
            char l = sql.charAt(sql.length() - 1);
            if (!Character.isWhitespace(l) && l != '(') {
                append(" ");
            }
        }
        append(str);
        if (suffixSpace) {
            append(" ");
        }
        return this;
    }

    private void breakMaxLength() {
        if (options.isCompact || options.maxLineLength <= 0) {
            return;
        }
        int preLineIndex = sql.lastIndexOf("\n");
        int currentLineLength = sql.length() - preLineIndex;
        if (currentLineLength > options.maxLineLength && lastBreakPoint > 0) {
            if (StringUtils.isBlank(sql.substring(preLineIndex, lastBreakPoint).trim())) {
                return;
            }
            // If current line exceeds max length, break at the last break point
            String content = sql.substring(lastBreakPoint);
            sql.setLength(lastBreakPoint);
            sql.append(newLine());
            sql.append(content);
        }
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
                append(" ");
            }
        }
        keywords.add(key);
        append(key);
        if (suffixSpace) {
            append(" ");
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

    public String comma() {
        return comma;
    }

    public String newBreak(boolean isBreak) {
        return isBreak ? newLine() : "";
    }

    public String newLine() {
        return newLine + Strings.repeat(options.indent, indentLevel) + linePrefix;
    }

    @Override
    public String toString() {
        return sql.toString().trim();
    }
}
