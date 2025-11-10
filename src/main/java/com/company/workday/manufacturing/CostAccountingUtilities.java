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
 * Utility class for transforming cost accounting data from legacy systems to Workday format.
 * Handles standard costs, variance analysis, ABC costing, product costing, and cost rollup.
 * Supports manufacturing cost management and financial analysis.
 */
public class CostAccountingUtilities {

    private static final Pattern STANDARD_COST_CODE_PATTERN = Pattern.compile("^SC-\\d{4}-\\d{6}$");
    private static final Pattern VARIANCE_CODE_PATTERN = Pattern.compile("^VAR-\\d{8}-\\d{4}$");
    private static final Pattern ABC_COST_CODE_PATTERN = Pattern.compile("^ABC-\\d{4}-\\d{4}$");
    private static final Pattern PRODUCT_COST_CODE_PATTERN = Pattern.compile("^PC-\\d{4}-\\d{6}$");
    private static final Pattern COST_ROLLUP_CODE_PATTERN = Pattern.compile("^CR-\\d{4}-\\d{6}$");

    // Cost accounting mappings
    private static final Map<String, String> STANDARD_COST_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> VARIANCE_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> ABC_COST_DRIVER_MAPPINGS = new HashMap<>();
    private static final Map<String, String> PRODUCT_COST_METHOD_MAPPINGS = new HashMap<>();
    private static final Map<String, String> COST_ROLLUP_METHOD_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> VARIANCE_THRESHOLDS = new HashMap<>();
    private static final Map<String, BigDecimal> COST_EFFICIENCY_THRESHOLDS = new HashMap<>();

    static {
        // Standard cost type mappings
        STANDARD_COST_TYPE_MAPPINGS.put("MATERIAL", "Material Cost - Direct material costs");
        STANDARD_COST_TYPE_MAPPINGS.put("LABOR", "Labor Cost - Direct labor costs");
        STANDARD_COST_TYPE_MAPPINGS.put("OVERHEAD", "Overhead Cost - Manufacturing overhead");
        STANDARD_COST_TYPE_MAPPINGS.put("TOTAL", "Total Standard Cost - Complete product cost");

        // Variance type mappings
        VARIANCE_TYPE_MAPPINGS.put("MATERIAL_PRICE", "Material Price Variance - Purchase price differences");
        VARIANCE_TYPE_MAPPINGS.put("MATERIAL_QUANTITY", "Material Quantity Variance - Usage efficiency");
        VARIANCE_TYPE_MAPPINGS.put("LABOR_RATE", "Labor Rate Variance - Wage rate differences");
        VARIANCE_TYPE_MAPPINGS.put("LABOR_EFFICIENCY", "Labor Efficiency Variance - Productivity differences");
        VARIANCE_TYPE_MAPPINGS.put("OVERHEAD_VOLUME", "Overhead Volume Variance - Production volume differences");
        VARIANCE_TYPE_MAPPINGS.put("OVERHEAD_SPENDING", "Overhead Spending Variance - Budget differences");

        // ABC cost driver mappings
        ABC_COST_DRIVER_MAPPINGS.put("UNITS_PRODUCED", "Units Produced - Volume-based driver");
        ABC_COST_DRIVER_MAPPINGS.put("DIRECT_LABOR_HOURS", "Direct Labor Hours - Labor-based driver");
        ABC_COST_DRIVER_MAPPINGS.put("MACHINE_HOURS", "Machine Hours - Equipment-based driver");
        ABC_COST_DRIVER_MAPPINGS.put("SETUP_HOURS", "Setup Hours - Changeover-based driver");
        ABC_COST_DRIVER_MAPPINGS.put("NUMBER_OF_SETUPS", "Number of Setups - Activity-based driver");
        ABC_COST_DRIVER_MAPPINGS.put("NUMBER_OF_INSPECTIONS", "Number of Inspections - Quality-based driver");

        // Product cost method mappings
        PRODUCT_COST_METHOD_MAPPINGS.put("JOB_ORDER", "Job Order Costing - Custom product costing");
        PRODUCT_COST_METHOD_MAPPINGS.put("PROCESS", "Process Costing - Mass production costing");
        PRODUCT_COST_METHOD_MAPPINGS.put("ACTIVITY_BASED", "Activity-Based Costing - Detailed cost allocation");
        PRODUCT_COST_METHOD_MAPPINGS.put("STANDARD", "Standard Costing - Budgeted cost comparison");

        // Cost rollup method mappings
        COST_ROLLUP_METHOD_MAPPINGS.put("BOTTOM_UP", "Bottom-Up Rollup - Component to finished product");
        COST_ROLLUP_METHOD_MAPPINGS.put("TOP_DOWN", "Top-Down Rollup - Product to component allocation");
        COST_ROLLUP_METHOD_MAPPINGS.put("WEIGHTED_AVERAGE", "Weighted Average - Blended cost calculation");

        // Variance thresholds (as percentages)
        VARIANCE_THRESHOLDS.put("ACCEPTABLE", new BigDecimal("5.00")); // <5%
        VARIANCE_THRESHOLDS.put("MONITOR", new BigDecimal("10.00")); // 5-10%
        VARIANCE_THRESHOLDS.put("INVESTIGATE", new BigDecimal("15.00")); // 10-15%
        VARIANCE_THRESHOLDS.put("CRITICAL", new BigDecimal("15.00")); // >15%

        // Cost efficiency thresholds (as percentages above standard)
        COST_EFFICIENCY_THRESHOLDS.put("EXCELLENT", new BigDecimal("95.00")); // <95% of standard
        COST_EFFICIENCY_THRESHOLDS.put("GOOD", new BigDecimal("105.00")); // 95-105% of standard
        COST_EFFICIENCY_THRESHOLDS.put("NEEDS_IMPROVEMENT", new BigDecimal("115.00")); // 105-115% of standard
        COST_EFFICIENCY_THRESHOLDS.put("POOR", new BigDecimal("115.00")); // >115% of standard
    }

