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
 * Utility class for transforming production data from legacy systems to Workday format.
 * Handles work orders, BOM, routing, capacity planning, reporting, and MES integration.
 * Supports manufacturing execution systems and production planning.
 */
public class ProductionUtilities {

    private static final Pattern WORK_ORDER_CODE_PATTERN = Pattern.compile("^WO-\\d{4}-\\d{6}$");
    private static final Pattern BOM_CODE_PATTERN = Pattern.compile("^BOM-\\d{8}-\\d{3}$");
    private static final Pattern ROUTING_CODE_PATTERN = Pattern.compile("^ROUTING-\\d{4}-\\d{4}$");
    private static final Pattern CAPACITY_PLAN_CODE_PATTERN = Pattern.compile("^CAP-\\d{4}-\\d{6}$");

    // Production mappings
    private static final Map<String, String> WORK_ORDER_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> BOM_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> ROUTING_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> CAPACITY_PLAN_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> PRODUCTION_EFFICIENCY_THRESHOLDS = new HashMap<>();
    private static final Map<String, BigDecimal> CAPACITY_UTILIZATION_THRESHOLDS = new HashMap<>();

    static {
        // Work order status mappings
        WORK_ORDER_STATUS_MAPPINGS.put("PLANNED", "Planned - Work order created and scheduled");
        WORK_ORDER_STATUS_MAPPINGS.put("RELEASED", "Released - Work order authorized for production");
        WORK_ORDER_STATUS_MAPPINGS.put("IN_PROGRESS", "In Progress - Production actively underway");
        WORK_ORDER_STATUS_MAPPINGS.put("COMPLETED", "Completed - Work order finished successfully");
        WORK_ORDER_STATUS_MAPPINGS.put("CANCELLED", "Cancelled - Work order terminated");
        WORK_ORDER_STATUS_MAPPINGS.put("ON_HOLD", "On Hold - Work order temporarily suspended");

        // BOM type mappings
        BOM_TYPE_MAPPINGS.put("ENGINEERING", "Engineering BOM - Design specification");
        BOM_TYPE_MAPPINGS.put("MANUFACTURING", "Manufacturing BOM - Production specification");
        BOM_TYPE_MAPPINGS.put("COSTING", "Costing BOM - Cost calculation basis");
        BOM_TYPE_MAPPINGS.put("SERVICE", "Service BOM - Maintenance and repair");

        // Routing type mappings
        ROUTING_TYPE_MAPPINGS.put("STANDARD", "Standard Routing - Default production process");
        ROUTING_TYPE_MAPPINGS.put("ALTERNATE", "Alternate Routing - Alternative production method");
        ROUTING_TYPE_MAPPINGS.put("REWORK", "Rework Routing - Repair and modification process");

        // Capacity plan status mappings
        CAPACITY_PLAN_STATUS_MAPPINGS.put("DRAFT", "Draft - Capacity plan in development");
        CAPACITY_PLAN_STATUS_MAPPINGS.put("APPROVED", "Approved - Capacity plan authorized");
        CAPACITY_PLAN_STATUS_MAPPINGS.put("ACTIVE", "Active - Capacity plan in use");
        CAPACITY_PLAN_STATUS_MAPPINGS.put("ARCHIVED", "Archived - Capacity plan no longer active");

        // Production efficiency thresholds (percentages)
        PRODUCTION_EFFICIENCY_THRESHOLDS.put("EXCELLENT", new BigDecimal("95.00")); // 95%+
        PRODUCTION_EFFICIENCY_THRESHOLDS.put("GOOD", new BigDecimal("85.00")); // 85-94%
        PRODUCTION_EFFICIENCY_THRESHOLDS.put("SATISFACTORY", new BigDecimal("75.00")); // 75-84%
        PRODUCTION_EFFICIENCY_THRESHOLDS.put("NEEDS_IMPROVEMENT", new BigDecimal("65.00")); // 65-74%
        PRODUCTION_EFFICIENCY_THRESHOLDS.put("POOR", new BigDecimal("65.00")); // <65%

        // Capacity utilization thresholds (percentages)
        CAPACITY_UTILIZATION_THRESHOLDS.put("UNDER_UTILIZED", new BigDecimal("70.00")); // <70%
        CAPACITY_UTILIZATION_THRESHOLDS.put("OPTIMAL", new BigDecimal("85.00")); // 70-85%
        CAPACITY_UTILIZATION_THRESHOLDS.put("OVER_UTILIZED", new BigDecimal("95.00")); // 85-95%
        CAPACITY_UTILIZATION_THRESHOLDS.put("CRITICAL", new BigDecimal("95.00")); // >95%
    }

