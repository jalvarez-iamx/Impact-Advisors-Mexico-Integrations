package com.company.workday.hr.position;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.NumberFormattingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility class for transforming HR position data from legacy systems to Workday format.
 * Handles job codes, position titles, salary structures, and healthcare-specific position mappings.
 * Supports various healthcare roles including clinical, administrative, and support positions.
 */
public class PositionTransformer {

    private static final Pattern LEGACY_JOB_CODE_PATTERN = Pattern.compile("^[A-Z]{2}\\d{4}$");
    private static final Pattern NUMERIC_JOB_CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Pattern HEALTHCARE_TITLE_PATTERN = Pattern.compile("^(DR|RN|LPN|NP|PA|MD|DO|DDS|DMD)\\s+.*$", Pattern.CASE_INSENSITIVE);

    // Healthcare-specific position mappings
    private static final Map<String, String> POSITION_MAPPINGS = new HashMap<>();
    private static final Map<String, String> JOB_FAMILY_MAPPINGS = new HashMap<>();

    static {
        // Position title mappings for healthcare roles
        POSITION_MAPPINGS.put("REGISTERED NURSE", "RN - Registered Nurse");
        POSITION_MAPPINGS.put("LICENSED PRACTICAL NURSE", "LPN - Licensed Practical Nurse");
        POSITION_MAPPINGS.put("NURSE PRACTITIONER", "NP - Nurse Practitioner");
        POSITION_MAPPINGS.put("PHYSICIAN ASSISTANT", "PA - Physician Assistant");
        POSITION_MAPPINGS.put("PHYSICIAN", "MD - Physician");
        POSITION_MAPPINGS.put("DOCTOR", "MD - Physician");
        POSITION_MAPPINGS.put("DENTIST", "DDS - Dentist");
        POSITION_MAPPINGS.put("MEDICAL ASSISTANT", "MA - Medical Assistant");
        POSITION_MAPPINGS.put("PHARMACIST", "PHARM - Pharmacist");
        POSITION_MAPPINGS.put("RADIOLOGIC TECHNOLOGIST", "RAD TECH - Radiologic Technologist");
        POSITION_MAPPINGS.put("LABORATORY TECHNICIAN", "LAB TECH - Laboratory Technician");
        POSITION_MAPPINGS.put("RESPIRATORY THERAPIST", "RT - Respiratory Therapist");
        POSITION_MAPPINGS.put("PHYSICAL THERAPIST", "PT - Physical Therapist");
        POSITION_MAPPINGS.put("OCCUPATIONAL THERAPIST", "OT - Occupational Therapist");
        POSITION_MAPPINGS.put("DIETITIAN", "RD - Registered Dietitian");
        POSITION_MAPPINGS.put("SOCIAL WORKER", "SW - Social Worker");
        POSITION_MAPPINGS.put("CASE MANAGER", "CM - Case Manager");
        POSITION_MAPPINGS.put("HEALTHCARE ADMINISTRATOR", "ADMIN - Healthcare Administrator");
        POSITION_MAPPINGS.put("BILLING SPECIALIST", "BILLING - Billing Specialist");
        POSITION_MAPPINGS.put("CODING SPECIALIST", "CODING - Coding Specialist");
        POSITION_MAPPINGS.put("PATIENT SERVICES REP", "PSR - Patient Services Representative");
        POSITION_MAPPINGS.put("HOUSEKEEPING", "HOUSEKEEPING - Housekeeping");
        POSITION_MAPPINGS.put("SECURITY OFFICER", "SECURITY - Security Officer");

        // Job family mappings
        JOB_FAMILY_MAPPINGS.put("RN", "Nursing");
        JOB_FAMILY_MAPPINGS.put("LPN", "Nursing");
        JOB_FAMILY_MAPPINGS.put("NP", "Advanced Practice");
        JOB_FAMILY_MAPPINGS.put("PA", "Advanced Practice");
        JOB_FAMILY_MAPPINGS.put("MD", "Physician");
        JOB_FAMILY_MAPPINGS.put("PHARM", "Pharmacy");
        JOB_FAMILY_MAPPINGS.put("RAD TECH", "Imaging");
        JOB_FAMILY_MAPPINGS.put("LAB TECH", "Laboratory");
        JOB_FAMILY_MAPPINGS.put("PT", "Therapy Services");
        JOB_FAMILY_MAPPINGS.put("OT", "Therapy Services");
        JOB_FAMILY_MAPPINGS.put("ADMIN", "Administration");
        JOB_FAMILY_MAPPINGS.put("BILLING", "Revenue Cycle");
        JOB_FAMILY_MAPPINGS.put("CODING", "Revenue Cycle");
    }

