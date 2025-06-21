package com.starsql;

import com.starsql.format.FormatOptions;
import com.starsql.format.FormatPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormatSubqueryTest extends PrinterTestBase {

    @Test
    public void testFormatSubqueryEnabled() {
        // Test with formatSubquery = true (default behavior)
        FormatOptions options = FormatOptions.allFormatOptions();
        options.formatSubquery = true;
        
        FormatPrinter printer = new FormatPrinter(options);
        String sql = "SELECT * FROM table1 WHERE id IN (SELECT id FROM table2 WHERE name = 'test')";
        String result = printer.format(sql);
        
        // Should format the subquery with proper indentation
        Assertions.assertTrue(result.contains("        SELECT \n"
                + "            id\n"
                + "        FROM table2\n"
                + "        WHERE name = 'test'\n"), result);
    }

    @Test
    public void testFormatSubqueryDisabled() {
        // Test with formatSubquery = false
        FormatOptions options = FormatOptions.allFormatOptions();
        options.formatSubquery = false;
        
        FormatPrinter printer = new FormatPrinter(options);
        String sql = "SELECT * FROM table1 WHERE id IN (SELECT id FROM table2 WHERE name = 'test')";
        String result = printer.format(sql);
        
        // Should not format the subquery content
        // The subquery should remain compact
        Assertions.assertTrue(result.contains("WHERE id IN (SELECT id FROM"), result);
    }

    @Test
    public void testExistsSubquery() {
        FormatOptions options = FormatOptions.allFormatOptions();
        options.formatSubquery = false;
        
        FormatPrinter printer = new FormatPrinter(options);
        String sql = "SELECT * FROM table1 WHERE EXISTS (SELECT 1 FROM table2 WHERE table2.id = table1.id)";
        String result = printer.format(sql);
        
        // Should not format the EXISTS subquery
        Assertions.assertTrue(result.contains("EXISTS (SELECT 1 FROM table2 WHERE table2.id = table1.id)"));
    }

    @Test
    public void testScalarSubquery() {
        FormatOptions options = FormatOptions.allFormatOptions();
        options.formatSubquery = false;
        
        FormatPrinter printer = new FormatPrinter(options);
        String sql = "SELECT *, (SELECT MAX(value) FROM table2 WHERE table2.id = table1.id) as max_value FROM table1";
        String result = printer.format(sql);
        
        // Should not format the scalar subquery
        Assertions.assertTrue(result.contains("(SELECT MAX(value) FROM table2 WHERE table2.id = table1.id)"), result);
    }

    @Test
    public void testComplexSubquery() {
        FormatOptions options = FormatOptions.allFormatOptions();
        options.formatSubquery = false;
        
        FormatPrinter printer = new FormatPrinter(options);
        String sql = "SELECT * FROM table1 WHERE id IN (SELECT id FROM table2 WHERE name IN (SELECT name FROM table3 WHERE type = 'test'))";
        String result = printer.format(sql);
        
        // Should not format any of the nested subqueries
        Assertions.assertTrue(result.contains("SELECT id FROM table2 WHERE"), result);
    }
} 