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
 * Utility class for transforming inventory data from legacy systems to Workday format.
 * Handles item masters, stock levels, warehouses, cycle counts, lot tracking, and ABC classification.
 * Supports healthcare-specific inventory management, expiration tracking, and regulatory compliance.
 */
public class InventoryUtilities {

    private static final Pattern ITEM_MASTER_CODE_PATTERN = Pattern.compile("^ITEM-\\d{6}-\\d{3}$");
    private static final Pattern STOCK_LEVEL_CODE_PATTERN = Pattern.compile("^STOCK-\\d{4}-\\d{6}$");
    private static final Pattern WAREHOUSE_CODE_PATTERN = Pattern.compile("^WH-\\d{3}$");
    private static final Pattern CYCLE_COUNT_CODE_PATTERN = Pattern.compile("^CC-\\d{4}-\\d{4}$");
    private static final Pattern LOT_NUMBER_PATTERN = Pattern.compile("^LOT-\\d{8}-\\d{3}$");

    // Inventory mappings
    private static final Map<String, String> ITEM_CATEGORY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> STOCK_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> WAREHOUSE_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> CYCLE_COUNT_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> ABC_CLASSIFICATION_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> STOCK_LEVEL_THRESHOLDS = new HashMap<>();
    private static final Map<String, Integer> EXPIRATION_WARNING_DAYS = new HashMap<>();

    static {
        // Item category mappings
        ITEM_CATEGORY_MAPPINGS.put("MEDICAL", "Medical Supplies - Healthcare equipment and supplies");
        ITEM_CATEGORY_MAPPINGS.put("PHARMACEUTICAL", "Pharmaceutical - Drugs and medications");
        ITEM_CATEGORY_MAPPINGS.put("CONSUMABLE", "Consumables - Office and facility supplies");
        ITEM_CATEGORY_MAPPINGS.put("EQUIPMENT", "Equipment - Medical and laboratory equipment");
        ITEM_CATEGORY_MAPPINGS.put("STERILE", "Sterile Supplies - Surgical and sterile items");

        // Stock status mappings
        STOCK_STATUS_MAPPINGS.put("IN_STOCK", "In Stock - Available for use");
        STOCK_STATUS_MAPPINGS.put("LOW_STOCK", "Low Stock - Below reorder point");
        STOCK_STATUS_MAPPINGS.put("OUT_OF_STOCK", "Out of Stock - Depleted inventory");
        STOCK_STATUS_MAPPINGS.put("OVERSTOCK", "Overstock - Excess inventory");
        STOCK_STATUS_MAPPINGS.put("QUARANTINE", "Quarantine - Held for quality control");

        // Warehouse type mappings
        WAREHOUSE_TYPE_MAPPINGS.put("CENTRAL", "Central Warehouse - Main distribution center");
        WAREHOUSE_TYPE_MAPPINGS.put("REGIONAL", "Regional Warehouse - Local distribution");
        WAREHOUSE_TYPE_MAPPINGS.put("DEPARTMENTAL", "Departmental - Unit-specific storage");
        WAREHOUSE_TYPE_MAPPINGS.put("EMERGENCY", "Emergency - Disaster preparedness supplies");

        // Cycle count status mappings
        CYCLE_COUNT_STATUS_MAPPINGS.put("SCHEDULED", "Scheduled - Planned count");
        CYCLE_COUNT_STATUS_MAPPINGS.put("IN_PROGRESS", "In Progress - Currently counting");
        CYCLE_COUNT_STATUS_MAPPINGS.put("COMPLETED", "Completed - Count finished");
        CYCLE_COUNT_STATUS_MAPPINGS.put("VERIFIED", "Verified - Count reconciled");

        // ABC classification mappings
        ABC_CLASSIFICATION_MAPPINGS.put("A", "Class A - High value, low volume items");
        ABC_CLASSIFICATION_MAPPINGS.put("B", "Class B - Medium value/volume items");
        ABC_CLASSIFICATION_MAPPINGS.put("C", "Class C - Low value, high volume items");

        // Stock level thresholds
        STOCK_LEVEL_THRESHOLDS.put("REORDER_POINT", new BigDecimal("10.00"));
        STOCK_LEVEL_THRESHOLDS.put("MINIMUM_STOCK", new BigDecimal("5.00"));
        STOCK_LEVEL_THRESHOLDS.put("MAXIMUM_STOCK", new BigDecimal("1000.00"));
        STOCK_LEVEL_THRESHOLDS.put("CRITICAL_LOW", new BigDecimal("2.00"));

        // Expiration warning days
        EXPIRATION_WARNING_DAYS.put("CRITICAL", 30);
        EXPIRATION_WARNING_DAYS.put("WARNING", 90);
        EXPIRATION_WARNING_DAYS.put("NOTICE", 180);
    }

