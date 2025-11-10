package com.company.workday.batch;

import com.company.workday.common.ErrorHandlingUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Batch processing utilities for large-scale data transformations.
 * Provides parallel processing, progress tracking, and error handling for bulk operations.
 */
public class BatchProcessingUtils {

    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Processes a list of items in batches with progress tracking.
     * @param items The list of items to process
     * @param processor The function to process each item
     * @param batchSize The size of each batch
     * @param progressCallback Callback for progress updates (receives current count and total)
     * @return List of processing results
     */
    public static <T, R> List<R> processBatch(List<T> items, Function<T, R> processor, int batchSize,
                                           ProgressCallback progressCallback) {
        List<R> results = new ArrayList<>();
        int totalItems = items.size();
        int processed = 0;

        for (int i = 0; i < totalItems; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalItems);
            List<T> batch = items.subList(i, endIndex);

            for (T item : batch) {
                try {
                    R result = processor.apply(item);
                    results.add(result);
                } catch (Exception e) {
                    // Log error but continue processing
                    ErrorHandlingUtils.logTransformationError("Batch processing error", item, e);
                    results.add(null); // Add null for failed items
                }
            }

            processed += batch.size();
            if (progressCallback != null) {
                progressCallback.onProgress(processed, totalItems);
            }
        }

        return results;
    }

    /**
     * Processes items in parallel using multiple threads.
     * @param items The list of items to process
     * @param processor The function to process each item
     * @param threadPoolSize Number of threads to use
     * @return List of processing results (order may not be preserved)
     */
    public static <T, R> List<R> processParallel(List<T> items, Function<T, R> processor, int threadPoolSize) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        try {
            List<CompletableFuture<R>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return processor.apply(item);
                    } catch (Exception e) {
                        ErrorHandlingUtils.logTransformationError("Parallel processing error", item, e);
                        return null;
                    }
                }, executor))
                .collect(Collectors.toList());

            return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Processes items in batches with parallel processing within each batch.
     * @param items The list of items to process
     * @param processor The function to process each item
     * @param batchSize The size of each batch
     * @param threadPoolSize Number of threads per batch
     * @param progressCallback Callback for progress updates
     * @return List of processing results
     */
    public static <T, R> List<R> processBatchParallel(List<T> items, Function<T, R> processor,
                                                   int batchSize, int threadPoolSize,
                                                   ProgressCallback progressCallback) {
        List<R> results = new ArrayList<>();
        int totalItems = items.size();
        int processed = 0;

        for (int i = 0; i < totalItems; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalItems);
            List<T> batch = items.subList(i, endIndex);

            List<R> batchResults = processParallel(batch, processor, threadPoolSize);
            results.addAll(batchResults);

            processed += batch.size();
            if (progressCallback != null) {
                progressCallback.onProgress(processed, totalItems);
            }
        }

        return results;
    }

    /**
     * Processes items with error recovery - retries failed items.
     * @param items The list of items to process
     * @param processor The function to process each item
     * @param maxRetries Maximum number of retries for failed items
     * @return Processing results with retry information
     */
    public static <T, R> BatchProcessingResult<R> processWithRetry(List<T> items, Function<T, R> processor, int maxRetries) {
        List<R> successful = new ArrayList<>();
        List<ProcessingError<T>> errors = new ArrayList<>();
        Map<Integer, Integer> retryCounts = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            int retryCount = 0;
            boolean processed = false;

            while (retryCount <= maxRetries && !processed) {
                try {
                    R result = processor.apply(item);
                    successful.add(result);
                    processed = true;
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount > maxRetries) {
                        errors.add(new ProcessingError<>(item, e, i));
                        successful.add(null);
                    }
                }
            }

            retryCounts.put(i, retryCount);
        }

        return new BatchProcessingResult<>(successful, errors, retryCounts);
    }

    /**
     * Filters and validates items before processing.
     * @param items The list of items to filter
     * @param validator The function to validate each item
     * @return Filtered list of valid items
     */
    public static <T> List<T> filterValidItems(List<T> items, Function<T, Boolean> validator) {
        return items.stream()
            .filter(item -> {
                try {
                    return validator.apply(item);
                } catch (Exception e) {
                    ErrorHandlingUtils.logTransformationError("Validation error", item, e);
                    return false;
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Creates processing statistics.
     * @param results The processing results
     * @param totalItems Total number of items processed
     * @return Processing statistics
     */
    public static <R> ProcessingStatistics createStatistics(List<R> results, int totalItems) {
        long successful = results.stream().filter(result -> result != null).count();
        long failed = totalItems - successful;
        double successRate = totalItems > 0 ? (double) successful / totalItems * 100 : 0;

        return new ProcessingStatistics(totalItems, successful, failed, successRate);
    }

    /**
     * Progress callback interface.
     */
    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(int current, int total);
    }

    /**
     * Batch processing result with error tracking.
     */
    public static class BatchProcessingResult<R> {
        private final List<R> results;
        private final List<ProcessingError<?>> errors;
        private final Map<Integer, Integer> retryCounts;

        public BatchProcessingResult(List<R> results, List<ProcessingError<?>> errors, Map<Integer, Integer> retryCounts) {
            this.results = results;
            this.errors = errors;
            this.retryCounts = retryCounts;
        }

        public List<R> getResults() { return results; }
        public List<ProcessingError<?>> getErrors() { return errors; }
        public Map<Integer, Integer> getRetryCounts() { return retryCounts; }
    }

    /**
     * Processing error information.
     */
    public static class ProcessingError<T> {
        private final T item;
        private final Exception exception;
        private final int index;

        public ProcessingError(T item, Exception exception, int index) {
            this.item = item;
            this.exception = exception;
            this.index = index;
        }

        public T getItem() { return item; }
        public Exception getException() { return exception; }
        public int getIndex() { return index; }
    }

    /**
     * Processing statistics.
     */
    public static class ProcessingStatistics {
        private final long totalItems;
        private final long successful;
        private final long failed;
        private final double successRate;

        public ProcessingStatistics(long totalItems, long successful, long failed, double successRate) {
            this.totalItems = totalItems;
            this.successful = successful;
            this.failed = failed;
            this.successRate = successRate;
        }

        public long getTotalItems() { return totalItems; }
        public long getSuccessful() { return successful; }
        public long getFailed() { return failed; }
        public double getSuccessRate() { return successRate; }
    }

    // Copy-paste usage examples:
    // List<Employee> results = BatchProcessingUtils.processBatch(employees, emp -> transformEmployee(emp), 100, (current, total) -> System.out.println("Progress: " + current + "/" + total));
    // List<Employee> parallelResults = BatchProcessingUtils.processParallel(employees, emp -> transformEmployee(emp), 4);
    // BatchProcessingResult<Employee> retryResults = BatchProcessingUtils.processWithRetry(employees, emp -> transformEmployee(emp), 3);
    // ProcessingStatistics stats = BatchProcessingUtils.createStatistics(results, employees.size());
}