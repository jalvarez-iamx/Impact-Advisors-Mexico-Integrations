package com.company.workday.finance.gl;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility class for transforming General Ledger (GL) account data from legacy systems to Workday format.
 * Handles GL account numbers, types, hierarchies, and financial reporting structures.
 * Supports account classification, hierarchy mapping, and financial statement categorization.
 */
public class GLAccountTransformer {

    private static final Pattern GL_ACCOUNT_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{3}$");
    private static final Pattern LEGACY_GL_PATTERN = Pattern.compile("^\\d{6,8}$");

    // GL account type mappings
    private static final Map<String, String> ACCOUNT_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> ACCOUNT_HIERARCHY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> FINANCIAL_STATEMENT_MAPPINGS = new HashMap<>();

    static {
        // Account type mappings
        ACCOUNT_TYPE_MAPPINGS.put("ASSET", "Asset - Balance Sheet Account");
        ACCOUNT_TYPE_MAPPINGS.put("LIABILITY", "Liability - Balance Sheet Account");
        ACCOUNT_TYPE_MAPPINGS.put("EQUITY", "Equity - Balance Sheet Account");
        ACCOUNT_TYPE_MAPPINGS.put("REVENUE", "Revenue - Income Statement Account");
        ACCOUNT_TYPE_MAPPINGS.put("EXPENSE", "Expense - Income Statement Account");
        ACCOUNT_TYPE_MAPPINGS.put("COST_OF_SALES", "Cost of Sales - Income Statement Account");

        // Account hierarchy mappings
        ACCOUNT_HIERARCHY_MAPPINGS.put("1000", "Assets");
        ACCOUNT_HIERARCHY_MAPPINGS.put("2000", "Liabilities");
        ACCOUNT_HIERARCHY_MAPPINGS.put("3000", "Equity");
        ACCOUNT_HIERARCHY_MAPPINGS.put("4000", "Revenue");
        ACCOUNT_HIERARCHY_MAPPINGS.put("5000", "Cost of Sales");
        ACCOUNT_HIERARCHY_MAPPINGS.put("6000", "Operating Expenses");
        ACCOUNT_HIERARCHY_MAPPINGS.put("7000", "Other Income/Expenses");

        // Financial statement mappings
        FINANCIAL_STATEMENT_MAPPINGS.put("BALANCE_SHEET", "Balance Sheet");
        FINANCIAL_STATEMENT_MAPPINGS.put("INCOME_STATEMENT", "Income Statement");
        FINANCIAL_STATEMENT_MAPPINGS.put("CASH_FLOW", "Cash Flow Statement");
    }

    /**
     * Transforms legacy GL account number to Workday standard format (####-##-###).
     * @param legacyAccountNumber The legacy GL account number
     * @return Standardized Workday GL account number
     * @throws IllegalArgumentException if account number format is invalid
     */
    public static String transformGLAccountNumber(String legacyAccountNumber) {
        if (!DataValidationUtils.isNotEmpty(legacyAccountNumber)) {
            throw new IllegalArgumentException("GL account number cannot be null or empty");
        }

        String cleanNumber = legacyAccountNumber.trim().replaceAll("[^0-9]", "");

        // Handle different legacy formats
        if (GL_ACCOUNT_PATTERN.matcher(legacyAccountNumber).matches()) {
            return legacyAccountNumber;
        } else if (LEGACY_GL_PATTERN.matcher(cleanNumber).matches()) {
            // Convert legacy 6-8 digit format to Workday format
            return formatToWorkdayGL(cleanNumber);
        } else {
            throw new IllegalArgumentException("Invalid GL account number format: " + legacyAccountNumber);
        }
    }

