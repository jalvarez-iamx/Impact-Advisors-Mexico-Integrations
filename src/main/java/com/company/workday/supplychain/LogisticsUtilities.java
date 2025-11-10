package com.company.workday.supplychain;

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
 * Utility class for transforming logistics data from legacy systems to Workday format.
 * Handles shipping, receiving, transportation, customs, and freight audit.
 * Supports healthcare-specific logistics requirements, temperature-controlled shipping, and regulatory compliance.
 */
public class LogisticsUtilities {

    private static final Pattern SHIPPING_ORDER_CODE_PATTERN = Pattern.compile("^SHIP-\\d{4}-\\d{6}$");
    private static final Pattern RECEIVING_ORDER_CODE_PATTERN = Pattern.compile("^RECV-\\d{4}-\\d{6}$");
    private static final Pattern TRANSPORTATION_CODE_PATTERN = Pattern.compile("^TRANS-\\d{4}-\\d{4}$");
    private static final Pattern CUSTOMS_ENTRY_CODE_PATTERN = Pattern.compile("^CUST-\\d{8}-\\d{3}$");
    private static final Pattern FREIGHT_AUDIT_CODE_PATTERN = Pattern.compile("^FA-\\d{4}-\\d{6}$");

    // Logistics mappings
    private static final Map<String, String> SHIPPING_METHOD_MAPPINGS = new HashMap<>();
    private static final Map<String, String> RECEIVING_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> TRANSPORTATION_MODE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> CUSTOMS_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> FREIGHT_AUDIT_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> SHIPPING_COST_THRESHOLDS = new HashMap<>();
    private static final Map<String, Integer> DELIVERY_TIME_WINDOWS = new HashMap<>();

    static {
        // Shipping method mappings
        SHIPPING_METHOD_MAPPINGS.put("GROUND", "Ground Shipping - Standard delivery");
        SHIPPING_METHOD_MAPPINGS.put("AIR", "Air Freight - Expedited delivery");
        SHIPPING_METHOD_MAPPINGS.put("SEA", "Sea Freight - International shipping");
        SHIPPING_METHOD_MAPPINGS.put("EXPRESS", "Express Courier - Overnight delivery");
        SHIPPING_METHOD_MAPPINGS.put("TEMPERATURE_CONTROLLED", "Temperature Controlled - Medical supplies");

        // Receiving status mappings
        RECEIVING_STATUS_MAPPINGS.put("EXPECTED", "Expected - Shipment en route");
        RECEIVING_STATUS_MAPPINGS.put("RECEIVED", "Received - Goods arrived");
        RECEIVING_STATUS_MAPPINGS.put("INSPECTED", "Inspected - Quality check completed");
        RECEIVING_STATUS_MAPPINGS.put("ACCEPTED", "Accepted - Goods accepted");
        RECEIVING_STATUS_MAPPINGS.put("REJECTED", "Rejected - Goods rejected");

        // Transportation mode mappings
        TRANSPORTATION_MODE_MAPPINGS.put("TRUCK", "Truck - Road transportation");
        TRANSPORTATION_MODE_MAPPINGS.put("RAIL", "Rail - Railway transportation");
        TRANSPORTATION_MODE_MAPPINGS.put("AIR", "Air - Air freight");
        TRANSPORTATION_MODE_MAPPINGS.put("SEA", "Sea - Maritime shipping");
        TRANSPORTATION_MODE_MAPPINGS.put("MULTIMODAL", "Multimodal - Multiple transport modes");

        // Customs status mappings
        CUSTOMS_STATUS_MAPPINGS.put("CLEARED", "Cleared - Customs clearance completed");
        CUSTOMS_STATUS_MAPPINGS.put("PENDING", "Pending - Awaiting customs review");
        CUSTOMS_STATUS_MAPPINGS.put("HELD", "Held - Customs hold for inspection");
        CUSTOMS_STATUS_MAPPINGS.put("RELEASED", "Released - Cleared for delivery");
        CUSTOMS_STATUS_MAPPINGS.put("REJECTED", "Rejected - Customs rejection");

        // Freight audit status mappings
        FREIGHT_AUDIT_STATUS_MAPPINGS.put("PENDING", "Pending - Audit in progress");
        FREIGHT_AUDIT_STATUS_MAPPINGS.put("APPROVED", "Approved - Charges verified");
        FREIGHT_AUDIT_STATUS_MAPPINGS.put("DISPUTED", "Disputed - Charges contested");
        FREIGHT_AUDIT_STATUS_MAPPINGS.put("RESOLVED", "Resolved - Audit completed");

        // Shipping cost thresholds
        SHIPPING_COST_THRESHOLDS.put("LOW_COST", new BigDecimal("50.00"));
        SHIPPING_COST_THRESHOLDS.put("MEDIUM_COST", new BigDecimal("200.00"));
        SHIPPING_COST_THRESHOLDS.put("HIGH_COST", new BigDecimal("1000.00"));
        SHIPPING_COST_THRESHOLDS.put("PREMIUM_COST", new BigDecimal("5000.00"));

        // Delivery time windows (in days)
        DELIVERY_TIME_WINDOWS.put("STANDARD", 5);
        DELIVERY_TIME_WINDOWS.put("EXPEDITED", 2);
        DELIVERY_TIME_WINDOWS.put("OVERNIGHT", 1);
        DELIVERY_TIME_WINDOWS.put("INTERNATIONAL", 14);
    }

