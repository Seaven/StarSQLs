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

public class SQLBuilder {
    private final FormatOptions options;

    private final String comma;
    private final String newLine;
    private final String prefixUnit;

    private final StringBuilder sql;
    private int indentLevel = 0;
    private String linePrefix = "";
    private int lastBreakPoint = 0;
    private int lastNewLineIndex = 0; // Cache last newline position for performance

    public SQLBuilder(FormatOptions options) {
        this.options = options;
        this.sql = new StringBuilder(10240); // Pre-allocate reasonable size
        this.prefixUnit = options.mode == FormatOptions.Mode.MINIFY ? "" : " ";

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
        this.newLine = options.mode == FormatOptions.Mode.MINIFY ? "" : "\n";
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
        if (str == null || str.isEmpty()) {
            return this;
        }
        sql.append(str);
        // Track newline positions for performance
        if (str.indexOf('\n') >= 0) {
            lastNewLineIndex = sql.lastIndexOf("\n");
        }
        // Only check for blank if needed for break
        if (options.mode != FormatOptions.Mode.MINIFY && options.maxLineLength > 0 && lastBreakPoint > 0) {
            breakMaxLength();
        }
        return this;
    }

    public SQLBuilder append(String str, boolean prefixSpace, boolean suffixSpace) {
        if (str == null || str.isEmpty()) {
            return this;
        }
        // Trim once and reuse
        int start = 0, end = str.length();
        while (start < end && Character.isWhitespace(str.charAt(start))) start++;
        while (end > start && Character.isWhitespace(str.charAt(end - 1))) end--;
        
        if (start >= end) {
            return this;
        }
        
        if (prefixSpace && !sql.isEmpty()) {
            char l = sql.charAt(sql.length() - 1);
            if (!Character.isWhitespace(l) && l != '(') {
                sql.append(' ');
            }
        }
        sql.append(str, start, end);
        if (suffixSpace) {
            sql.append(' ');
        }
        // Track newline positions
        if (str.indexOf('\n', start) >= 0 && str.indexOf('\n', start) < end) {
            lastNewLineIndex = sql.lastIndexOf("\n");
        }
        if (options.mode != FormatOptions.Mode.MINIFY && options.maxLineLength > 0 && lastBreakPoint > 0) {
            breakMaxLength();
        }
        return this;
    }

    private void breakMaxLength() {
        // Use cached newline position instead of lastIndexOf
        int preLineIndex = Math.max(0, lastNewLineIndex);
        int currentLineLength = sql.length() - preLineIndex;

        if (currentLineLength > options.maxLineLength && preLineIndex < lastBreakPoint) {
            // Check if content between preLineIndex and lastBreakPoint is not blank
            boolean hasContent = false;
            for (int i = preLineIndex; i < lastBreakPoint && i < sql.length(); i++) {
                if (!Character.isWhitespace(sql.charAt(i))) {
                    hasContent = true;
                    break;
                }
            }
            
            if (!hasContent) {
                return;
            }
            
            int saveLineLength = preLineIndex + options.maxLineLength - lastBreakPoint;
            int validContentLength = lastBreakPoint - preLineIndex;
            if (saveLineLength > validContentLength * 3 / 2) {
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
            String newLineStr = newLine();
            sql.append(newLineStr);
            lastNewLineIndex = sql.length() - newLineStr.length() + newLineStr.lastIndexOf('\n');
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
        if (key == null || key.isEmpty()) {
            return this;
        }
        // Trim manually to avoid creating intermediate strings
        int start = 0, end = key.length();
        while (start < end && Character.isWhitespace(key.charAt(start))) start++;
        while (end > start && Character.isWhitespace(key.charAt(end - 1))) end--;
        
        if (start >= end) {
            return this;
        }
        
        if (prefixSpace && !sql.isEmpty()) {
            char l = sql.charAt(sql.length() - 1);
            if (!Character.isWhitespace(l) && l != '(') {
                sql.append(' ');
            }
        }
        
        // Apply case transformation directly without creating intermediate string
        if (options.keyWordStyle == FormatOptions.KeyWordStyle.UPPER_CASE) {
            for (int i = start; i < end; i++) {
                sql.append(Character.toUpperCase(key.charAt(i)));
            }
        } else if (options.keyWordStyle == FormatOptions.KeyWordStyle.LOWER_CASE) {
            for (int i = start; i < end; i++) {
                sql.append(Character.toLowerCase(key.charAt(i)));
            }
        } else {
            sql.append(key, start, end);
        }
        
        if (suffixSpace) {
            sql.append(' ');
        }
        return this;
    }

    public SQLBuilder appendNewLine() {
        String newLineStr = newLine();
        sql.append(newLineStr);
        lastNewLineIndex = sql.length() - newLineStr.length() + newLineStr.lastIndexOf('\n');
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