    /**
     * Safely transforms GL account number with error handling.
     * @param legacyAccountNumber The legacy GL account number
     * @param defaultAccount The default account to return if transformation fails
     * @return Standardized Workday GL account number or default
     */
    public static String safeTransformGLAccountNumber(String legacyAccountNumber, String defaultAccount) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformGLAccountNumber(legacyAccountNumber),
            defaultAccount,
            "GL account number transformation"
        );
    }

    /**
     * Standardizes GL account type description.
     * @param legacyAccountType The legacy account type
     * @return Standardized GL account type
     */
    public static String standardizeGLAccountType(String legacyAccountType) {
        if (!DataValidationUtils.isNotEmpty(legacyAccountType)) {
            return "Unknown Account Type";
        }

        String cleanType = legacyAccountType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = ACCOUNT_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("ASSET") || cleanType.equals("A")) {
            return ACCOUNT_TYPE_MAPPINGS.get("ASSET");
        } else if (cleanType.contains("LIABIL") || cleanType.equals("L")) {
            return ACCOUNT_TYPE_MAPPINGS.get("LIABILITY");
        } else if (cleanType.contains("EQUITY") || cleanType.equals("E")) {
            return ACCOUNT_TYPE_MAPPINGS.get("EQUITY");
        } else if (cleanType.contains("REVENUE") || cleanType.contains("INCOME") || cleanType.equals("R")) {
            return ACCOUNT_TYPE_MAPPINGS.get("REVENUE");
        } else if (cleanType.contains("EXPENSE") || cleanType.equals("X")) {
            return ACCOUNT_TYPE_MAPPINGS.get("EXPENSE");
        } else if (cleanType.contains("COST") || cleanType.contains("COS")) {
            return ACCOUNT_TYPE_MAPPINGS.get("COST_OF_SALES");
        }

        return cleanType;
    }

    /**
     * Safely standardizes GL account type with error handling.
     * @param legacyAccountType The legacy account type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized GL account type or default
     */
    public static String safeStandardizeGLAccountType(String legacyAccountType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeGLAccountType(legacyAccountType),
            defaultType,
            "GL account type standardization"
        );
    }

    /**
     * Determines GL account hierarchy level from account number.
     * @param accountNumber The GL account number
     * @return Account hierarchy level description
     */
    public static String determineGLAccountHierarchy(String accountNumber) {
        if (!DataValidationUtils.isNotEmpty(accountNumber)) {
            return "Unknown Hierarchy";
        }

        String cleanNumber = accountNumber.trim();
        if (GL_ACCOUNT_PATTERN.matcher(cleanNumber).matches()) {
            String[] parts = cleanNumber.split("-");
            String hierarchyCode = parts[0];
            return ACCOUNT_HIERARCHY_MAPPINGS.getOrDefault(hierarchyCode, "Custom Account");
        } else {
            // Try to extract hierarchy from legacy format
            String hierarchyCode = cleanNumber.substring(0, Math.min(4, cleanNumber.length()));
            return ACCOUNT_HIERARCHY_MAPPINGS.getOrDefault(hierarchyCode, "Legacy Account");
        }
    }

    /**
     * Maps GL account to financial statement category.
     * @param accountType The account type
     * @return Financial statement category
     */
    public static String mapToFinancialStatement(String accountType) {
        if (!DataValidationUtils.isNotEmpty(accountType)) {
            return "Unknown Statement";
        }

        String upperType = accountType.toUpperCase();

        if (upperType.contains("ASSET") || upperType.contains("LIABIL") || upperType.contains("EQUITY")) {
            return FINANCIAL_STATEMENT_MAPPINGS.get("BALANCE_SHEET");
        } else if (upperType.contains("REVENUE") || upperType.contains("EXPENSE") || upperType.contains("COST")) {
            return FINANCIAL_STATEMENT_MAPPINGS.get("INCOME_STATEMENT");
        } else {
            return FINANCIAL_STATEMENT_MAPPINGS.get("CASH_FLOW");
        }
    }

    /**
     * Validates if a GL account number is in proper Workday format.
     * @param accountNumber The account number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGLAccountNumber(String accountNumber) {
        return DataValidationUtils.isNotEmpty(accountNumber) && GL_ACCOUNT_PATTERN.matcher(accountNumber.trim()).matches();
    }

    /**
     * Generates a GL account summary for reporting purposes.
     * @param accountNumber The account number
     * @param accountName The account name
     * @param accountType The account type
     * @return Formatted GL account summary
     */
    public static String generateGLAccountSummary(String accountNumber, String accountName, String accountType) {
        StringBuilder summary = new StringBuilder();
        summary.append("Account Number: ").append(transformGLAccountNumber(accountNumber)).append("\n");
        summary.append("Account Name: ").append(accountName != null ? accountName : "Not specified").append("\n");
        summary.append("Account Type: ").append(standardizeGLAccountType(accountType)).append("\n");
        summary.append("Hierarchy: ").append(determineGLAccountHierarchy(accountNumber)).append("\n");
        summary.append("Financial Statement: ").append(mapToFinancialStatement(accountType)).append("\n");
        summary.append("Valid Format: ").append(isValidGLAccountNumber(accountNumber) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Formats a legacy GL number to Workday format.
     * @param legacyNumber The legacy number (6-8 digits)
     * @return Formatted Workday GL number
     */
    private static String formatToWorkdayGL(String legacyNumber) {
        // Pad or truncate to 8 digits, then format as ####-##-###
        String padded = String.format("%8s", legacyNumber).replace(' ', '0');
        return padded.substring(0, 4) + "-" + padded.substring(4, 6) + "-" + padded.substring(6, 9);
    }

    // Copy-paste usage examples:
    // String accountNumber = GLAccountTransformer.transformGLAccountNumber(legacyAccount);
    // String accountType = GLAccountTransformer.standardizeGLAccountType(legacyType);
    // String hierarchy = GLAccountTransformer.determineGLAccountHierarchy(accountNumber);
    // String statement = GLAccountTransformer.mapToFinancialStatement(accountType);
    // String summary = GLAccountTransformer.generateGLAccountSummary(accountNumber, name, type);
}