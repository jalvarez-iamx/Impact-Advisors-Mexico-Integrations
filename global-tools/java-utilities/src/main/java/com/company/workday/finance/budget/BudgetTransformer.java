package com.company.workday.finance.budget;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for transforming budget data from legacy systems to Workday format.
 * Handles budget versions, allocations, variance calculations, and financial planning.
 * Supports budget tracking, forecasting, and financial control mechanisms.
 */
public class BudgetTransformer {

    private static final Pattern BUDGET_CODE_PATTERN = Pattern.compile("^BUD-\\d{4}-\\w{3}$");
    private static final Pattern LEGACY_BUDGET_PATTERN = Pattern.compile("^\\d{6,8}$");

    // Budget type mappings
    private static final Map<String, String> BUDGET_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> BUDGET_PERIOD_MAPPINGS = new HashMap<>();
    private static final Map<String, String> VARIANCE_THRESHOLD_MAPPINGS = new HashMap<>();

    static {
        // Budget type mappings
        BUDGET_TYPE_MAPPINGS.put("OPERATING", "Operating Budget - Core business operations");
        BUDGET_TYPE_MAPPINGS.put("CAPITAL", "Capital Budget - Long-term asset investments");
        BUDGET_TYPE_MAPPINGS.put("DEPARTMENTAL", "Departmental Budget - Department-specific allocations");
        BUDGET_TYPE_MAPPINGS.put("PROJECT", "Project Budget - Project-specific funding");
        BUDGET_TYPE_MAPPINGS.put("PERSONNEL", "Personnel Budget - Staffing and compensation");

        // Budget period mappings
        BUDGET_PERIOD_MAPPINGS.put("ANNUAL", "Annual Budget - Full year planning");
        BUDGET_PERIOD_MAPPINGS.put("QUARTERLY", "Quarterly Budget - 3-month cycles");
        BUDGET_PERIOD_MAPPINGS.put("MONTHLY", "Monthly Budget - Monthly planning");
        BUDGET_PERIOD_MAPPINGS.put("WEEKLY", "Weekly Budget - Weekly tracking");

        // Variance threshold mappings
        VARIANCE_THRESHOLD_MAPPINGS.put("LOW", "Low Variance - Within 5%");
        VARIANCE_THRESHOLD_MAPPINGS.put("MEDIUM", "Medium Variance - 5-10% deviation");
        VARIANCE_THRESHOLD_MAPPINGS.put("HIGH", "High Variance - 10-20% deviation");
        VARIANCE_THRESHOLD_MAPPINGS.put("CRITICAL", "Critical Variance - Over 20% deviation");
    }

    /**
     * Transforms legacy budget code to Workday standard format (BUD-YYYY-XXX).
     * @param legacyBudgetCode The legacy budget code
     * @return Standardized Workday budget code
     * @throws IllegalArgumentException if budget code format is invalid
     */
    public static String transformBudgetCode(String legacyBudgetCode) {
        if (!DataValidationUtils.isNotEmpty(legacyBudgetCode)) {
            throw new IllegalArgumentException("Budget code cannot be null or empty");
        }

        String cleanCode = legacyBudgetCode.trim();

        // Handle different legacy formats
        if (BUDGET_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else if (LEGACY_BUDGET_PATTERN.matcher(cleanCode.replaceAll("[^0-9]", "")).matches()) {
            // Convert legacy numeric format to Workday format
            return formatToWorkdayBudgetCode(cleanCode);
        } else {
            throw new IllegalArgumentException("Invalid budget code format: " + legacyBudgetCode);
        }
    }

    /**
     * Safely transforms budget code with error handling.
     * @param legacyBudgetCode The legacy budget code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday budget code or default
     */
    public static String safeTransformBudgetCode(String legacyBudgetCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformBudgetCode(legacyBudgetCode),
            defaultCode,
            "budget code transformation"
        );
    }

