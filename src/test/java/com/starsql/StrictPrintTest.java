package com.starsql;// Licensed under the Apache License, Version 2.0 (the "License");
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
import com.starsql.format.FormatOptions;
import com.starsql.format.FormatPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StrictPrintTest extends PrinterTestBase {
    private final FormatOptions options = new FormatOptions();

    @ParameterizedTest(name = "{0}")
    @MethodSource("tpchCase")
    public void testCase(String caseName, String resultName) {
        FormatPrinter printer = new FormatPrinter(options);
        String sql = sql(caseName);
        String expected = result(resultName).trim();
        String actual = printer.format(sql).trim();

        String nonSpaceSql = sql.replace(" ", "").replace("\n", "");
        String nonSpaceActual = actual.replace(" ", "").replace("\n", "");

        Assertions.assertEquals(nonSpaceSql, nonSpaceActual);
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> tpchCase() {
        List<Arguments> list = Lists.newArrayList();
        String path = Objects.requireNonNull(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("case/")).getPath());

        File file = new File(path + "tpch");
        for (String s : Objects.requireNonNull(file.list())) {
            list.add(Arguments.arguments("tpch/" + s, "strict_tpch/" + s));
        }
        return list.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("complexCase")
    public void testComplex(String caseName, String resultName) {
        FormatPrinter printer = new FormatPrinter(options);
        String sql = sql(caseName);
        String expected = result(resultName).trim();
        String actual = printer.format(sql).trim();

        String nonSpaceSql = sql.replace(" ", "").replace("\n", "");
        String nonSpaceActual = actual.replace(" ", "").replace("\n", "");

        Assertions.assertEquals(nonSpaceSql, nonSpaceActual);
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> complexCase() {
        List<Arguments> list = Lists.newArrayList();
        String path = Objects.requireNonNull(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("case/")).getPath());

        File file = new File(path + "complex");
        for (String s : Objects.requireNonNull(file.list())) {
            list.add(Arguments.arguments("complex/" + s, "strict_complex/" + s));
        }
        return list.stream();
    }

    @Test
    public void testSelect() {
        testCase("tpch/q7.sql", "strict_tpch/q7.sql");
    }

    @Test
    public void testWindow() {
        testComplex("complex/complex_case_4.sql", "strict_complex/complex_case_4.sql");
    }
}
