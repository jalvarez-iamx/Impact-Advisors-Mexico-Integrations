package com.company.workday.finance.transactions;

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
 * Utility class for transforming financial transaction data from legacy systems to Workday format.
 * Handles journal entries, debits/credits, currencies, and transaction processing.
 * Supports transaction validation, currency conversion, and financial posting rules.
 */
public class TransactionTransformer {

    private static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile("^TXN-\\d{8}-\\d{4}$");
    private static final Pattern LEGACY_TRANSACTION_PATTERN = Pattern.compile("^\\d{10,12}$");
    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");

    // Transaction type mappings
    private static final Map<String, String> TRANSACTION_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> JOURNAL_ENTRY_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> CURRENCY_RATES = new HashMap<>();

    static {
        // Transaction type mappings
        TRANSACTION_TYPE_MAPPINGS.put("DEBIT", "Debit - Money flowing out");
        TRANSACTION_TYPE_MAPPINGS.put("CREDIT", "Credit - Money flowing in");
        TRANSACTION_TYPE_MAPPINGS.put("ADJUSTMENT", "Adjustment - Balance correction");
        TRANSACTION_TYPE_MAPPINGS.put("REVERSAL", "Reversal - Transaction reversal");
        TRANSACTION_TYPE_MAPPINGS.put("TRANSFER", "Transfer - Inter-account movement");

        // Journal entry type mappings
        JOURNAL_ENTRY_TYPE_MAPPINGS.put("JE", "Standard Journal Entry");
        JOURNAL_ENTRY_TYPE_MAPPINGS.put("AJE", "Adjusting Journal Entry");
        JOURNAL_ENTRY_TYPE_MAPPINGS.put("RJE", "Reversing Journal Entry");
        JOURNAL_ENTRY_TYPE_MAPPINGS.put("CJE", "Closing Journal Entry");

        // Sample currency exchange rates (base: USD)
        CURRENCY_RATES.put("USD", BigDecimal.ONE);
        CURRENCY_RATES.put("EUR", new BigDecimal("0.85"));
        CURRENCY_RATES.put("GBP", new BigDecimal("0.73"));
        CURRENCY_RATES.put("JPY", new BigDecimal("110.0"));
        CURRENCY_RATES.put("CAD", new BigDecimal("1.25"));
    }

    /**
     * Transforms legacy transaction ID to Workday standard format (TXN-YYYYMMDD-XXXX).
     * @param legacyTransactionId The legacy transaction ID
     * @return Standardized Workday transaction ID
     * @throws IllegalArgumentException if transaction ID format is invalid
     */
    public static String transformTransactionId(String legacyTransactionId) {
        if (!DataValidationUtils.isNotEmpty(legacyTransactionId)) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }

        String cleanId = legacyTransactionId.trim();

