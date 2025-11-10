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
 * Utility class for transforming procurement data from legacy systems to Workday format.
 * Handles purchase orders, vendors, contracts, requisitions, RFQs, and procurement analytics.
 * Supports healthcare-specific procurement requirements, vendor compliance, and supply chain optimization.
 */
public class ProcurementUtilities {

    private static final Pattern PURCHASE_ORDER_CODE_PATTERN = Pattern.compile("^PO-\\d{4}-\\d{6}$");
    private static final Pattern VENDOR_CODE_PATTERN = Pattern.compile("^VENDOR-\\d{6}$");
    private static final Pattern CONTRACT_CODE_PATTERN = Pattern.compile("^CONTRACT-\\d{4}-\\d{4}$");
    private static final Pattern REQUISITION_CODE_PATTERN = Pattern.compile("^REQ-\\d{4}-\\d{6}$");
    private static final Pattern RFQ_CODE_PATTERN = Pattern.compile("^RFQ-\\d{4}-\\d{4}$");

    // Procurement mappings
    private static final Map<String, String> PURCHASE_ORDER_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> VENDOR_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> CONTRACT_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> REQUISITION_PRIORITY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> RFQ_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> VENDOR_RATING_THRESHOLDS = new HashMap<>();
    private static final Map<String, BigDecimal> CONTRACT_VALUE_THRESHOLDS = new HashMap<>();

    static {
        // Purchase order status mappings
        PURCHASE_ORDER_STATUS_MAPPINGS.put("OPEN", "Open - Purchase order is active");
        PURCHASE_ORDER_STATUS_MAPPINGS.put("CLOSED", "Closed - Purchase order completed");
        PURCHASE_ORDER_STATUS_MAPPINGS.put("CANCELLED", "Cancelled - Purchase order cancelled");
        PURCHASE_ORDER_STATUS_MAPPINGS.put("PENDING", "Pending - Awaiting approval");
        PURCHASE_ORDER_STATUS_MAPPINGS.put("APPROVED", "Approved - Ready for fulfillment");

        // Vendor type mappings
        VENDOR_TYPE_MAPPINGS.put("DIRECT", "Direct Supplier - Primary material supplier");
        VENDOR_TYPE_MAPPINGS.put("INDIRECT", "Indirect Supplier - Services and MRO");
        VENDOR_TYPE_MAPPINGS.put("STRATEGIC", "Strategic Partner - Key business partner");
        VENDOR_TYPE_MAPPINGS.put("PREFERRED", "Preferred Supplier - Approved vendor list");

        // Contract type mappings
        CONTRACT_TYPE_MAPPINGS.put("FRAMEWORK", "Framework Agreement - Multi-year agreement");
        CONTRACT_TYPE_MAPPINGS.put("MASTER", "Master Agreement - Umbrella contract");
        CONTRACT_TYPE_MAPPINGS.put("STANDARD", "Standard Contract - Single transaction");
        CONTRACT_TYPE_MAPPINGS.put("BLANKET", "Blanket Order - Ongoing supply agreement");

        // Requisition priority mappings
        REQUISITION_PRIORITY_MAPPINGS.put("CRITICAL", "Critical - Immediate medical supply needs");
        REQUISITION_PRIORITY_MAPPINGS.put("HIGH", "High - Urgent operational requirements");
        REQUISITION_PRIORITY_MAPPINGS.put("MEDIUM", "Medium - Standard business needs");
        REQUISITION_PRIORITY_MAPPINGS.put("LOW", "Low - Non-urgent items");

        // RFQ status mappings
        RFQ_STATUS_MAPPINGS.put("DRAFT", "Draft - RFQ in preparation");
        RFQ_STATUS_MAPPINGS.put("OPEN", "Open - Accepting bids");
        RFQ_STATUS_MAPPINGS.put("CLOSED", "Closed - Bid period ended");
        RFQ_STATUS_MAPPINGS.put("AWARDED", "Awarded - Contract awarded");

        // Vendor rating thresholds
        VENDOR_RATING_THRESHOLDS.put("EXCELLENT", new BigDecimal("4.5"));
        VENDOR_RATING_THRESHOLDS.put("GOOD", new BigDecimal("3.5"));
        VENDOR_RATING_THRESHOLDS.put("AVERAGE", new BigDecimal("2.5"));
        VENDOR_RATING_THRESHOLDS.put("POOR", new BigDecimal("1.5"));

        // Contract value thresholds
        CONTRACT_VALUE_THRESHOLDS.put("SMALL", new BigDecimal("10000.00"));
        CONTRACT_VALUE_THRESHOLDS.put("MEDIUM", new BigDecimal("50000.00"));
        CONTRACT_VALUE_THRESHOLDS.put("LARGE", new BigDecimal("250000.00"));
    }

