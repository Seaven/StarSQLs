package com.nicesql.sql;

import com.nicesql.sql.format.FormatOptions;
import com.nicesql.sql.format.FormatPrinter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplifyBracketsTest {

    @Test
    public void testWhereConditionBracketsSimplification() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM table WHERE ((a > 10) AND (b < 20))";
        String expected = "SELECT * FROM table WHERE a > 10 AND b < 20";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testSimpleExpressionBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT ((a + b)) FROM table";
        String expected = "SELECT (a + b) FROM table";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testComplexExpressionBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT (((a + b) * c)) FROM table";
        String expected = "SELECT ((a + b) * c) FROM table";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testSubqueryBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM ((SELECT * FROM table1)) t";
        String expected = "SELECT * FROM (SELECT * FROM table1) t";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testFunctionBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT func(((a + b))) FROM table";
        String expected = "SELECT func((a + b)) FROM table";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testJoinConditionBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM t1 JOIN t2 ON ((t1.id = t2.id))";
        String expected = "SELECT * FROM t1 JOIN t2 ON t1.id = t2.id";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testNestedSubqueryBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM (((SELECT * FROM table1))) t";
        String expected = "SELECT * FROM (SELECT * FROM table1) t";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testSimplifyBracketsDisabled() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = false;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM table WHERE ((a > 10) AND (b < 20))";
        String expected = "SELECT * FROM table WHERE ((a > 10) AND (b < 20))";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testNecessaryBracketsPreserved() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        // This should preserve the outer parentheses as they are necessary for operator precedence
        String input = "SELECT (a + b) * c FROM table";
        String expected = "SELECT (a + b) * c FROM table";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testComplexWhereCondition() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM table WHERE (((a > 10) AND (b < 20)) OR (c = 30))";
        String expected = "SELECT * FROM table WHERE (a > 10 AND b < 20) OR c = 30";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }

    @Test
    public void testMultipleNestedBrackets() {
        FormatOptions options = new FormatOptions();
        options.simplifyBrackets = true;
        FormatPrinter printer = new FormatPrinter(options);

        String input = "SELECT * FROM table WHERE ((((a > 10)) AND ((b < 20))))";
        String expected = "SELECT * FROM table WHERE a > 10 AND b < 20";
        String result = printer.format(input);
        
        assertEquals(expected, result);
    }
} 