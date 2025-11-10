package com.company.workday.payroll.deductions;

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
 * Utility class for transforming payroll deductions data from legacy systems to Workday format.
 * Handles taxes, benefits deductions, garnishments, and deduction calculations specific to healthcare organizations.
 * Supports HIPAA compliance, healthcare deduction standards, and deduction processing.
 */
public class PayrollDeductionsUtilities {

    private static final Pattern DEDUCTION_CODE_PATTERN = Pattern.compile("^DED-\\d{4}-\\d{3}$");
    private static final Pattern GARNISHMENT_CODE_PATTERN = Pattern.compile("^GAR-\\d{8}-\\d{4}$");
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?%$");

    // Healthcare deduction mappings
    private static final Map<String, String> DEDUCTION_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> GARNISHMENT_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> STANDARD_DEDUCTION_RATES = new HashMap<>();
    private static final Map<String, BigDecimal> BENEFIT_DEDUCTION_AMOUNTS = new HashMap<>();

    static {
        // Deduction type mappings
        DEDUCTION_TYPE_MAPPINGS.put("FEDERAL_TAX", "Federal Income Tax - Federal withholding");
        DEDUCTION_TYPE_MAPPINGS.put("STATE_TAX", "State Income Tax - State withholding");
        DEDUCTION_TYPE_MAPPINGS.put("SOCIAL_SECURITY", "Social Security - FICA withholding");
        DEDUCTION_TYPE_MAPPINGS.put("MEDICARE", "Medicare - FICA withholding");
        DEDUCTION_TYPE_MAPPINGS.put("HEALTH_INSURANCE", "Health Insurance Premium - Medical coverage");
        DEDUCTION_TYPE_MAPPINGS.put("DENTAL_INSURANCE", "Dental Insurance Premium - Dental coverage");
        DEDUCTION_TYPE_MAPPINGS.put("VISION_INSURANCE", "Vision Insurance Premium - Vision coverage");
        DEDUCTION_TYPE_MAPPINGS.put("RETIREMENT_401K", "401(k) Contribution - Retirement savings");
        DEDUCTION_TYPE_MAPPINGS.put("RETIREMENT_403B", "403(b) Contribution - Retirement savings");
        DEDUCTION_TYPE_MAPPINGS.put("LIFE_INSURANCE", "Life Insurance Premium - Life coverage");
        DEDUCTION_TYPE_MAPPINGS.put("DISABILITY_INSURANCE", "Disability Insurance Premium - Disability coverage");
        DEDUCTION_TYPE_MAPPINGS.put("GARNISHMENT", "Wage Garnishment - Court-ordered deduction");

        // Garnishment type mappings
        GARNISHMENT_TYPE_MAPPINGS.put("CHILD_SUPPORT", "Child Support - Court-ordered child support");
        GARNISHMENT_TYPE_MAPPINGS.put("ALIMONY", "Alimony - Court-ordered spousal support");
        GARNISHMENT_TYPE_MAPPINGS.put("STUDENT_LOAN", "Student Loan - Federal student loan repayment");
        GARNISHMENT_TYPE_MAPPINGS.put("TAX_LEVY", "Tax Levy - IRS tax debt collection");
        GARNISHMENT_TYPE_MAPPINGS.put("BANKRUPTCY", "Bankruptcy - Court-ordered bankruptcy payment");

        // Standard deduction rates (as percentages)
        STANDARD_DEDUCTION_RATES.put("FEDERAL_TAX", new BigDecimal("22.00")); // 22% effective rate
        STANDARD_DEDUCTION_RATES.put("STATE_TAX", new BigDecimal("5.00")); // 5% average state rate
        STANDARD_DEDUCTION_RATES.put("SOCIAL_SECURITY", new BigDecimal("6.20")); // 6.2% FICA
        STANDARD_DEDUCTION_RATES.put("MEDICARE", new BigDecimal("1.45")); // 1.45% FICA
        STANDARD_DEDUCTION_RATES.put("RETIREMENT_401K", new BigDecimal("6.00")); // 6% employee contribution

        // Monthly benefit deduction amounts
        BENEFIT_DEDUCTION_AMOUNTS.put("HEALTH_INSURANCE_EE", new BigDecimal("450.00")); // Employee only
        BENEFIT_DEDUCTION_AMOUNTS.put("HEALTH_INSURANCE_ES", new BigDecimal("810.00")); // Employee + spouse
        BENEFIT_DEDUCTION_AMOUNTS.put("HEALTH_INSURANCE_EF", new BigDecimal("1200.00")); // Employee + family
        BENEFIT_DEDUCTION_AMOUNTS.put("DENTAL_INSURANCE_EE", new BigDecimal("35.00"));
        BENEFIT_DEDUCTION_AMOUNTS.put("VISION_INSURANCE_EE", new BigDecimal("15.00"));
        BENEFIT_DEDUCTION_AMOUNTS.put("LIFE_INSURANCE_50K", new BigDecimal("25.00"));
        BENEFIT_DEDUCTION_AMOUNTS.put("DISABILITY_SHORT_TERM", new BigDecimal("45.00"));
    }

