package com.company.workday.hr.compliance;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for transforming HR compliance data from legacy systems to Workday format.
 * Handles HIPAA, OSHA, and FMLA compliance tracking and reporting specific to healthcare organizations.
 * Supports compliance event tracking, certification management, and regulatory reporting requirements.
 */
public class ComplianceTransformer {

    private static final Pattern COMPLIANCE_CODE_PATTERN = Pattern.compile("^[A-Z]{2}\\d{4}$");
    private static final Pattern HIPAA_EVENT_PATTERN = Pattern.compile("^(PHI|BREACH|AUTHORIZATION|NOTICE)\\s*.*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OSHA_INCIDENT_PATTERN = Pattern.compile("^(INJURY|ILLNESS|NEAR_MISS|ACCIDENT)\\s*.*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FMLA_EVENT_PATTERN = Pattern.compile("^(LEAVE|CERTIFICATION|INTERMITTENT|SERIOUS)\\s*.*$", Pattern.CASE_INSENSIVE);

    // Compliance event type mappings
    private static final Map<String, String> COMPLIANCE_EVENT_MAPPINGS = new HashMap<>();
    private static final Map<String, String> HIPAA_VIOLATION_LEVELS = new HashMap<>();
    private static final Map<String, Integer> OSHA_SEVERITY_CODES = new HashMap<>();

    static {
        // Compliance event mappings
        COMPLIANCE_EVENT_MAPPINGS.put("HIPAA BREACH", "HIPAA-BREACH - HIPAA Security Breach");
        COMPLIANCE_EVENT_MAPPINGS.put("PHI DISCLOSURE", "HIPAA-PHI - Unauthorized PHI Disclosure");
        COMPLIANCE_EVENT_MAPPINGS.put("OSHA INJURY", "OSHA-INJURY - Workplace Injury");
        COMPLIANCE_EVENT_MAPPINGS.put("OSHA ILLNESS", "OSHA-ILLNESS - Occupational Illness");
        COMPLIANCE_EVENT_MAPPINGS.put("FMLA LEAVE", "FMLA-LEAVE - FMLA Leave Request");
        COMPLIANCE_EVENT_MAPPINGS.put("FMLA CERTIFICATION", "FMLA-CERT - FMLA Medical Certification");
        COMPLIANCE_EVENT_MAPPINGS.put("WORKERS COMP", "WC-CLAIM - Workers Compensation Claim");
        COMPLIANCE_EVENT_MAPPINGS.put("HARASSMENT REPORT", "HR-HARASS - Harassment Complaint");
        COMPLIANCE_EVENT_MAPPINGS.put("DISCRIMINATION CLAIM", "HR-DISCRIM - Discrimination Claim");

        // HIPAA violation severity levels
        HIPAA_VIOLATION_LEVELS.put("LOW", "Level 1 - No breach of unsecured PHI");
        HIPAA_VIOLATION_LEVELS.put("MODERATE", "Level 2 - Breach affecting fewer than 500 individuals");
        HIPAA_VIOLATION_LEVELS.put("HIGH", "Level 3 - Breach affecting 500 or more individuals");
        HIPAA_VIOLATION_LEVELS.put("CRITICAL", "Level 4 - Catastrophic breach with potential for serious harm");

        // OSHA incident severity codes
        OSHA_SEVERITY_CODES.put("FIRST_AID", 1);
        OSHA_SEVERITY_CODES.put("MEDICAL_TREATMENT", 2);
        OSHA_SEVERITY_CODES.put("LOST_TIME", 3);
        OSHA_SEVERITY_CODES.put("DAYS_AWAY", 4);
        OSHA_SEVERITY_CODES.put("JOB_TRANSFER", 5);
        OSHA_SEVERITY_CODES.put("RESTRICTION", 6);
        OSHA_SEVERITY_CODES.put("OTHER_RECORDABLE", 7);
        OSHA_SEVERITY_CODES.put("FATALITY", 8);
    }

    /**
     * Transforms legacy compliance event code to Workday standard format (XX####).
     * @param legacyEventCode The legacy compliance event code
     * @return Standardized Workday compliance event code
     * @throws IllegalArgumentException if event code format is invalid
     */
    public static String transformComplianceEventCode(String legacyEventCode) {
        if (!DataValidationUtils.isNotEmpty(legacyEventCode)) {
            throw new IllegalArgumentException("Compliance event code cannot be null or empty");
        }

        String cleanCode = legacyEventCode.trim().toUpperCase();

        // Handle different legacy formats
        if (COMPLIANCE_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on event type
            return generateComplianceCodeFromType(cleanCode);
        }
    }

    /**
     * Safely transforms compliance event code with error handling.
     * @param legacyEventCode The legacy compliance event code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday compliance event code or default
     */
    public static String safeTransformComplianceEventCode(String legacyEventCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformComplianceEventCode(legacyEventCode),
            defaultCode,
            "compliance event code transformation"
        );
    }