    /**
     * Transforms legacy item master code to Workday standard format (ITEM-NNNNNN-NNN).
     * @param legacyItemCode The legacy item master code
     * @return Standardized Workday item master code
     * @throws IllegalArgumentException if item code format is invalid
     */
    public static String transformItemMasterCode(String legacyItemCode) {
        if (!DataValidationUtils.isNotEmpty(legacyItemCode)) {
            throw new IllegalArgumentException("Item master code cannot be null or empty");
        }

        String cleanCode = legacyItemCode.trim();

        // Handle different legacy formats
        if (ITEM_MASTER_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on item identifier
            return generateItemMasterCode(cleanCode);
        }
    }

    /**
     * Safely transforms item master code with error handling.
     * @param legacyItemCode The legacy item master code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday item master code or default
     */
    public static String safeTransformItemMasterCode(String legacyItemCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformItemMasterCode(legacyItemCode),
            defaultCode,
            "item master code transformation"
        );
    }

    /**
     * Transforms legacy stock level code to Workday standard format (STOCK-YYYY-NNNNNN).
     * @param legacyStockCode The legacy stock level code
     * @return Standardized Workday stock level code
     * @throws IllegalArgumentException if stock code format is invalid
     */
    public static String transformStockLevelCode(String legacyStockCode) {
        if (!DataValidationUtils.isNotEmpty(legacyStockCode)) {
            throw new IllegalArgumentException("Stock level code cannot be null or empty");
        }

        String cleanCode = legacyStockCode.trim();

        // Handle different legacy formats
        if (STOCK_LEVEL_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateStockLevelCode();
        }
    }

    /**
     * Safely transforms stock level code with error handling.
     * @param legacyStockCode The legacy stock level code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday stock level code or default
     */
    public static String safeTransformStockLevelCode(String legacyStockCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformStockLevelCode(legacyStockCode),
            defaultCode,
            "stock level code transformation"
        );
    }

    /**
     * Transforms legacy warehouse code to Workday standard format (WH-NNN).
     * @param legacyWhCode The legacy warehouse code
     * @return Standardized Workday warehouse code
     * @throws IllegalArgumentException if warehouse code format is invalid
     */
    public static String transformWarehouseCode(String legacyWhCode) {
        if (!DataValidationUtils.isNotEmpty(legacyWhCode)) {
            throw new IllegalArgumentException("Warehouse code cannot be null or empty");
        }

        String cleanCode = legacyWhCode.trim();

        // Handle different legacy formats
        if (WAREHOUSE_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on warehouse identifier
            return generateWarehouseCode(cleanCode);
        }
    }

    /**
     * Safely transforms warehouse code with error handling.
     * @param legacyWhCode The legacy warehouse code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday warehouse code or default
     */
    public static String safeTransformWarehouseCode(String legacyWhCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformWarehouseCode(legacyWhCode),
            defaultCode,
            "warehouse code transformation"
        );
    }

    /**
     * Transforms legacy cycle count code to Workday standard format (CC-YYYY-NNNN).
     * @param legacyCcCode The legacy cycle count code
     * @return Standardized Workday cycle count code
     * @throws IllegalArgumentException if cycle count code format is invalid
     */
    public static String transformCycleCountCode(String legacyCcCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCcCode)) {
            throw new IllegalArgumentException("Cycle count code cannot be null or empty");
        }

        String cleanCode = legacyCcCode.trim();

        // Handle different legacy formats
        if (CYCLE_COUNT_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCycleCountCode();
        }
    }

    /**
     * Safely transforms cycle count code with error handling.
     * @param legacyCcCode The legacy cycle count code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday cycle count code or default
     */
    public static String safeTransformCycleCountCode(String legacyCcCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCycleCountCode(legacyCcCode),
            defaultCode,
            "cycle count code transformation"
        );
    }

    /**
     * Transforms legacy lot number to Workday standard format (LOT-YYYYMMDD-NNN).
     * @param legacyLotNumber The legacy lot number
     * @return Standardized Workday lot number
     * @throws IllegalArgumentException if lot number format is invalid
     */
    public static String transformLotNumber(String legacyLotNumber) {
        if (!DataValidationUtils.isNotEmpty(legacyLotNumber)) {
            throw new IllegalArgumentException("Lot number cannot be null or empty");
        }

        String cleanLot = legacyLotNumber.trim();

        // Handle different legacy formats
        if (LOT_NUMBER_PATTERN.matcher(cleanLot).matches()) {
            return cleanLot;
        } else {
            // Generate lot number based on current date
            return generateLotNumber();
        }
    }

    /**
     * Safely transforms lot number with error handling.
     * @param legacyLotNumber The legacy lot number
     * @param defaultLot The default lot number to return if transformation fails
     * @return Standardized Workday lot number or default
     */
    public static String safeTransformLotNumber(String legacyLotNumber, String defaultLot) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformLotNumber(legacyLotNumber),
            defaultLot,
            "lot number transformation"
        );
    }

    /**
     * Standardizes item category description.
     * @param legacyCategory The legacy item category
     * @return Standardized item category
     */
    public static String standardizeItemCategory(String legacyCategory) {
        if (!DataValidationUtils.isNotEmpty(legacyCategory)) {
            return "Unknown Category";
        }

        String cleanCategory = legacyCategory.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedCategory = ITEM_CATEGORY_MAPPINGS.get(cleanCategory);
        if (mappedCategory != null) {
            return mappedCategory;
        }

        // Handle common variations
        if (cleanCategory.contains("MEDICAL") || cleanCategory.contains("HEALTH")) {
            return ITEM_CATEGORY_MAPPINGS.get("MEDICAL");
        } else if (cleanCategory.contains("PHARM") || cleanCategory.contains("DRUG")) {
            return ITEM_CATEGORY_MAPPINGS.get("PHARMACEUTICAL");
        } else if (cleanCategory.contains("CONSUMABLE") || cleanCategory.contains("SUPPLY")) {
            return ITEM_CATEGORY_MAPPINGS.get("CONSUMABLE");
        } else if (cleanCategory.contains("EQUIPMENT") || cleanCategory.contains("DEVICE")) {
            return ITEM_CATEGORY_MAPPINGS.get("EQUIPMENT");
        } else if (cleanCategory.contains("STERILE") || cleanCategory.contains("SURGICAL")) {
            return ITEM_CATEGORY_MAPPINGS.get("STERILE");
        }

        return cleanCategory;
    }

    /**
     * Safely standardizes item category with error handling.
     * @param legacyCategory The legacy item category
     * @param defaultCategory The default category to return if standardization fails
     * @return Standardized item category or default
     */
    public static String safeStandardizeItemCategory(String legacyCategory, String defaultCategory) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeItemCategory(legacyCategory),
            defaultCategory,
            "item category standardization"
        );
    }

    /**
     * Determines stock status based on current quantity and thresholds.
     * @param currentQuantity The current stock quantity
     * @param reorderPoint The reorder point threshold
     * @param minimumStock The minimum stock level
     * @param maximumStock The maximum stock level
     * @return Stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK, OVERSTOCK)
     */
    public static String determineStockStatus(BigDecimal currentQuantity, BigDecimal reorderPoint,
                                             BigDecimal minimumStock, BigDecimal maximumStock) {
        if (currentQuantity == null) {
            return STOCK_STATUS_MAPPINGS.get("OUT_OF_STOCK");
        }

        BigDecimal reorderThreshold = reorderPoint != null ? reorderPoint : STOCK_LEVEL_THRESHOLDS.get("REORDER_POINT");
        BigDecimal minThreshold = minimumStock != null ? minimumStock : STOCK_LEVEL_THRESHOLDS.get("MINIMUM_STOCK");
        BigDecimal maxThreshold = maximumStock != null ? maximumStock : STOCK_LEVEL_THRESHOLDS.get("MAXIMUM_STOCK");

        if (currentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return STOCK_STATUS_MAPPINGS.get("OUT_OF_STOCK");
        } else if (currentQuantity.compareTo(minThreshold) <= 0) {
            return STOCK_STATUS_MAPPINGS.get("LOW_STOCK");
        } else if (currentQuantity.compareTo(maxThreshold) >= 0) {
            return STOCK_STATUS_MAPPINGS.get("OVERSTOCK");
        } else if (currentQuantity.compareTo(reorderThreshold) <= 0) {
            return STOCK_STATUS_MAPPINGS.get("LOW_STOCK");
        } else {
            return STOCK_STATUS_MAPPINGS.get("IN_STOCK");
        }
    }

    /**
     * Calculates ABC classification based on annual usage value and frequency.
     * @param annualValue The annual consumption value
     * @param annualFrequency The annual usage frequency
     * @return ABC classification (A, B, or C)
     */
    public static String calculateAbcClassification(BigDecimal annualValue, BigDecimal annualFrequency) {
        if (annualValue == null || annualFrequency == null) {
            return "C";
        }

        // Simple ABC classification based on value
        // A items: Top 20% of value
        // B items: Next 30% of value
        // C items: Bottom 50% of value

        if (annualValue.compareTo(new BigDecimal("10000.00")) >= 0) {
            return "A";
        } else if (annualValue.compareTo(new BigDecimal("1000.00")) >= 0) {
            return "B";
        } else {
            return "C";
        }
    }

    /**
     * Calculates inventory turnover ratio.
     * @param costOfGoodsSold The cost of goods sold
     * @param averageInventory The average inventory value
     * @return Inventory turnover ratio
     */
    public static BigDecimal calculateInventoryTurnover(BigDecimal costOfGoodsSold, BigDecimal averageInventory) {
        if (costOfGoodsSold == null || averageInventory == null || averageInventory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return costOfGoodsSold.divide(averageInventory, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines expiration alert level based on days until expiration.
     * @param daysUntilExpiration The number of days until item expires
     * @return Alert level (CRITICAL, WARNING, NOTICE, NONE)
     */
    public static String determineExpirationAlertLevel(int daysUntilExpiration) {
        if (daysUntilExpiration <= EXPIRATION_WARNING_DAYS.get("CRITICAL")) {
            return "CRITICAL";
        } else if (daysUntilExpiration <= EXPIRATION_WARNING_DAYS.get("WARNING")) {
            return "WARNING";
        } else if (daysUntilExpiration <= EXPIRATION_WARNING_DAYS.get("NOTICE")) {
            return "NOTICE";
        } else {
            return "NONE";
        }
    }

    /**
     * Validates if an item master code is in proper Workday format.
     * @param itemCode The item master code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidItemMasterCode(String itemCode) {
        return DataValidationUtils.isNotEmpty(itemCode) && ITEM_MASTER_CODE_PATTERN.matcher(itemCode.trim()).matches();
    }

    /**
     * Validates if a stock level code is in proper Workday format.
     * @param stockCode The stock level code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStockLevelCode(String stockCode) {
        return DataValidationUtils.isNotEmpty(stockCode) && STOCK_LEVEL_CODE_PATTERN.matcher(stockCode.trim()).matches();
    }

    /**
     * Validates if a warehouse code is in proper Workday format.
     * @param whCode The warehouse code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidWarehouseCode(String whCode) {
        return DataValidationUtils.isNotEmpty(whCode) && WAREHOUSE_CODE_PATTERN.matcher(whCode.trim()).matches();
    }

    /**
     * Validates if a cycle count code is in proper Workday format.
     * @param ccCode The cycle count code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCycleCountCode(String ccCode) {
        return DataValidationUtils.isNotEmpty(ccCode) && CYCLE_COUNT_CODE_PATTERN.matcher(ccCode.trim()).matches();
    }

    /**
     * Validates if a lot number is in proper Workday format.
     * @param lotNumber The lot number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLotNumber(String lotNumber) {
        return DataValidationUtils.isNotEmpty(lotNumber) && LOT_NUMBER_PATTERN.matcher(lotNumber.trim()).matches();
    }

    /**
     * Generates an inventory management summary for reporting purposes.
     * @param itemCode The item master code
     * @param stockCode The stock level code
     * @param warehouseCode The warehouse code
     * @param itemCategory The item category
     * @param currentStock The current stock quantity
     * @param abcClass The ABC classification
     * @param turnoverRatio The inventory turnover ratio
     * @return Formatted inventory management summary
     */
    public static String generateInventoryManagementSummary(String itemCode, String stockCode, String warehouseCode,
                                                          String itemCategory, BigDecimal currentStock,
                                                          String abcClass, BigDecimal turnoverRatio) {
        StringBuilder summary = new StringBuilder();
        summary.append("Item Code: ").append(safeTransformItemMasterCode(itemCode, "Not specified")).append("\n");
        summary.append("Stock Code: ").append(safeTransformStockLevelCode(stockCode, "Not specified")).append("\n");
        summary.append("Warehouse Code: ").append(safeTransformWarehouseCode(warehouseCode, "Not specified")).append("\n");
        summary.append("Item Category: ").append(standardizeItemCategory(itemCategory)).append("\n");
        summary.append("Current Stock: ").append(currentStock != null ? currentStock.toString() : "0").append("\n");
        summary.append("ABC Classification: ").append(abcClass != null ? abcClass : "Not classified").append("\n");
        summary.append("Turnover Ratio: ").append(turnoverRatio != null ? turnoverRatio.toString() : "Not calculated").append("\n");
        summary.append("Valid Item Code: ").append(isValidItemMasterCode(itemCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Stock Code: ").append(isValidStockLevelCode(stockCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Warehouse Code: ").append(isValidWarehouseCode(warehouseCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates an item master code based on item identifier.
     * @param itemIdentifier The item identifier
     * @return Generated item master code
     */
    private static String generateItemMasterCode(String itemIdentifier) {
        String hashPart = String.format("%06d", Math.abs(itemIdentifier.hashCode()) % 1000000);
        String sequencePart = String.format("%03d", (int)(Math.random() * 1000));
        return "ITEM-" + hashPart + "-" + sequencePart;
    }

    /**
     * Generates a stock level code based on current date.
     * @return Generated stock level code
     */
    private static String generateStockLevelCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("STOCK-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a warehouse code based on warehouse identifier.
     * @param warehouseIdentifier The warehouse identifier
     * @return Generated warehouse code
     */
    private static String generateWarehouseCode(String warehouseIdentifier) {
        String hashPart = String.format("%03d", Math.abs(warehouseIdentifier.hashCode()) % 1000);
        return "WH-" + hashPart;
    }

    /**
     * Generates a cycle count code based on current date.
     * @return Generated cycle count code
     */
    private static String generateCycleCountCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("CC-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a lot number based on current date.
     * @return Generated lot number
     */
    private static String generateLotNumber() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%03d", (int)(Math.random() * 1000));
        return "LOT-" + datePart + "-" + sequencePart;
    }

    // Copy-paste usage examples:
    // String itemCode = InventoryUtilities.transformItemMasterCode(legacyItemCode);
    // String stockCode = InventoryUtilities.transformStockLevelCode(legacyStockCode);
    // String whCode = InventoryUtilities.transformWarehouseCode(legacyWhCode);
    // String ccCode = InventoryUtilities.transformCycleCountCode(legacyCcCode);
    // String lotNumber = InventoryUtilities.transformLotNumber(legacyLotNumber);
    // String itemCategory = InventoryUtilities.standardizeItemCategory(legacyCategory);
    // String stockStatus = InventoryUtilities.determineStockStatus(currentQty, reorderPoint, minStock, maxStock);
    // String abcClass = InventoryUtilities.calculateAbcClassification(annualValue, annualFrequency);
    // BigDecimal turnover = InventoryUtilities.calculateInventoryTurnover(cogs, avgInventory);
    // String alertLevel = InventoryUtilities.determineExpirationAlertLevel(daysUntilExpiration);
    // String summary = InventoryUtilities.generateInventoryManagementSummary(itemCode, stockCode, whCode, itemCategory, currentStock, abcClass, turnover);
}