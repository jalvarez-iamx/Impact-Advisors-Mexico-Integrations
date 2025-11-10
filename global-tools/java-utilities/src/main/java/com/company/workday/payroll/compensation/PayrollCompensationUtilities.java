package com.company.workday.payroll.compensation;

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
 * Utility class for transforming payroll compensation data from legacy systems to Workday format.
 * Handles salary structures, pay rates, bonuses, and compensation calculations specific to healthcare organizations.
 * Supports FLSA compliance, healthcare compensation standards, and pay structure transformations.
 */
public class PayrollCompensationUtilities {

    private static final Pattern SALARY_CODE_PATTERN = Pattern.compile("^SAL-\\d{4}-\\d{2}$");
    private static final Pattern BONUS_CODE_PATTERN = Pattern.compile("^BON-\\d{4}-\\d{3}$");
    private static final Pattern PAY_RATE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    // Healthcare compensation mappings
    private static final Map<String, String> COMPENSATION_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> PAY_FREQUENCY_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> BASE_PAY_RATES = new HashMap<>();
    private static final Map<String, Double> BONUS_MULTIPLIERS = new HashMap<>();

    static {
        // Compensation type mappings
        COMPENSATION_TYPE_MAPPINGS.put("SALARY", "Salary - Fixed annual compensation");
        COMPENSATION_TYPE_MAPPINGS.put("HOURLY", "Hourly - Time-based compensation");
        COMPENSATION_TYPE_MAPPINGS.put("COMMISSION", "Commission - Performance-based compensation");
        COMPENSATION_TYPE_MAPPINGS.put("BONUS", "Bonus - Discretionary compensation");
        COMPENSATION_TYPE_MAPPINGS.put("OVERTIME", "Overtime - Premium time compensation");
        COMPENSATION_TYPE_MAPPINGS.put("SHIFT_DIFFERENTIAL", "Shift Differential - Premium shift compensation");
        COMPENSATION_TYPE_MAPPINGS.put("ON_CALL", "On-Call Pay - Standby compensation");

        // Pay frequency mappings
        PAY_FREQUENCY_MAPPINGS.put("ANNUAL", "Annual - Paid once per year");
        PAY_FREQUENCY_MAPPINGS.put("MONTHLY", "Monthly - Paid monthly");
        PAY_FREQUENCY_MAPPINGS.put("BIWEEKLY", "Biweekly - Paid every two weeks");
        PAY_FREQUENCY_MAPPINGS.put("WEEKLY", "Weekly - Paid weekly");
        PAY_FREQUENCY_MAPPINGS.put("DAILY", "Daily - Paid daily");

        // Base pay rates for healthcare roles (hourly)
        BASE_PAY_RATES.put("RN", new BigDecimal("45.00")); // Registered Nurse
        BASE_PAY_RATES.put("LPN", new BigDecimal("28.00")); // Licensed Practical Nurse
        BASE_PAY_RATES.put("CNA", new BigDecimal("18.00")); // Certified Nursing Assistant
        BASE_PAY_RATES.put("MD", new BigDecimal("120.00")); // Medical Doctor
        BASE_PAY_RATES.put("PA", new BigDecimal("65.00")); // Physician Assistant
        BASE_PAY_RATES.put("TECH", new BigDecimal("22.00")); // Medical Technician

        // Bonus multipliers by performance level
        BONUS_MULTIPLIERS.put("EXCELLENT", 1.5);
        BONUS_MULTIPLIERS.put("GOOD", 1.0);
        BONUS_MULTIPLIERS.put("SATISFACTORY", 0.5);
        BONUS_MULTIPLIERS.put("NEEDS_IMPROVEMENT", 0.0);
    }

    /**
     * Transforms legacy salary code to Workday standard format (SAL-YYYY-MM).
     * @param legacySalaryCode The legacy salary code
     * @return Standardized Workday salary code
     * @throws IllegalArgumentException if salary code format is invalid
     */
    public static String transformSalaryCode(String legacySalaryCode) {
        if (!DataValidationUtils.isNotEmpty(legacySalaryCode)) {
            throw new IllegalArgumentException("Salary code cannot be null or empty");
        }

        String cleanCode = legacySalaryCode.trim();

        // Handle different legacy formats
        if (SALARY_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateSalaryCode();
        }
    }

