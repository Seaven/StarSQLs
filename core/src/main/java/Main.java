import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final String HELP_MESSAGE = """
            StarSQLs - StarRocks SQL Formatter
            
            Usage: java -jar starsqls.jar [options]
            
            Input/Output Options:
              -i, --input <file>           Input SQL file
              -o, --output <file>          Output file (default: stdout)
              -s, --sql <string>           Input SQL string directly
              -m, --minify                 Minify mode (ignore formatting options)
            
            Formatting Options:
              --indent <string>            Indent string (default: "  ")
              --max-line-length <int>      Maximum line length (default: 120)
              --keyword-style <style>      Keyword style: UPPER, LOWER, NONE (default: UPPER)
              --comma-style <style>        Comma style: AFTER, BEFORE (default: END)
              --break-function-args        Break function arguments
              --align-function-args        Align function arguments
              --break-case-when            Break CASE WHEN statements
              --align-case-when            Align CASE WHEN statements
              --break-in-list              Break IN lists
              --align-in-list              Align IN lists
              --break-and-or               Break AND/OR conditions
              --break-cte                  Break CTE statements (default: true)
              --break-join-relations       Break JOIN relations (default: true)
              --break-join-on              Break JOIN ON conditions
              --align-join-on              Align JOIN ON conditions
              --break-select-items         Break SELECT items
              --break-group-by-items       Break GROUP BY items
              --break-order-by             Break ORDER BY items
              --format-subquery            Format subqueries (default: true)
            
            Other Options:
              -h, --help                   Show this help message
            
            Examples:
              java -jar starsqls.jar -i query.sql -o formatted.sql
              java -jar starsqls.jar -s "SELECT * FROM table" --keyword-style LOWER
              java -jar starsqls.jar -i query.sql --indent "    " --max-line-length 80
              java -jar starsqls.jar -i query.sql --minify
            """;

    public static void main(String[] args) {
        try {
            CommandLineArgs cliArgs = parseArgs(args);
            
            if (cliArgs.showHelp) {
                System.out.println(HELP_MESSAGE);
                return;
            }
            
            // Validate input
            if (cliArgs.inputFile == null && cliArgs.sqlString == null) {
                System.err.println("Error: Must specify either input file (-i) or SQL string (-s)");
                System.err.println("Use --help for usage information");
                System.exit(1);
            }
            
            // Read SQL
            String sql = readSql(cliArgs);
            
            // Create format options
            FormatOptions options = createFormatOptions(cliArgs);
            
            // Format SQL
            FormatPrinter printer = new FormatPrinter(options);
            String result = printer.format(sql);
            
            // Output result
            writeOutput(result, cliArgs.outputFile);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (e instanceof IllegalArgumentException) {
                System.err.println("Use --help for usage information");
            }
            System.exit(1);
        }
    }
    
    private static String readSql(CommandLineArgs cliArgs) throws IOException {
        if (cliArgs.sqlString != null) {
            return cliArgs.sqlString;
        } else if (cliArgs.inputFile != null) {
            Path path = Paths.get(cliArgs.inputFile);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Input file not found: " + cliArgs.inputFile);
            }
            return Files.readString(path);
        }
        throw new IllegalArgumentException("No input specified");
    }
    
    private static void writeOutput(String result, String outputFile) throws IOException {
        if (outputFile != null) {
            Path path = Paths.get(outputFile);
            Files.writeString(path, result);
        } else {
            System.out.print(result);
        }
    }
    
    private static FormatOptions createFormatOptions(CommandLineArgs cliArgs) {
        FormatOptions options = new FormatOptions();
        
        if (cliArgs.minify) {
            options.isMinify = true;
            return options;
        }
        
        // Apply all formatting options
        if (cliArgs.indent != null) {
            options.indent = cliArgs.indent;
        }
        if (cliArgs.maxLineLength != null) {
            options.maxLineLength = cliArgs.maxLineLength;
        }
        if (cliArgs.keywordStyle != null) {
            options.keyWordStyle = cliArgs.keywordStyle;
        }
        if (cliArgs.commaStyle != null) {
            options.commaStyle = cliArgs.commaStyle;
        }
        if (cliArgs.breakFunctionArgs != null) {
            options.breakFunctionArgs = cliArgs.breakFunctionArgs;
        }
        if (cliArgs.alignFunctionArgs != null) {
            options.alignFunctionArgs = cliArgs.alignFunctionArgs;
        }
        if (cliArgs.breakCaseWhen != null) {
            options.breakCaseWhen = cliArgs.breakCaseWhen;
        }
        if (cliArgs.alignCaseWhen != null) {
            options.alignCaseWhen = cliArgs.alignCaseWhen;
        }
        if (cliArgs.breakInList != null) {
            options.breakInList = cliArgs.breakInList;
        }
        if (cliArgs.alignInList != null) {
            options.alignInList = cliArgs.alignInList;
        }
        if (cliArgs.breakAndOr != null) {
            options.breakAndOr = cliArgs.breakAndOr;
        }
        if (cliArgs.breakCTE != null) {
            options.breakCTE = cliArgs.breakCTE;
        }
        if (cliArgs.breakJoinRelations != null) {
            options.breakJoinRelations = cliArgs.breakJoinRelations;
        }
        if (cliArgs.breakJoinOn != null) {
            options.breakJoinOn = cliArgs.breakJoinOn;
        }
        if (cliArgs.alignJoinOn != null) {
            options.alignJoinOn = cliArgs.alignJoinOn;
        }
        if (cliArgs.breakSelectItems != null) {
            options.breakSelectItems = cliArgs.breakSelectItems;
        }
        if (cliArgs.breakGroupByItems != null) {
            options.breakGroupByItems = cliArgs.breakGroupByItems;
        }
        if (cliArgs.breakOrderBy != null) {
            options.breakOrderBy = cliArgs.breakOrderBy;
        }
        if (cliArgs.formatSubquery != null) {
            options.formatSubquery = cliArgs.formatSubquery;
        }
        
        return options;
    }
    
    private static CommandLineArgs parseArgs(String[] args) {
        CommandLineArgs cliArgs = new CommandLineArgs();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-h", "--help" -> cliArgs.showHelp = true;
                case "-i", "--input" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    cliArgs.inputFile = args[++i];
                }
                case "-o", "--output" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    cliArgs.outputFile = args[++i];
                }
                case "-s", "--sql" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    cliArgs.sqlString = args[++i];
                }
                case "-m", "--minify" -> cliArgs.minify = true;
                case "--indent" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    cliArgs.indent = args[++i];
                }
                case "--max-line-length" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    try {
                        cliArgs.maxLineLength = Integer.parseInt(args[++i]);
                        if (cliArgs.maxLineLength <= 0) {
                            throw new IllegalArgumentException("Max line length must be positive");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid max line length: " + args[i]);
                    }
                }
                case "--keyword-style" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    cliArgs.keywordStyle = parseKeywordStyle(args[++i]);
                }
                case "--comma-style" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing value for " + arg);
                    }
                    cliArgs.commaStyle = parseCommaStyle(args[++i]);
                }
                case "--break-function-args" -> cliArgs.breakFunctionArgs = true;
                case "--align-function-args" -> cliArgs.alignFunctionArgs = true;
                case "--break-case-when" -> cliArgs.breakCaseWhen = true;
                case "--align-case-when" -> cliArgs.alignCaseWhen = true;
                case "--break-in-list" -> cliArgs.breakInList = true;
                case "--align-in-list" -> cliArgs.alignInList = true;
                case "--break-and-or" -> cliArgs.breakAndOr = true;
                case "--break-cte" -> cliArgs.breakCTE = true;
                case "--break-join-relations" -> cliArgs.breakJoinRelations = true;
                case "--break-join-on" -> cliArgs.breakJoinOn = true;
                case "--align-join-on" -> cliArgs.alignJoinOn = true;
                case "--break-select-items" -> cliArgs.breakSelectItems = true;
                case "--break-group-by-items" -> cliArgs.breakGroupByItems = true;
                case "--break-order-by" -> cliArgs.breakOrderBy = true;
                case "--format-subquery" -> cliArgs.formatSubquery = true;
                default -> throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }
        
        return cliArgs;
    }
    
    private static FormatOptions.KeyWordStyle parseKeywordStyle(String value) {
        return switch (value.toUpperCase()) {
            case "UPPER" -> FormatOptions.KeyWordStyle.UPPER_CASE;
            case "LOWER" -> FormatOptions.KeyWordStyle.LOWER_CASE;
            case "NONE" -> FormatOptions.KeyWordStyle.NONE;
            default -> throw new IllegalArgumentException("Invalid keyword style: " + value + ". Use UPPER, LOWER, or NONE");
        };
    }
    
    private static FormatOptions.CommaStyle parseCommaStyle(String value) {
        return switch (value.toUpperCase()) {
            case "AFTER" -> FormatOptions.CommaStyle.SPACE_AFTER;
            case "BEFORE" -> FormatOptions.CommaStyle.SPACE_BEFORE;
            default -> throw new IllegalArgumentException("Invalid comma style: " + value + ". Use END or START");
        };
    }
    
    private static class CommandLineArgs {
        String inputFile;
        String outputFile;
        String sqlString;
        boolean minify = false;
        boolean showHelp = false;
        
        // Formatting options
        String indent;
        Integer maxLineLength;
        FormatOptions.KeyWordStyle keywordStyle;
        FormatOptions.CommaStyle commaStyle;
        Boolean breakFunctionArgs;
        Boolean alignFunctionArgs;
        Boolean breakCaseWhen;
        Boolean alignCaseWhen;
        Boolean breakInList;
        Boolean alignInList;
        Boolean breakAndOr;
        Boolean breakCTE;
        Boolean breakJoinRelations;
        Boolean breakJoinOn;
        Boolean alignJoinOn;
        Boolean breakSelectItems;
        Boolean breakGroupByItems;
        Boolean breakOrderBy;
        Boolean formatSubquery;
    }
} 