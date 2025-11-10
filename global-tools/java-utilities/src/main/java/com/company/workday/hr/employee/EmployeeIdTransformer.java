package com.company.workday.hr.employee;

import com.company.workday.common.ErrorHandlingUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for transforming employee IDs from legacy ERP systems to Workday format.
 * Handles various ID formats and standardization requirements common in healthcare organizations.
 */
public class EmployeeIdTransformer {

    private static final Pattern LEGACY_ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{6}$");
    private static final Pattern NUMERIC_ID_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[A-Z]{1,3}\\d{1,7}$");

    /**
     * Transforms legacy employee ID to Workday standard format (XX######).
     * @param legacyId The legacy employee ID
     * @return Standardized Workday employee ID
     * @throws IllegalArgumentException if ID format is invalid
     */
    public static String transformEmployeeId(String legacyId) {
        if (legacyId == null || legacyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }

        String cleanId = legacyId.trim().toUpperCase();

        // Handle different legacy formats
        if (LEGACY_ID_PATTERN.matcher(cleanId).matches()) {
            // Already in good format, just return
            return cleanId;
        } else if (NUMERIC_ID_PATTERN.matcher(cleanId).matches()) {
            // Convert 8-digit numeric to prefixed format
            return "EM" + cleanId;
        } else if (ALPHA_NUMERIC_PATTERN.matcher(cleanId).matches()) {
            // Handle other alpha-numeric formats
            return normalizeToStandardFormat(cleanId);
        } else {
            // Attempt to extract numeric portion
            Matcher matcher = Pattern.compile("\\d+").matcher(cleanId);
            if (matcher.find()) {
                String numbers = matcher.group();
                return String.format("EM%06d", Integer.parseInt(numbers));
            }
        }

        throw new IllegalArgumentException("Unable to transform employee ID: " + legacyId);
    }

    /**
     * Safely transforms employee ID with error handling.
     * @param legacyId The legacy employee ID
     * @param defaultId The default ID to return if transformation fails
     * @return Standardized Workday employee ID or default
     */
    public static String safeTransformEmployeeId(String legacyId, String defaultId) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformEmployeeId(legacyId),
            defaultId,
            "employee ID transformation"
        );
    }

    /**
     * Validates if an employee ID is in proper Workday format.
     * @param employeeId The employee ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmployeeId(String employeeId) {
        return employeeId != null && LEGACY_ID_PATTERN.matcher(employeeId.trim()).matches();
    }

    /**
     * Generates a temporary employee ID for new hires.
     * @param sequenceNumber A sequence number for uniqueness
     * @return Temporary employee ID in format TMP######
     */
    public static String generateTemporaryEmployeeId(int sequenceNumber) {
        return String.format("TMP%06d", sequenceNumber);
    }

    /**
     * Converts employee ID to a numeric representation for sorting.
     * @param employeeId The employee ID
     * @return Numeric representation or -1 if invalid
     */
    public static long toNumericValue(String employeeId) {
        if (!isValidEmployeeId(employeeId)) {
            return -1;
        }
        String numericPart = employeeId.substring(2);
        try {
            return Long.parseLong(numericPart);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Checks if employee ID represents a temporary/contract worker.
     * @param employeeId The employee ID
     * @return true if temporary, false otherwise
     */
    public static boolean isTemporaryEmployee(String employeeId) {
        return employeeId != null && employeeId.startsWith("TMP");
    }

    /**
     * Normalizes various alpha-numeric formats to standard XX###### format.
     * @param id The ID to normalize
     * @return Normalized ID
     */
    private static String normalizeToStandardFormat(String id) {
        // Extract letters and numbers
        String letters = id.replaceAll("\\d", "");
        String numbers = id.replaceAll("\\D", "");

        // Ensure we have at least 2 letters and pad numbers to 6 digits
        String prefix = letters.length() >= 2 ? letters.substring(0, 2) : letters + "X";
        String paddedNumbers = String.format("%06d", Integer.parseInt(numbers));

        return prefix + paddedNumbers;
    }

    // Copy-paste usage examples:
    // String workdayId = EmployeeIdTransformer.transformEmployeeId(legacyEmployeeId);
    // if (!EmployeeIdTransformer.isValidEmployeeId(workdayId)) {
    //     // Handle invalid ID
    // }
    // String safeId = EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN");
}