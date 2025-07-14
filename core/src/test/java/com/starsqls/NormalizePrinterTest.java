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

package com.starsqls;

import com.starsqls.format.FormatOptions;
import com.starsqls.format.Printer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NormalizePrinterTest extends PrinterTestBase {

    @Test
    public void testStringEscapeSequences() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT \\n xx FROM t1 WHERE name = \\'test\\'";
        String expected = "SELECT \n xx FROM t1 WHERE name = 'test'";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testStringLiteralsProtection() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        // Test that string literals are not processed
        String input = "SELECT * FROM table WHERE name = 'John\\nDoe' AND value = \"Hello\\tWorld\"";
        String expected = "SELECT * FROM table WHERE name = 'John\\nDoe' AND value = \"Hello\\tWorld\"";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testMixedContent() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        // Test mixed content: escape sequences outside strings, but not inside
        String input = "SELECT \\n * FROM table WHERE name = 'John\\nDoe' AND value = \"Hello\\tWorld\" AND age &lt; 30";
        String expected = "SELECT \n * FROM table WHERE name = 'John\\nDoe' AND value = \"Hello\\tWorld\" AND age < 30";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testHtmlEntityUnescaping() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT * FROM table WHERE name = &quot;John&quot; AND age &lt; 30";
        String expected = "SELECT * FROM table WHERE name = \"John\" AND age < 30";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testHtmlEntitiesInStringLiterals() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        // Test that HTML entities inside string literals are not processed
        String input = "SELECT * FROM table WHERE name = 'John &amp; Jane' AND value = \"Hello &lt; World\"";
        String expected = "SELECT * FROM table WHERE name = 'John &amp; Jane' AND value = \"Hello &lt; World\"";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testNumericHtmlEntities() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT * FROM table WHERE char = &#65; AND code = &#x42;";
        String expected = "SELECT * FROM table WHERE char = A AND code = B";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testControlCharacters() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT\r\n*\rFROM\ttable\nWHERE\rname = 'test'";
        String expected = "SELECT\n*\nFROM    table\nWHERE\nname = 'test'";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testMultipleNewlines() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT *\n\n\nFROM table\n\n\nWHERE name = 'test'";
        String expected = "SELECT *\n\nFROM table\n\nWHERE name = 'test'";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testZeroWidthCharacters() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT\u200B*\u200CFROM\u200Dtable\uFEFFWHERE name = 'test'";
        String expected = "SELECT*FROMtableWHERE name = 'test'";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testComplexNormalization() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT \\n * FROM table WHERE name = &quot;John&quot; AND age &lt; 30";
        String expected = "SELECT \n * FROM table WHERE name = \"John\" AND age < 30";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testNullAndEmptyInput() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        // Test null input
        assertNull(printer.format(null));

        // Test empty input
        assertEquals("", printer.format(""));
    }

    @Test
    public void testSpecialHtmlEntities() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT * FROM table WHERE space = &nbsp; AND quote = &#39;";
        String expected = "SELECT * FROM table WHERE space =   AND quote = '";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testMixedEscapeSequences() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        String input = "SELECT \\n * FROM table WHERE name = \\'John\\' AND age &lt; 30";
        String expected = "SELECT \n * FROM table WHERE name = 'John' AND age < 30";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapedQuotesInStrings() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.NORMALIZE;
        Printer printer = Printer.create(options);

        // Test that escaped quotes in strings are preserved
        String input = "SELECT * FROM table WHERE name = 'John''s name' AND value = \"Hello\"\"World\"";
        String expected = "SELECT * FROM table WHERE name = 'John''s name' AND value = \"Hello\"\"World\"";
        String result = printer.format(input);
        assertEquals(expected, result);
    }
} 