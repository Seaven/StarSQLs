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
        this.prefixUnit = options.isMinify ? "" : " ";

        String temp = ",";
        if (options.commaStyle == FormatOptions.CommaStyle.SPACE_AFTER
                || options.commaStyle == FormatOptions.CommaStyle.BOTH) {
            temp += " ";
        }
        if (options.commaStyle == FormatOptions.CommaStyle.SPACE_BEFORE
                || options.commaStyle == FormatOptions.CommaStyle.BOTH) {
            temp = " " + temp;
        }
        this.comma = temp;
        this.newLine = options.isMinify ? "" : "\n";
    }

    public void intoLevel(Runnable func) {
        indentLevel++;
        func.run();
        indentLevel--;
    }

    public void intoFixPrefix(Runnable func) {
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
        if (options.isMinify || options.maxLineLength <= 0) {
            return;
        }
        int preLineIndex = Math.max(0, sql.lastIndexOf("\n"));
        int currentLineLength = sql.length() - preLineIndex;
        if (currentLineLength > options.maxLineLength && lastBreakPoint > 0 && preLineIndex < lastBreakPoint) {
            if (StringUtils.isBlank(sql.substring(preLineIndex, lastBreakPoint).trim())) {
                return;
            }
            // If current line exceeds max length, break at the last break point
            int breakIndex = lastBreakPoint;
            for (; breakIndex < sql.length(); breakIndex++) {
                if (!Character.isWhitespace(sql.charAt(breakIndex))) {
                    break;
                }
            }
            String content = sql.substring(breakIndex);
            sql.setLength(breakIndex);
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
        if (options.keyWordStyle == FormatOptions.KeyWordStyle.UPPER_CASE) {
            key = key.toUpperCase();
        } else if (options.keyWordStyle == FormatOptions.KeyWordStyle.LOWER_CASE) {
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
