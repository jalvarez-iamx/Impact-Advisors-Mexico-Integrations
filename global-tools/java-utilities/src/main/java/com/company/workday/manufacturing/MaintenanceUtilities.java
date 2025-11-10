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
 * Utility class for transforming maintenance management data from legacy systems to Workday format.
 * Handles preventive maintenance, work orders, asset management, spare parts, analytics, and RCM.
 * Supports maintenance planning and asset lifecycle management.
 */
public class MaintenanceUtilities {

    private static final Pattern MAINTENANCE_WORK_ORDER_CODE_PATTERN = Pattern.compile("^MWO-\\d{4}-\\d{6}$");
    private static final Pattern ASSET_CODE_PATTERN = Pattern.compile("^ASSET-\\d{8}-\\d{4}$");
    private static final Pattern SPARE_PART_CODE_PATTERN = Pattern.compile("^SP-\\d{4}-\\d{6}$");
    private static final Pattern PM_SCHEDULE_CODE_PATTERN = Pattern.compile("^PM-\\d{4}-\\d{4}$");
    private static final Pattern RCM_CODE_PATTERN = Pattern.compile("^RCM-\\d{4}-\\d{6}$");

    // Maintenance mappings
    private static final Map<String, String> MAINTENANCE_WORK_ORDER_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> MAINTENANCE_PRIORITY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> ASSET_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> SPARE_PART_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> PM_FREQUENCY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> RCM_CRITICALITY_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> MAINTENANCE_COST_THRESHOLDS = new HashMap<>();
    private static final Map<String, BigDecimal> ASSET_UTILIZATION_THRESHOLDS = new HashMap<>();

