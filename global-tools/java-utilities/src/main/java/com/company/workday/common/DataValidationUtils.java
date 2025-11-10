package com.company.workday.common;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Common data validation utilities for healthcare data transformations.
 * Provides reusable validation methods for various data types and formats.
 */
public class DataValidationUtils {

    // Common patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?1?[-.\\s]?\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$"
    );
    private static final Pattern SSN_PATTERN = Pattern.compile(
        "^(?!000|666|9)\\d{3}-?(?!00)\\d{2}-?(?!0000)\\d{4}$"
    );
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile(
        "^\\d{5}(-\\d{4})?$"
    );

    /**
     * Validates if a string is not null, empty, or whitespace-only.
     * @param value The string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates email format.
     * @param email The email string to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format (US format).
     * @param phone The phone number to validate
     * @return true if valid phone format, false otherwise
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates SSN format.
     * @param ssn The SSN to validate
     * @return true if valid SSN format, false otherwise
     */
    public static boolean isValidSSN(String ssn) {
        return ssn != null && SSN_PATTERN.matcher(ssn).matches();
    }

    /**
     * Validates ZIP code format.
     * @param zipCode The ZIP code to validate
     * @return true if valid ZIP format, false otherwise
     */
    public static boolean isValidZipCode(String zipCode) {
        return zipCode != null && ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Validates date string against multiple common formats.
     * @param dateStr The date string to validate
     * @return true if valid date, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }

        String[] formats = {
            "yyyy-MM-dd", "MM/dd/yyyy", "MM-dd-yyyy",
            "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd"
        };

        for (String format : formats) {
            try {
                LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
                return true;
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }
        return false;
    }

    /**
     * Validates if a number is within a specified range.
     * @param value The number to validate
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Validates if a required field is present in a data map.
     * @param data The data map to check
     * @param fieldName The field name to validate
     * @return true if field exists and is not empty, false otherwise
     */
    public static boolean isRequiredFieldPresent(Map<String, Object> data, String fieldName) {
        if (data == null || fieldName == null) {
            return false;
        }
        Object value = data.get(fieldName);
        return value != null && !String.valueOf(value).trim().isEmpty();
    }

    /**
     * Performs comprehensive validation on employee data.
     * @param employeeData Map containing employee data
     * @return Map of validation errors (empty if valid)
     */
    public static Map<String, String> validateEmployeeData(Map<String, Object> employeeData) {
        Map<String, String> errors = new HashMap<>();

        // Required fields
        if (!isRequiredFieldPresent(employeeData, "employeeId")) {
            errors.put("employeeId", "Employee ID is required");
        }
        if (!isRequiredFieldPresent(employeeData, "firstName")) {
            errors.put("firstName", "First name is required");
        }
        if (!isRequiredFieldPresent(employeeData, "lastName")) {
            errors.put("lastName", "Last name is required");
        }

        // Email validation
        String email = (String) employeeData.get("email");
        if (email != null && !email.isEmpty() && !isValidEmail(email)) {
            errors.put("email", "Invalid email format");
        }

        // Phone validation
        String phone = (String) employeeData.get("phone");
        if (phone != null && !phone.isEmpty() && !isValidPhoneNumber(phone)) {
            errors.put("phone", "Invalid phone number format");
        }

        // SSN validation
        String ssn = (String) employeeData.get("ssn");
        if (ssn != null && !ssn.isEmpty() && !isValidSSN(ssn)) {
            errors.put("ssn", "Invalid SSN format");
        }

        // Date validation
        String hireDate = (String) employeeData.get("hireDate");
        if (hireDate != null && !hireDate.isEmpty() && !isValidDate(hireDate)) {
            errors.put("hireDate", "Invalid hire date format");
        }

        return errors;
    }

    // Copy-paste usage examples:
    // if (!DataValidationUtils.isValidEmail(email)) {
    //     throw new IllegalArgumentException("Invalid email");
    // }
    //
    // Map<String, String> errors = DataValidationUtils.validateEmployeeData(employeeMap);
    // if (!errors.isEmpty()) {
    //     // Handle validation errors
    // }
}