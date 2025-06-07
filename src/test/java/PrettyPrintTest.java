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

import com.google.common.collect.Lists;
import com.nicesql.sql.format.FormatOptions;
import com.nicesql.sql.format.FormatPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class PrettyPrintTest extends PrinterTestBase {
    private final FormatOptions options = new FormatOptions();

    @ParameterizedTest(name = "{0}")
    @MethodSource("caseNames")
    public void testCase(String caseName) {
        FormatPrinter printer = new FormatPrinter(options);
        String sql = sql(caseName);
        String expected = result(caseName).trim();
        String actual = printer.format(sql).trim();

        Assertions.assertEquals(expected, actual);
    }

    public static Stream<String> caseNames() {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < 22; i++) {
            list.add("tpch/q" + (i + 1) + ".sql");
        }
        return list.stream();
    }


    @Test
    public void testSelect() {
        testCase("tpch/q1.sql");
    }

}
