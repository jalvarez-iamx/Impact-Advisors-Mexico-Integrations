package com.company.workday.finance.healthcare;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for transforming healthcare-specific financial data from legacy systems to Workday format.
 * Handles charge codes, claims, patient billing, and healthcare reimbursement processing.
 * Supports medical coding standards, insurance claims, and healthcare financial workflows.
 */
public class HealthcareFinanceTransformer {

    private static final Pattern CHARGE_CODE_PATTERN = Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{3}$");
    private static final Pattern CLAIM_NUMBER_PATTERN = Pattern.compile("^CLM-\\d{8}-\\d{4}$");
    private static final Pattern CPT_CODE_PATTERN = Pattern.compile("^\\d{5}$");
    private static final Pattern ICD_CODE_PATTERN = Pattern.compile("^\\w\\d{2}\\.?\\d*$");

    // Healthcare financial mappings
    private static final Map<String, String> CHARGE_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> INSURANCE_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> BILLING_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> PROCEDURE_RATES = new HashMap<>();

    static {
        // Charge type mappings
        CHARGE_TYPE_MAPPINGS.put("PROCEDURE", "Procedure Charge - Medical service performed");
        CHARGE_TYPE_MAPPINGS.put("PHARMACY", "Pharmacy Charge - Medication dispensing");
        CHARGE_TYPE_MAPPINGS.put("LABORATORY", "Laboratory Charge - Diagnostic testing");
        CHARGE_TYPE_MAPPINGS.put("RADIOLOGY", "Radiology Charge - Imaging services");
        CHARGE_TYPE_MAPPINGS.put("EMERGENCY", "Emergency Charge - Emergency department services");
        CHARGE_TYPE_MAPPINGS.put("INPATIENT", "Inpatient Charge - Hospital admission");
        CHARGE_TYPE_MAPPINGS.put("OUTPATIENT", "Outpatient Charge - Clinic visit");

        // Insurance type mappings
        INSURANCE_TYPE_MAPPINGS.put("MEDICARE", "Medicare - Federal health insurance");
        INSURANCE_TYPE_MAPPINGS.put("MEDICAID", "Medicaid - State health insurance");
        INSURANCE_TYPE_MAPPINGS.put("COMMERCIAL", "Commercial Insurance - Private health insurance");
        INSURANCE_TYPE_MAPPINGS.put("SELF_PAY", "Self-Pay - Patient responsible");
        INSURANCE_TYPE_MAPPINGS.put("WORKERS_COMP", "Workers Compensation - Occupational injury");

        // Billing status mappings
        BILLING_STATUS_MAPPINGS.put("PENDING", "Pending - Awaiting processing");
        BILLING_STATUS_MAPPINGS.put("SUBMITTED", "Submitted - Sent to payer");
        BILLING_STATUS_MAPPINGS.put("APPROVED", "Approved - Payment authorized");
        BILLING_STATUS_MAPPINGS.put("DENIED", "Denied - Claim rejected");
        BILLING_STATUS_MAPPINGS.put("PAID", "Paid - Payment received");
        BILLING_STATUS_MAPPINGS.put("WRITE_OFF", "Write-off - Uncollectible amount");

        // Sample procedure rates (for demonstration)
        PROCEDURE_RATES.put("99213", new BigDecimal("50.00")); // Office visit
        PROCEDURE_RATES.put("99214", new BigDecimal("110.00")); // Office visit
        PROCEDURE_RATES.put("85025", new BigDecimal("10.00")); // CBC
        PROCEDURE_RATES.put("71045", new BigDecimal("35.00")); // Chest X-ray
    }

    /**
     * Transforms legacy charge code to Workday standard format (####.##.###).
     * @param legacyChargeCode The legacy charge code
     * @return Standardized Workday charge code
     * @throws IllegalArgumentException if charge code format is invalid
     */
    public static String transformChargeCode(String legacyChargeCode) {
        if (!DataValidationUtils.isNotEmpty(legacyChargeCode)) {
            throw new IllegalArgumentException("Charge code cannot be null or empty");
        }

        String cleanCode = legacyChargeCode.trim();

        // Handle different legacy formats
        if (CHARGE_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on charge type
            return generateChargeCodeFromType(cleanCode);
        }
    }