    static {
        // Maintenance work order type mappings
        MAINTENANCE_WORK_ORDER_TYPE_MAPPINGS.put("PREVENTIVE", "Preventive Maintenance - Scheduled upkeep");
        MAINTENANCE_WORK_ORDER_TYPE_MAPPINGS.put("CORRECTIVE", "Corrective Maintenance - Repair of failures");
        MAINTENANCE_WORK_ORDER_TYPE_MAPPINGS.put("PREDICTIVE", "Predictive Maintenance - Condition-based maintenance");
        MAINTENANCE_WORK_ORDER_TYPE_MAPPINGS.put("BREAKDOWN", "Breakdown Maintenance - Emergency repair");
        MAINTENANCE_WORK_ORDER_TYPE_MAPPINGS.put("MODIFICATION", "Modification - Equipment upgrade or change");

        // Maintenance priority mappings
        MAINTENANCE_PRIORITY_MAPPINGS.put("CRITICAL", "Critical - Immediate action required, safety risk");
        MAINTENANCE_PRIORITY_MAPPINGS.put("HIGH", "High - Urgent, significant impact on operations");
        MAINTENANCE_PRIORITY_MAPPINGS.put("MEDIUM", "Medium - Important, moderate impact");
        MAINTENANCE_PRIORITY_MAPPINGS.put("LOW", "Low - Routine, minimal impact");

        // Asset status mappings
        ASSET_STATUS_MAPPINGS.put("OPERATIONAL", "Operational - Asset in service and functioning");
        ASSET_STATUS_MAPPINGS.put("UNDER_MAINTENANCE", "Under Maintenance - Asset being serviced");
        ASSET_STATUS_MAPPINGS.put("OUT_OF_SERVICE", "Out of Service - Asset unavailable for use");
        ASSET_STATUS_MAPPINGS.put("RETIRED", "Retired - Asset permanently removed from service");
        ASSET_STATUS_MAPPINGS.put("STANDBY", "Standby - Asset available but not in use");

        // Spare part type mappings
        SPARE_PART_TYPE_MAPPINGS.put("CONSUMABLE", "Consumable - Regularly replaced items");
        SPARE_PART_TYPE_MAPPINGS.put("ROTATING", "Rotating - Wear items with predictable life");
        SPARE_PART_TYPE_MAPPINGS.put("REPAIRABLE", "Repairable - Items that can be refurbished");
        SPARE_PART_TYPE_MAPPINGS.put("CAPITAL", "Capital - Major components or assemblies");

        // PM frequency mappings
        PM_FREQUENCY_MAPPINGS.put("DAILY", "Daily - Every working day");
        PM_FREQUENCY_MAPPINGS.put("WEEKLY", "Weekly - Every 7 days");
        PM_FREQUENCY_MAPPINGS.put("MONTHLY", "Monthly - Every 30 days");
        PM_FREQUENCY_MAPPINGS.put("QUARTERLY", "Quarterly - Every 90 days");
        PM_FREQUENCY_MAPPINGS.put("SEMI_ANNUAL", "Semi-Annual - Every 180 days");
        PM_FREQUENCY_MAPPINGS.put("ANNUAL", "Annual - Every 365 days");

        // RCM criticality mappings
        RCM_CRITICALITY_MAPPINGS.put("SAFETY_CRITICAL", "Safety Critical - Failure affects safety");
        RCM_CRITICALITY_MAPPINGS.put("PRODUCTION_CRITICAL", "Production Critical - Failure stops production");
        RCM_CRITICALITY_MAPPINGS.put("QUALITY_CRITICAL", "Quality Critical - Failure affects product quality");
        RCM_CRITICALITY_MAPPINGS.put("NON_CRITICAL", "Non-Critical - Failure has minimal impact");

        // Maintenance cost thresholds (as percentages of asset value)
        MAINTENANCE_COST_THRESHOLDS.put("LOW", new BigDecimal("2.00")); // <2%
        MAINTENANCE_COST_THRESHOLDS.put("MODERATE", new BigDecimal("5.00")); // 2-5%
        MAINTENANCE_COST_THRESHOLDS.put("HIGH", new BigDecimal("10.00")); // 5-10%
        MAINTENANCE_COST_THRESHOLDS.put("EXCESSIVE", new BigDecimal("10.00")); // >10%

        // Asset utilization thresholds (percentages)
        ASSET_UTILIZATION_THRESHOLDS.put("UNDER_UTILIZED", new BigDecimal("60.00")); // <60%
        ASSET_UTILIZATION_THRESHOLDS.put("OPTIMAL", new BigDecimal("85.00")); // 60-85%
        ASSET_UTILIZATION_THRESHOLDS.put("OVER_UTILIZED", new BigDecimal("95.00")); // 85-95%
        ASSET_UTILIZATION_THRESHOLDS.put("CRITICAL", new BigDecimal("95.00")); // >95%
    }

    /**
     * Transforms legacy maintenance work order code to Workday standard format (MWO-YYYY-NNNNNN).
     * @param legacyMwoCode The legacy maintenance work order code
     * @return Standardized Workday maintenance work order code
     * @throws IllegalArgumentException if maintenance work order code format is invalid
     */
    public static String transformMaintenanceWorkOrderCode(String legacyMwoCode) {
        if (!DataValidationUtils.isNotEmpty(legacyMwoCode)) {
            throw new IllegalArgumentException("Maintenance work order code cannot be null or empty");
        }

        String cleanCode = legacyMwoCode.trim();

        // Handle different legacy formats
        if (MAINTENANCE_WORK_ORDER_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateMaintenanceWorkOrderCode();
        }
    }

