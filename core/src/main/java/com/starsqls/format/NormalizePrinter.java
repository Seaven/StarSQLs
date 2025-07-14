package com.starsqls.format;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class NormalizePrinter implements Printer {

    // HTML entity mappings
    private static final String[][] HTML_ENTITIES = {
            {"&amp;", "&"}, {"&lt;", "<"}, {"&gt;", ">"},
            {"&quot;", "\""}, {"&#39;", "'"}, {"&#x27;", "'"},
            {"&apos;", "'"}, {"&nbsp;", " "}, {"&#32;", " "},
            {"&#160;", " "}, {"&#xa0;", " "}, {"&#xA0;", " "}
    };

    private static final Pattern NUMERIC_ENTITY_PATTERN = Pattern.compile("&#\\d+;");
    private static final Pattern HEX_ENTITY_PATTERN = Pattern.compile("&#[xX][0-9a-fA-F]+;");
    // Pattern to match SQL string literals (supports backslash escaping)
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'([^'\\\\]|\\\\.)*'|\"([^\"\\\\]|\\\\.)*\"");

    public NormalizePrinter(FormatOptions options) {
    }

    @Override
    public String format(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }
        StringBuilder result = new StringBuilder();
        Matcher matcher = STRING_LITERAL_PATTERN.matcher(sql);
        int lastEnd = 0;
        while (matcher.find()) {
            String before = sql.substring(lastEnd, matcher.start());
            result.append(normalize(before));
            result.append(matcher.group());
            lastEnd = matcher.end();
        }
        if (lastEnd < sql.length()) {
            result.append(normalize(sql.substring(lastEnd)));
        }
        return result.toString();
    }

    private String normalize(String text) {
        if (text.isEmpty()) {
            return text;
        }
        String result = normalizeEscapeSequences(text);
        result = normalizeHtmlEntities(result);
        result = normalizeControlCharacters(result);
        result = normalizeWhitespace(result);
        return result;
    }

    private String normalizeEscapeSequences(String text) {
        return text.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\f", "\f")
                .replace("\\b", "\b")
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
    }

    private String normalizeHtmlEntities(String text) {
        String result = text;
        for (String[] entity : HTML_ENTITIES) {
            result = result.replace(entity[0], entity[1]);
        }
        result = NUMERIC_ENTITY_PATTERN.matcher(result).replaceAll(matchResult -> {
            String match = matchResult.group();
            try {
                int code = Integer.parseInt(match.substring(2, match.length() - 1));
                return String.valueOf((char) code);
            } catch (NumberFormatException e) {
                return match;
            }
        });
        result = HEX_ENTITY_PATTERN.matcher(result).replaceAll(matchResult -> {
            String match = matchResult.group();
            try {
                int code = Integer.parseInt(match.substring(3, match.length() - 1), 16);
                return String.valueOf((char) code);
            } catch (NumberFormatException e) {
                return match;
            }
        });
        return result;
    }

    private String normalizeControlCharacters(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n").replace("\f", "").replace("\b", "").replace("\u0000", "")
                .replace("\u001B", "").replace("\u200B", "").replace("\u200C", "").replace("\u200D", "")
                .replace("\uFEFF", "");
    }

    private String normalizeWhitespace(String text) {
        return text.replace("\t", "    ").replaceAll("\n{3,}", "\n\n");
    }
}