    /**
     * Safely transforms charge code with error handling.
     * @param legacyChargeCode The legacy charge code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday charge code or default
     */
    public static String safeTransformChargeCode(String legacyChargeCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformChargeCode(legacyChargeCode),
            defaultCode,
            "charge code transformation"
        );
    }

    /**
     * Transforms legacy claim number to Workday standard format (CLM-YYYYMMDD-XXXX).
     * @param legacyClaimNumber The legacy claim number
     * @return Standardized Workday claim number
     * @throws IllegalArgumentException if claim number format is invalid
     */
    public static String transformClaimNumber(String legacyClaimNumber) {
        if (!DataValidationUtils.isNotEmpty(legacyClaimNumber)) {
            throw new IllegalArgumentException("Claim number cannot be null or empty");
        }

        String cleanNumber = legacyClaimNumber.trim();

        // Handle different legacy formats
        if (CLAIM_NUMBER_PATTERN.matcher(cleanNumber).matches()) {
            return cleanNumber;
        } else {
            // Convert legacy format to Workday format
            return formatToWorkdayClaimNumber(cleanNumber);
        }
    }

    /**
     * Safely transforms claim number with error handling.
     * @param legacyClaimNumber The legacy claim number
     * @param defaultNumber The default number to return if transformation fails
     * @return Standardized Workday claim number or default
     */
    public static String safeTransformClaimNumber(String legacyClaimNumber, String defaultNumber) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformClaimNumber(legacyClaimNumber),
            defaultNumber,
            "claim number transformation"
        );
    }

    /**
     * Standardizes charge type description.
     * @param legacyChargeType The legacy charge type
     * @return Standardized charge type
     */
    public static String standardizeChargeType(String legacyChargeType) {
        if (!DataValidationUtils.isNotEmpty(legacyChargeType)) {
            return "Unknown Charge Type";
        }

        String cleanType = legacyChargeType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = CHARGE_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("PROCEDURE") || cleanType.contains("PROC")) {
            return CHARGE_TYPE_MAPPINGS.get("PROCEDURE");
        } else if (cleanType.contains("PHARMACY") || cleanType.contains("PHARM") || cleanType.contains("MED")) {
            return CHARGE_TYPE_MAPPINGS.get("PHARMACY");
        } else if (cleanType.contains("LAB") || cleanType.contains("TEST")) {
            return CHARGE_TYPE_MAPPINGS.get("LABORATORY");
        } else if (cleanType.contains("RADIO") || cleanType.contains("XRAY") || cleanType.contains("IMAGING")) {
            return CHARGE_TYPE_MAPPINGS.get("RADIOLOGY");
        } else if (cleanType.contains("EMERG") || cleanType.contains("ER")) {
            return CHARGE_TYPE_MAPPINGS.get("EMERGENCY");
        } else if (cleanType.contains("INPATIENT") || cleanType.contains("HOSPITAL")) {
            return CHARGE_TYPE_MAPPINGS.get("INPATIENT");
        } else if (cleanType.contains("OUTPATIENT") || cleanType.contains("CLINIC")) {
            return CHARGE_TYPE_MAPPINGS.get("OUTPATIENT");
        }

        return cleanType;
    }

    /**
     * Safely standardizes charge type with error handling.
     * @param legacyChargeType The legacy charge type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized charge type or default
     */
    public static String safeStandardizeChargeType(String legacyChargeType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeChargeType(legacyChargeType),
            defaultType,
            "charge type standardization"
        );
    }

    /**
     * Standardizes insurance type description.
     * @param legacyInsuranceType The legacy insurance type
     * @return Standardized insurance type
     */
    public static String standardizeInsuranceType(String legacyInsuranceType) {
        if (!DataValidationUtils.isNotEmpty(legacyInsuranceType)) {
            return "Unknown Insurance Type";
        }

        String cleanType = legacyInsuranceType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = INSURANCE_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("MEDICARE") || cleanType.equals("MC")) {
            return INSURANCE_TYPE_MAPPINGS.get("MEDICARE");
        } else if (cleanType.contains("MEDICAID") || cleanType.equals("MD")) {
            return INSURANCE_TYPE_MAPPINGS.get("MEDICAID");
        } else if (cleanType.contains("COMMERCIAL") || cleanType.contains("PRIVATE") || cleanType.equals("COMM")) {
            return INSURANCE_TYPE_MAPPINGS.get("COMMERCIAL");
        } else if (cleanType.contains("SELF") || cleanType.contains("PATIENT") || cleanType.equals("SP")) {
            return INSURANCE_TYPE_MAPPINGS.get("SELF_PAY");
        } else if (cleanType.contains("WORKERS") || cleanType.contains("COMP") || cleanType.equals("WC")) {
            return INSURANCE_TYPE_MAPPINGS.get("WORKERS_COMP");
        }

        return cleanType;
    }

    /**
     * Calculates patient responsibility amount based on insurance coverage.
     * @param totalCharge The total charge amount
     * @param insuranceCoveragePercentage The insurance coverage percentage (0-100)
     * @param deductibleRemaining The remaining deductible amount
     * @return Patient responsibility amount
     */
    public static BigDecimal calculatePatientResponsibility(BigDecimal totalCharge,
                                                          BigDecimal insuranceCoveragePercentage,
                                                          BigDecimal deductibleRemaining) {
        if (totalCharge == null || totalCharge.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (insuranceCoveragePercentage == null) {
            insuranceCoveragePercentage = BigDecimal.ZERO;
        }

        if (deductibleRemaining == null) {
            deductibleRemaining = BigDecimal.ZERO;
        }

        // Calculate insurance payment
        BigDecimal insurancePayment = totalCharge.multiply(insuranceCoveragePercentage.divide(new BigDecimal("100")));

        // Apply deductible
        BigDecimal patientResponsibility = totalCharge.subtract(insurancePayment);
        if (deductibleRemaining.compareTo(BigDecimal.ZERO) > 0) {
            patientResponsibility = patientResponsibility.add(deductibleRemaining.min(patientResponsibility));
        }

        return patientResponsibility.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Validates CPT (Current Procedural Terminology) code format.
     * @param cptCode The CPT code to validate
     * @return true if valid CPT code format, false otherwise
     */
    public static boolean isValidCptCode(String cptCode) {
        return DataValidationUtils.isNotEmpty(cptCode) && CPT_CODE_PATTERN.matcher(cptCode.trim()).matches();
    }

    /**
     * Validates ICD (International Classification of Diseases) code format.
     * @param icdCode The ICD code to validate
     * @return true if valid ICD code format, false otherwise
     */
    public static boolean isValidIcdCode(String icdCode) {
        return DataValidationUtils.isNotEmpty(icdCode) && ICD_CODE_PATTERN.matcher(icdCode.trim()).matches();
    }

    /**
     * Gets standard rate for a CPT procedure code.
     * @param cptCode The CPT code
     * @return Standard rate or null if not found
     */
    public static BigDecimal getProcedureRate(String cptCode) {
        if (!isValidCptCode(cptCode)) {
            return null;
        }
        return PROCEDURE_RATES.get(cptCode.trim());
    }

    /**
     * Determines billing status based on claim processing stage.
     * @param statusDescription The status description
     * @return Standardized billing status
     */
    public static String determineBillingStatus(String statusDescription) {
        if (!DataValidationUtils.isNotEmpty(statusDescription)) {
            return BILLING_STATUS_MAPPINGS.get("PENDING");
        }

        String upperDesc = statusDescription.toUpperCase();

        if (upperDesc.contains("PENDING") || upperDesc.contains("PROCESSING")) {
            return BILLING_STATUS_MAPPINGS.get("PENDING");
        } else if (upperDesc.contains("SUBMIT") || upperDesc.contains("SENT")) {
            return BILLING_STATUS_MAPPINGS.get("SUBMITTED");
        } else if (upperDesc.contains("APPROVE") || upperDesc.contains("AUTH")) {
            return BILLING_STATUS_MAPPINGS.get("APPROVED");
        } else if (upperDesc.contains("DENY") || upperDesc.contains("REJECT")) {
            return BILLING_STATUS_MAPPINGS.get("DENIED");
        } else if (upperDesc.contains("PAID") || upperDesc.contains("PAYMENT")) {
            return BILLING_STATUS_MAPPINGS.get("PAID");
        } else if (upperDesc.contains("WRITE") || upperDesc.contains("UNCOLLECT")) {
            return BILLING_STATUS_MAPPINGS.get("WRITE_OFF");
        }

        return BILLING_STATUS_MAPPINGS.get("PENDING");
    }

    /**
     * Validates if a charge code is in proper Workday format.
     * @param chargeCode The charge code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidChargeCode(String chargeCode) {
        return DataValidationUtils.isNotEmpty(chargeCode) && CHARGE_CODE_PATTERN.matcher(chargeCode.trim()).matches();
    }

    /**
     * Validates if a claim number is in proper Workday format.
     * @param claimNumber The claim number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidClaimNumber(String claimNumber) {
        return DataValidationUtils.isNotEmpty(claimNumber) && CLAIM_NUMBER_PATTERN.matcher(claimNumber.trim()).matches();
    }

    /**
     * Generates a healthcare financial summary for reporting purposes.
     * @param chargeCode The charge code
     * @param claimNumber The claim number
     * @param chargeType The charge type
     * @param insuranceType The insurance type
     * @param totalCharge The total charge amount
     * @param patientResponsibility The patient responsibility amount
     * @return Formatted healthcare financial summary
     */
    public static String generateHealthcareFinancialSummary(String chargeCode, String claimNumber,
                                                          String chargeType, String insuranceType,
                                                          BigDecimal totalCharge, BigDecimal patientResponsibility) {
        StringBuilder summary = new StringBuilder();
        summary.append("Charge Code: ").append(transformChargeCode(chargeCode)).append("\n");
        summary.append("Claim Number: ").append(transformClaimNumber(claimNumber)).append("\n");
        summary.append("Charge Type: ").append(standardizeChargeType(chargeType)).append("\n");
        summary.append("Insurance Type: ").append(standardizeInsuranceType(insuranceType)).append("\n");
        summary.append("Total Charge: ").append(totalCharge != null ? totalCharge.toString() : "0.00").append("\n");
        summary.append("Patient Responsibility: ").append(patientResponsibility != null ? patientResponsibility.toString() : "0.00").append("\n");
        summary.append("Valid Charge Code: ").append(isValidChargeCode(chargeCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Claim Number: ").append(isValidClaimNumber(claimNumber) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a charge code based on charge type.
     * @param chargeType The charge type description
     * @return Generated charge code
     */
    private static String generateChargeCodeFromType(String chargeType) {
        String upperType = chargeType.toUpperCase();

        if (upperType.contains("PROCEDURE") || upperType.contains("PROC")) {
            return "1000.01.001";
        } else if (upperType.contains("PHARMACY") || upperType.contains("PHARM")) {
            return "2000.02.001";
        } else if (upperType.contains("LABORATORY") || upperType.contains("LAB")) {
            return "3000.03.001";
        } else if (upperType.contains("RADIOLOGY") || upperType.contains("RADIO")) {
            return "4000.04.001";
        } else if (upperType.contains("EMERGENCY") || upperType.contains("ER")) {
            return "5000.05.001";
        } else if (upperType.contains("INPATIENT")) {
            return "6000.06.001";
        } else if (upperType.contains("OUTPATIENT")) {
            return "7000.07.001";
        } else {
            return "0000.00.001"; // Generic charge
        }
    }

    /**
     * Formats a legacy claim number to Workday format.
     * @param legacyNumber The legacy claim number
     * @return Formatted Workday claim number
     */
    private static String formatToWorkdayClaimNumber(String legacyNumber) {
        String numericNumber = legacyNumber.replaceAll("[^0-9]", "");
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", Integer.parseInt(numericNumber.substring(Math.max(0, numericNumber.length() - 4))));
        return "CLM-" + datePart + "-" + sequencePart;
    }

    // Copy-paste usage examples:
    // String chargeCode = HealthcareFinanceTransformer.transformChargeCode(legacyCode);
    // String claimNumber = HealthcareFinanceTransformer.transformClaimNumber(legacyNumber);
    // String chargeType = HealthcareFinanceTransformer.standardizeChargeType(legacyType);
    // String insuranceType = HealthcareFinanceTransformer.standardizeInsuranceType(legacyInsurance);
    // BigDecimal patientResp = HealthcareFinanceTransformer.calculatePatientResponsibility(totalCharge, coverage, deductible);
    // boolean validCpt = HealthcareFinanceTransformer.isValidCptCode(cptCode);
    // String summary = HealthcareFinanceTransformer.generateHealthcareFinancialSummary(code, claim, type, insurance, charge, responsibility);
}