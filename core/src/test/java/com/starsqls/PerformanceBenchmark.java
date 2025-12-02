package com.starsqls;

import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;

public class PerformanceBenchmark {
    
    private static final String SIMPLE_SQL = "SELECT id, name, age FROM users WHERE age > 18 AND status = 'active' ORDER BY created_at DESC LIMIT 100";
    
    private static final String COMPLEX_SQL = 
        "WITH regional_sales AS (" +
        "    SELECT region, SUM(amount) AS total_sales FROM orders GROUP BY region" +
        "), top_regions AS (" +
        "    SELECT region FROM regional_sales WHERE total_sales > (SELECT SUM(total_sales)/10 FROM regional_sales)" +
        ") " +
        "SELECT region, product, SUM(quantity) AS product_units, SUM(amount) AS product_sales " +
        "FROM orders WHERE region IN (SELECT region FROM top_regions) GROUP BY region, product " +
        "UNION ALL " +
        "SELECT 'Other' AS region, product, SUM(quantity) AS product_units, SUM(amount) AS product_sales " +
        "FROM orders WHERE region NOT IN (SELECT region FROM top_regions) GROUP BY product";
    
    private static final String LARGE_SQL = generateLargeSQL();
    
    private static String generateLargeSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int i = 0; i < 50; i++) {
            if (i > 0) sb.append(", ");
            sb.append("col").append(i);
        }
        sb.append(" FROM ");
        for (int i = 0; i < 10; i++) {
            if (i > 0) sb.append(" JOIN ");
            sb.append("table").append(i);
            if (i > 0) {
                sb.append(" ON table0.id = table").append(i).append(".fk");
            }
        }
        sb.append(" WHERE ");
        for (int i = 0; i < 20; i++) {
            if (i > 0) sb.append(" AND ");
            sb.append("col").append(i).append(" > ").append(i * 10);
        }
        sb.append(" ORDER BY col0, col1, col2");
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("=== SQL Formatter Performance Benchmark ===\n");
        
        FormatOptions prettyOptions = FormatOptions.allFormatOptions();
        FormatOptions strictOptions = FormatOptions.defaultOptions();
        
        // 预热JVM
        warmup(prettyOptions);
        
        // 测试简单SQL
        System.out.println("1. Simple SQL (Length: " + SIMPLE_SQL.length() + " chars)");
        benchmark(SIMPLE_SQL, prettyOptions, 1000);
        System.out.println();
        
        // 测试复杂SQL
        System.out.println("2. Complex SQL (Length: " + COMPLEX_SQL.length() + " chars)");
        benchmark(COMPLEX_SQL, prettyOptions, 500);
        System.out.println();
        
        // 测试大型SQL
        System.out.println("3. Large SQL (Length: " + LARGE_SQL.length() + " chars)");
        benchmark(LARGE_SQL, strictOptions, 200);
        System.out.println();
        
        // 测试多次格式化
        System.out.println("4. Batch Processing (1000 simple SQLs)");
        batchBenchmark(SIMPLE_SQL, prettyOptions, 1000);
    }
    
    private static void warmup(FormatOptions options) {
        System.out.println("Warming up JVM...");
        FormatPrinter printer = new FormatPrinter(options);
        for (int i = 0; i < 100; i++) {
            printer.format(SIMPLE_SQL);
        }
        System.out.println("Warmup completed.\n");
    }
    
    private static void benchmark(String sql, FormatOptions options, int iterations) {
        FormatPrinter printer = new FormatPrinter(options);
        
        // 第一次运行（包含初始化开销）
        long firstStart = System.nanoTime();
        String result = printer.format(sql);
        long firstTime = System.nanoTime() - firstStart;
        
        // 多次运行取平均
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            printer.format(sql);
            totalTime += System.nanoTime() - start;
        }
        
        double avgTime = totalTime / (double) iterations / 1_000_000.0; // 转换为毫秒
        double firstTimeMs = firstTime / 1_000_000.0;
        
        System.out.println("  First run:     " + String.format("%.3f", firstTimeMs) + " ms");
        System.out.println("  Average time:  " + String.format("%.3f", avgTime) + " ms (" + iterations + " iterations)");
        System.out.println("  Throughput:    " + String.format("%.0f", 1000.0 / avgTime) + " formats/sec");
        System.out.println("  Output length: " + result.length() + " chars");
    }
    
    private static void batchBenchmark(String sql, FormatOptions options, int batchSize) {
        FormatPrinter printer = new FormatPrinter(options);
        
        long start = System.nanoTime();
        for (int i = 0; i < batchSize; i++) {
            printer.format(sql);
        }
        long totalTime = System.nanoTime() - start;
        
        double totalTimeMs = totalTime / 1_000_000.0;
        double avgTime = totalTimeMs / batchSize;
        
        System.out.println("  Total time:    " + String.format("%.3f", totalTimeMs) + " ms");
        System.out.println("  Average time:  " + String.format("%.3f", avgTime) + " ms");
        System.out.println("  Throughput:    " + String.format("%.0f", 1000.0 / avgTime) + " formats/sec");
    }
}