    /**
     * Transforms legacy job code to Workday standard format (XX####).
     * @param legacyJobCode The legacy job code
     * @return Standardized Workday job code
     * @throws IllegalArgumentException if job code format is invalid
     */
    public static String transformJobCode(String legacyJobCode) {
        if (!DataValidationUtils.isNotEmpty(legacyJobCode)) {
            throw new IllegalArgumentException("Job code cannot be null or empty");
        }

        String cleanCode = legacyJobCode.trim().toUpperCase();

        // Handle different legacy formats
        if (LEGACY_JOB_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else if (NUMERIC_JOB_CODE_PATTERN.matcher(cleanCode).matches()) {
            // Convert 6-digit numeric to prefixed format
            return "JB" + cleanCode;
        } else {
            // Attempt to extract numeric portion
            Matcher matcher = Pattern.compile("\\d+").matcher(cleanCode);
            if (matcher.find()) {
                String numbers = matcher.group();
                return String.format("JB%04d", Integer.parseInt(numbers));
            }
        }

        throw new IllegalArgumentException("Unable to transform job code: " + legacyJobCode);
    }

    /**
     * Safely transforms job code with error handling.
     * @param legacyJobCode The legacy job code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday job code or default
     */
    public static String safeTransformJobCode(String legacyJobCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformJobCode(legacyJobCode),
            defaultCode,
            "job code transformation"
        );
    }

    /**
     * Standardizes position title to Workday format with healthcare-specific mappings.
     * @param legacyTitle The legacy position title
     * @return Standardized position title
     */
    public static String standardizePositionTitle(String legacyTitle) {
        if (!DataValidationUtils.isNotEmpty(legacyTitle)) {
            return "Unknown Position";
        }

        String cleanTitle = legacyTitle.trim().toUpperCase();

        // Check for exact mappings first
        String mappedTitle = POSITION_MAPPINGS.get(cleanTitle);
        if (mappedTitle != null) {
            return mappedTitle;
        }

        // Handle healthcare credential prefixes
        if (HEALTHCARE_TITLE_PATTERN.matcher(cleanTitle).matches()) {
            // Extract credential and standardize
            String[] parts = cleanTitle.split("\\s+", 2);
            String credential = parts[0];
            String remainder = parts.length > 1 ? parts[1] : "";

            switch (credential) {
                case "DR":
                case "MD":
                case "DO":
                    return "MD - Physician" + (remainder.isEmpty() ? "" : " - " + remainder);
                case "RN":
                    return "RN - Registered Nurse" + (remainder.isEmpty() ? "" : " - " + remainder);
                case "LPN":
                    return "LPN - Licensed Practical Nurse" + (remainder.isEmpty() ? "" : " - " + remainder);
                case "NP":
                    return "NP - Nurse Practitioner" + (remainder.isEmpty() ? "" : " - " + remainder);
                case "PA":
                    return "PA - Physician Assistant" + (remainder.isEmpty() ? "" : " - " + remainder);
                case "DDS":
                case "DMD":
                    return "DDS - Dentist" + (remainder.isEmpty() ? "" : " - " + remainder);
                default:
                    return cleanTitle; // Return as-is if no mapping
            }
        }

        // Return cleaned title if no mapping found
        return cleanTitle;
    }

