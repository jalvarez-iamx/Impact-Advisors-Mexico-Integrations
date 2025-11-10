package com.company.workday.manufacturing;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.NumberFormattingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for transforming quality management data from legacy systems to Workday format.
 * Handles inspections, non-conformance, SPC, calibration, COA, and quality audits.
 * Supports quality control processes and compliance management.
 */
public class QualityUtilities {

    private static final Pattern INSPECTION_CODE_PATTERN = Pattern.compile("^INSP-\\d{4}-\\d{6}$");
    private static final Pattern NON_CONFORMANCE_CODE_PATTERN = Pattern.compile("^NC-\\d{8}-\\d{4}$");
    private static final Pattern SPC_CODE_PATTERN = Pattern.compile("^SPC-\\d{4}-\\d{4}$");
    private static final Pattern CALIBRATION_CODE_PATTERN = Pattern.compile("^CAL-\\d{4}-\\d{6}$");
    private static final Pattern COA_CODE_PATTERN = Pattern.compile("^COA-\\d{8}-\\d{3}$");
    private static final Pattern AUDIT_CODE_PATTERN = Pattern.compile("^QA-\\d{4}-\\d{4}$");

    // Quality mappings
    private static final Map<String, String> INSPECTION_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> NON_CONFORMANCE_SEVERITY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> SPC_CONTROL_METHOD_MAPPINGS = new HashMap<>();
    private static final Map<String, String> CALIBRATION_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> COA_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> AUDIT_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> QUALITY_METRIC_THRESHOLDS = new HashMap<>();
    private static final Map<String, BigDecimal> DEFECT_RATE_THRESHOLDS = new HashMap<>();

    static {
        // Inspection type mappings
        INSPECTION_TYPE_MAPPINGS.put("INCOMING", "Incoming Inspection - Raw material quality check");
        INSPECTION_TYPE_MAPPINGS.put("IN_PROCESS", "In-Process Inspection - Production quality control");
        INSPECTION_TYPE_MAPPINGS.put("FINAL", "Final Inspection - Finished product verification");
        INSPECTION_TYPE_MAPPINGS.put("AUDIT", "Audit Inspection - Process compliance check");

        // Non-conformance severity mappings
        NON_CONFORMANCE_SEVERITY_MAPPINGS.put("CRITICAL", "Critical - Immediate action required, product quarantine");
        NON_CONFORMANCE_SEVERITY_MAPPINGS.put("MAJOR", "Major - Significant quality issue, corrective action needed");
        NON_CONFORMANCE_SEVERITY_MAPPINGS.put("MINOR", "Minor - Minor deviation, monitor and document");
        NON_CONFORMANCE_SEVERITY_MAPPINGS.put("OBSERVATION", "Observation - Potential issue, track for trends");

        // SPC control method mappings
        SPC_CONTROL_METHOD_MAPPINGS.put("X_BAR_R", "X-bar and R Chart - Process mean and range control");
        SPC_CONTROL_METHOD_MAPPINGS.put("X_BAR_S", "X-bar and S Chart - Process mean and standard deviation");
        SPC_CONTROL_METHOD_MAPPINGS.put("INDIVIDUALS", "Individuals Chart - Single measurements control");
        SPC_CONTROL_METHOD_MAPPINGS.put("P_CHART", "P Chart - Proportion defective control");
        SPC_CONTROL_METHOD_MAPPINGS.put("NP_CHART", "NP Chart - Number defective control");
        SPC_CONTROL_METHOD_MAPPINGS.put("C_CHART", "C Chart - Defect count control");
        SPC_CONTROL_METHOD_MAPPINGS.put("U_CHART", "U Chart - Defect rate control");

        // Calibration status mappings
        CALIBRATION_STATUS_MAPPINGS.put("DUE", "Due - Calibration required soon");
        CALIBRATION_STATUS_MAPPINGS.put("OVERDUE", "Overdue - Calibration past due date");
        CALIBRATION_STATUS_MAPPINGS.put("CURRENT", "Current - Equipment calibrated and valid");
        CALIBRATION_STATUS_MAPPINGS.put("OUT_OF_TOLERANCE", "Out of Tolerance - Equipment needs adjustment");

        // COA status mappings
        COA_STATUS_MAPPINGS.put("PENDING", "Pending - Certificate of Analysis in progress");
        COA_STATUS_MAPPINGS.put("APPROVED", "Approved - Certificate of Analysis accepted");
        COA_STATUS_MAPPINGS.put("REJECTED", "Rejected - Certificate of Analysis not accepted");
        COA_STATUS_MAPPINGS.put("EXPIRED", "Expired - Certificate of Analysis no longer valid");

        // Audit type mappings
        AUDIT_TYPE_MAPPINGS.put("INTERNAL", "Internal Audit - Company self-assessment");
        AUDIT_TYPE_MAPPINGS.put("EXTERNAL", "External Audit - Third-party assessment");
        AUDIT_TYPE_MAPPINGS.put("SUPPLIER", "Supplier Audit - Vendor quality assessment");
        AUDIT_TYPE_MAPPINGS.put("REGULATORY", "Regulatory Audit - Compliance verification");

        // Quality metric thresholds (percentages)
        QUALITY_METRIC_THRESHOLDS.put("EXCELLENT", new BigDecimal("99.50")); // 99.5%+
        QUALITY_METRIC_THRESHOLDS.put("GOOD", new BigDecimal("98.00")); // 98-99.49%
        QUALITY_METRIC_THRESHOLDS.put("SATISFACTORY", new BigDecimal("95.00")); // 95-97.99%
        QUALITY_METRIC_THRESHOLDS.put("NEEDS_IMPROVEMENT", new BigDecimal("90.00")); // 90-94.99%
        QUALITY_METRIC_THRESHOLDS.put("POOR", new BigDecimal("90.00")); // <90%

        // Defect rate thresholds (percentages)
        DEFECT_RATE_THRESHOLDS.put("ACCEPTABLE", new BigDecimal("1.00")); // <1%
        DEFECT_RATE_THRESHOLDS.put("MONITOR", new BigDecimal("2.00")); // 1-2%
        DEFECT_RATE_THRESHOLDS.put("ACTION_REQUIRED", new BigDecimal("5.00")); // 2-5%
        DEFECT_RATE_THRESHOLDS.put("CRITICAL", new BigDecimal("5.00")); // >5%
    }