    /**
     * Safely transforms maintenance work order code with error handling.
     * @param legacyMwoCode The legacy maintenance work order code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday maintenance work order code or default
     */
    public static String safeTransformMaintenanceWorkOrderCode(String legacyMwoCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformMaintenanceWorkOrderCode(legacyMwoCode),
            defaultCode,
            "maintenance work order code transformation"
        );
    }

    /**
     * Transforms legacy asset code to Workday standard format (ASSET-YYYYMMDD-NNNN).
     * @param legacyAssetCode The legacy asset code
     * @return Standardized Workday asset code
     * @throws IllegalArgumentException if asset code format is invalid
     */
    public static String transformAssetCode(String legacyAssetCode) {
        if (!DataValidationUtils.isNotEmpty(legacyAssetCode)) {
            throw new IllegalArgumentException("Asset code cannot be null or empty");
        }

        String cleanCode = legacyAssetCode.trim();

        // Handle different legacy formats
        if (ASSET_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateAssetCode();
        }
    }

    /**
     * Safely transforms asset code with error handling.
     * @param legacyAssetCode The legacy asset code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday asset code or default
     */
    public static String safeTransformAssetCode(String legacyAssetCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformAssetCode(legacyAssetCode),
            defaultCode,
            "asset code transformation"
        );
    }

    /**
     * Transforms legacy spare part code to Workday standard format (SP-YYYY-NNNNNN).
     * @param legacySparePartCode The legacy spare part code
     * @return Standardized Workday spare part code
     * @throws IllegalArgumentException if spare part code format is invalid
     */
    public static String transformSparePartCode(String legacySparePartCode) {
        if (!DataValidationUtils.isNotEmpty(legacySparePartCode)) {
            throw new IllegalArgumentException("Spare part code cannot be null or empty");
        }

        String cleanCode = legacySparePartCode.trim();

        // Handle different legacy formats
        if (SPARE_PART_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateSparePartCode();
        }
    }

    /**
     * Safely transforms spare part code with error handling.
     * @param legacySparePartCode The legacy spare part code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday spare part code or default
     */
    public static String safeTransformSparePartCode(String legacySparePartCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformSparePartCode(legacySparePartCode),
            defaultCode,
            "spare part code transformation"
        );
    }

    /**
     * Transforms legacy PM schedule code to Workday standard format (PM-YYYY-NNNN).
     * @param legacyPmCode The legacy PM schedule code
     * @return Standardized Workday PM schedule code
     * @throws IllegalArgumentException if PM schedule code format is invalid
     */
    public static String transformPmScheduleCode(String legacyPmCode) {
        if (!DataValidationUtils.isNotEmpty(legacyPmCode)) {
            throw new IllegalArgumentException("PM schedule code cannot be null or empty");
        }

        String cleanCode = legacyPmCode.trim();

        // Handle different legacy formats
        if (PM_SCHEDULE_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generatePmScheduleCode();
        }
    }

    /**
     * Safely transforms PM schedule code with error handling.
     * @param legacyPmCode The legacy PM schedule code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday PM schedule code or default
     */
    public static String safeTransformPmScheduleCode(String legacyPmCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformPmScheduleCode(legacyPmCode),
            defaultCode,
            "PM schedule code transformation"
        );
    }

    /**
     * Transforms legacy RCM code to Workday standard format (RCM-YYYY-NNNNNN).
     * @param legacyRcmCode The legacy RCM code
     * @return Standardized Workday RCM code
     * @throws IllegalArgumentException if RCM code format is invalid
     */
    public static String transformRcmCode(String legacyRcmCode) {
        if (!DataValidationUtils.isNotEmpty(legacyRcmCode)) {
            throw new IllegalArgumentException("RCM code cannot be null or empty");
        }

        String cleanCode = legacyRcmCode.trim();

        // Handle different legacy formats
        if (RCM_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateRcmCode();
        }
    }

    /**
     * Safely transforms RCM code with error handling.
     * @param legacyRcmCode The legacy RCM code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday RCM code or default
     */
    public static String safeTransformRcmCode(String legacyRcmCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformRcmCode(legacyRcmCode),
            defaultCode,
            "RCM code transformation"
        );
    }

    /**
     * Standardizes maintenance priority description.
     * @param legacyPriority The legacy maintenance priority
     * @return Standardized maintenance priority
     */
    public static String standardizeMaintenancePriority(String legacyPriority) {
        if (!DataValidationUtils.isNotEmpty(legacyPriority)) {
            return "Unknown Priority";
        }

        String cleanPriority = legacyPriority.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedPriority = MAINTENANCE_PRIORITY_MAPPINGS.get(cleanPriority);
        if (mappedPriority != null) {
            return mappedPriority;
        }

        // Handle common variations
        if (cleanPriority.contains("CRITICAL") || cleanPriority.contains("SAFETY") || cleanPriority.contains("IMMEDIATE")) {
            return MAINTENANCE_PRIORITY_MAPPINGS.get("CRITICAL");
        } else if (cleanPriority.contains("HIGH") || cleanPriority.contains("URGENT")) {
            return MAINTENANCE_PRIORITY_MAPPINGS.get("HIGH");
        } else if (cleanPriority.contains("MEDIUM") || cleanPriority.contains("IMPORTANT")) {
            return MAINTENANCE_PRIORITY_MAPPINGS.get("MEDIUM");
        } else if (cleanPriority.contains("LOW") || cleanPriority.contains("ROUTINE")) {
            return MAINTENANCE_PRIORITY_MAPPINGS.get("LOW");
        }

        return cleanPriority;
    }

    /**
     * Safely standardizes maintenance priority with error handling.
     * @param legacyPriority The legacy maintenance priority
     * @param defaultPriority The default priority to return if standardization fails
     * @return Standardized maintenance priority or default
     */
    public static String safeStandardizeMaintenancePriority(String legacyPriority, String defaultPriority) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeMaintenancePriority(legacyPriority),
            defaultPriority,
            "maintenance priority standardization"
        );
    }

    /**
     * Calculates maintenance cost as percentage of asset value.
     * @param maintenanceCost Total maintenance cost for period
     * @param assetValue Asset book value
     * @return Maintenance cost percentage (0-100)
     */
    public static BigDecimal calculateMaintenanceCostRatio(BigDecimal maintenanceCost, BigDecimal assetValue) {
        if (maintenanceCost == null || assetValue == null || assetValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal costRatio = maintenanceCost.divide(assetValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        return costRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines maintenance cost efficiency based on cost ratio.
     * @param costRatio The calculated maintenance cost ratio
     * @return Cost efficiency level (LOW, MODERATE, HIGH, EXCESSIVE)
     */
    public static String determineMaintenanceCostEfficiency(BigDecimal costRatio) {
        if (costRatio == null) {
            return "UNKNOWN";
        }

        if (costRatio.compareTo(MAINTENANCE_COST_THRESHOLDS.get("EXCESSIVE")) > 0) {
            return "EXCESSIVE";
        } else if (costRatio.compareTo(MAINTENANCE_COST_THRESHOLDS.get("HIGH")) > 0) {
            return "HIGH";
        } else if (costRatio.compareTo(MAINTENANCE_COST_THRESHOLDS.get("MODERATE")) > 0) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }

    /**
     * Calculates mean time between failures (MTBF).
     * @param totalOperatingTime Total operating time (hours)
     * @param numberOfFailures Number of failures during period
     * @return MTBF in hours
     */
    public static BigDecimal calculateMtbf(BigDecimal totalOperatingTime, BigDecimal numberOfFailures) {
        if (totalOperatingTime == null || numberOfFailures == null || numberOfFailures.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal mtbf = totalOperatingTime.divide(numberOfFailures, 2, BigDecimal.ROUND_HALF_UP);

        return mtbf.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates mean time to repair (MTTR).
     * @param totalDowntime Total downtime for repairs (hours)
     * @param numberOfRepairs Number of repairs during period
     * @return MTTR in hours
     */
    public static BigDecimal calculateMttr(BigDecimal totalDowntime, BigDecimal numberOfRepairs) {
        if (totalDowntime == null || numberOfRepairs == null || numberOfRepairs.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal mttr = totalDowntime.divide(numberOfRepairs, 2, BigDecimal.ROUND_HALF_UP);

        return mttr.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates asset utilization percentage.
     * @param actualRuntime Actual operating time
     * @param availableTime Total available time
     * @return Asset utilization percentage (0-100)
     */
    public static BigDecimal calculateAssetUtilization(BigDecimal actualRuntime, BigDecimal availableTime) {
        if (actualRuntime == null || availableTime == null || availableTime.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal utilization = actualRuntime.divide(availableTime, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        return utilization.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines asset utilization level based on utilization percentage.
     * @param utilization The calculated utilization percentage
     * @return Utilization level (UNDER_UTILIZED, OPTIMAL, OVER_UTILIZED, CRITICAL)
     */
    public static String determineAssetUtilizationLevel(BigDecimal utilization) {
        if (utilization == null) {
            return "UNKNOWN";
        }

        if (utilization.compareTo(ASSET_UTILIZATION_THRESHOLDS.get("CRITICAL")) > 0) {
            return "CRITICAL";
        } else if (utilization.compareTo(ASSET_UTILIZATION_THRESHOLDS.get("OVER_UTILIZED")) > 0) {
            return "OVER_UTILIZED";
        } else if (utilization.compareTo(ASSET_UTILIZATION_THRESHOLDS.get("OPTIMAL")) >= 0) {
            return "OPTIMAL";
        } else {
            return "UNDER_UTILIZED";
        }
    }

    /**
     * Validates if a maintenance work order code is in proper Workday format.
     * @param mwoCode The maintenance work order code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidMaintenanceWorkOrderCode(String mwoCode) {
        return DataValidationUtils.isNotEmpty(mwoCode) && MAINTENANCE_WORK_ORDER_CODE_PATTERN.matcher(mwoCode.trim()).matches();
    }

    /**
     * Validates if an asset code is in proper Workday format.
     * @param assetCode The asset code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAssetCode(String assetCode) {
        return DataValidationUtils.isNotEmpty(assetCode) && ASSET_CODE_PATTERN.matcher(assetCode.trim()).matches();
    }

    /**
     * Validates if a spare part code is in proper Workday format.
     * @param sparePartCode The spare part code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSparePartCode(String sparePartCode) {
        return DataValidationUtils.isNotEmpty(sparePartCode) && SPARE_PART_CODE_PATTERN.matcher(sparePartCode.trim()).matches();
    }

    /**
     * Validates if a PM schedule code is in proper Workday format.
     * @param pmCode The PM schedule code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPmScheduleCode(String pmCode) {
        return DataValidationUtils.isNotEmpty(pmCode) && PM_SCHEDULE_CODE_PATTERN.matcher(pmCode.trim()).matches();
    }

    /**
     * Validates if an RCM code is in proper Workday format.
     * @param rcmCode The RCM code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRcmCode(String rcmCode) {
        return DataValidationUtils.isNotEmpty(rcmCode) && RCM_CODE_PATTERN.matcher(rcmCode.trim()).matches();
    }

    /**
     * Generates a maintenance management summary for reporting purposes.
     * @param mwoCode The maintenance work order code
     * @param assetCode The asset code
     * @param sparePartCode The spare part code
     * @param pmCode The PM schedule code
     * @param rcmCode The RCM code
     * @param costRatio The maintenance cost ratio
     * @param costEfficiency The cost efficiency level
     * @param mtbf The mean time between failures
     * @param mttr The mean time to repair
     * @param utilization The asset utilization
     * @param utilizationLevel The utilization level
     * @return Formatted maintenance management summary
     */
    public static String generateMaintenanceManagementSummary(String mwoCode, String assetCode, String sparePartCode,
                                                            String pmCode, String rcmCode, BigDecimal costRatio,
                                                            String costEfficiency, BigDecimal mtbf, BigDecimal mttr,
                                                            BigDecimal utilization, String utilizationLevel) {
        StringBuilder summary = new StringBuilder();
        summary.append("Maintenance Work Order Code: ").append(safeTransformMaintenanceWorkOrderCode(mwoCode, "Not specified")).append("\n");
        summary.append("Asset Code: ").append(safeTransformAssetCode(assetCode, "Not specified")).append("\n");
        summary.append("Spare Part Code: ").append(safeTransformSparePartCode(sparePartCode, "Not specified")).append("\n");
        summary.append("PM Schedule Code: ").append(safeTransformPmScheduleCode(pmCode, "Not specified")).append("\n");
        summary.append("RCM Code: ").append(safeTransformRcmCode(rcmCode, "Not specified")).append("\n");
        summary.append("Maintenance Cost Ratio: ").append(costRatio != null ? costRatio.toString() + "%" : "Not calculated").append("\n");
        summary.append("Cost Efficiency: ").append(costEfficiency != null ? costEfficiency : "Not assessed").append("\n");
        summary.append("MTBF: ").append(mtbf != null ? mtbf.toString() + " hours" : "Not calculated").append("\n");
        summary.append("MTTR: ").append(mttr != null ? mttr.toString() + " hours" : "Not calculated").append("\n");
        summary.append("Asset Utilization: ").append(utilization != null ? utilization.toString() + "%" : "Not calculated").append("\n");
        summary.append("Utilization Level: ").append(utilizationLevel != null ? utilizationLevel : "Not assessed").append("\n");
        summary.append("Valid MWO Code: ").append(isValidMaintenanceWorkOrderCode(mwoCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Asset Code: ").append(isValidAssetCode(assetCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Spare Part Code: ").append(isValidSparePartCode(sparePartCode) ? "Yes" : "No").append("\n");
        summary.append("Valid PM Code: ").append(isValidPmScheduleCode(pmCode) ? "Yes" : "No").append("\n");
        summary.append("Valid RCM Code: ").append(isValidRcmCode(rcmCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a maintenance work order code based on current date.
     * @return Generated maintenance work order code
     */
    private static String generateMaintenanceWorkOrderCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("MWO-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates an asset code based on current date.
     * @return Generated asset code
     */
    private static String generateAssetCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return "ASSET-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates a spare part code based on current date.
     * @return Generated spare part code
     */
    private static String generateSparePartCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("SP-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a PM schedule code based on current date.
     * @return Generated PM schedule code
     */
    private static String generatePmScheduleCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("PM-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates an RCM code based on current date.
     * @return Generated RCM code
     */
    private static String generateRcmCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("RCM-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String mwoCode = MaintenanceUtilities.transformMaintenanceWorkOrderCode(legacyMwoCode);
    // String assetCode = MaintenanceUtilities.transformAssetCode(legacyAssetCode);
    // String sparePartCode = MaintenanceUtilities.transformSparePartCode(legacySparePartCode);
    // String pmCode = MaintenanceUtilities.transformPmScheduleCode(legacyPmCode);
    // String rcmCode = MaintenanceUtilities.transformRcmCode(legacyRcmCode);
    // String priority = MaintenanceUtilities.standardizeMaintenancePriority(legacyPriority);
    // BigDecimal costRatio = MaintenanceUtilities.calculateMaintenanceCostRatio(maintenanceCost, assetValue);
    // String costEfficiency = MaintenanceUtilities.determineMaintenanceCostEfficiency(costRatio);
    // BigDecimal mtbf = MaintenanceUtilities.calculateMtbf(totalOperatingTime, numberOfFailures);
    // BigDecimal mttr = MaintenanceUtilities.calculateMttr(totalDowntime, numberOfRepairs);
    // BigDecimal utilization = MaintenanceUtilities.calculateAssetUtilization(actualRuntime, availableTime);
    // String utilizationLevel = MaintenanceUtilities.determineAssetUtilizationLevel(utilization);
    // String summary = MaintenanceUtilities.generateMaintenanceManagementSummary(mwoCode, assetCode, sparePartCode, pmCode, rcmCode, costRatio, costEfficiency, mtbf, mttr, utilization, utilizationLevel);
}