    /**
     * Transforms legacy purchase order code to Workday standard format (PO-YYYY-NNNNNN).
     * @param legacyPoCode The legacy purchase order code
     * @return Standardized Workday purchase order code
     * @throws IllegalArgumentException if PO code format is invalid
     */
    public static String transformPurchaseOrderCode(String legacyPoCode) {
        if (!DataValidationUtils.isNotEmpty(legacyPoCode)) {
            throw new IllegalArgumentException("Purchase order code cannot be null or empty");
        }

        String cleanCode = legacyPoCode.trim();

        // Handle different legacy formats
        if (PURCHASE_ORDER_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generatePurchaseOrderCode();
        }
    }

    /**
     * Safely transforms purchase order code with error handling.
     * @param legacyPoCode The legacy purchase order code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday purchase order code or default
     */
    public static String safeTransformPurchaseOrderCode(String legacyPoCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformPurchaseOrderCode(legacyPoCode),
            defaultCode,
            "purchase order code transformation"
        );
    }

    /**
     * Transforms legacy vendor code to Workday standard format (VENDOR-NNNNNN).
     * @param legacyVendorCode The legacy vendor code
     * @return Standardized Workday vendor code
     * @throws IllegalArgumentException if vendor code format is invalid
     */
    public static String transformVendorCode(String legacyVendorCode) {
        if (!DataValidationUtils.isNotEmpty(legacyVendorCode)) {
            throw new IllegalArgumentException("Vendor code cannot be null or empty");
        }

        String cleanCode = legacyVendorCode.trim();

        // Handle different legacy formats
        if (VENDOR_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on vendor name or ID
            return generateVendorCode(cleanCode);
        }
    }

    /**
     * Safely transforms vendor code with error handling.
     * @param legacyVendorCode The legacy vendor code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday vendor code or default
     */
    public static String safeTransformVendorCode(String legacyVendorCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformVendorCode(legacyVendorCode),
            defaultCode,
            "vendor code transformation"
        );
    }

    /**
     * Transforms legacy contract code to Workday standard format (CONTRACT-YYYY-NNNN).
     * @param legacyContractCode The legacy contract code
     * @return Standardized Workday contract code
     * @throws IllegalArgumentException if contract code format is invalid
     */
    public static String transformContractCode(String legacyContractCode) {
        if (!DataValidationUtils.isNotEmpty(legacyContractCode)) {
            throw new IllegalArgumentException("Contract code cannot be null or empty");
        }

        String cleanCode = legacyContractCode.trim();

        // Handle different legacy formats
        if (CONTRACT_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateContractCode();
        }
    }

    /**
     * Safely transforms contract code with error handling.
     * @param legacyContractCode The legacy contract code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday contract code or default
     */
    public static String safeTransformContractCode(String legacyContractCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformContractCode(legacyContractCode),
            defaultCode,
            "contract code transformation"
        );
    }

    /**
     * Transforms legacy requisition code to Workday standard format (REQ-YYYY-NNNNNN).
     * @param legacyReqCode The legacy requisition code
     * @return Standardized Workday requisition code
     * @throws IllegalArgumentException if requisition code format is invalid
     */
    public static String transformRequisitionCode(String legacyReqCode) {
        if (!DataValidationUtils.isNotEmpty(legacyReqCode)) {
            throw new IllegalArgumentException("Requisition code cannot be null or empty");
        }

        String cleanCode = legacyReqCode.trim();

        // Handle different legacy formats
        if (REQUISITION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateRequisitionCode();
        }
    }

    /**
     * Safely transforms requisition code with error handling.
     * @param legacyReqCode The legacy requisition code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday requisition code or default
     */
    public static String safeTransformRequisitionCode(String legacyReqCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformRequisitionCode(legacyReqCode),
            defaultCode,
            "requisition code transformation"
        );
    }

    /**
     * Transforms legacy RFQ code to Workday standard format (RFQ-YYYY-NNNN).
     * @param legacyRfqCode The legacy RFQ code
     * @return Standardized Workday RFQ code
     * @throws IllegalArgumentException if RFQ code format is invalid
     */
    public static String transformRfqCode(String legacyRfqCode) {
        if (!DataValidationUtils.isNotEmpty(legacyRfqCode)) {
            throw new IllegalArgumentException("RFQ code cannot be null or empty");
        }

        String cleanCode = legacyRfqCode.trim();

        // Handle different legacy formats
        if (RFQ_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateRfqCode();
        }
    }

    /**
     * Safely transforms RFQ code with error handling.
     * @param legacyRfqCode The legacy RFQ code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday RFQ code or default
     */
    public static String safeTransformRfqCode(String legacyRfqCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformRfqCode(legacyRfqCode),
            defaultCode,
            "RFQ code transformation"
        );
    }

    /**
     * Standardizes purchase order status description.
     * @param legacyStatus The legacy purchase order status
     * @return Standardized purchase order status
     */
    public static String standardizePurchaseOrderStatus(String legacyStatus) {
        if (!DataValidationUtils.isNotEmpty(legacyStatus)) {
            return "Unknown Status";
        }

        String cleanStatus = legacyStatus.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedStatus = PURCHASE_ORDER_STATUS_MAPPINGS.get(cleanStatus);
        if (mappedStatus != null) {
            return mappedStatus;
        }

        // Handle common variations
        if (cleanStatus.contains("OPEN") || cleanStatus.contains("ACTIVE")) {
            return PURCHASE_ORDER_STATUS_MAPPINGS.get("OPEN");
        } else if (cleanStatus.contains("CLOSE") || cleanStatus.contains("COMPLETE")) {
            return PURCHASE_ORDER_STATUS_MAPPINGS.get("CLOSED");
        } else if (cleanStatus.contains("CANCEL")) {
            return PURCHASE_ORDER_STATUS_MAPPINGS.get("CANCELLED");
        } else if (cleanStatus.contains("PEND") || cleanStatus.contains("WAIT")) {
            return PURCHASE_ORDER_STATUS_MAPPINGS.get("PENDING");
        } else if (cleanStatus.contains("APPROVE") || cleanStatus.contains("READY")) {
            return PURCHASE_ORDER_STATUS_MAPPINGS.get("APPROVED");
        }

        return cleanStatus;
    }

    /**
     * Safely standardizes purchase order status with error handling.
     * @param legacyStatus The legacy purchase order status
     * @param defaultStatus The default status to return if standardization fails
     * @return Standardized purchase order status or default
     */
    public static String safeStandardizePurchaseOrderStatus(String legacyStatus, String defaultStatus) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizePurchaseOrderStatus(legacyStatus),
            defaultStatus,
            "purchase order status standardization"
        );
    }

    /**
     * Standardizes vendor type description.
     * @param legacyType The legacy vendor type
     * @return Standardized vendor type
     */
    public static String standardizeVendorType(String legacyType) {
        if (!DataValidationUtils.isNotEmpty(legacyType)) {
            return "Unknown Vendor Type";
        }

        String cleanType = legacyType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = VENDOR_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("DIRECT") || cleanType.contains("PRIMARY")) {
            return VENDOR_TYPE_MAPPINGS.get("DIRECT");
        } else if (cleanType.contains("INDIRECT") || cleanType.contains("SERVICE")) {
            return VENDOR_TYPE_MAPPINGS.get("INDIRECT");
        } else if (cleanType.contains("STRATEGIC") || cleanType.contains("PARTNER")) {
            return VENDOR_TYPE_MAPPINGS.get("STRATEGIC");
        } else if (cleanType.contains("PREFERRED") || cleanType.contains("APPROVED")) {
            return VENDOR_TYPE_MAPPINGS.get("PREFERRED");
        }

        return cleanType;
    }

    /**
     * Safely standardizes vendor type with error handling.
     * @param legacyType The legacy vendor type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized vendor type or default
     */
    public static String safeStandardizeVendorType(String legacyType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeVendorType(legacyType),
            defaultType,
            "vendor type standardization"
        );
    }

    /**
     * Calculates vendor performance rating based on multiple criteria.
     * @param qualityScore Quality score (0-5)
     * @param deliveryScore Delivery score (0-5)
     * @param costScore Cost score (0-5)
     * @param complianceScore Compliance score (0-5)
     * @return Overall vendor rating
     */
    public static BigDecimal calculateVendorRating(BigDecimal qualityScore, BigDecimal deliveryScore,
                                                  BigDecimal costScore, BigDecimal complianceScore) {
        if (qualityScore == null || deliveryScore == null || costScore == null || complianceScore == null) {
            return BigDecimal.ZERO;
        }

        // Weighted average: Quality 40%, Delivery 30%, Cost 20%, Compliance 10%
        BigDecimal weightedScore = qualityScore.multiply(new BigDecimal("0.40"))
                .add(deliveryScore.multiply(new BigDecimal("0.30")))
                .add(costScore.multiply(new BigDecimal("0.20")))
                .add(complianceScore.multiply(new BigDecimal("0.10")));

        return weightedScore.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines vendor rating category based on score.
     * @param rating The vendor rating score
     * @return Rating category (EXCELLENT, GOOD, AVERAGE, POOR)
     */
    public static String determineVendorRatingCategory(BigDecimal rating) {
        if (rating == null) {
            return "UNKNOWN";
        }

        if (rating.compareTo(VENDOR_RATING_THRESHOLDS.get("EXCELLENT")) >= 0) {
            return "EXCELLENT";
        } else if (rating.compareTo(VENDOR_RATING_THRESHOLDS.get("GOOD")) >= 0) {
            return "GOOD";
        } else if (rating.compareTo(VENDOR_RATING_THRESHOLDS.get("AVERAGE")) >= 0) {
            return "AVERAGE";
        } else {
            return "POOR";
        }
    }

    /**
     * Calculates contract approval threshold based on contract value.
     * @param contractValue The total contract value
     * @return Approval level required (SMALL, MEDIUM, LARGE, EXECUTIVE)
     */
    public static String calculateContractApprovalLevel(BigDecimal contractValue) {
        if (contractValue == null || contractValue.compareTo(BigDecimal.ZERO) <= 0) {
            return "NONE";
        }

        if (contractValue.compareTo(CONTRACT_VALUE_THRESHOLDS.get("SMALL")) <= 0) {
            return "SMALL";
        } else if (contractValue.compareTo(CONTRACT_VALUE_THRESHOLDS.get("MEDIUM")) <= 0) {
            return "MEDIUM";
        } else if (contractValue.compareTo(CONTRACT_VALUE_THRESHOLDS.get("LARGE")) <= 0) {
            return "LARGE";
        } else {
            return "EXECUTIVE";
        }
    }

    /**
     * Validates if a purchase order code is in proper Workday format.
     * @param poCode The purchase order code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPurchaseOrderCode(String poCode) {
        return DataValidationUtils.isNotEmpty(poCode) && PURCHASE_ORDER_CODE_PATTERN.matcher(poCode.trim()).matches();
    }

    /**
     * Validates if a vendor code is in proper Workday format.
     * @param vendorCode The vendor code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidVendorCode(String vendorCode) {
        return DataValidationUtils.isNotEmpty(vendorCode) && VENDOR_CODE_PATTERN.matcher(vendorCode.trim()).matches();
    }

    /**
     * Validates if a contract code is in proper Workday format.
     * @param contractCode The contract code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidContractCode(String contractCode) {
        return DataValidationUtils.isNotEmpty(contractCode) && CONTRACT_CODE_PATTERN.matcher(contractCode.trim()).matches();
    }

    /**
     * Validates if a requisition code is in proper Workday format.
     * @param reqCode The requisition code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRequisitionCode(String reqCode) {
        return DataValidationUtils.isNotEmpty(reqCode) && REQUISITION_CODE_PATTERN.matcher(reqCode.trim()).matches();
    }

    /**
     * Validates if an RFQ code is in proper Workday format.
     * @param rfqCode The RFQ code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRfqCode(String rfqCode) {
        return DataValidationUtils.isNotEmpty(rfqCode) && RFQ_CODE_PATTERN.matcher(rfqCode.trim()).matches();
    }

    /**
     * Generates a procurement analytics summary for reporting purposes.
     * @param poCode The purchase order code
     * @param vendorCode The vendor code
     * @param contractCode The contract code
     * @param poStatus The purchase order status
     * @param vendorRating The vendor rating
     * @param contractValue The contract value
     * @return Formatted procurement analytics summary
     */
    public static String generateProcurementAnalyticsSummary(String poCode, String vendorCode, String contractCode,
                                                           String poStatus, BigDecimal vendorRating,
                                                           BigDecimal contractValue) {
        StringBuilder summary = new StringBuilder();
        summary.append("Purchase Order Code: ").append(safeTransformPurchaseOrderCode(poCode, "Not specified")).append("\n");
        summary.append("Vendor Code: ").append(safeTransformVendorCode(vendorCode, "Not specified")).append("\n");
        summary.append("Contract Code: ").append(safeTransformContractCode(contractCode, "Not applicable")).append("\n");
        summary.append("PO Status: ").append(standardizePurchaseOrderStatus(poStatus)).append("\n");
        summary.append("Vendor Rating: ").append(vendorRating != null ? vendorRating.toString() : "Not rated").append("\n");
        summary.append("Contract Value: ").append(contractValue != null ? NumberFormattingUtils.formatCurrency(contractValue.doubleValue()) : "$0.00").append("\n");
        summary.append("Valid PO Code: ").append(isValidPurchaseOrderCode(poCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Vendor Code: ").append(isValidVendorCode(vendorCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Contract Code: ").append(isValidContractCode(contractCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a purchase order code based on current date.
     * @return Generated purchase order code
     */
    private static String generatePurchaseOrderCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("PO-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a vendor code based on vendor identifier.
     * @param vendorIdentifier The vendor identifier
     * @return Generated vendor code
     */
    private static String generateVendorCode(String vendorIdentifier) {
        String hashPart = String.format("%06d", Math.abs(vendorIdentifier.hashCode()) % 1000000);
        return "VENDOR-" + hashPart;
    }

    /**
     * Generates a contract code based on current date.
     * @return Generated contract code
     */
    private static String generateContractCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("CONTRACT-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a requisition code based on current date.
     * @return Generated requisition code
     */
    private static String generateRequisitionCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("REQ-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates an RFQ code based on current date.
     * @return Generated RFQ code
     */
    private static String generateRfqCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("RFQ-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String poCode = ProcurementUtilities.transformPurchaseOrderCode(legacyPoCode);
    // String vendorCode = ProcurementUtilities.transformVendorCode(legacyVendorCode);
    // String contractCode = ProcurementUtilities.transformContractCode(legacyContractCode);
    // String reqCode = ProcurementUtilities.transformRequisitionCode(legacyReqCode);
    // String rfqCode = ProcurementUtilities.transformRfqCode(legacyRfqCode);
    // String poStatus = ProcurementUtilities.standardizePurchaseOrderStatus(legacyStatus);
    // String vendorType = ProcurementUtilities.standardizeVendorType(legacyType);
    // BigDecimal vendorRating = ProcurementUtilities.calculateVendorRating(qualityScore, deliveryScore, costScore, complianceScore);
    // String ratingCategory = ProcurementUtilities.determineVendorRatingCategory(vendorRating);
    // String approvalLevel = ProcurementUtilities.calculateContractApprovalLevel(contractValue);
    // String summary = ProcurementUtilities.generateProcurementAnalyticsSummary(poCode, vendorCode, contractCode, poStatus, vendorRating, contractValue);
}