    /**
     * Transforms legacy deduction code to Workday standard format (DED-YYYY-XXX).
     * @param legacyDeductionCode The legacy deduction code
     * @return Standardized Workday deduction code
     * @throws IllegalArgumentException if deduction code format is invalid
     */
    public static String transformDeductionCode(String legacyDeductionCode) {
        if (!DataValidationUtils.isNotEmpty(legacyDeductionCode)) {
            throw new IllegalArgumentException("Deduction code cannot be null or empty");
        }

        String cleanCode = legacyDeductionCode.trim();

        // Handle different legacy formats
        if (DEDUCTION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on deduction type
            return generateDeductionCodeFromType(cleanCode);
        }
    }

    /**
     * Safely transforms deduction code with error handling.
     * @param legacyDeductionCode The legacy deduction code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday deduction code or default
     */
    public static String safeTransformDeductionCode(String legacyDeductionCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformDeductionCode(legacyDeductionCode),
            defaultCode,
            "deduction code transformation"
        );
    }

    /**
     * Transforms legacy garnishment code to Workday standard format (GAR-YYYYMMDD-XXXX).
     * @param legacyGarnishmentCode The legacy garnishment code
     * @return Standardized Workday garnishment code
     * @throws IllegalArgumentException if garnishment code format is invalid
     */
    public static String transformGarnishmentCode(String legacyGarnishmentCode) {
        if (!DataValidationUtils.isNotEmpty(legacyGarnishmentCode)) {
            throw new IllegalArgumentException("Garnishment code cannot be null or empty");
        }

        String cleanCode = legacyGarnishmentCode.trim();

        // Handle different legacy formats
        if (GARNISHMENT_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateGarnishmentCode();
        }
    }

    /**
     * Safely transforms garnishment code with error handling.
     * @param legacyGarnishmentCode The legacy garnishment code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday garnishment code or default
     */
    public static String safeTransformGarnishmentCode(String legacyGarnishmentCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformGarnishmentCode(legacyGarnishmentCode),
            defaultCode,
            "garnishment code transformation"
        );
    }

    /**
     * Standardizes deduction type description.
     * @param legacyDeductionType The legacy deduction type
     * @return Standardized deduction type
     */
    public static String standardizeDeductionType(String legacyDeductionType) {
        if (!DataValidationUtils.isNotEmpty(legacyDeductionType)) {
            return "Unknown Deduction Type";
        }

        String cleanType = legacyDeductionType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = DEDUCTION_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("FEDERAL") || cleanType.contains("FIT")) {
            return DEDUCTION_TYPE_MAPPINGS.get("FEDERAL_TAX");
        } else if (cleanType.contains("STATE") || cleanType.contains("SIT")) {
            return DEDUCTION_TYPE_MAPPINGS.get("STATE_TAX");
        } else if (cleanType.contains("SOCIAL") || cleanType.contains("SS")) {
            return DEDUCTION_TYPE_MAPPINGS.get("SOCIAL_SECURITY");
        } else if (cleanType.contains("MEDICARE") || cleanType.contains("MEDI")) {
            return DEDUCTION_TYPE_MAPPINGS.get("MEDICARE");
        } else if (cleanType.contains("HEALTH") || cleanType.contains("MEDICAL")) {
            return DEDUCTION_TYPE_MAPPINGS.get("HEALTH_INSURANCE");
        } else if (cleanType.contains("DENTAL")) {
            return DEDUCTION_TYPE_MAPPINGS.get("DENTAL_INSURANCE");
        } else if (cleanType.contains("VISION")) {
            return DEDUCTION_TYPE_MAPPINGS.get("VISION_INSURANCE");
        } else if (cleanType.contains("401K")) {
            return DEDUCTION_TYPE_MAPPINGS.get("RETIREMENT_401K");
        } else if (cleanType.contains("403B")) {
            return DEDUCTION_TYPE_MAPPINGS.get("RETIREMENT_403B");
        } else if (cleanType.contains("LIFE")) {
            return DEDUCTION_TYPE_MAPPINGS.get("LIFE_INSURANCE");
        } else if (cleanType.contains("DISABILITY")) {
            return DEDUCTION_TYPE_MAPPINGS.get("DISABILITY_INSURANCE");
        } else if (cleanType.contains("GARNISH")) {
            return DEDUCTION_TYPE_MAPPINGS.get("GARNISHMENT");
        }

        return cleanType;
    }

    /**
     * Safely standardizes deduction type with error handling.
     * @param legacyDeductionType The legacy deduction type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized deduction type or default
     */
    public static String safeStandardizeDeductionType(String legacyDeductionType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeDeductionType(legacyDeductionType),
            defaultType,
            "deduction type standardization"
        );
    }

    /**
     * Calculates tax deduction amount based on gross pay and tax rate.
     * @param grossPay The gross pay amount
     * @param taxRate The tax rate as percentage (0-100)
     * @param taxType The type of tax (FEDERAL_TAX, STATE_TAX, etc.)
     * @return Tax deduction amount
     */
    public static BigDecimal calculateTaxDeduction(BigDecimal grossPay, BigDecimal taxRate, String taxType) {
        if (grossPay == null || grossPay.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Use standard rate if not provided
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            taxRate = STANDARD_DEDUCTION_RATES.getOrDefault(taxType, BigDecimal.ZERO);
        }

        BigDecimal deductionAmount = grossPay.multiply(taxRate.divide(new BigDecimal("100")));
        return deductionAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates benefit deduction amount based on coverage level.
     * @param benefitType The benefit type (HEALTH_INSURANCE, DENTAL_INSURANCE, etc.)
     * @param coverageLevel The coverage level (EE, ES, EF)
     * @return Monthly benefit deduction amount
     */
    public static BigDecimal calculateBenefitDeduction(String benefitType, String coverageLevel) {
        if (!DataValidationUtils.isNotEmpty(benefitType)) {
            return BigDecimal.ZERO;
        }

        String key = benefitType.toUpperCase() + "_" + (coverageLevel != null ? coverageLevel.toUpperCase() : "EE");
        return BENEFIT_DEDUCTION_AMOUNTS.getOrDefault(key, BigDecimal.ZERO);
    }

    /**
     * Calculates garnishment amount based on disposable income and garnishment type.
     * @param disposableIncome The disposable income amount
     * @param garnishmentType The type of garnishment
     * @param percentage The garnishment percentage (0-100)
     * @return Garnishment deduction amount
     */
    public static BigDecimal calculateGarnishmentAmount(BigDecimal disposableIncome, String garnishmentType, BigDecimal percentage) {
        if (disposableIncome == null || disposableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
            // Default percentages based on garnishment type
            switch (garnishmentType != null ? garnishmentType.toUpperCase() : "") {
                case "CHILD_SUPPORT":
                    percentage = new BigDecimal("20.00"); // Up to 20% for child support
                    break;
                case "ALIMONY":
                    percentage = new BigDecimal("10.00"); // Up to 10% for alimony
                    break;
                case "STUDENT_LOAN":
                    percentage = new BigDecimal("15.00"); // Up to 15% for student loans
                    break;
                default:
                    percentage = new BigDecimal("25.00"); // Federal limit
            }
        }

        BigDecimal garnishmentAmount = disposableIncome.multiply(percentage.divide(new BigDecimal("100")));
        return garnishmentAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Gets standard deduction rate for a tax type.
     * @param taxType The tax type (FEDERAL_TAX, STATE_TAX, etc.)
     * @return Standard deduction rate as percentage or null if not found
     */
    public static BigDecimal getStandardDeductionRate(String taxType) {
        if (!DataValidationUtils.isNotEmpty(taxType)) {
            return null;
        }
        return STANDARD_DEDUCTION_RATES.get(taxType.trim().toUpperCase());
    }

    /**
     * Validates if a deduction code is in proper Workday format.
     * @param deductionCode The deduction code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDeductionCode(String deductionCode) {
        return DataValidationUtils.isNotEmpty(deductionCode) && DEDUCTION_CODE_PATTERN.matcher(deductionCode.trim()).matches();
    }

    /**
     * Validates if a garnishment code is in proper Workday format.
     * @param garnishmentCode The garnishment code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGarnishmentCode(String garnishmentCode) {
        return DataValidationUtils.isNotEmpty(garnishmentCode) && GARNISHMENT_CODE_PATTERN.matcher(garnishmentCode.trim()).matches();
    }

    /**
     * Validates percentage format.
     * @param percentage The percentage to validate
     * @return true if valid percentage format, false otherwise
     */
    public static boolean isValidPercentage(String percentage) {
        return DataValidationUtils.isNotEmpty(percentage) && PERCENTAGE_PATTERN.matcher(percentage.trim()).matches();
    }

    /**
     * Generates a deductions summary for reporting purposes.
     * @param deductionCode The deduction code
     * @param deductionType The deduction type
     * @param grossPay The gross pay amount
     * @param totalDeductions The total deductions amount
     * @param netPay The net pay amount
     * @return Formatted deductions summary
     */
    public static String generateDeductionsSummary(String deductionCode, String deductionType,
                                                 BigDecimal grossPay, BigDecimal totalDeductions,
                                                 BigDecimal netPay) {
        StringBuilder summary = new StringBuilder();
        summary.append("Deduction Code: ").append(transformDeductionCode(deductionCode)).append("\n");
        summary.append("Deduction Type: ").append(standardizeDeductionType(deductionType)).append("\n");
        summary.append("Gross Pay: ").append(grossPay != null ? NumberFormattingUtils.formatCurrency(grossPay.doubleValue()) : "$0.00").append("\n");
        summary.append("Total Deductions: ").append(totalDeductions != null ? NumberFormattingUtils.formatCurrency(totalDeductions.doubleValue()) : "$0.00").append("\n");
        summary.append("Net Pay: ").append(netPay != null ? NumberFormattingUtils.formatCurrency(netPay.doubleValue()) : "$0.00").append("\n");
        summary.append("Deduction Rate: ").append(calculateDeductionRate(grossPay, totalDeductions)).append("\n");
        summary.append("Valid Deduction Code: ").append(isValidDeductionCode(deductionCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Calculates deduction rate as percentage of gross pay.
     * @param grossPay The gross pay
     * @param totalDeductions The total deductions
     * @return Deduction rate formatted as percentage string
     */
    private static String calculateDeductionRate(BigDecimal grossPay, BigDecimal totalDeductions) {
        if (grossPay == null || grossPay.compareTo(BigDecimal.ZERO) <= 0 ||
            totalDeductions == null || totalDeductions.compareTo(BigDecimal.ZERO) <= 0) {
            return "0.00%";
        }

        BigDecimal rate = totalDeductions.divide(grossPay, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        return NumberFormattingUtils.formatPercentage(rate.doubleValue());
    }

    /**
     * Generates a deduction code based on deduction type.
     * @param deductionType The deduction type description
     * @return Generated deduction code
     */
    private static String generateDeductionCodeFromType(String deductionType) {
        String upperType = deductionType.toUpperCase();
        String codeSuffix;

        if (upperType.contains("FEDERAL") || upperType.contains("FIT")) {
            codeSuffix = "001";
        } else if (upperType.contains("STATE") || upperType.contains("SIT")) {
            codeSuffix = "002";
        } else if (upperType.contains("SOCIAL") || upperType.contains("SS")) {
            codeSuffix = "003";
        } else if (upperType.contains("MEDICARE")) {
            codeSuffix = "004";
        } else if (upperType.contains("HEALTH") || upperType.contains("MEDICAL")) {
            codeSuffix = "101";
        } else if (upperType.contains("DENTAL")) {
            codeSuffix = "102";
        } else if (upperType.contains("VISION")) {
            codeSuffix = "103";
        } else if (upperType.contains("RETIREMENT") || upperType.contains("401K") || upperType.contains("403B")) {
            codeSuffix = "201";
        } else if (upperType.contains("LIFE")) {
            codeSuffix = "301";
        } else if (upperType.contains("DISABILITY")) {
            codeSuffix = "302";
        } else if (upperType.contains("GARNISH")) {
            codeSuffix = "401";
        } else {
            codeSuffix = "000";
        }

        LocalDate now = LocalDate.now();
        return String.format("DED-%d-%s", now.getYear(), codeSuffix);
    }

    /**
     * Generates a garnishment code based on current date.
     * @return Generated garnishment code
     */
    private static String generateGarnishmentCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return "GAR-" + datePart + "-" + sequencePart;
    }

    // Copy-paste usage examples:
    // String deductionCode = PayrollDeductionsUtilities.transformDeductionCode(legacyCode);
    // String garnishmentCode = PayrollDeductionsUtilities.transformGarnishmentCode(legacyGarnishmentCode);
    // String deductionType = PayrollDeductionsUtilities.standardizeDeductionType(legacyType);
    // BigDecimal taxDeduction = PayrollDeductionsUtilities.calculateTaxDeduction(grossPay, taxRate, "FEDERAL_TAX");
    // BigDecimal benefitDeduction = PayrollDeductionsUtilities.calculateBenefitDeduction("HEALTH_INSURANCE", "EF");
    // BigDecimal garnishment = PayrollDeductionsUtilities.calculateGarnishmentAmount(disposableIncome, "CHILD_SUPPORT", new BigDecimal("20"));
    // String summary = PayrollDeductionsUtilities.generateDeductionsSummary(code, type, grossPay, totalDeductions, netPay);
}