    /**
     * Standardizes budget type description.
     * @param legacyBudgetType The legacy budget type
     * @return Standardized budget type
     */
    public static String standardizeBudgetType(String legacyBudgetType) {
        if (!DataValidationUtils.isNotEmpty(legacyBudgetType)) {
            return "Unknown Budget Type";
        }

        String cleanType = legacyBudgetType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = BUDGET_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("OPERAT") || cleanType.equals("OP")) {
            return BUDGET_TYPE_MAPPINGS.get("OPERATING");
        } else if (cleanType.contains("CAPITAL") || cleanType.equals("CAP")) {
            return BUDGET_TYPE_MAPPINGS.get("CAPITAL");
        } else if (cleanType.contains("DEPARTMENT") || cleanType.contains("DEPT")) {
            return BUDGET_TYPE_MAPPINGS.get("DEPARTMENTAL");
        } else if (cleanType.contains("PROJECT") || cleanType.equals("PROJ")) {
            return BUDGET_TYPE_MAPPINGS.get("PROJECT");
        } else if (cleanType.contains("PERSONNEL") || cleanType.contains("STAFF") || cleanType.contains("HR")) {
            return BUDGET_TYPE_MAPPINGS.get("PERSONNEL");
        }

        return cleanType;
    }

    /**
     * Safely standardizes budget type with error handling.
     * @param legacyBudgetType The legacy budget type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized budget type or default
     */
    public static String safeStandardizeBudgetType(String legacyBudgetType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeBudgetType(legacyBudgetType),
            defaultType,
            "budget type standardization"
        );
    }

    /**
     * Calculates budget variance between budgeted and actual amounts.
     * @param budgetedAmount The budgeted amount
     * @param actualAmount The actual amount
     * @return Map containing variance amount and percentage
     */
    public static Map<String, BigDecimal> calculateBudgetVariance(BigDecimal budgetedAmount, BigDecimal actualAmount) {
        Map<String, BigDecimal> variance = new HashMap<>();

        if (budgetedAmount == null || actualAmount == null || budgetedAmount.compareTo(BigDecimal.ZERO) == 0) {
            variance.put("varianceAmount", BigDecimal.ZERO);
            variance.put("variancePercentage", BigDecimal.ZERO);
            return variance;
        }

        BigDecimal varianceAmount = actualAmount.subtract(budgetedAmount);
        BigDecimal variancePercentage = varianceAmount.divide(budgetedAmount, 4, BigDecimal.ROUND_HALF_UP)
                                                     .multiply(new BigDecimal("100"));

        variance.put("varianceAmount", varianceAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
        variance.put("variancePercentage", variancePercentage.setScale(2, BigDecimal.ROUND_HALF_UP));

        return variance;
    }

    /**
     * Determines variance threshold level based on percentage.
     * @param variancePercentage The variance percentage
     * @return Variance threshold level
     */
    public static String determineVarianceThreshold(BigDecimal variancePercentage) {
        if (variancePercentage == null) {
            return "Unknown";
        }

        BigDecimal absPercentage = variancePercentage.abs();

        if (absPercentage.compareTo(new BigDecimal("5")) <= 0) {
            return VARIANCE_THRESHOLD_MAPPINGS.get("LOW");
        } else if (absPercentage.compareTo(new BigDecimal("10")) <= 0) {
            return VARIANCE_THRESHOLD_MAPPINGS.get("MEDIUM");
        } else if (absPercentage.compareTo(new BigDecimal("20")) <= 0) {
            return VARIANCE_THRESHOLD_MAPPINGS.get("HIGH");
        } else {
            return VARIANCE_THRESHOLD_MAPPINGS.get("CRITICAL");
        }
    }

    /**
     * Calculates budget utilization percentage.
     * @param actualAmount The actual spent/used amount
     * @param budgetedAmount The total budgeted amount
     * @return Utilization percentage (0-100)
     */
    public static BigDecimal calculateBudgetUtilization(BigDecimal actualAmount, BigDecimal budgetedAmount) {
        if (budgetedAmount == null || budgetedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (actualAmount == null) {
            actualAmount = BigDecimal.ZERO;
        }

        return actualAmount.divide(budgetedAmount, 4, BigDecimal.ROUND_HALF_UP)
                          .multiply(new BigDecimal("100"))
                          .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Validates budget period and returns standardized period type.
     * @param periodType The budget period type
     * @return Standardized budget period
     */
    public static String standardizeBudgetPeriod(String periodType) {
        if (!DataValidationUtils.isNotEmpty(periodType)) {
            return BUDGET_PERIOD_MAPPINGS.get("ANNUAL"); // Default to annual
        }

        String cleanPeriod = periodType.trim().toUpperCase();

        // Check for exact mappings
        String mappedPeriod = BUDGET_PERIOD_MAPPINGS.get(cleanPeriod);
        if (mappedPeriod != null) {
            return mappedPeriod;
        }

        // Handle common variations
        if (cleanPeriod.contains("YEAR") || cleanPeriod.equals("YR")) {
            return BUDGET_PERIOD_MAPPINGS.get("ANNUAL");
        } else if (cleanPeriod.contains("QUARTER") || cleanPeriod.equals("QTR")) {
            return BUDGET_PERIOD_MAPPINGS.get("QUARTERLY");
        } else if (cleanPeriod.contains("MONTH") || cleanPeriod.equals("MO")) {
            return BUDGET_PERIOD_MAPPINGS.get("MONTHLY");
        } else if (cleanPeriod.contains("WEEK") || cleanPeriod.equals("WK")) {
            return BUDGET_PERIOD_MAPPINGS.get("WEEKLY");
        }

        return BUDGET_PERIOD_MAPPINGS.get("ANNUAL"); // Default fallback
    }

    /**
     * Validates if a budget code is in proper Workday format.
     * @param budgetCode The budget code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBudgetCode(String budgetCode) {
        return DataValidationUtils.isNotEmpty(budgetCode) && BUDGET_CODE_PATTERN.matcher(budgetCode.trim()).matches();
    }

    /**
     * Generates a budget summary for reporting purposes.
     * @param budgetCode The budget code
     * @param budgetType The budget type
     * @param budgetedAmount The budgeted amount
     * @param actualAmount The actual amount
     * @param periodType The budget period
     * @return Formatted budget summary
     */
    public static String generateBudgetSummary(String budgetCode, String budgetType,
                                             BigDecimal budgetedAmount, BigDecimal actualAmount, String periodType) {
        StringBuilder summary = new StringBuilder();
        summary.append("Budget Code: ").append(transformBudgetCode(budgetCode)).append("\n");
        summary.append("Type: ").append(standardizeBudgetType(budgetType)).append("\n");
        summary.append("Period: ").append(standardizeBudgetPeriod(periodType)).append("\n");
        summary.append("Budgeted: ").append(budgetedAmount != null ? budgetedAmount.toString() : "0.00").append("\n");
        summary.append("Actual: ").append(actualAmount != null ? actualAmount.toString() : "0.00").append("\n");

        Map<String, BigDecimal> variance = calculateBudgetVariance(budgetedAmount, actualAmount);
        summary.append("Variance Amount: ").append(variance.get("varianceAmount")).append("\n");
        summary.append("Variance %: ").append(variance.get("variancePercentage")).append("%\n");
        summary.append("Variance Level: ").append(determineVarianceThreshold(variance.get("variancePercentage"))).append("\n");

        BigDecimal utilization = calculateBudgetUtilization(actualAmount, budgetedAmount);
        summary.append("Utilization: ").append(utilization).append("%\n");
        summary.append("Valid Code: ").append(isValidBudgetCode(budgetCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Formats a legacy budget code to Workday format.
     * @param legacyCode The legacy budget code
     * @return Formatted Workday budget code
     */
    private static String formatToWorkdayBudgetCode(String legacyCode) {
        String numericCode = legacyCode.replaceAll("[^0-9]", "");
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String suffix = String.format("%03d", Integer.parseInt(numericCode.substring(Math.max(0, numericCode.length() - 3))));
        return "BUD-" + year + "-" + suffix;
    }

    // Copy-paste usage examples:
    // String budgetCode = BudgetTransformer.transformBudgetCode(legacyCode);
    // String budgetType = BudgetTransformer.standardizeBudgetType(legacyType);
    // Map<String, BigDecimal> variance = BudgetTransformer.calculateBudgetVariance(budgeted, actual);
    // String threshold = BudgetTransformer.determineVarianceThreshold(variancePercentage);
    // BigDecimal utilization = BudgetTransformer.calculateBudgetUtilization(actual, budgeted);
    // String summary = BudgetTransformer.generateBudgetSummary(code, type, budgeted, actual, period);
}