    /**
     * Standardizes HIPAA compliance event description.
     * @param legacyEventDescription The legacy HIPAA event description
     * @return Standardized HIPAA event description
     */
    public static String standardizeHipaaEvent(String legacyEventDescription) {
        if (!DataValidationUtils.isNotEmpty(legacyEventDescription)) {
            return "Unknown HIPAA Event";
        }

        String cleanDescription = legacyEventDescription.trim().toUpperCase();

        // Check for exact mappings first
        String mappedEvent = COMPLIANCE_EVENT_MAPPINGS.get(cleanDescription);
        if (mappedEvent != null) {
            return mappedEvent;
        }

        // Handle HIPAA-specific patterns
        if (HIPAA_EVENT_PATTERN.matcher(cleanDescription).matches()) {
            String[] parts = cleanDescription.split("\\s+", 2);
            String eventType = parts[0];
            String remainder = parts.length > 1 ? parts[1] : "";

            switch (eventType) {
                case "PHI":
                    return "HIPAA-PHI - " + remainder + " - PHI Disclosure";
                case "BREACH":
                    return "HIPAA-BREACH - " + remainder + " - Security Breach";
                case "AUTHORIZATION":
                    return "HIPAA-AUTH - " + remainder + " - Authorization Issue";
                case "NOTICE":
                    return "HIPAA-NOTICE - " + remainder + " - Privacy Notice Issue";
                default:
                    return "HIPAA-OTHER - " + cleanDescription;
            }
        }

        return cleanDescription;
    }