    /**
     * Safely standardizes position title with error handling.
     * @param legacyTitle The legacy position title
     * @param defaultTitle The default title to return if standardization fails
     * @return Standardized position title or default
     */
    public static String safeStandardizePositionTitle(String legacyTitle, String defaultTitle) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizePositionTitle(legacyTitle),
            defaultTitle,
            "position title standardization"
        );
    }

    /**
     * Transforms salary amount to standardized format with healthcare market adjustments.
     * @param legacySalary The legacy salary amount (string or numeric)
     * @param positionTitle The position title for market adjustment context
     * @return Standardized salary amount
     */
    public static double transformSalary(String legacySalary, String positionTitle) {
        if (!DataValidationUtils.isNotEmpty(legacySalary)) {
            throw new IllegalArgumentException("Salary cannot be null or empty");
        }

        double baseSalary = NumberFormattingUtils.safeParseDouble(legacySalary, 0.0);
        if (baseSalary <= 0) {
            throw new IllegalArgumentException("Invalid salary amount: " + legacySalary);
        }

        // Apply healthcare market adjustments based on position
        double adjustmentFactor = getHealthcareMarketAdjustment(positionTitle);
        double adjustedSalary = baseSalary * adjustmentFactor;

        // Round to nearest dollar
        return NumberFormattingUtils.round(adjustedSalary, 0);
    }

    /**
     * Safely transforms salary with error handling.
     * @param legacySalary The legacy salary amount
     * @param positionTitle The position title
     * @param defaultSalary The default salary to return if transformation fails
     * @return Standardized salary amount or default
     */
    public static double safeTransformSalary(String legacySalary, String positionTitle, double defaultSalary) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformSalary(legacySalary, positionTitle),
            defaultSalary,
            "salary transformation"
        );
    }

    /**
     * Determines job family based on job code or position title.
     * @param jobCode The job code
     * @param positionTitle The position title
     * @return Job family name
     */
    public static String determineJobFamily(String jobCode, String positionTitle) {
        // Try to extract from job code first
        if (DataValidationUtils.isNotEmpty(jobCode)) {
            String prefix = jobCode.replaceAll("\\d", "").toUpperCase();
            String family = JOB_FAMILY_MAPPINGS.get(prefix);
            if (family != null) {
                return family;
            }
        }

        // Fall back to position title analysis
        if (DataValidationUtils.isNotEmpty(positionTitle)) {
            String upperTitle = positionTitle.toUpperCase();
            for (Map.Entry<String, String> entry : POSITION_MAPPINGS.entrySet()) {
                if (upperTitle.contains(entry.getKey())) {
                    String code = entry.getValue().split(" - ")[0];
                    return JOB_FAMILY_MAPPINGS.getOrDefault(code, "General Staff");
                }
            }
        }

        return "General Staff";
    }

    /**
     * Validates if a job code is in proper Workday format.
     * @param jobCode The job code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidJobCode(String jobCode) {
        return DataValidationUtils.isNotEmpty(jobCode) && LEGACY_JOB_CODE_PATTERN.matcher(jobCode.trim()).matches();
    }

    /**
     * Checks if a position is a clinical healthcare role.
     * @param positionTitle The position title
     * @return true if clinical, false otherwise
     */
    public static boolean isClinicalPosition(String positionTitle) {
        if (!DataValidationUtils.isNotEmpty(positionTitle)) {
            return false;
        }

        String upperTitle = positionTitle.toUpperCase();
        return upperTitle.contains("NURSE") ||
               upperTitle.contains("PHYSICIAN") ||
               upperTitle.contains("THERAPIST") ||
               upperTitle.contains("TECHNICIAN") ||
               upperTitle.contains("PRACTITIONER") ||
               upperTitle.contains("ASSISTANT") ||
               upperTitle.contains("PHARMACIST") ||
               upperTitle.contains("DIETITIAN") ||
               HEALTHCARE_TITLE_PATTERN.matcher(upperTitle).matches();
    }

    /**
     * Gets healthcare market adjustment factor based on position type.
     * @param positionTitle The position title
     * @return Adjustment factor (1.0 = no adjustment)
     */
    private static double getHealthcareMarketAdjustment(String positionTitle) {
        if (!DataValidationUtils.isNotEmpty(positionTitle)) {
            return 1.0;
        }

        String upperTitle = positionTitle.toUpperCase();

        // Premium adjustments for high-demand roles
        if (upperTitle.contains("PHYSICIAN") || upperTitle.contains("SURGEON")) {
            return 1.15; // 15% premium for physicians
        } else if (upperTitle.contains("NURSE PRACTITIONER") || upperTitle.contains("PHYSICIAN ASSISTANT")) {
            return 1.10; // 10% premium for advanced practice
        } else if (upperTitle.contains("REGISTERED NURSE") || upperTitle.contains("CRITICAL CARE")) {
            return 1.08; // 8% premium for RNs
        } else if (upperTitle.contains("SPECIALIST") || upperTitle.contains("THERAPIST")) {
            return 1.05; // 5% premium for specialists
        }

        return 1.0; // No adjustment for other roles
    }

    /**
     * Generates a position summary for reporting purposes.
     * @param jobCode The job code
     * @param positionTitle The position title
     * @param salary The salary amount
     * @return Formatted position summary
     */
    public static String generatePositionSummary(String jobCode, String positionTitle, double salary) {
        StringBuilder summary = new StringBuilder();
        summary.append("Position: ").append(standardizePositionTitle(positionTitle)).append("\n");
        summary.append("Job Code: ").append(transformJobCode(jobCode)).append("\n");
        summary.append("Job Family: ").append(determineJobFamily(jobCode, positionTitle)).append("\n");
        summary.append("Salary: ").append(NumberFormattingUtils.formatCurrency(salary)).append("\n");
        summary.append("Clinical Role: ").append(isClinicalPosition(positionTitle) ? "Yes" : "No");

        return summary.toString();
    }

    // Copy-paste usage examples:
    // String workdayJobCode = PositionTransformer.transformJobCode(legacyJobCode);
    // String standardTitle = PositionTransformer.standardizePositionTitle(legacyTitle);
    // double salary = PositionTransformer.transformSalary(legacySalary, positionTitle);
    // String jobFamily = PositionTransformer.determineJobFamily(jobCode, positionTitle);
    // boolean isClinical = PositionTransformer.isClinicalPosition(positionTitle);
    // String summary = PositionTransformer.generatePositionSummary(jobCode, title, salary);
}