    /**
     * Transforms legacy shipping order code to Workday standard format (SHIP-YYYY-NNNNNN).
     * @param legacyShipCode The legacy shipping order code
     * @return Standardized Workday shipping order code
     * @throws IllegalArgumentException if shipping code format is invalid
     */
    public static String transformShippingOrderCode(String legacyShipCode) {
        if (!DataValidationUtils.isNotEmpty(legacyShipCode)) {
            throw new IllegalArgumentException("Shipping order code cannot be null or empty");
        }

        String cleanCode = legacyShipCode.trim();

        // Handle different legacy formats
        if (SHIPPING_ORDER_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateShippingOrderCode();
        }
    }

    /**
     * Safely transforms shipping order code with error handling.
     * @param legacyShipCode The legacy shipping order code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday shipping order code or default
     */
    public static String safeTransformShippingOrderCode(String legacyShipCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformShippingOrderCode(legacyShipCode),
            defaultCode,
            "shipping order code transformation"
        );
    }

    /**
     * Transforms legacy receiving order code to Workday standard format (RECV-YYYY-NNNNNN).
     * @param legacyRecvCode The legacy receiving order code
     * @return Standardized Workday receiving order code
     * @throws IllegalArgumentException if receiving code format is invalid
     */
    public static String transformReceivingOrderCode(String legacyRecvCode) {
        if (!DataValidationUtils.isNotEmpty(legacyRecvCode)) {
            throw new IllegalArgumentException("Receiving order code cannot be null or empty");
        }

        String cleanCode = legacyRecvCode.trim();

        // Handle different legacy formats
        if (RECEIVING_ORDER_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateReceivingOrderCode();
        }
    }

    /**
     * Safely transforms receiving order code with error handling.
     * @param legacyRecvCode The legacy receiving order code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday receiving order code or default
     */
    public static String safeTransformReceivingOrderCode(String legacyRecvCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformReceivingOrderCode(legacyRecvCode),
            defaultCode,
            "receiving order code transformation"
        );
    }

    /**
     * Transforms legacy transportation code to Workday standard format (TRANS-YYYY-NNNN).
     * @param legacyTransCode The legacy transportation code
     * @return Standardized Workday transportation code
     * @throws IllegalArgumentException if transportation code format is invalid
     */
    public static String transformTransportationCode(String legacyTransCode) {
        if (!DataValidationUtils.isNotEmpty(legacyTransCode)) {
            throw new IllegalArgumentException("Transportation code cannot be null or empty");
        }

        String cleanCode = legacyTransCode.trim();

        // Handle different legacy formats
        if (TRANSPORTATION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateTransportationCode();
        }
    }

    /**
     * Safely transforms transportation code with error handling.
     * @param legacyTransCode The legacy transportation code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday transportation code or default
     */
    public static String safeTransformTransportationCode(String legacyTransCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformTransportationCode(legacyTransCode),
            defaultCode,
            "transportation code transformation"
        );
    }

    /**
     * Transforms legacy customs entry code to Workday standard format (CUST-YYYYMMDD-NNN).
     * @param legacyCustomsCode The legacy customs entry code
     * @return Standardized Workday customs entry code
     * @throws IllegalArgumentException if customs code format is invalid
     */
    public static String transformCustomsEntryCode(String legacyCustomsCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCustomsCode)) {
            throw new IllegalArgumentException("Customs entry code cannot be null or empty");
        }

        String cleanCode = legacyCustomsCode.trim();

        // Handle different legacy formats
        if (CUSTOMS_ENTRY_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCustomsEntryCode();
        }
    }

    /**
     * Safely transforms customs entry code with error handling.
     * @param legacyCustomsCode The legacy customs entry code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday customs entry code or default
     */
    public static String safeTransformCustomsEntryCode(String legacyCustomsCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCustomsEntryCode(legacyCustomsCode),
            defaultCode,
            "customs entry code transformation"
        );
    }

    /**
     * Transforms legacy freight audit code to Workday standard format (FA-YYYY-NNNNNN).
     * @param legacyFaCode The legacy freight audit code
     * @return Standardized Workday freight audit code
     * @throws IllegalArgumentException if freight audit code format is invalid
     */
    public static String transformFreightAuditCode(String legacyFaCode) {
        if (!DataValidationUtils.isNotEmpty(legacyFaCode)) {
            throw new IllegalArgumentException("Freight audit code cannot be null or empty");
        }

        String cleanCode = legacyFaCode.trim();

        // Handle different legacy formats
        if (FREIGHT_AUDIT_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateFreightAuditCode();
        }
    }

    /**
     * Safely transforms freight audit code with error handling.
     * @param legacyFaCode The legacy freight audit code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday freight audit code or default
     */
    public static String safeTransformFreightAuditCode(String legacyFaCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformFreightAuditCode(legacyFaCode),
            defaultCode,
            "freight audit code transformation"
        );
    }

    /**
     * Standardizes shipping method description.
     * @param legacyMethod The legacy shipping method
     * @return Standardized shipping method
     */
    public static String standardizeShippingMethod(String legacyMethod) {
        if (!DataValidationUtils.isNotEmpty(legacyMethod)) {
            return "Unknown Shipping Method";
        }

        String cleanMethod = legacyMethod.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedMethod = SHIPPING_METHOD_MAPPINGS.get(cleanMethod);
        if (mappedMethod != null) {
            return mappedMethod;
        }

        // Handle common variations
        if (cleanMethod.contains("GROUND") || cleanMethod.contains("STANDARD")) {
            return SHIPPING_METHOD_MAPPINGS.get("GROUND");
        } else if (cleanMethod.contains("AIR") || cleanMethod.contains("FLY")) {
            return SHIPPING_METHOD_MAPPINGS.get("AIR");
        } else if (cleanMethod.contains("SEA") || cleanMethod.contains("OCEAN")) {
            return SHIPPING_METHOD_MAPPINGS.get("SEA");
        } else if (cleanMethod.contains("EXPRESS") || cleanMethod.contains("OVERNIGHT")) {
            return SHIPPING_METHOD_MAPPINGS.get("EXPRESS");
        } else if (cleanMethod.contains("TEMPERATURE") || cleanMethod.contains("CONTROLLED")) {
            return SHIPPING_METHOD_MAPPINGS.get("TEMPERATURE_CONTROLLED");
        }

        return cleanMethod;
    }

    /**
     * Safely standardizes shipping method with error handling.
     * @param legacyMethod The legacy shipping method
     * @param defaultMethod The default method to return if standardization fails
     * @return Standardized shipping method or default
     */
    public static String safeStandardizeShippingMethod(String legacyMethod, String defaultMethod) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeShippingMethod(legacyMethod),
            defaultMethod,
            "shipping method standardization"
        );
    }

    /**
     * Calculates shipping cost based on weight, distance, and shipping method.
     * @param weightKg The weight in kilograms
     * @param distanceKm The distance in kilometers
     * @param shippingMethod The shipping method
     * @return Estimated shipping cost
     */
    public static BigDecimal calculateShippingCost(BigDecimal weightKg, BigDecimal distanceKm, String shippingMethod) {
        if (weightKg == null || distanceKm == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseRate;
        BigDecimal distanceMultiplier;

        String upperMethod = shippingMethod != null ? shippingMethod.toUpperCase() : "GROUND";

        switch (upperMethod) {
            case "AIR":
            case "EXPRESS":
                baseRate = new BigDecimal("5.00"); // Higher rate for air/express
                distanceMultiplier = new BigDecimal("0.10");
                break;
            case "SEA":
                baseRate = new BigDecimal("1.00"); // Lower rate for sea
                distanceMultiplier = new BigDecimal("0.02");
                break;
            case "TEMPERATURE_CONTROLLED":
                baseRate = new BigDecimal("8.00"); // Premium rate for temp control
                distanceMultiplier = new BigDecimal("0.15");
                break;
            default: // GROUND
                baseRate = new BigDecimal("2.50");
                distanceMultiplier = new BigDecimal("0.05");
        }

        BigDecimal weightCost = weightKg.multiply(baseRate);
        BigDecimal distanceCost = distanceKm.multiply(distanceMultiplier);

        BigDecimal totalCost = weightCost.add(distanceCost);
        return totalCost.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates delivery time based on distance and shipping method.
     * @param distanceKm The distance in kilometers
     * @param shippingMethod The shipping method
     * @return Estimated delivery time in days
     */
    public static int calculateDeliveryTime(BigDecimal distanceKm, String shippingMethod) {
        if (distanceKm == null) {
            return DELIVERY_TIME_WINDOWS.get("STANDARD");
        }

        String upperMethod = shippingMethod != null ? shippingMethod.toUpperCase() : "GROUND";

        int baseTime;
        double distanceFactor;

        switch (upperMethod) {
            case "AIR":
            case "EXPRESS":
                baseTime = DELIVERY_TIME_WINDOWS.get("EXPEDITED");
                distanceFactor = 0.001; // Air travel is fast regardless of distance
                break;
            case "SEA":
                baseTime = DELIVERY_TIME_WINDOWS.get("INTERNATIONAL");
                distanceFactor = 0.01; // Sea travel is slow
                break;
            case "OVERNIGHT":
                baseTime = DELIVERY_TIME_WINDOWS.get("OVERNIGHT");
                distanceFactor = 0.0005;
                break;
            default: // GROUND
                baseTime = DELIVERY_TIME_WINDOWS.get("STANDARD");
                distanceFactor = 0.005;
        }

        int distanceTime = (int) Math.ceil(distanceKm.doubleValue() * distanceFactor);
        return Math.max(baseTime, distanceTime);
    }

    /**
     * Determines freight audit status based on variance percentage.
     * @param billedAmount The amount billed by carrier
     * @param actualAmount The actual calculated amount
     * @return Freight audit status
     */
    public static String determineFreightAuditStatus(BigDecimal billedAmount, BigDecimal actualAmount) {
        if (billedAmount == null || actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) == 0) {
            return FREIGHT_AUDIT_STATUS_MAPPINGS.get("PENDING");
        }

        BigDecimal variance = billedAmount.subtract(actualAmount).abs();
        BigDecimal variancePercent = variance.divide(actualAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));

        if (variancePercent.compareTo(new BigDecimal("10.00")) > 0) {
            return FREIGHT_AUDIT_STATUS_MAPPINGS.get("DISPUTED");
        } else if (variancePercent.compareTo(new BigDecimal("5.00")) > 0) {
            return FREIGHT_AUDIT_STATUS_MAPPINGS.get("PENDING");
        } else {
            return FREIGHT_AUDIT_STATUS_MAPPINGS.get("APPROVED");
        }
    }

    /**
     * Validates if a shipping order code is in proper Workday format.
     * @param shipCode The shipping order code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidShippingOrderCode(String shipCode) {
        return DataValidationUtils.isNotEmpty(shipCode) && SHIPPING_ORDER_CODE_PATTERN.matcher(shipCode.trim()).matches();
    }

    /**
     * Validates if a receiving order code is in proper Workday format.
     * @param recvCode The receiving order code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidReceivingOrderCode(String recvCode) {
        return DataValidationUtils.isNotEmpty(recvCode) && RECEIVING_ORDER_CODE_PATTERN.matcher(recvCode.trim()).matches();
    }

    /**
     * Validates if a transportation code is in proper Workday format.
     * @param transCode The transportation code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTransportationCode(String transCode) {
        return DataValidationUtils.isNotEmpty(transCode) && TRANSPORTATION_CODE_PATTERN.matcher(transCode.trim()).matches();
    }

    /**
     * Validates if a customs entry code is in proper Workday format.
     * @param customsCode The customs entry code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCustomsEntryCode(String customsCode) {
        return DataValidationUtils.isNotEmpty(customsCode) && CUSTOMS_ENTRY_CODE_PATTERN.matcher(customsCode.trim()).matches();
    }

    /**
     * Validates if a freight audit code is in proper Workday format.
     * @param faCode The freight audit code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidFreightAuditCode(String faCode) {
        return DataValidationUtils.isNotEmpty(faCode) && FREIGHT_AUDIT_CODE_PATTERN.matcher(faCode.trim()).matches();
    }

    /**
     * Generates a logistics operations summary for reporting purposes.
     * @param shipCode The shipping order code
     * @param recvCode The receiving order code
     * @param transCode The transportation code
     * @param shippingMethod The shipping method
     * @param shippingCost The shipping cost
     * @param deliveryTime The delivery time in days
     * @param freightAuditStatus The freight audit status
     * @return Formatted logistics operations summary
     */
    public static String generateLogisticsOperationsSummary(String shipCode, String recvCode, String transCode,
                                                          String shippingMethod, BigDecimal shippingCost,
                                                          int deliveryTime, String freightAuditStatus) {
        StringBuilder summary = new StringBuilder();
        summary.append("Shipping Code: ").append(safeTransformShippingOrderCode(shipCode, "Not specified")).append("\n");
        summary.append("Receiving Code: ").append(safeTransformReceivingOrderCode(recvCode, "Not specified")).append("\n");
        summary.append("Transportation Code: ").append(safeTransformTransportationCode(transCode, "Not specified")).append("\n");
        summary.append("Shipping Method: ").append(standardizeShippingMethod(shippingMethod)).append("\n");
        summary.append("Shipping Cost: ").append(shippingCost != null ? NumberFormattingUtils.formatCurrency(shippingCost.doubleValue()) : "$0.00").append("\n");
        summary.append("Delivery Time: ").append(deliveryTime).append(" days\n");
        summary.append("Freight Audit Status: ").append(freightAuditStatus != null ? freightAuditStatus : "Not audited").append("\n");
        summary.append("Valid Shipping Code: ").append(isValidShippingOrderCode(shipCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Receiving Code: ").append(isValidReceivingOrderCode(recvCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Transportation Code: ").append(isValidTransportationCode(transCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a shipping order code based on current date.
     * @return Generated shipping order code
     */
    private static String generateShippingOrderCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("SHIP-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a receiving order code based on current date.
     * @return Generated receiving order code
     */
    private static String generateReceivingOrderCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("RECV-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a transportation code based on current date.
     * @return Generated transportation code
     */
    private static String generateTransportationCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("TRANS-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a customs entry code based on current date.
     * @return Generated customs entry code
     */
    private static String generateCustomsEntryCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%03d", (int)(Math.random() * 1000));
        return "CUST-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates a freight audit code based on current date.
     * @return Generated freight audit code
     */
    private static String generateFreightAuditCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("FA-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String shipCode = LogisticsUtilities.transformShippingOrderCode(legacyShipCode);
    // String recvCode = LogisticsUtilities.transformReceivingOrderCode(legacyRecvCode);
    // String transCode = LogisticsUtilities.transformTransportationCode(legacyTransCode);
    // String customsCode = LogisticsUtilities.transformCustomsEntryCode(legacyCustomsCode);
    // String faCode = LogisticsUtilities.transformFreightAuditCode(legacyFaCode);
    // String shippingMethod = LogisticsUtilities.standardizeShippingMethod(legacyMethod);
    // BigDecimal shippingCost = LogisticsUtilities.calculateShippingCost(weightKg, distanceKm, shippingMethod);
    // int deliveryDays = LogisticsUtilities.calculateDeliveryTime(distanceKm, shippingMethod);
    // String auditStatus = LogisticsUtilities.determineFreightAuditStatus(billedAmount, actualAmount);
    // String summary = LogisticsUtilities.generateLogisticsOperationsSummary(shipCode, recvCode, transCode, shippingMethod, shippingCost, deliveryDays, auditStatus);
}