    /**
     * Transforms legacy inspection code to Workday standard format (INSP-YYYY-NNNNNN).
     * @param legacyInspectionCode The legacy inspection code
     * @return Standardized Workday inspection code
     * @throws IllegalArgumentException if inspection code format is invalid
     */
    public static String transformInspectionCode(String legacyInspectionCode) {
        if (!DataValidationUtils.isNotEmpty(legacyInspectionCode)) {
            throw new IllegalArgumentException("Inspection code cannot be null or empty");
        }

        String cleanCode = legacyInspectionCode.trim();

        // Handle different legacy formats
        if (INSPECTION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateInspectionCode();
        }
    }

    /**
     * Safely transforms inspection code with error handling.
     * @param legacyInspectionCode The legacy inspection code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday inspection code or default
     */
    public static String safeTransformInspectionCode(String legacyInspectionCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformInspectionCode(legacyInspectionCode),
            defaultCode,
            "inspection code transformation"
        );
    }

    /**
     * Transforms legacy non-conformance code to Workday standard format (NC-YYYYMMDD-NNNN).
     * @param legacyNcCode The legacy non-conformance code
     * @return Standardized Workday non-conformance code
     * @throws IllegalArgumentException if non-conformance code format is invalid
     */
    public static String transformNonConformanceCode(String legacyNcCode) {
        if (!DataValidationUtils.isNotEmpty(legacyNcCode)) {
            throw new IllegalArgumentException("Non-conformance code cannot be null or empty");
        }

        String cleanCode = legacyNcCode.trim();

        // Handle different legacy formats
        if (NON_CONFORMANCE_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateNonConformanceCode();
        }
    }

    /**
     * Safely transforms non-conformance code with error handling.
     * @param legacyNcCode The legacy non-conformance code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday non-conformance code or default
     */
    public static String safeTransformNonConformanceCode(String legacyNcCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformNonConformanceCode(legacyNcCode),
            defaultCode,
            "non-conformance code transformation"
        );
    }

    /**
     * Transforms legacy SPC code to Workday standard format (SPC-YYYY-NNNN).
     * @param legacySpcCode The legacy SPC code
     * @return Standardized Workday SPC code
     * @throws IllegalArgumentException if SPC code format is invalid
     */
    public static String transformSpcCode(String legacySpcCode) {
        if (!DataValidationUtils.isNotEmpty(legacySpcCode)) {
            throw new IllegalArgumentException("SPC code cannot be null or empty");
        }

        String cleanCode = legacySpcCode.trim();

        // Handle different legacy formats
        if (SPC_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateSpcCode();
        }
    }

    /**
     * Safely transforms SPC code with error handling.
     * @param legacySpcCode The legacy SPC code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday SPC code or default
     */
    public static String safeTransformSpcCode(String legacySpcCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformSpcCode(legacySpcCode),
            defaultCode,
            "SPC code transformation"
        );
    }

    /**
     * Transforms legacy calibration code to Workday standard format (CAL-YYYY-NNNNNN).
     * @param legacyCalibrationCode The legacy calibration code
     * @return Standardized Workday calibration code
     * @throws IllegalArgumentException if calibration code format is invalid
     */
    public static String transformCalibrationCode(String legacyCalibrationCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCalibrationCode)) {
            throw new IllegalArgumentException("Calibration code cannot be null or empty");
        }

        String cleanCode = legacyCalibrationCode.trim();

        // Handle different legacy formats
        if (CALIBRATION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCalibrationCode();
        }
    }

    /**
     * Safely transforms calibration code with error handling.
     * @param legacyCalibrationCode The legacy calibration code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday calibration code or default
     */
    public static String safeTransformCalibrationCode(String legacyCalibrationCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCalibrationCode(legacyCalibrationCode),
            defaultCode,
            "calibration code transformation"
        );
    }

    /**
     * Transforms legacy COA code to Workday standard format (COA-YYYYMMDD-NNN).
     * @param legacyCoaCode The legacy COA code
     * @return Standardized Workday COA code
     * @throws IllegalArgumentException if COA code format is invalid
     */
    public static String transformCoaCode(String legacyCoaCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCoaCode)) {
            throw new IllegalArgumentException("COA code cannot be null or empty");
        }

        String cleanCode = legacyCoaCode.trim();

        // Handle different legacy formats
        if (COA_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCoaCode();
        }
    }

    /**
     * Safely transforms COA code with error handling.
     * @param legacyCoaCode The legacy COA code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday COA code or default
     */
    public static String safeTransformCoaCode(String legacyCoaCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCoaCode(legacyCoaCode),
            defaultCode,
            "COA code transformation"
        );
    }

    /**
     * Transforms legacy audit code to Workday standard format (QA-YYYY-NNNN).
     * @param legacyAuditCode The legacy audit code
     * @return Standardized Workday audit code
     * @throws IllegalArgumentException if audit code format is invalid
     */
    public static String transformAuditCode(String legacyAuditCode) {
        if (!DataValidationUtils.isNotEmpty(legacyAuditCode)) {
            throw new IllegalArgumentException("Audit code cannot be null or empty");
        }

        String cleanCode = legacyAuditCode.trim();

        // Handle different legacy formats
        if (AUDIT_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateAuditCode();
        }
    }

    /**
     * Safely transforms audit code with error handling.
     * @param legacyAuditCode The legacy audit code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday audit code or default
     */
    public static String safeTransformAuditCode(String legacyAuditCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformAuditCode(legacyAuditCode),
            defaultCode,
            "audit code transformation"
        );
    }

    /**
     * Standardizes non-conformance severity description.
     * @param legacySeverity The legacy non-conformance severity
     * @return Standardized non-conformance severity
     */
    public static String standardizeNonConformanceSeverity(String legacySeverity) {
        if (!DataValidationUtils.isNotEmpty(legacySeverity)) {
            return "Unknown Severity";
        }

        String cleanSeverity = legacySeverity.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedSeverity = NON_CONFORMANCE_SEVERITY_MAPPINGS.get(cleanSeverity);
        if (mappedSeverity != null) {
            return mappedSeverity;
        }

        // Handle common variations
        if (cleanSeverity.contains("CRITICAL") || cleanSeverity.contains("MAJOR") && cleanSeverity.contains("IMMEDIATE")) {
            return NON_CONFORMANCE_SEVERITY_MAPPINGS.get("CRITICAL");
        } else if (cleanSeverity.contains("MAJOR") || cleanSeverity.contains("SIGNIFICANT")) {
            return NON_CONFORMANCE_SEVERITY_MAPPINGS.get("MAJOR");
        } else if (cleanSeverity.contains("MINOR") || cleanSeverity.contains("DEVIATION")) {
            return NON_CONFORMANCE_SEVERITY_MAPPINGS.get("MINOR");
        } else if (cleanSeverity.contains("OBSERVATION") || cleanSeverity.contains("POTENTIAL")) {
            return NON_CONFORMANCE_SEVERITY_MAPPINGS.get("OBSERVATION");
        }

        return cleanSeverity;
    }

    /**
     * Safely standardizes non-conformance severity with error handling.
     * @param legacySeverity The legacy non-conformance severity
     * @param defaultSeverity The default severity to return if standardization fails
     * @return Standardized non-conformance severity or default
     */
    public static String safeStandardizeNonConformanceSeverity(String legacySeverity, String defaultSeverity) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeNonConformanceSeverity(legacySeverity),
            defaultSeverity,
            "non-conformance severity standardization"
        );
    }

    /**
     * Calculates quality yield percentage.
     * @param goodUnits Number of good units produced
     * @param totalUnits Total units produced
     * @return Quality yield percentage (0-100)
     */
    public static BigDecimal calculateQualityYield(BigDecimal goodUnits, BigDecimal totalUnits) {
        if (goodUnits == null || totalUnits == null || totalUnits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal yield = goodUnits.divide(totalUnits, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        return yield.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines quality rating based on yield percentage.
     * @param yield The calculated yield percentage
     * @return Quality rating (EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT, POOR)
     */
    public static String determineQualityRating(BigDecimal yield) {
        if (yield == null) {
            return "UNKNOWN";
        }

        if (yield.compareTo(QUALITY_METRIC_THRESHOLDS.get("EXCELLENT")) >= 0) {
            return "EXCELLENT";
        } else if (yield.compareTo(QUALITY_METRIC_THRESHOLDS.get("GOOD")) >= 0) {
            return "GOOD";
        } else if (yield.compareTo(QUALITY_METRIC_THRESHOLDS.get("SATISFACTORY")) >= 0) {
            return "SATISFACTORY";
        } else if (yield.compareTo(QUALITY_METRIC_THRESHOLDS.get("NEEDS_IMPROVEMENT")) >= 0) {
            return "NEEDS_IMPROVEMENT";
        } else {
            return "POOR";
        }
    }

    /**
     * Calculates defect rate percentage.
     * @param defectiveUnits Number of defective units
     * @param totalUnits Total units inspected
     * @return Defect rate percentage (0-100)
     */
    public static BigDecimal calculateDefectRate(BigDecimal defectiveUnits, BigDecimal totalUnits) {
        if (defectiveUnits == null || totalUnits == null || totalUnits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal defectRate = defectiveUnits.divide(totalUnits, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        return defectRate.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines defect rate severity based on defect rate percentage.
     * @param defectRate The calculated defect rate percentage
     * @return Defect rate severity (ACCEPTABLE, MONITOR, ACTION_REQUIRED, CRITICAL)
     */
    public static String determineDefectRateSeverity(BigDecimal defectRate) {
        if (defectRate == null) {
            return "UNKNOWN";
        }

        if (defectRate.compareTo(DEFECT_RATE_THRESHOLDS.get("CRITICAL")) > 0) {
            return "CRITICAL";
        } else if (defectRate.compareTo(DEFECT_RATE_THRESHOLDS.get("ACTION_REQUIRED")) > 0) {
            return "ACTION_REQUIRED";
        } else if (defectRate.compareTo(DEFECT_RATE_THRESHOLDS.get("MONITOR")) > 0) {
            return "MONITOR";
        } else {
            return "ACCEPTABLE";
        }
    }

    /**
     * Validates if an inspection code is in proper Workday format.
     * @param inspectionCode The inspection code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInspectionCode(String inspectionCode) {
        return DataValidationUtils.isNotEmpty(inspectionCode) && INSPECTION_CODE_PATTERN.matcher(inspectionCode.trim()).matches();
    }

    /**
     * Validates if a non-conformance code is in proper Workday format.
     * @param ncCode The non-conformance code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidNonConformanceCode(String ncCode) {
        return DataValidationUtils.isNotEmpty(ncCode) && NON_CONFORMANCE_CODE_PATTERN.matcher(ncCode.trim()).matches();
    }

    /**
     * Validates if an SPC code is in proper Workday format.
     * @param spcCode The SPC code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSpcCode(String spcCode) {
        return DataValidationUtils.isNotEmpty(spcCode) && SPC_CODE_PATTERN.matcher(spcCode.trim()).matches();
    }

    /**
     * Validates if a calibration code is in proper Workday format.
     * @param calibrationCode The calibration code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCalibrationCode(String calibrationCode) {
        return DataValidationUtils.isNotEmpty(calibrationCode) && CALIBRATION_CODE_PATTERN.matcher(calibrationCode.trim()).matches();
    }

    /**
     * Validates if a COA code is in proper Workday format.
     * @param coaCode The COA code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCoaCode(String coaCode) {
        return DataValidationUtils.isNotEmpty(coaCode) && COA_CODE_PATTERN.matcher(coaCode.trim()).matches();
    }

    /**
     * Validates if an audit code is in proper Workday format.
     * @param auditCode The audit code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAuditCode(String auditCode) {
        return DataValidationUtils.isNotEmpty(auditCode) && AUDIT_CODE_PATTERN.matcher(auditCode.trim()).matches();
    }

    /**
     * Generates a quality management summary for reporting purposes.
     * @param inspectionCode The inspection code
     * @param ncCode The non-conformance code
     * @param spcCode The SPC code
     * @param calibrationCode The calibration code
     * @param coaCode The COA code
     * @param auditCode The audit code
     * @param yield The quality yield
     * @param qualityRating The quality rating
     * @param defectRate The defect rate
     * @param defectSeverity The defect rate severity
     * @return Formatted quality management summary
     */
    public static String generateQualityManagementSummary(String inspectionCode, String ncCode, String spcCode,
                                                        String calibrationCode, String coaCode, String auditCode,
                                                        BigDecimal yield, String qualityRating,
                                                        BigDecimal defectRate, String defectSeverity) {
        StringBuilder summary = new StringBuilder();
        summary.append("Inspection Code: ").append(safeTransformInspectionCode(inspectionCode, "Not specified")).append("\n");
        summary.append("Non-Conformance Code: ").append(safeTransformNonConformanceCode(ncCode, "Not specified")).append("\n");
        summary.append("SPC Code: ").append(safeTransformSpcCode(spcCode, "Not specified")).append("\n");
        summary.append("Calibration Code: ").append(safeTransformCalibrationCode(calibrationCode, "Not specified")).append("\n");
        summary.append("COA Code: ").append(safeTransformCoaCode(coaCode, "Not specified")).append("\n");
        summary.append("Audit Code: ").append(safeTransformAuditCode(auditCode, "Not specified")).append("\n");
        summary.append("Quality Yield: ").append(yield != null ? yield.toString() + "%" : "Not calculated").append("\n");
        summary.append("Quality Rating: ").append(qualityRating != null ? qualityRating : "Not rated").append("\n");
        summary.append("Defect Rate: ").append(defectRate != null ? defectRate.toString() + "%" : "Not calculated").append("\n");
        summary.append("Defect Severity: ").append(defectSeverity != null ? defectSeverity : "Not assessed").append("\n");
        summary.append("Valid Inspection Code: ").append(isValidInspectionCode(inspectionCode) ? "Yes" : "No").append("\n");
        summary.append("Valid NC Code: ").append(isValidNonConformanceCode(ncCode) ? "Yes" : "No").append("\n");
        summary.append("Valid SPC Code: ").append(isValidSpcCode(spcCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Calibration Code: ").append(isValidCalibrationCode(calibrationCode) ? "Yes" : "No").append("\n");
        summary.append("Valid COA Code: ").append(isValidCoaCode(coaCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Audit Code: ").append(isValidAuditCode(auditCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates an inspection code based on current date.
     * @return Generated inspection code
     */
    private static String generateInspectionCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("INSP-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a non-conformance code based on current date.
     * @return Generated non-conformance code
     */
    private static String generateNonConformanceCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return "NC-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates an SPC code based on current date.
     * @return Generated SPC code
     */
    private static String generateSpcCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("SPC-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a calibration code based on current date.
     * @return Generated calibration code
     */
    private static String generateCalibrationCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("CAL-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a COA code based on current date.
     * @return Generated COA code
     */
    private static String generateCoaCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%03d", (int)(Math.random() * 1000));
        return "COA-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates an audit code based on current date.
     * @return Generated audit code
     */
    private static String generateAuditCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("QA-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String inspectionCode = QualityUtilities.transformInspectionCode(legacyInspectionCode);
    // String ncCode = QualityUtilities.transformNonConformanceCode(legacyNcCode);
    // String spcCode = QualityUtilities.transformSpcCode(legacySpcCode);
    // String calibrationCode = QualityUtilities.transformCalibrationCode(legacyCalibrationCode);
    // String coaCode = QualityUtilities.transformCoaCode(legacyCoaCode);
    // String auditCode = QualityUtilities.transformAuditCode(legacyAuditCode);
    // String severity = QualityUtilities.standardizeNonConformanceSeverity(legacySeverity);
    // BigDecimal yield = QualityUtilities.calculateQualityYield(goodUnits, totalUnits);
    // String qualityRating = QualityUtilities.determineQualityRating(yield);
    // BigDecimal defectRate = QualityUtilities.calculateDefectRate(defectiveUnits, totalUnits);
    // String defectSeverity = QualityUtilities.determineDefectRateSeverity(defectRate);
    // String summary = QualityUtilities.generateQualityManagementSummary(inspectionCode, ncCode, spcCode, calibrationCode, coaCode, auditCode, yield, qualityRating, defectRate, defectSeverity);
}