        // Handle different legacy formats
        if (TRANSACTION_ID_PATTERN.matcher(cleanId).matches()) {
            return cleanId;
        } else if (LEGACY_TRANSACTION_PATTERN.matcher(cleanId.replaceAll("[^0-9]", "")).matches()) {
            // Convert legacy numeric format to Workday format
            return formatToWorkdayTransactionId(cleanId);
        } else {
            throw new IllegalArgumentException("Invalid transaction ID format: " + legacyTransactionId);
        }
    }

    /**
     * Safely transforms transaction ID with error handling.
     * @param legacyTransactionId The legacy transaction ID
     * @param defaultId The default ID to return if transformation fails
     * @return Standardized Workday transaction ID or default
     */
    public static String safeTransformTransactionId(String legacyTransactionId, String defaultId) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformTransactionId(legacyTransactionId),
            defaultId,
            "transaction ID transformation"
        );
    }

    /**
     * Standardizes transaction type description.
     * @param legacyTransactionType The legacy transaction type
     * @return Standardized transaction type
     */
    public static String standardizeTransactionType(String legacyTransactionType) {
        if (!DataValidationUtils.isNotEmpty(legacyTransactionType)) {
            return "Unknown Transaction Type";
        }

        String cleanType = legacyTransactionType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = TRANSACTION_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("DEBIT") || cleanType.equals("DR") || cleanType.equals("D")) {
            return TRANSACTION_TYPE_MAPPINGS.get("DEBIT");
        } else if (cleanType.contains("CREDIT") || cleanType.equals("CR") || cleanType.equals("C")) {
            return TRANSACTION_TYPE_MAPPINGS.get("CREDIT");
        } else if (cleanType.contains("ADJUST") || cleanType.contains("CORRECT")) {
            return TRANSACTION_TYPE_MAPPINGS.get("ADJUSTMENT");
        } else if (cleanType.contains("REVERSE") || cleanType.contains("VOID")) {
            return TRANSACTION_TYPE_MAPPINGS.get("REVERSAL");
        } else if (cleanType.contains("TRANSFER") || cleanType.contains("MOVE")) {
            return TRANSACTION_TYPE_MAPPINGS.get("TRANSFER");
        }

        return cleanType;
    }

    /**
     * Safely standardizes transaction type with error handling.
     * @param legacyTransactionType The legacy transaction type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized transaction type or default
     */
    public static String safeStandardizeTransactionType(String legacyTransactionType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeTransactionType(legacyTransactionType),
            defaultType,
            "transaction type standardization"
        );
    }

    /**
     * Validates and standardizes currency code.
     * @param currencyCode The currency code to validate
     * @return Standardized currency code (uppercase 3-letter)
     */
    public static String standardizeCurrencyCode(String currencyCode) {
        if (!DataValidationUtils.isNotEmpty(currencyCode)) {
            return "USD"; // Default to USD
        }

        String cleanCode = currencyCode.trim().toUpperCase();

        if (CURRENCY_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Handle common variations
            switch (cleanCode) {
                case "$":
                case "DOLLAR":
                case "DOLLARS":
                    return "USD";
                case "€":
                case "EURO":
                case "EUROS":
                    return "EUR";
                case "£":
                case "POUND":
                case "POUNDS":
                    return "GBP";
                case "¥":
                case "YEN":
                    return "JPY";
                case "C$":
                case "CAD":
                    return "CAD";
                default:
                    return "USD"; // Default fallback
            }
        }
    }

    /**
     * Converts transaction amount to target currency.
     * @param amount The transaction amount
     * @param fromCurrency The source currency
     * @param toCurrency The target currency
     * @return Converted amount
     */
    public static BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        String from = standardizeCurrencyCode(fromCurrency);
        String to = standardizeCurrencyCode(toCurrency);

        if (from.equals(to)) {
            return amount;
        }

        BigDecimal fromRate = CURRENCY_RATES.getOrDefault(from, BigDecimal.ONE);
        BigDecimal toRate = CURRENCY_RATES.getOrDefault(to, BigDecimal.ONE);

        // Convert to USD first, then to target currency
        BigDecimal usdAmount = amount.divide(fromRate, 4, BigDecimal.ROUND_HALF_UP);
        return usdAmount.multiply(toRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Validates journal entry balance (debits must equal credits).
     * @param debitAmount The debit amount
     * @param creditAmount The credit amount
     * @return true if balanced, false otherwise
     */
    public static boolean isJournalEntryBalanced(BigDecimal debitAmount, BigDecimal creditAmount) {
        if (debitAmount == null || creditAmount == null) {
            return false;
        }
        return debitAmount.compareTo(creditAmount) == 0;
    }

    /**
     * Determines journal entry type from description.
     * @param description The journal entry description
     * @return Journal entry type
     */
    public static String determineJournalEntryType(String description) {
        if (!DataValidationUtils.isNotEmpty(description)) {
            return "JE"; // Default to standard journal entry
        }

        String upperDesc = description.toUpperCase();

        if (upperDesc.contains("ADJUST") || upperDesc.contains("CORRECT")) {
            return "AJE";
        } else if (upperDesc.contains("REVERSE") || upperDesc.contains("REVERSAL")) {
            return "RJE";
        } else if (upperDesc.contains("CLOSE") || upperDesc.contains("CLOSING")) {
            return "CJE";
        } else {
            return "JE";
        }
    }

    /**
     * Validates if a transaction ID is in proper Workday format.
     * @param transactionId The transaction ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTransactionId(String transactionId) {
        return DataValidationUtils.isNotEmpty(transactionId) && TRANSACTION_ID_PATTERN.matcher(transactionId.trim()).matches();
    }

    /**
     * Generates a transaction summary for reporting purposes.
     * @param transactionId The transaction ID
     * @param transactionType The transaction type
     * @param amount The transaction amount
     * @param currency The transaction currency
     * @param description The transaction description
     * @return Formatted transaction summary
     */
    public static String generateTransactionSummary(String transactionId, String transactionType,
                                                  BigDecimal amount, String currency, String description) {
        StringBuilder summary = new StringBuilder();
        summary.append("Transaction ID: ").append(transformTransactionId(transactionId)).append("\n");
        summary.append("Type: ").append(standardizeTransactionType(transactionType)).append("\n");
        summary.append("Amount: ").append(amount != null ? amount.toString() : "0.00").append(" ")
               .append(standardizeCurrencyCode(currency)).append("\n");
        summary.append("Description: ").append(description != null ? description : "Not specified").append("\n");
        summary.append("Journal Type: ").append(JOURNAL_ENTRY_TYPE_MAPPINGS.get(determineJournalEntryType(description))).append("\n");
        summary.append("Valid ID: ").append(isValidTransactionId(transactionId) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Formats a legacy transaction ID to Workday format.
     * @param legacyId The legacy transaction ID
     * @return Formatted Workday transaction ID
     */
    private static String formatToWorkdayTransactionId(String legacyId) {
        String numericId = legacyId.replaceAll("[^0-9]", "");
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", Integer.parseInt(numericId.substring(Math.max(0, numericId.length() - 4))));
        return "TXN-" + datePart + "-" + sequencePart;
    }

    // Copy-paste usage examples:
    // String transactionId = TransactionTransformer.transformTransactionId(legacyId);
    // String transactionType = TransactionTransformer.standardizeTransactionType(legacyType);
    // String currencyCode = TransactionTransformer.standardizeCurrencyCode(legacyCurrency);
    // BigDecimal convertedAmount = TransactionTransformer.convertCurrency(amount, fromCurrency, toCurrency);
    // boolean balanced = TransactionTransformer.isJournalEntryBalanced(debitAmount, creditAmount);
    // String summary = TransactionTransformer.generateTransactionSummary(id, type, amount, currency, description);
}