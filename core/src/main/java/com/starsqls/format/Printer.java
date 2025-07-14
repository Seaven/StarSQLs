package com.starsqls.format;

public interface Printer {
    String format(String sql);

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
