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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnoreCommentTest {
    @Test
    public void testIgnoreComment() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.MINIFY;
        options.ignoreComment = true;
        Printer printer = Printer.create(options);

        String input = """
                SELECT  /* This is a comment */* FROM tableA -- this is a comment\s
                 where name = 'John' AND age < 30 /* This is a comment */""";
        String expected = "SELECT * FROM tableA where name = 'John' AND age < 30";
        String result = printer.format(input);
        assertEquals(expected, result);
    }

    @Test
    public void testNotIgnoreComment() {
        FormatOptions options = new FormatOptions();
        options.mode = FormatOptions.Mode.MINIFY;
        options.ignoreComment = true;
        Printer printer = Printer.create(options);

        String input = """
                SELECT  /*+SET_VAR(xx=xx) */* FROM tableA -- this is a comment\s
                 where name = 'John' AND age < 30 /* This is a comment */""";
        String expected = "SELECT/*+SET_VAR(xx=xx) */ * FROM tableA where name = 'John' AND age < 30";
        String result = printer.format(input);
        assertEquals(expected, result);
    }
}
