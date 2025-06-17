package com.nicesql.sql;

import com.google.common.collect.Lists;
import com.nicesql.sql.format.FormatOptions;
import com.nicesql.sql.format.FormatPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PrettyPrintTest extends PrinterTestBase {
    private final FormatOptions options = FormatOptions.allFormatOptions();

    @ParameterizedTest(name = "{0}")
    @MethodSource("tpchCase")
    public void testCase(String caseName, String resultName) {
        FormatPrinter printer = new FormatPrinter(options);
        String sql = sql(caseName);
        String expected = result(resultName).trim();
        String actual = printer.format(sql).trim();

        String nonSpaceSql = sql.replace(" ", "").replace("\n", "").toLowerCase();
        String nonSpaceActual = actual.replace(" ", "").replace("\n", "").toLowerCase();

        Assertions.assertEquals(nonSpaceSql, nonSpaceActual);
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> tpchCase() {
        List<Arguments> list = Lists.newArrayList();
        String path = Objects.requireNonNull(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("case/")).getPath());

        File file = new File(path + "tpch");
        for (String s : Objects.requireNonNull(file.list())) {
            list.add(Arguments.arguments("tpch/" + s, "pretty_tpch/" + s));
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

        String nonSpaceSql = sql.replace(" ", "").replace("\n", "").toLowerCase();
        String nonSpaceActual = actual.replace(" ", "").replace("\n", "").toLowerCase();

        Assertions.assertEquals(nonSpaceSql, nonSpaceActual);
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> complexCase() {
        List<Arguments> list = Lists.newArrayList();
        String path = Objects.requireNonNull(
                Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("case/")).getPath());

        File file = new File(path + "complex");
        for (String s : Objects.requireNonNull(file.list())) {
            list.add(Arguments.arguments("complex/" + s, "pretty_complex/" + s));
        }
        return list.stream();
    }

    @Test
    public void testSelect() {
        testCase("tpch/q7.sql", "pretty_tpch/q7.sql");
    }

    @Test
    public void testWindow() {
        testComplex("complex/complex_case_1.sql", "pretty_complex/complex_case_1.sql");
    }
}