    /**
     * Transforms legacy standard cost code to Workday standard format (SC-YYYY-NNNNNN).
     * @param legacyScCode The legacy standard cost code
     * @return Standardized Workday standard cost code
     * @throws IllegalArgumentException if standard cost code format is invalid
     */
    public static String transformStandardCostCode(String legacyScCode) {
        if (!DataValidationUtils.isNotEmpty(legacyScCode)) {
            throw new IllegalArgumentException("Standard cost code cannot be null or empty");
        }

        String cleanCode = legacyScCode.trim();

        // Handle different legacy formats
        if (STANDARD_COST_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateStandardCostCode();
        }
    }

    /**
     * Safely transforms standard cost code with error handling.
     * @param legacyScCode The legacy standard cost code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday standard cost code or default
     */
    public static String safeTransformStandardCostCode(String legacyScCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformStandardCostCode(legacyScCode),
            defaultCode,
            "standard cost code transformation"
        );
    }

    /**
     * Transforms legacy variance code to Workday standard format (VAR-YYYYMMDD-NNNN).
     * @param legacyVarianceCode The legacy variance code
     * @return Standardized Workday variance code
     * @throws IllegalArgumentException if variance code format is invalid
     */
    public static String transformVarianceCode(String legacyVarianceCode) {
        if (!DataValidationUtils.isNotEmpty(legacyVarianceCode)) {
            throw new IllegalArgumentException("Variance code cannot be null or empty");
        }

        String cleanCode = legacyVarianceCode.trim();

        // Handle different legacy formats
        if (VARIANCE_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateVarianceCode();
        }
    }

    /**
     * Safely transforms variance code with error handling.
     * @param legacyVarianceCode The legacy variance code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday variance code or default
     */
    public static String safeTransformVarianceCode(String legacyVarianceCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformVarianceCode(legacyVarianceCode),
            defaultCode,
            "variance code transformation"
        );
    }

    /**
     * Transforms legacy ABC cost code to Workday standard format (ABC-YYYY-NNNN).
     * @param legacyAbcCode The legacy ABC cost code
     * @return Standardized Workday ABC cost code
     * @throws IllegalArgumentException if ABC cost code format is invalid
     */
    public static String transformAbcCostCode(String legacyAbcCode) {
        if (!DataValidationUtils.isNotEmpty(legacyAbcCode)) {
            throw new IllegalArgumentException("ABC cost code cannot be null or empty");
        }

        String cleanCode = legacyAbcCode.trim();

        // Handle different legacy formats
        if (ABC_COST_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateAbcCostCode();
        }
    }

    /**
     * Safely transforms ABC cost code with error handling.
     * @param legacyAbcCode The legacy ABC cost code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday ABC cost code or default
     */
    public static String safeTransformAbcCostCode(String legacyAbcCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformAbcCostCode(legacyAbcCode),
            defaultCode,
            "ABC cost code transformation"
        );
    }

    /**
     * Transforms legacy product cost code to Workday standard format (PC-YYYY-NNNNNN).
     * @param legacyPcCode The legacy product cost code
     * @return Standardized Workday product cost code
     * @throws IllegalArgumentException if product cost code format is invalid
     */
    public static String transformProductCostCode(String legacyPcCode) {
        if (!DataValidationUtils.isNotEmpty(legacyPcCode)) {
            throw new IllegalArgumentException("Product cost code cannot be null or empty");
        }

        String cleanCode = legacyPcCode.trim();

        // Handle different legacy formats
        if (PRODUCT_COST_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateProductCostCode();
        }
    }

    /**
     * Safely transforms product cost code with error handling.
     * @param legacyPcCode The legacy product cost code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday product cost code or default
     */
    public static String safeTransformProductCostCode(String legacyPcCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformProductCostCode(legacyPcCode),
            defaultCode,
            "product cost code transformation"
        );
    }

    /**
     * Transforms legacy cost rollup code to Workday standard format (CR-YYYY-NNNNNN).
     * @param legacyCrCode The legacy cost rollup code
     * @return Standardized Workday cost rollup code
     * @throws IllegalArgumentException if cost rollup code format is invalid
     */
    public static String transformCostRollupCode(String legacyCrCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCrCode)) {
            throw new IllegalArgumentException("Cost rollup code cannot be null or empty");
        }

        String cleanCode = legacyCrCode.trim();

        // Handle different legacy formats
        if (COST_ROLLUP_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCostRollupCode();
        }
    }

    /**
     * Safely transforms cost rollup code with error handling.
     * @param legacyCrCode The legacy cost rollup code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday cost rollup code or default
     */
    public static String safeTransformCostRollupCode(String legacyCrCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCostRollupCode(legacyCrCode),
            defaultCode,
            "cost rollup code transformation"
        );
    }

    /**
     * Calculates material price variance.
     * @param actualQuantity Actual quantity purchased
     * @param actualPrice Actual price per unit
     * @param standardPrice Standard price per unit
     * @return Material price variance (positive = unfavorable, negative = favorable)
     */
    public static BigDecimal calculateMaterialPriceVariance(BigDecimal actualQuantity, BigDecimal actualPrice, BigDecimal standardPrice) {
        if (actualQuantity == null || actualPrice == null || standardPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal variance = actualQuantity.multiply(actualPrice.subtract(standardPrice));

        return variance.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates material quantity variance.
     * @param actualQuantity Actual quantity used
     * @param standardQuantity Standard quantity allowed
     * @param standardPrice Standard price per unit
     * @return Material quantity variance (positive = unfavorable, negative = favorable)
     */
    public static BigDecimal calculateMaterialQuantityVariance(BigDecimal actualQuantity, BigDecimal standardQuantity, BigDecimal standardPrice) {
        if (actualQuantity == null || standardQuantity == null || standardPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal variance = (actualQuantity.subtract(standardQuantity)).multiply(standardPrice);

        return variance.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates labor rate variance.
     * @param actualHours Actual hours worked
     * @param actualRate Actual hourly rate
     * @param standardRate Standard hourly rate
     * @return Labor rate variance (positive = unfavorable, negative = favorable)
     */
    public static BigDecimal calculateLaborRateVariance(BigDecimal actualHours, BigDecimal actualRate, BigDecimal standardRate) {
        if (actualHours == null || actualRate == null || standardRate == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal variance = actualHours.multiply(actualRate.subtract(standardRate));

        return variance.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates labor efficiency variance.
     * @param actualHours Actual hours worked
     * @param standardHours Standard hours allowed
     * @param standardRate Standard hourly rate
     * @return Labor efficiency variance (positive = unfavorable, negative = favorable)
     */
    public static BigDecimal calculateLaborEfficiencyVariance(BigDecimal actualHours, BigDecimal standardHours, BigDecimal standardRate) {
        if (actualHours == null || standardHours == null || standardRate == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal variance = (actualHours.subtract(standardHours)).multiply(standardRate);

        return variance.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates total variance percentage.
     * @param actualCost Actual total cost
     * @param standardCost Standard total cost
     * @return Variance percentage (positive = unfavorable, negative = favorable)
     */
    public static BigDecimal calculateTotalVariancePercentage(BigDecimal actualCost, BigDecimal standardCost) {
        if (actualCost == null || standardCost == null || standardCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal variance = ((actualCost.subtract(standardCost)).divide(standardCost, 4, BigDecimal.ROUND_HALF_UP))
                .multiply(new BigDecimal("100"));

        return variance.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines variance severity based on variance percentage.
     * @param variancePercent The calculated variance percentage
     * @return Variance severity (ACCEPTABLE, MONITOR, INVESTIGATE, CRITICAL)
     */
    public static String determineVarianceSeverity(BigDecimal variancePercent) {
        if (variancePercent == null) {
            return "UNKNOWN";
        }

        BigDecimal absVariance = variancePercent.abs();

        if (absVariance.compareTo(VARIANCE_THRESHOLDS.get("CRITICAL")) > 0) {
            return "CRITICAL";
        } else if (absVariance.compareTo(VARIANCE_THRESHOLDS.get("INVESTIGATE")) > 0) {
            return "INVESTIGATE";
        } else if (absVariance.compareTo(VARIANCE_THRESHOLDS.get("MONITOR")) > 0) {
            return "MONITOR";
        } else {
            return "ACCEPTABLE";
        }
    }

    /**
     * Calculates cost efficiency ratio.
     * @param actualCost Actual cost incurred
     * @param standardCost Standard cost allowed
     * @return Cost efficiency ratio as percentage (100% = at standard)
     */
    public static BigDecimal calculateCostEfficiencyRatio(BigDecimal actualCost, BigDecimal standardCost) {
        if (actualCost == null || standardCost == null || standardCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal efficiency = (actualCost.divide(standardCost, 4, BigDecimal.ROUND_HALF_UP))
                .multiply(new BigDecimal("100"));

        return efficiency.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines cost efficiency rating based on efficiency ratio.
     * @param efficiencyRatio The calculated efficiency ratio
     * @return Efficiency rating (EXCELLENT, GOOD, NEEDS_IMPROVEMENT, POOR)
     */
    public static String determineCostEfficiencyRating(BigDecimal efficiencyRatio) {
        if (efficiencyRatio == null) {
            return "UNKNOWN";
        }

        if (efficiencyRatio.compareTo(COST_EFFICIENCY_THRESHOLDS.get("POOR")) > 0) {
            return "POOR";
        } else if (efficiencyRatio.compareTo(COST_EFFICIENCY_THRESHOLDS.get("NEEDS_IMPROVEMENT")) > 0) {
            return "NEEDS_IMPROVEMENT";
        } else if (efficiencyRatio.compareTo(COST_EFFICIENCY_THRESHOLDS.get("GOOD")) > 0) {
            return "GOOD";
        } else {
            return "EXCELLENT";
        }
    }

    /**
     * Validates if a standard cost code is in proper Workday format.
     * @param scCode The standard cost code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStandardCostCode(String scCode) {
        return DataValidationUtils.isNotEmpty(scCode) && STANDARD_COST_CODE_PATTERN.matcher(scCode.trim()).matches();
    }

    /**
     * Validates if a variance code is in proper Workday format.
     * @param varianceCode The variance code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidVarianceCode(String varianceCode) {
        return DataValidationUtils.isNotEmpty(varianceCode) && VARIANCE_CODE_PATTERN.matcher(varianceCode.trim()).matches();
    }

    /**
     * Validates if an ABC cost code is in proper Workday format.
     * @param abcCode The ABC cost code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAbcCostCode(String abcCode) {
        return DataValidationUtils.isNotEmpty(abcCode) && ABC_COST_CODE_PATTERN.matcher(abcCode.trim()).matches();
    }

    /**
     * Validates if a product cost code is in proper Workday format.
     * @param pcCode The product cost code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidProductCostCode(String pcCode) {
        return DataValidationUtils.isNotEmpty(pcCode) && PRODUCT_COST_CODE_PATTERN.matcher(pcCode.trim()).matches();
    }

    /**
     * Validates if a cost rollup code is in proper Workday format.
     * @param crCode The cost rollup code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCostRollupCode(String crCode) {
        return DataValidationUtils.isNotEmpty(crCode) && COST_ROLLUP_CODE_PATTERN.matcher(crCode.trim()).matches();
    }

    /**
     * Generates a cost accounting summary for reporting purposes.
     * @param scCode The standard cost code
     * @param varianceCode The variance code
     * @param abcCode The ABC cost code
     * @param pcCode The product cost code
     * @param crCode The cost rollup code
     * @param totalVariancePercent The total variance percentage
     * @param varianceSeverity The variance severity
     * @param efficiencyRatio The cost efficiency ratio
     * @param efficiencyRating The efficiency rating
     * @return Formatted cost accounting summary
     */
    public static String generateCostAccountingSummary(String scCode, String varianceCode, String abcCode,
                                                     String pcCode, String crCode, BigDecimal totalVariancePercent,
                                                     String varianceSeverity, BigDecimal efficiencyRatio,
                                                     String efficiencyRating) {
        StringBuilder summary = new StringBuilder();
        summary.append("Standard Cost Code: ").append(safeTransformStandardCostCode(scCode, "Not specified")).append("\n");
        summary.append("Variance Code: ").append(safeTransformVarianceCode(varianceCode, "Not specified")).append("\n");
        summary.append("ABC Cost Code: ").append(safeTransformAbcCostCode(abcCode, "Not specified")).append("\n");
        summary.append("Product Cost Code: ").append(safeTransformProductCostCode(pcCode, "Not specified")).append("\n");
        summary.append("Cost Rollup Code: ").append(safeTransformCostRollupCode(crCode, "Not specified")).append("\n");
        summary.append("Total Variance: ").append(totalVariancePercent != null ? totalVariancePercent.toString() + "%" : "Not calculated").append("\n");
        summary.append("Variance Severity: ").append(varianceSeverity != null ? varianceSeverity : "Not assessed").append("\n");
        summary.append("Cost Efficiency Ratio: ").append(efficiencyRatio != null ? efficiencyRatio.toString() + "%" : "Not calculated").append("\n");
        summary.append("Efficiency Rating: ").append(efficiencyRating != null ? efficiencyRating : "Not rated").append("\n");
        summary.append("Valid Standard Cost Code: ").append(isValidStandardCostCode(scCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Variance Code: ").append(isValidVarianceCode(varianceCode) ? "Yes" : "No").append("\n");
        summary.append("Valid ABC Cost Code: ").append(isValidAbcCostCode(abcCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Product Cost Code: ").append(isValidProductCostCode(pcCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Cost Rollup Code: ").append(isValidCostRollupCode(crCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a standard cost code based on current date.
     * @return Generated standard cost code
     */
    private static String generateStandardCostCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("SC-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a variance code based on current date.
     * @return Generated variance code
     */
    private static String generateVarianceCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return "VAR-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates an ABC cost code based on current date.
     * @return Generated ABC cost code
     */
    private static String generateAbcCostCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("ABC-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a product cost code based on current date.
     * @return Generated product cost code
     */
    private static String generateProductCostCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("PC-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a cost rollup code based on current date.
     * @return Generated cost rollup code
     */
    private static String generateCostRollupCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("CR-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String scCode = CostAccountingUtilities.transformStandardCostCode(legacyScCode);
    // String varianceCode = CostAccountingUtilities.transformVarianceCode(legacyVarianceCode);
    // String abcCode = CostAccountingUtilities.transformAbcCostCode(legacyAbcCode);
    // String pcCode = CostAccountingUtilities.transformProductCostCode(legacyPcCode);
    // String crCode = CostAccountingUtilities.transformCostRollupCode(legacyCrCode);
    // BigDecimal materialPriceVar = CostAccountingUtilities.calculateMaterialPriceVariance(actualQty, actualPrice, standardPrice);
    // BigDecimal materialQtyVar = CostAccountingUtilities.calculateMaterialQuantityVariance(actualQty, standardQty, standardPrice);
    // BigDecimal laborRateVar = CostAccountingUtilities.calculateLaborRateVariance(actualHours, actualRate, standardRate);
    // BigDecimal laborEffVar = CostAccountingUtilities.calculateLaborEfficiencyVariance(actualHours, standardHours, standardRate);
    // BigDecimal totalVariancePct = CostAccountingUtilities.calculateTotalVariancePercentage(actualCost, standardCost);
    // String varianceSeverity = CostAccountingUtilities.determineVarianceSeverity(totalVariancePct);
    // BigDecimal efficiencyRatio = CostAccountingUtilities.calculateCostEfficiencyRatio(actualCost, standardCost);
    // String efficiencyRating = CostAccountingUtilities.determineCostEfficiencyRating(efficiencyRatio);
    // String summary = CostAccountingUtilities.generateCostAccountingSummary(scCode, varianceCode, abcCode, pcCode, crCode, totalVariancePct, varianceSeverity, efficiencyRatio, efficiencyRating);
}