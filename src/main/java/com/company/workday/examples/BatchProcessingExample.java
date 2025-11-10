package com.company.workday.examples;

import com.company.workday.batch.BatchProcessingUtils;
import com.company.workday.batch.BatchProcessingUtils.BatchProcessingResult;
import com.company.workday.batch.BatchProcessingUtils.ProcessingStatistics;
import com.company.workday.batch.BatchProcessingUtils.ProgressCallback;
import com.company.workday.hr.employee.EmployeeIdTransformer;
import com.company.workday.common.DateFormatterUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Example application demonstrating batch processing utilities.
 * Shows how to process large volumes of employee data efficiently.
 */
public class BatchProcessingExample {

    public static void main(String[] args) {
        System.out.println("=== IAMX Batch Processing Example ===\n");

        // Create sample employee data (simulate large dataset)
        List<Map<String, Object>> employees = createSampleEmployeeBatch(10000);

        System.out.println("Created " + employees.size() + " employee records for processing.\n");

        // Example 1: Sequential batch processing with progress tracking
        System.out.println("1. Sequential Batch Processing:");
        long startTime = System.currentTimeMillis();

        List<Map<String, Object>> sequentialResults = BatchProcessingUtils.processBatch(
            employees,
            BatchProcessingExample::transformEmployee,
            1000, // batch size
            new ProgressCallback() {
                @Override
                public void onProgress(int current, int total) {
                    System.out.printf("Progress: %d/%d (%.1f%%)%n",
                        current, total, (double) current / total * 100);
                }
            }
        );

        long sequentialTime = System.currentTimeMillis() - startTime;
        System.out.println("Sequential processing completed in " + sequentialTime + "ms\n");

        // Example 2: Parallel processing
        System.out.println("2. Parallel Processing:");
        startTime = System.currentTimeMillis();

        List<Map<String, Object>> parallelResults = BatchProcessingUtils.processParallel(
            employees,
            BatchProcessingExample::transformEmployee,
            4 // thread pool size
        );

        long parallelTime = System.currentTimeMillis() - startTime;
        System.out.println("Parallel processing completed in " + parallelTime + "ms\n");

        // Example 3: Batch processing with retry logic
        System.out.println("3. Batch Processing with Retry Logic:");

        // Create some problematic data to test retry
        List<Map<String, Object>> mixedData = new ArrayList<>(employees.subList(0, 100));
        Map<String, Object> badEmployee = new HashMap<>();
        badEmployee.put("legacyId", null); // This will cause transformation to fail
        mixedData.add(badEmployee);

        BatchProcessingResult<Map<String, Object>> retryResults = BatchProcessingUtils.processWithRetry(
            mixedData,
            BatchProcessingExample::transformEmployee,
            3 // max retries
        );

        System.out.println("Processed " + retryResults.getResults().size() + " records");
        System.out.println("Errors: " + retryResults.getErrors().size());

        if (!retryResults.getErrors().isEmpty()) {
            System.out.println("Sample error: " + retryResults.getErrors().get(0).getException().getMessage());
        }
        System.out.println();

        // Example 4: Processing statistics
        System.out.println("4. Processing Statistics:");
        ProcessingStatistics stats = BatchProcessingUtils.createStatistics(sequentialResults, employees.size());
        System.out.printf("Total items: %d%n", stats.getTotalItems());
        System.out.printf("Successful: %d%n", stats.getSuccessful());
        System.out.printf("Failed: %d%n", stats.getFailed());
        System.out.printf("Success rate: %.2f%%%n", stats.getSuccessRate());

        System.out.println("\n=== Performance Comparison ===");
        System.out.printf("Sequential: %dms%n", sequentialTime);
        System.out.printf("Parallel: %dms%n", parallelTime);
        System.out.printf("Speedup: %.2fx%n", (double) sequentialTime / parallelTime);
    }

    /**
     * Creates a batch of sample employee data for testing.
     */
    private static List<Map<String, Object>> createSampleEmployeeBatch(int count) {
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
     * Transforms a single employee record.
     * This simulates the transformation logic used in real applications.
     */
    private static Map<String, Object> transformEmployee(Map<String, Object> employee) {
        Map<String, Object> transformed = new HashMap<>();

        // Transform employee ID
        String legacyId = (String) employee.get("legacyId");
        transformed.put("employeeId", EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN"));

        // Transform name (simple concatenation for demo)
        String firstName = (String) employee.get("firstName");
        String lastName = (String) employee.get("lastName");
        transformed.put("fullName", firstName + " " + lastName);

        // Transform hire date
        String hireDate = (String) employee.get("hireDate");
        transformed.put("hireDate", DateFormatterUtils.safeToWorkdayDateFormat(hireDate, "1900-01-01"));

        // Add processing timestamp
        transformed.put("processedAt", DateFormatterUtils.getCurrentTimestamp());

        return transformed;
    }
}