    /**
     * Safely standardizes HIPAA event with error handling.
     * @param legacyEventDescription The legacy HIPAA event description
     * @param defaultDescription The default description to return if standardization fails
     * @return Standardized HIPAA event description or default
     */
    public static String safeStandardizeHipaaEvent(String legacyEventDescription, String defaultDescription) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeHipaaEvent(legacyEventDescription),
            defaultDescription,
            "HIPAA event standardization"
        );
    }

    /**
     * Standardizes OSHA compliance event description.
     * @param legacyEventDescription The legacy OSHA event description
     * @return Standardized OSHA event description
     */
    public static String standardizeOshaEvent(String legacyEventDescription) {
        if (!DataValidationUtils.isNotEmpty(legacyEventDescription)) {
            return "Unknown OSHA Event";
        }

        String cleanDescription = legacyEventDescription.trim().toUpperCase();

        // Check for exact mappings first
        String mappedEvent = COMPLIANCE_EVENT_MAPPINGS.get(cleanDescription);
        if (mappedEvent != null) {
            return mappedEvent;
        }

        // Handle OSHA-specific patterns
        if (OSHA_INCIDENT_PATTERN.matcher(cleanDescription).matches()) {
            String[] parts = cleanDescription.split("\\s+", 2);
            String eventType = parts[0];
            String remainder = parts.length > 1 ? parts[1] : "";

            switch (eventType) {
                case "INJURY":
                    return "OSHA-INJURY - " + remainder + " - Workplace Injury";
                case "ILLNESS":
                    return "OSHA-ILLNESS - " + remainder + " - Occupational Illness";
                case "NEAR_MISS":
                    return "OSHA-NEAR - " + remainder + " - Near Miss Incident";
                case "ACCIDENT":
                    return "OSHA-ACCIDENT - " + remainder + " - Workplace Accident";
                default:
                    return "OSHA-OTHER - " + cleanDescription;
            }
        }

        return cleanDescription;
    }

    /**
     * Safely standardizes OSHA event with error handling.
     * @param legacyEventDescription The legacy OSHA event description
     * @param defaultDescription The default description to return if standardization fails
     * @return Standardized OSHA event description or default
     */
    public static String safeStandardizeOshaEvent(String legacyEventDescription, String defaultDescription) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeOshaEvent(legacyEventDescription),
            defaultDescription,
            "OSHA event standardization"
        );
    }

    /**
     * Standardizes FMLA compliance event description.
     * @param legacyEventDescription The legacy FMLA event description
     * @return Standardized FMLA event description
     */
    public static String standardizeFmlaEvent(String legacyEventDescription) {
        if (!DataValidationUtils.isNotEmpty(legacyEventDescription)) {
            return "Unknown FMLA Event";
        }

        String cleanDescription = legacyEventDescription.trim().toUpperCase();

        // Check for exact mappings first
        String mappedEvent = COMPLIANCE_EVENT_MAPPINGS.get(cleanDescription);
        if (mappedEvent != null) {
            return mappedEvent;
        }

        // Handle FMLA-specific patterns
        if (FMLA_EVENT_PATTERN.matcher(cleanDescription).matches()) {
            String[] parts = cleanDescription.split("\\s+", 2);
            String eventType = parts[0];
            String remainder = parts.length > 1 ? parts[1] : "";

            switch (eventType) {
                case "LEAVE":
                    return "FMLA-LEAVE - " + remainder + " - FMLA Leave";
                case "CERTIFICATION":
                    return "FMLA-CERT - " + remainder + " - Medical Certification";
                case "INTERMITTENT":
                    return "FMLA-INTER - " + remainder + " - Intermittent Leave";
                case "SERIOUS":
                    if (remainder.toUpperCase().contains("HEALTH")) {
                        return "FMLA-SHC - " + remainder + " - Serious Health Condition";
                    } else {
                        return "FMLA-OTHER - " + cleanDescription;
                    }
                default:
                    return "FMLA-OTHER - " + cleanDescription;
            }
        }

        return cleanDescription;
    }

    /**
     * Safely standardizes FMLA event with error handling.
     * @param legacyEventDescription The legacy FMLA event description
     * @param defaultDescription The default description to return if standardization fails
     * @return Standardized FMLA event description or default
     */
    public static String safeStandardizeFmlaEvent(String legacyEventDescription, String defaultDescription) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeFmlaEvent(legacyEventDescription),
            defaultDescription,
            "FMLA event standardization"
        );
    }

    /**
     * Calculates HIPAA breach notification timeline.
     * @param breachDate The date of the breach (yyyy-MM-dd)
     * @param affectedIndividuals Number of individuals affected
     * @return Map containing notification deadlines and requirements
     */
    public static Map<String, String> calculateHipaaNotificationTimeline(String breachDate, int affectedIndividuals) {
        Map<String, String> timeline = new HashMap<>();

        if (!DataValidationUtils.isNotEmpty(breachDate)) {
            timeline.put("error", "Breach date is required");
            return timeline;
        }

        try {
            LocalDate breachLocalDate = LocalDate.parse(breachDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // OCR notification (within 60 days for breaches of 500+ individuals)
            if (affectedIndividuals >= 500) {
                LocalDate ocrDeadline = breachLocalDate.plusDays(60);
                timeline.put("OCR_NOTIFICATION", ocrDeadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            // Individual notification (within 60 days)
            LocalDate individualDeadline = breachLocalDate.plusDays(60);
            timeline.put("INDIVIDUAL_NOTIFICATION", individualDeadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // Media notification (if 500+ individuals, within 60 days)
            if (affectedIndividuals >= 500) {
                LocalDate mediaDeadline = breachLocalDate.plusDays(60);
                timeline.put("MEDIA_NOTIFICATION", mediaDeadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            // Determine breach level
            String breachLevel;
            if (affectedIndividuals < 500) {
                breachLevel = "MODERATE";
            } else if (affectedIndividuals < 50000) {
                breachLevel = "HIGH";
            } else {
                breachLevel = "CRITICAL";
            }
            timeline.put("BREACH_LEVEL", breachLevel);

        } catch (Exception e) {
            timeline.put("error", "Invalid breach date format: " + breachDate);
        }

        return timeline;
    }

    /**
     * Calculates FMLA leave entitlement and usage.
     * @param hireDate Employee hire date (yyyy-MM-dd)
     * @param hoursWorkedPastYear Hours worked in past 12 months
     * @return Map containing FMLA eligibility and entitlement information
     */
    public static Map<String, Object> calculateFmlaEntitlement(String hireDate, double hoursWorkedPastYear) {
        Map<String, Object> entitlement = new HashMap<>();

        if (!DataValidationUtils.isNotEmpty(hireDate)) {
            entitlement.put("error", "Hire date is required");
            return entitlement;
        }

        try {
            LocalDate hireLocalDate = LocalDate.parse(hireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate currentDate = LocalDate.now();

            // Check eligibility period (employed for 12 months)
            long monthsEmployed = ChronoUnit.MONTHS.between(hireLocalDate, currentDate);
            boolean eligibleByTime = monthsEmployed >= 12;

            // Check hours worked requirement (1,250 hours in past 12 months)
            boolean eligibleByHours = hoursWorkedPastYear >= 1250;

            boolean fmlaEligible = eligibleByTime && eligibleByHours;

            entitlement.put("FMLA_ELIGIBLE", fmlaEligible);
            entitlement.put("MONTHS_EMPLOYED", monthsEmployed);
            entitlement.put("HOURS_WORKED_PAST_YEAR", hoursWorkedPastYear);
            entitlement.put("ELIGIBLE_BY_TIME", eligibleByTime);
            entitlement.put("ELIGIBLE_BY_HOURS", eligibleByHours);

            if (fmlaEligible) {
                // Calculate available leave days (up to 12 weeks = 60 business days)
                entitlement.put("AVAILABLE_LEAVE_DAYS", 60);
                entitlement.put("AVAILABLE_LEAVE_WEEKS", 12);
            } else {
                entitlement.put("AVAILABLE_LEAVE_DAYS", 0);
                entitlement.put("AVAILABLE_LEAVE_WEEKS", 0);
            }

        } catch (Exception e) {
            entitlement.put("error", "Invalid hire date format: " + hireDate);
        }

        return entitlement;
    }

    /**
     * Determines compliance event type from description.
     * @param eventDescription The event description
     * @return Compliance event type (HIPAA, OSHA, FMLA, HR, OTHER)
     */
    public static String determineComplianceEventType(String eventDescription) {
        if (!DataValidationUtils.isNotEmpty(eventDescription)) {
            return "OTHER";
        }

        String upperDescription = eventDescription.toUpperCase();

        if (HIPAA_EVENT_PATTERN.matcher(upperDescription).matches() ||
            upperDescription.contains("HIPAA") || upperDescription.contains("PHI") ||
            upperDescription.contains("BREACH") || upperDescription.contains("PRIVACY")) {
            return "HIPAA";
        } else if (OSHA_INCIDENT_PATTERN.matcher(upperDescription).matches() ||
                   upperDescription.contains("OSHA") || upperDescription.contains("INJURY") ||
                   upperDescription.contains("ILLNESS") || upperDescription.contains("ACCIDENT")) {
            return "OSHA";
        } else if (FMLA_EVENT_PATTERN.matcher(upperDescription).matches() ||
                   upperDescription.contains("FMLA") || upperDescription.contains("LEAVE") ||
                   upperDescription.contains("CERTIFICATION")) {
            return "FMLA";
        } else if (upperDescription.contains("HARASSMENT") || upperDescription.contains("DISCRIMINATION") ||
                   upperDescription.contains("RETALIATION")) {
            return "HR";
        } else {
            return "OTHER";
        }
    }

    /**
     * Validates if a compliance event code is in proper Workday format.
     * @param eventCode The event code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidComplianceEventCode(String eventCode) {
        return DataValidationUtils.isNotEmpty(eventCode) && COMPLIANCE_CODE_PATTERN.matcher(eventCode.trim()).matches();
    }

    /**
     * Checks if a compliance event requires immediate reporting.
     * @param eventType The compliance event type
     * @param severity The event severity level
     * @return true if immediate reporting required, false otherwise
     */
    public static boolean requiresImmediateReporting(String eventType, String severity) {
        if (!DataValidationUtils.isNotEmpty(eventType) || !DataValidationUtils.isNotEmpty(severity)) {
            return false;
        }

        String upperType = eventType.toUpperCase();
        String upperSeverity = severity.toUpperCase();

        // HIPAA breaches affecting 500+ individuals require immediate OCR notification
        if (upperType.equals("HIPAA") && (upperSeverity.equals("HIGH") || upperSeverity.equals("CRITICAL"))) {
            return true;
        }

        // OSHA fatalities require immediate reporting
        if (upperType.equals("OSHA") && upperSeverity.equals("FATALITY")) {
            return true;
        }

        // FMLA interference claims may require immediate attention
        if (upperType.equals("FMLA") && upperSeverity.equals("CRITICAL")) {
            return true;
        }

        return false;
    }

    /**
     * Generates a compliance event summary for reporting purposes.
     * @param eventCode The event code
     * @param eventDescription The event description
     * @param eventDate The event date
     * @param severity The event severity
     * @return Formatted compliance event summary
     */
    public static String generateComplianceEventSummary(String eventCode, String eventDescription,
                                                       String eventDate, String severity) {
        StringBuilder summary = new StringBuilder();
        summary.append("Event: ").append(standardizeHipaaEvent(eventDescription)).append("\n");
        summary.append("Code: ").append(transformComplianceEventCode(eventCode)).append("\n");
        summary.append("Type: ").append(determineComplianceEventType(eventDescription)).append("\n");
        summary.append("Date: ").append(eventDate != null ? eventDate : "Not specified").append("\n");
        summary.append("Severity: ").append(severity != null ? severity : "Unknown").append("\n");
        summary.append("Immediate Reporting: ").append(requiresImmediateReporting(
            determineComplianceEventType(eventDescription), severity) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a compliance code based on event type.
     * @param eventType The event type description
     * @return Generated compliance code
     */
    private static String generateComplianceCodeFromType(String eventType) {
        String upperType = eventType.toUpperCase();

        if (upperType.contains("HIPAA") || upperType.contains("PHI") || upperType.contains("BREACH")) {
            return "HP0001";
        } else if (upperType.contains("OSHA") || upperType.contains("INJURY") || upperType.contains("ILLNESS")) {
            return "OS0001";
        } else if (upperType.contains("FMLA") || upperType.contains("LEAVE")) {
            return "FL0001";
        } else if (upperType.contains("HARASSMENT") || upperType.contains("DISCRIMINATION")) {
            return "HR0001";
        } else {
            return "CM0001"; // Generic compliance
        }
    }

    // Copy-paste usage examples:
    // String eventCode = ComplianceTransformer.transformComplianceEventCode(legacyEventCode);
    // String hipaaEvent = ComplianceTransformer.standardizeHipaaEvent(legacyHipaaDescription);
    // String oshaEvent = ComplianceTransformer.standardizeOshaEvent(legacyOshaDescription);
    // String fmlaEvent = ComplianceTransformer.standardizeFmlaEvent(legacyFmlaDescription);
    // Map<String, String> timeline = ComplianceTransformer.calculateHipaaNotificationTimeline(breachDate, affectedCount);
    // Map<String, Object> entitlement = ComplianceTransformer.calculateFmlaEntitlement(hireDate, hoursWorked);
    // String eventType = ComplianceTransformer.determineComplianceEventType(eventDescription);
    // String summary = ComplianceTransformer.generateComplianceEventSummary(eventCode, description, date, severity);
}