    /**
     * Safely transforms salary code with error handling.
     * @param legacySalaryCode The legacy salary code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday salary code or default
     */
    public static String safeTransformSalaryCode(String legacySalaryCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformSalaryCode(legacySalaryCode),
            defaultCode,
            "salary code transformation"
        );
    }

    /**
     * Transforms legacy bonus code to Workday standard format (BON-YYYY-XXX).
     * @param legacyBonusCode The legacy bonus code
     * @return Standardized Workday bonus code
     * @throws IllegalArgumentException if bonus code format is invalid
     */
    public static String transformBonusCode(String legacyBonusCode) {
        if (!DataValidationUtils.isNotEmpty(legacyBonusCode)) {
            throw new IllegalArgumentException("Bonus code cannot be null or empty");
        }

        String cleanCode = legacyBonusCode.trim();

        // Handle different legacy formats
        if (BONUS_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on type
            return generateBonusCodeFromType(cleanCode);
        }
    }

    /**
     * Safely transforms bonus code with error handling.
     * @param legacyBonusCode The legacy bonus code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday bonus code or default
     */
    public static String safeTransformBonusCode(String legacyBonusCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformBonusCode(legacyBonusCode),
            defaultCode,
            "bonus code transformation"
        );
    }

    /**
     * Standardizes compensation type description.
     * @param legacyCompensationType The legacy compensation type
     * @return Standardized compensation type
     */
    public static String standardizeCompensationType(String legacyCompensationType) {
        if (!DataValidationUtils.isNotEmpty(legacyCompensationType)) {
            return "Unknown Compensation Type";
        }

        String cleanType = legacyCompensationType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = COMPENSATION_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("SALARY") || cleanType.contains("ANNUAL")) {
            return COMPENSATION_TYPE_MAPPINGS.get("SALARY");
        } else if (cleanType.contains("HOURLY") || cleanType.contains("WAGE")) {
            return COMPENSATION_TYPE_MAPPINGS.get("HOURLY");
        } else if (cleanType.contains("COMM") || cleanType.contains("SALES")) {
            return COMPENSATION_TYPE_MAPPINGS.get("COMMISSION");
        } else if (cleanType.contains("BONUS") || cleanType.contains("INCENTIVE")) {
            return COMPENSATION_TYPE_MAPPINGS.get("BONUS");
        } else if (cleanType.contains("OT") || cleanType.contains("OVERTIME")) {
            return COMPENSATION_TYPE_MAPPINGS.get("OVERTIME");
        } else if (cleanType.contains("SHIFT") || cleanType.contains("DIFFERENTIAL")) {
            return COMPENSATION_TYPE_MAPPINGS.get("SHIFT_DIFFERENTIAL");
        } else if (cleanType.contains("ONCALL") || cleanType.contains("STANDBY")) {
            return COMPENSATION_TYPE_MAPPINGS.get("ON_CALL");
        }

        return cleanType;
    }

    /**
     * Safely standardizes compensation type with error handling.
     * @param legacyCompensationType The legacy compensation type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized compensation type or default
     */
    public static String safeStandardizeCompensationType(String legacyCompensationType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeCompensationType(legacyCompensationType),
            defaultType,
            "compensation type standardization"
        );
    }

    /**
     * Calculates annual salary from hourly rate and standard hours.
     * @param hourlyRate The hourly pay rate
     * @param standardHoursPerWeek Standard hours worked per week
     * @param weeksPerYear Number of weeks worked per year
     * @return Annual salary amount
     */
    public static BigDecimal calculateAnnualSalary(BigDecimal hourlyRate, int standardHoursPerWeek, int weeksPerYear) {
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (standardHoursPerWeek <= 0) {
            standardHoursPerWeek = 40; // Default to 40 hours/week
        }

        if (weeksPerYear <= 0) {
            weeksPerYear = 52; // Default to 52 weeks/year
        }

        BigDecimal annualHours = new BigDecimal(standardHoursPerWeek * weeksPerYear);
        return hourlyRate.multiply(annualHours).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates bonus amount based on base salary and performance multiplier.
     * @param baseSalary The base salary amount
     * @param performanceLevel The performance level (EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT)
     * @param bonusPercentage The bonus percentage (0-100)
     * @return Bonus amount
     */
    public static BigDecimal calculateBonusAmount(BigDecimal baseSalary, String performanceLevel, BigDecimal bonusPercentage) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (bonusPercentage == null || bonusPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        double performanceMultiplier = BONUS_MULTIPLIERS.getOrDefault(
            performanceLevel != null ? performanceLevel.toUpperCase() : "GOOD", 1.0);

        BigDecimal bonusAmount = baseSalary.multiply(bonusPercentage.divide(new BigDecimal("100")));
        bonusAmount = bonusAmount.multiply(new BigDecimal(performanceMultiplier));

        return bonusAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Gets standard pay rate for a healthcare role.
     * @param roleCode The role code (RN, LPN, CNA, MD, PA, TECH)
     * @return Standard hourly pay rate or null if not found
     */
    public static BigDecimal getStandardPayRate(String roleCode) {
        if (!DataValidationUtils.isNotEmpty(roleCode)) {
            return null;
        }
        return BASE_PAY_RATES.get(roleCode.trim().toUpperCase());
    }

    /**
     * Validates if a salary code is in proper Workday format.
     * @param salaryCode The salary code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSalaryCode(String salaryCode) {
        return DataValidationUtils.isNotEmpty(salaryCode) && SALARY_CODE_PATTERN.matcher(salaryCode.trim()).matches();
    }

    /**
     * Validates if a bonus code is in proper Workday format.
     * @param bonusCode The bonus code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBonusCode(String bonusCode) {
        return DataValidationUtils.isNotEmpty(bonusCode) && BONUS_CODE_PATTERN.matcher(bonusCode.trim()).matches();
    }

    /**
     * Validates pay rate format.
     * @param payRate The pay rate to validate
     * @return true if valid pay rate format, false otherwise
     */
    public static boolean isValidPayRate(String payRate) {
        return DataValidationUtils.isNotEmpty(payRate) && PAY_RATE_PATTERN.matcher(payRate.trim()).matches();
    }

    /**
     * Generates a compensation summary for reporting purposes.
     * @param salaryCode The salary code
     * @param compensationType The compensation type
     * @param baseSalary The base salary amount
     * @param bonusAmount The bonus amount
     * @param payFrequency The pay frequency
     * @return Formatted compensation summary
     */
    public static String generateCompensationSummary(String salaryCode, String compensationType,
                                                   BigDecimal baseSalary, BigDecimal bonusAmount,
                                                   String payFrequency) {
        StringBuilder summary = new StringBuilder();
        summary.append("Salary Code: ").append(transformSalaryCode(salaryCode)).append("\n");
        summary.append("Compensation Type: ").append(standardizeCompensationType(compensationType)).append("\n");
        summary.append("Base Salary: ").append(baseSalary != null ? NumberFormattingUtils.formatCurrency(baseSalary.doubleValue()) : "$0.00").append("\n");
        summary.append("Bonus Amount: ").append(bonusAmount != null ? NumberFormattingUtils.formatCurrency(bonusAmount.doubleValue()) : "$0.00").append("\n");
        summary.append("Pay Frequency: ").append(payFrequency != null ? payFrequency : "Monthly").append("\n");
        summary.append("Total Annual Compensation: ").append(calculateTotalAnnualCompensation(baseSalary, bonusAmount)).append("\n");
        summary.append("Valid Salary Code: ").append(isValidSalaryCode(salaryCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Calculates total annual compensation including base salary and bonus.
     * @param baseSalary The base salary
     * @param bonusAmount The bonus amount
     * @return Total annual compensation formatted as currency string
     */
    private static String calculateTotalAnnualCompensation(BigDecimal baseSalary, BigDecimal bonusAmount) {
        BigDecimal total = (baseSalary != null ? baseSalary : BigDecimal.ZERO)
                          .add(bonusAmount != null ? bonusAmount : BigDecimal.ZERO);
        return NumberFormattingUtils.formatCurrency(total.doubleValue());
    }

    /**
     * Generates a salary code based on current date.
     * @return Generated salary code
     */
    private static String generateSalaryCode() {
        LocalDate now = LocalDate.now();
        return String.format("SAL-%d-%02d", now.getYear(), now.getMonthValue());
    }

    /**
     * Generates a bonus code based on bonus type.
     * @param bonusType The bonus type description
     * @return Generated bonus code
     */
    private static String generateBonusCodeFromType(String bonusType) {
        String upperType = bonusType.toUpperCase();
        String codeSuffix;

        if (upperType.contains("PERFORMANCE") || upperType.contains("ANNUAL")) {
            codeSuffix = "001";
        } else if (upperType.contains("SIGNING") || upperType.contains("RETENTION")) {
            codeSuffix = "002";
        } else if (upperType.contains("SPOT") || upperType.contains("SPECIAL")) {
            codeSuffix = "003";
        } else {
            codeSuffix = "000";
        }

        LocalDate now = LocalDate.now();
        return String.format("BON-%d-%s", now.getYear(), codeSuffix);
    }

    // Copy-paste usage examples:
    // String salaryCode = PayrollCompensationUtilities.transformSalaryCode(legacyCode);
    // String bonusCode = PayrollCompensationUtilities.transformBonusCode(legacyBonusCode);
    // String compType = PayrollCompensationUtilities.standardizeCompensationType(legacyType);
    // BigDecimal annualSalary = PayrollCompensationUtilities.calculateAnnualSalary(hourlyRate, 40, 52);
    // BigDecimal bonus = PayrollCompensationUtilities.calculateBonusAmount(baseSalary, "EXCELLENT", new BigDecimal("10"));
    // BigDecimal payRate = PayrollCompensationUtilities.getStandardPayRate("RN");
    // String summary = PayrollCompensationUtilities.generateCompensationSummary(code, type, salary, bonus, frequency);
}