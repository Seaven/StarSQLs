package com.starsqls.format;

import org.antlr.v4.runtime.tree.ParseTree;

public interface Printer {
    String format(String sql);

    String format(ParseTree tree);

    static Printer create(FormatOptions options) {
        if (options.mode == FormatOptions.Mode.FORMAT) {
            return new FormatPrinter(options);
        } else if (options.mode == FormatOptions.Mode.MINIFY) {
            return new FormatPrinter(options);
        } else if (options.mode == FormatOptions.Mode.NORMALIZE) {
            return new NormalizePrinter(options);
        }
        throw new IllegalArgumentException("Unsupported format mode: " + options.mode);
    }
}