    /**
     * Transforms legacy work order code to Workday standard format (WO-YYYY-NNNNNN).
     * @param legacyWorkOrderCode The legacy work order code
     * @return Standardized Workday work order code
     * @throws IllegalArgumentException if work order code format is invalid
     */
    public static String transformWorkOrderCode(String legacyWorkOrderCode) {
        if (!DataValidationUtils.isNotEmpty(legacyWorkOrderCode)) {
            throw new IllegalArgumentException("Work order code cannot be null or empty");
        }

        String cleanCode = legacyWorkOrderCode.trim();

        // Handle different legacy formats
        if (WORK_ORDER_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateWorkOrderCode();
        }
    }

    /**
     * Safely transforms work order code with error handling.
     * @param legacyWorkOrderCode The legacy work order code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday work order code or default
     */
    public static String safeTransformWorkOrderCode(String legacyWorkOrderCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformWorkOrderCode(legacyWorkOrderCode),
            defaultCode,
            "work order code transformation"
        );
    }

    /**
     * Transforms legacy BOM code to Workday standard format (BOM-YYYYMMDD-NNN).
     * @param legacyBomCode The legacy BOM code
     * @return Standardized Workday BOM code
     * @throws IllegalArgumentException if BOM code format is invalid
     */
    public static String transformBomCode(String legacyBomCode) {
        if (!DataValidationUtils.isNotEmpty(legacyBomCode)) {
            throw new IllegalArgumentException("BOM code cannot be null or empty");
        }

        String cleanCode = legacyBomCode.trim();

        // Handle different legacy formats
        if (BOM_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateBomCode();
        }
    }

    /**
     * Safely transforms BOM code with error handling.
     * @param legacyBomCode The legacy BOM code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday BOM code or default
     */
    public static String safeTransformBomCode(String legacyBomCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformBomCode(legacyBomCode),
            defaultCode,
            "BOM code transformation"
        );
    }

    /**
     * Transforms legacy routing code to Workday standard format (ROUTING-YYYY-NNNN).
     * @param legacyRoutingCode The legacy routing code
     * @return Standardized Workday routing code
     * @throws IllegalArgumentException if routing code format is invalid
     */
    public static String transformRoutingCode(String legacyRoutingCode) {
        if (!DataValidationUtils.isNotEmpty(legacyRoutingCode)) {
            throw new IllegalArgumentException("Routing code cannot be null or empty");
        }

        String cleanCode = legacyRoutingCode.trim();

        // Handle different legacy formats
        if (ROUTING_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateRoutingCode();
        }
    }

    /**
     * Safely transforms routing code with error handling.
     * @param legacyRoutingCode The legacy routing code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday routing code or default
     */
    public static String safeTransformRoutingCode(String legacyRoutingCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformRoutingCode(legacyRoutingCode),
            defaultCode,
            "routing code transformation"
        );
    }

    /**
     * Transforms legacy capacity plan code to Workday standard format (CAP-YYYY-NNNNNN).
     * @param legacyCapacityCode The legacy capacity plan code
     * @return Standardized Workday capacity plan code
     * @throws IllegalArgumentException if capacity plan code format is invalid
     */
    public static String transformCapacityPlanCode(String legacyCapacityCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCapacityCode)) {
            throw new IllegalArgumentException("Capacity plan code cannot be null or empty");
        }

        String cleanCode = legacyCapacityCode.trim();

        // Handle different legacy formats
        if (CAPACITY_PLAN_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCapacityPlanCode();
        }
    }

    /**
     * Safely transforms capacity plan code with error handling.
     * @param legacyCapacityCode The legacy capacity plan code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday capacity plan code or default
     */
    public static String safeTransformCapacityPlanCode(String legacyCapacityCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCapacityPlanCode(legacyCapacityCode),
            defaultCode,
            "capacity plan code transformation"
        );
    }

    /**
     * Standardizes work order status description.
     * @param legacyStatus The legacy work order status
     * @return Standardized work order status
     */
    public static String standardizeWorkOrderStatus(String legacyStatus) {
        if (!DataValidationUtils.isNotEmpty(legacyStatus)) {
            return "Unknown Status";
        }

        String cleanStatus = legacyStatus.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedStatus = WORK_ORDER_STATUS_MAPPINGS.get(cleanStatus);
        if (mappedStatus != null) {
            return mappedStatus;
        }

        // Handle common variations
        if (cleanStatus.contains("PLANNED") || cleanStatus.contains("CREATED")) {
            return WORK_ORDER_STATUS_MAPPINGS.get("PLANNED");
        } else if (cleanStatus.contains("RELEASED") || cleanStatus.contains("AUTHORIZED")) {
            return WORK_ORDER_STATUS_MAPPINGS.get("RELEASED");
        } else if (cleanStatus.contains("IN_PROGRESS") || cleanStatus.contains("ACTIVE") || cleanStatus.contains("RUNNING")) {
            return WORK_ORDER_STATUS_MAPPINGS.get("IN_PROGRESS");
        } else if (cleanStatus.contains("COMPLETED") || cleanStatus.contains("FINISHED") || cleanStatus.contains("DONE")) {
            return WORK_ORDER_STATUS_MAPPINGS.get("COMPLETED");
        } else if (cleanStatus.contains("CANCELLED") || cleanStatus.contains("TERMINATED")) {
            return WORK_ORDER_STATUS_MAPPINGS.get("CANCELLED");
        } else if (cleanStatus.contains("ON_HOLD") || cleanStatus.contains("SUSPENDED") || cleanStatus.contains("PAUSED")) {
            return WORK_ORDER_STATUS_MAPPINGS.get("ON_HOLD");
        }

        return cleanStatus;
    }

    /**
     * Safely standardizes work order status with error handling.
     * @param legacyStatus The legacy work order status
     * @param defaultStatus The default status to return if standardization fails
     * @return Standardized work order status or default
     */
    public static String safeStandardizeWorkOrderStatus(String legacyStatus, String defaultStatus) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeWorkOrderStatus(legacyStatus),
            defaultStatus,
            "work order status standardization"
        );
    }

    /**
     * Calculates production efficiency based on planned vs actual metrics.
     * @param plannedQuantity Planned production quantity
     * @param actualQuantity Actual production quantity
     * @param plannedTime Planned production time (hours)
     * @param actualTime Actual production time (hours)
     * @return Production efficiency percentage (0-100)
     */
    public static BigDecimal calculateProductionEfficiency(BigDecimal plannedQuantity, BigDecimal actualQuantity,
                                                         BigDecimal plannedTime, BigDecimal actualTime) {
        if (plannedQuantity == null || actualQuantity == null || plannedTime == null || actualTime == null ||
            plannedQuantity.compareTo(BigDecimal.ZERO) == 0 || plannedTime.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Efficiency = (Actual Quantity / Planned Quantity) * (Planned Time / Actual Time) * 100
        BigDecimal quantityEfficiency = actualQuantity.divide(plannedQuantity, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal timeEfficiency = plannedTime.divide(actualTime, 4, BigDecimal.ROUND_HALF_UP);

        BigDecimal overallEfficiency = quantityEfficiency.multiply(timeEfficiency).multiply(new BigDecimal("100"));

        return overallEfficiency.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines production efficiency rating based on efficiency percentage.
     * @param efficiency The calculated efficiency percentage
     * @return Efficiency rating (EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT, POOR)
     */
    public static String determineProductionEfficiencyRating(BigDecimal efficiency) {
        if (efficiency == null) {
            return "UNKNOWN";
        }

        if (efficiency.compareTo(PRODUCTION_EFFICIENCY_THRESHOLDS.get("EXCELLENT")) >= 0) {
            return "EXCELLENT";
        } else if (efficiency.compareTo(PRODUCTION_EFFICIENCY_THRESHOLDS.get("GOOD")) >= 0) {
            return "GOOD";
        } else if (efficiency.compareTo(PRODUCTION_EFFICIENCY_THRESHOLDS.get("SATISFACTORY")) >= 0) {
            return "SATISFACTORY";
        } else if (efficiency.compareTo(PRODUCTION_EFFICIENCY_THRESHOLDS.get("NEEDS_IMPROVEMENT")) >= 0) {
            return "NEEDS_IMPROVEMENT";
        } else {
            return "POOR";
        }
    }

    /**
     * Calculates capacity utilization percentage.
     * @param actualOutput Actual production output
     * @param capacityLimit Maximum capacity limit
     * @return Capacity utilization percentage (0-100)
     */
    public static BigDecimal calculateCapacityUtilization(BigDecimal actualOutput, BigDecimal capacityLimit) {
        if (actualOutput == null || capacityLimit == null || capacityLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal utilization = actualOutput.divide(capacityLimit, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        return utilization.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines capacity utilization level based on utilization percentage.
     * @param utilization The calculated utilization percentage
     * @return Utilization level (UNDER_UTILIZED, OPTIMAL, OVER_UTILIZED, CRITICAL)
     */
    public static String determineCapacityUtilizationLevel(BigDecimal utilization) {
        if (utilization == null) {
            return "UNKNOWN";
        }

        if (utilization.compareTo(CAPACITY_UTILIZATION_THRESHOLDS.get("CRITICAL")) > 0) {
            return "CRITICAL";
        } else if (utilization.compareTo(CAPACITY_UTILIZATION_THRESHOLDS.get("OVER_UTILIZED")) > 0) {
            return "OVER_UTILIZED";
        } else if (utilization.compareTo(CAPACITY_UTILIZATION_THRESHOLDS.get("OPTIMAL")) >= 0) {
            return "OPTIMAL";
        } else {
            return "UNDER_UTILIZED";
        }
    }

    /**
     * Validates if a work order code is in proper Workday format.
     * @param workOrderCode The work order code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidWorkOrderCode(String workOrderCode) {
        return DataValidationUtils.isNotEmpty(workOrderCode) && WORK_ORDER_CODE_PATTERN.matcher(workOrderCode.trim()).matches();
    }

    /**
     * Validates if a BOM code is in proper Workday format.
     * @param bomCode The BOM code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBomCode(String bomCode) {
        return DataValidationUtils.isNotEmpty(bomCode) && BOM_CODE_PATTERN.matcher(bomCode.trim()).matches();
    }

    /**
     * Validates if a routing code is in proper Workday format.
     * @param routingCode The routing code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRoutingCode(String routingCode) {
        return DataValidationUtils.isNotEmpty(routingCode) && ROUTING_CODE_PATTERN.matcher(routingCode.trim()).matches();
    }

    /**
     * Validates if a capacity plan code is in proper Workday format.
     * @param capacityCode The capacity plan code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCapacityPlanCode(String capacityCode) {
        return DataValidationUtils.isNotEmpty(capacityCode) && CAPACITY_PLAN_CODE_PATTERN.matcher(capacityCode.trim()).matches();
    }

    /**
     * Generates a production summary for reporting purposes.
     * @param workOrderCode The work order code
     * @param bomCode The BOM code
     * @param routingCode The routing code
     * @param capacityCode The capacity plan code
     * @param efficiency The production efficiency
     * @param efficiencyRating The efficiency rating
     * @param utilization The capacity utilization
     * @param utilizationLevel The utilization level
     * @return Formatted production summary
     */
    public static String generateProductionSummary(String workOrderCode, String bomCode, String routingCode,
                                                String capacityCode, BigDecimal efficiency,
                                                String efficiencyRating, BigDecimal utilization,
                                                String utilizationLevel) {
        StringBuilder summary = new StringBuilder();
        summary.append("Work Order Code: ").append(safeTransformWorkOrderCode(workOrderCode, "Not specified")).append("\n");
        summary.append("BOM Code: ").append(safeTransformBomCode(bomCode, "Not specified")).append("\n");
        summary.append("Routing Code: ").append(safeTransformRoutingCode(routingCode, "Not specified")).append("\n");
        summary.append("Capacity Plan Code: ").append(safeTransformCapacityPlanCode(capacityCode, "Not specified")).append("\n");
        summary.append("Production Efficiency: ").append(efficiency != null ? efficiency.toString() + "%" : "Not calculated").append("\n");
        summary.append("Efficiency Rating: ").append(efficiencyRating != null ? efficiencyRating : "Not rated").append("\n");
        summary.append("Capacity Utilization: ").append(utilization != null ? utilization.toString() + "%" : "Not calculated").append("\n");
        summary.append("Utilization Level: ").append(utilizationLevel != null ? utilizationLevel : "Not assessed").append("\n");
        summary.append("Valid Work Order Code: ").append(isValidWorkOrderCode(workOrderCode) ? "Yes" : "No").append("\n");
        summary.append("Valid BOM Code: ").append(isValidBomCode(bomCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Routing Code: ").append(isValidRoutingCode(routingCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Capacity Code: ").append(isValidCapacityPlanCode(capacityCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a work order code based on current date.
     * @return Generated work order code
     */
    private static String generateWorkOrderCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("WO-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a BOM code based on current date.
     * @return Generated BOM code
     */
    private static String generateBomCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%03d", (int)(Math.random() * 1000));
        return "BOM-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates a routing code based on current date.
     * @return Generated routing code
     */
    private static String generateRoutingCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("ROUTING-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a capacity plan code based on current date.
     * @return Generated capacity plan code
     */
    private static String generateCapacityPlanCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("CAP-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String workOrderCode = ProductionUtilities.transformWorkOrderCode(legacyWorkOrderCode);
    // String bomCode = ProductionUtilities.transformBomCode(legacyBomCode);
    // String routingCode = ProductionUtilities.transformRoutingCode(legacyRoutingCode);
    // String capacityCode = ProductionUtilities.transformCapacityPlanCode(legacyCapacityCode);
    // String status = ProductionUtilities.standardizeWorkOrderStatus(legacyStatus);
    // BigDecimal efficiency = ProductionUtilities.calculateProductionEfficiency(plannedQty, actualQty, plannedTime, actualTime);
    // String efficiencyRating = ProductionUtilities.determineProductionEfficiencyRating(efficiency);
    // BigDecimal utilization = ProductionUtilities.calculateCapacityUtilization(actualOutput, capacityLimit);
    // String utilizationLevel = ProductionUtilities.determineCapacityUtilizationLevel(utilization);
    // String summary = ProductionUtilities.generateProductionSummary(workOrderCode, bomCode, routingCode, capacityCode, efficiency, efficiencyRating, utilization, utilizationLevel);
}