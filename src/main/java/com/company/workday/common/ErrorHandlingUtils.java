package com.company.workday.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;
import java.util.function.Function;

/**
 * Error handling utilities for data transformation processes.
 * Provides consistent error handling patterns for CloverDX integrations.
 */
public class ErrorHandlingUtils {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingUtils.class);

    /**
     * Executes a transformation function with error handling.
     * Logs errors and returns a default value on failure.
     * @param operation The operation to perform
     * @param defaultValue The default value to return on error
     * @param context Context information for logging
     * @return The result of the operation or default value
     */
    public static <T> T safeExecute(Supplier<T> operation, T defaultValue, String context) {
        try {
            return operation.get();
        } catch (Exception e) {
            logger.error("Error in {}: {}", context, e.getMessage(), e);
            return defaultValue;
        }
    }

    /**
     * Executes a transformation function with error handling.
     * Logs errors and returns null on failure.
     * @param operation The operation to perform
     * @param context Context information for logging
     * @return The result of the operation or null
     */
    public static <T> T safeExecute(Supplier<T> operation, String context) {
        return safeExecute(operation, null, context);
    }

    /**
     * Transforms data with error handling and fallback.
     * @param input The input data
     * @param transformer The transformation function
     * @param fallbackValue The fallback value if transformation fails
     * @param context Context information for logging
     * @return The transformed result or fallback value
     */
    public static <T, R> R safeTransform(T input, Function<T, R> transformer, R fallbackValue, String context) {
        try {
            return transformer.apply(input);
        } catch (Exception e) {
            logger.error("Error transforming {} in {}: {}", input, context, e.getMessage(), e);
            return fallbackValue;
        }
    }

    /**
     * Validates data and throws exception if invalid.
     * @param data The data to validate
     * @param validator The validation function
     * @param errorMessage The error message if validation fails
     * @throws IllegalArgumentException if validation fails
     */
    public static <T> void validateOrThrow(T data, Function<T, Boolean> validator, String errorMessage) {
        if (!validator.apply(data)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Logs a warning for data quality issues but continues processing.
     * @param message The warning message
     * @param data The problematic data
     */
    public static void logDataQualityWarning(String message, Object data) {
        logger.warn("Data quality issue: {} - Data: {}", message, data);
    }

    /**
     * Logs an error for transformation failures.
     * @param message The error message
     * @param data The data that caused the error
     * @param exception The exception that occurred
     */
    public static void logTransformationError(String message, Object data, Exception exception) {
        logger.error("Transformation error: {} - Data: {} - Exception: {}",
                     message, data, exception.getMessage(), exception);
    }

    /**
     * Creates a standardized error message for missing required fields.
     * @param fieldName The name of the missing field
     * @param recordType The type of record (e.g., "Employee", "Transaction")
     * @return Standardized error message
     */
    public static String createMissingFieldError(String fieldName, String recordType) {
        return String.format("Required field '%s' is missing or empty in %s record", fieldName, recordType);
    }

    /**
     * Creates a standardized error message for invalid data format.
     * @param fieldName The name of the field with invalid data
     * @param expectedFormat The expected format
     * @param actualValue The actual value that was invalid
     * @return Standardized error message
     */
    public static String createInvalidFormatError(String fieldName, String expectedFormat, Object actualValue) {
        return String.format("Field '%s' has invalid format. Expected: %s, Actual: %s",
                            fieldName, expectedFormat, actualValue);
    }

    /**
     * Checks if an exception is recoverable (should continue processing).
     * @param exception The exception to check
     * @return true if recoverable, false if fatal
     */
    public static boolean isRecoverableException(Exception exception) {
        // Define recoverable exceptions (data format issues, missing optional fields, etc.)
        return !(exception instanceof OutOfMemoryError ||
                  exception instanceof StackOverflowError ||
                  exception instanceof InternalError);
    }

    /**
     * Wraps a runtime exception with additional context.
     * @param original The original exception
     * @param context Additional context information
     * @return Wrapped exception with context
     */
    public static RuntimeException wrapWithContext(RuntimeException original, String context) {
        return new RuntimeException(context + ": " + original.getMessage(), original);
    }

    /**
     * Creates a data transformation exception with detailed information.
     * @param message The error message
     * @param fieldName The field that caused the error
     * @param fieldValue The value that caused the error
     * @param recordId The record identifier
     * @return DataTransformationException
     */
    public static DataTransformationException createTransformationException(
            String message, String fieldName, Object fieldValue, String recordId) {
        return new DataTransformationException(message, fieldName, fieldValue, recordId);
    }

    /**
     * Custom exception for data transformation errors.
     */
    public static class DataTransformationException extends RuntimeException {
        private final String fieldName;
        private final Object fieldValue;
        private final String recordId;

        public DataTransformationException(String message, String fieldName, Object fieldValue, String recordId) {
            super(message);
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.recordId = recordId;
        }

        public String getFieldName() { return fieldName; }
        public Object getFieldValue() { return fieldValue; }
        public String getRecordId() { return recordId; }
    }

    // Copy-paste usage examples:
    // String result = ErrorHandlingUtils.safeExecute(() -> transformData(input), "default", "employee transformation");
    // ErrorHandlingUtils.validateOrThrow(email, DataValidationUtils::isValidEmail, "Invalid email format");
    // try {
    //     // transformation code
    // } catch (Exception e) {
    //     ErrorHandlingUtils.logTransformationError("Failed to transform employee", employeeData, e);
    // }
}