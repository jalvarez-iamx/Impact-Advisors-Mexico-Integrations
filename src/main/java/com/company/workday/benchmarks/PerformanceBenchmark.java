package com.company.workday.benchmarks;

import com.company.workday.batch.BatchProcessingUtils;
import com.company.workday.hr.employee.EmployeeIdTransformer;
import com.company.workday.common.DateFormatterUtils;
import com.company.workday.common.StringManipulationUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Performance benchmarking utilities for IAMX transformations.
 * Measures throughput, latency, and scalability of transformation operations.
 */
public class PerformanceBenchmark {

    private static final int[] DATASET_SIZES = {1000, 10000, 100000};
    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASUREMENT_ITERATIONS = 10;

    public static void main(String[] args) {
        System.out.println("=== IAMX Performance Benchmarks ===\n");

        runEmployeeTransformationBenchmark();
        runBatchProcessingBenchmark();
        runStringManipulationBenchmark();
        runDateFormattingBenchmark();

        System.out.println("Benchmarks completed.");
    }

    /**
     * Benchmarks employee ID transformation performance.
     */
    public static void runEmployeeTransformationBenchmark() {
        System.out.println("1. Employee ID Transformation Benchmark");

        for (int size : DATASET_SIZES) {
            List<String> legacyIds = generateLegacyIds(size);

            // Warmup
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                legacyIds.forEach(EmployeeIdTransformer::safeTransformEmployeeId);
            }

            // Measure
            long totalTime = 0;
            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                legacyIds.forEach(id -> EmployeeIdTransformer.safeTransformEmployeeId(id, "UNKNOWN"));
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime);
            }

            double avgTimeMs = TimeUnit.NANOSECONDS.toMillis(totalTime) / (double) MEASUREMENT_ITERATIONS;
            double throughput = size / (avgTimeMs / 1000.0); // records per second

            System.out.printf("  Dataset Size: %,d records%n", size);
            System.out.printf("  Average Time: %.2f ms%n", avgTimeMs);
            System.out.printf("  Throughput: %,.0f records/sec%n", throughput);
            System.out.printf("  Latency: %.3f μs/record%n", (avgTimeMs * 1000) / size);
            System.out.println();
        }
    }

    /**
     * Benchmarks batch processing performance.
     */
    public static void runBatchProcessingBenchmark() {
        System.out.println("2. Batch Processing Benchmark");

        for (int size : DATASET_SIZES) {
            List<Map<String, Object>> employees = generateEmployeeBatch(size);

            // Sequential processing
            long seqStart = System.nanoTime();
            List<Map<String, Object>> seqResults = BatchProcessingUtils.processBatch(
                employees, PerformanceBenchmark::transformEmployee, 1000, null);
            long seqEnd = System.nanoTime();
            double seqTimeMs = TimeUnit.NANOSECONDS.toMillis(seqEnd - seqStart);

            // Parallel processing
            long parStart = System.nanoTime();
            List<Map<String, Object>> parResults = BatchProcessingUtils.processParallel(
                employees, PerformanceBenchmark::transformEmployee, 4);
            long parEnd = System.nanoTime();
            double parTimeMs = TimeUnit.NANOSECONDS.toMillis(parEnd - parStart);

            double speedup = seqTimeMs / parTimeMs;

            System.out.printf("  Dataset Size: %,d records%n", size);
            System.out.printf("  Sequential: %.2f ms%n", seqTimeMs);
            System.out.printf("  Parallel: %.2f ms%n", parTimeMs);
            System.out.printf("  Speedup: %.2fx%n", speedup);
            System.out.printf("  Seq Throughput: %,.0f records/sec%n", size / (seqTimeMs / 1000.0));
            System.out.printf("  Par Throughput: %,.0f records/sec%n", size / (parTimeMs / 1000.0));
            System.out.println();
        }
    }

    /**
     * Benchmarks string manipulation performance.
     */
    public static void runStringManipulationBenchmark() {
        System.out.println("3. String Manipulation Benchmark");

        List<String> testStrings = generateTestStrings(10000);

        // Benchmark different operations
        benchmarkStringOperation("normalizeWhitespace", testStrings,
            str -> StringManipulationUtils.normalizeWhitespace(str + "  "));

        benchmarkStringOperation("toTitleCase", testStrings,
            str -> StringManipulationUtils.toTitleCase(str.toLowerCase()));

        benchmarkStringOperation("standardizePhoneNumber", testStrings,
            str -> StringManipulationUtils.standardizePhoneNumber(extractDigits(str)));
    }

    /**
     * Benchmarks date formatting performance.
     */
    public static void runDateFormattingBenchmark() {
        System.out.println("4. Date Formatting Benchmark");

        List<String> testDates = generateTestDates(10000);

        benchmarkStringOperation("toWorkdayDateFormat", testDates,
            date -> DateFormatterUtils.safeToWorkdayDateFormat(date, "1900-01-01"));

        benchmarkStringOperation("calculateAge", testDates,
            date -> String.valueOf(DateFormatterUtils.calculateAge(DateFormatterUtils.safeToWorkdayDateFormat(date, "1990-01-01"))));
    }

    /**
     * Helper method to benchmark a string operation.
     */
    private static void benchmarkStringOperation(String operationName, List<String> inputs,
                                               java.util.function.Function<String, String> operation) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            inputs.forEach(operation);
        }

        // Measure
        long startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            inputs.forEach(operation);
        }
        long endTime = System.nanoTime();

        double totalTimeMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double avgTimeMs = totalTimeMs / MEASUREMENT_ITERATIONS;
        double throughput = inputs.size() / (avgTimeMs / 1000.0);

        System.out.printf("  %s: %.2f ms (%,.0f ops/sec)%n", operationName, avgTimeMs, throughput);
    }

    /**
     * Generates test legacy employee IDs.
     */
    private static List<String> generateLegacyIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(String.format("%06d", i + 1));
        }
        return ids;
    }

    /**
     * Generates test employee data.
     */
    private static List<Map<String, Object>> generateEmployeeBatch(int count) {
        List<Map<String, Object>> employees = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> employee = new HashMap<>();
            employee.put("legacyId", String.format("%06d", i + 1));
            employee.put("firstName", "Employee" + (i + 1));
            employee.put("lastName", "Test");
            employee.put("hireDate", "01/01/2023");
            employees.add(employee);
        }
        return employees;
    }

    /**
     * Generates test strings for string manipulation benchmarks.
     */
    private static List<String> generateTestStrings(int count) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            strings.add("test string " + i + " with   extra   spaces");
        }
        return strings;
    }

    /**
     * Generates test dates for date formatting benchmarks.
     */
    private static List<String> generateTestDates(int count) {
        List<String> dates = new ArrayList<>();
        String[] formats = {"01/15/2023", "15/01/2023", "2023-01-15", "01-15-2023"};
        for (int i = 0; i < count; i++) {
            dates.add(formats[i % formats.length]);
        }
        return dates;
    }

    /**
     * Extracts digits from a string for phone number testing.
     */
    private static String extractDigits(String str) {
        return str.replaceAll("\\D", "").substring(0, Math.min(10, str.length()));
    }

    /**
     * Transforms a single employee record (for benchmarking).
     */
    private static Map<String, Object> transformEmployee(Map<String, Object> employee) {
        Map<String, Object> transformed = new HashMap<>();

        String legacyId = (String) employee.get("legacyId");
        transformed.put("employeeId", EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN"));

        String firstName = (String) employee.get("firstName");
        String lastName = (String) employee.get("lastName");
        transformed.put("fullName", firstName + " " + lastName);

        String hireDate = (String) employee.get("hireDate");
        transformed.put("hireDate", DateFormatterUtils.safeToWorkdayDateFormat(hireDate, "1900-01-01"));

        return transformed;
    }

    /**
     * Memory usage benchmark.
     */
    public static void runMemoryBenchmark() {
        System.out.println("5. Memory Usage Benchmark");

        Runtime runtime = Runtime.getRuntime();

        // Force garbage collection
        System.gc();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        // Process large dataset
        List<Map<String, Object>> largeDataset = generateEmployeeBatch(100000);
        List<Map<String, Object>> results = BatchProcessingUtils.processBatch(
            largeDataset, PerformanceBenchmark::transformEmployee, 10000, null);

        System.gc();
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();

        long memoryUsed = afterMemory - beforeMemory;
        double memoryPerRecord = (double) memoryUsed / largeDataset.size();

        System.out.printf("  Memory used: %.2f MB%n", memoryUsed / (1024.0 * 1024.0));
        System.out.printf("  Memory per record: %.2f KB%n", memoryPerRecord / 1024.0);
        System.out.printf("  Records processed: %,d%n", results.size());
    }
}