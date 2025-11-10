package com.company.workday.payroll.taxes;

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
 * Utility class for transforming payroll taxes data from legacy systems to Workday format.
 * Handles withholding calculations, W2 processing, tax jurisdictions, and tax compliance specific to healthcare organizations.
 * Supports federal/state tax calculations, tax jurisdiction mapping, and W2 form processing.
 */
public class PayrollTaxesUtilities {

    private static final Pattern TAX_CODE_PATTERN = Pattern.compile("^TAX-\\d{4}-\\d{3}$");
    private static final Pattern W2_CODE_PATTERN = Pattern.compile("^W2-\\d{9}-\\d{4}$");
    private static final Pattern EIN_PATTERN = Pattern.compile("^\\d{2}-\\d{7}$");
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");

    // Tax jurisdiction mappings
    private static final Map<String, String> TAX_JURISDICTION_MAPPINGS = new HashMap<>();
    private static final Map<String, String> TAX_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> FEDERAL_TAX_BRACKETS = new HashMap<>();
    private static final Map<String, BigDecimal> STATE_TAX_RATES = new HashMap<>();
    private static final Map<String, BigDecimal> FICA_LIMITS = new HashMap<>();

    static {
        // Tax jurisdiction mappings (state codes)
        TAX_JURISDICTION_MAPPINGS.put("AL", "Alabama - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("AK", "Alaska - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("AZ", "Arizona - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("AR", "Arkansas - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("CA", "California - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("CO", "Colorado - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("CT", "Connecticut - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("DE", "Delaware - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("FL", "Florida - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("GA", "Georgia - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("HI", "Hawaii - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("ID", "Idaho - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("IL", "Illinois - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("IN", "Indiana - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("IA", "Iowa - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("KS", "Kansas - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("KY", "Kentucky - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("LA", "Louisiana - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("ME", "Maine - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MD", "Maryland - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MA", "Massachusetts - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MI", "Michigan - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MN", "Minnesota - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MS", "Mississippi - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MO", "Missouri - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("MT", "Montana - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NE", "Nebraska - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NV", "Nevada - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NH", "New Hampshire - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NJ", "New Jersey - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NM", "New Mexico - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NY", "New York - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("NC", "North Carolina - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("ND", "North Dakota - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("OH", "Ohio - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("OK", "Oklahoma - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("OR", "Oregon - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("PA", "Pennsylvania - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("RI", "Rhode Island - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("SC", "South Carolina - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("SD", "South Dakota - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("TN", "Tennessee - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("TX", "Texas - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("UT", "Utah - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("VT", "Vermont - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("VA", "Virginia - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("WA", "Washington - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("WV", "West Virginia - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("WI", "Wisconsin - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("WY", "Wyoming - State tax jurisdiction");
        TAX_JURISDICTION_MAPPINGS.put("DC", "District of Columbia - Federal district tax jurisdiction");

        // Tax type mappings
        TAX_TYPE_MAPPINGS.put("FEDERAL_INCOME", "Federal Income Tax - Federal withholding");
        TAX_TYPE_MAPPINGS.put("STATE_INCOME", "State Income Tax - State withholding");
        TAX_TYPE_MAPPINGS.put("SOCIAL_SECURITY", "Social Security Tax - FICA withholding");
        TAX_TYPE_MAPPINGS.put("MEDICARE", "Medicare Tax - FICA withholding");
        TAX_TYPE_MAPPINGS.put("LOCAL_INCOME", "Local Income Tax - City/county withholding");
        TAX_TYPE_MAPPINGS.put("UNEMPLOYMENT", "Unemployment Insurance - State unemployment tax");

        // Federal tax brackets (2024 single filer, effective rates)
        FEDERAL_TAX_BRACKETS.put("10%", new BigDecimal("11000.00")); // 10% bracket up to $11,000
        FEDERAL_TAX_BRACKETS.put("12%", new BigDecimal("44725.00")); // 12% bracket up to $44,725
        FEDERAL_TAX_BRACKETS.put("22%", new BigDecimal("95375.00")); // 22% bracket up to $95,375
        FEDERAL_TAX_BRACKETS.put("24%", new BigDecimal("182100.00")); // 24% bracket up to $182,100
        FEDERAL_TAX_BRACKETS.put("32%", new BigDecimal("231250.00")); // 32% bracket up to $231,250
        FEDERAL_TAX_BRACKETS.put("35%", new BigDecimal("578125.00")); // 35% bracket up to $578,125
        FEDERAL_TAX_BRACKETS.put("37%", new BigDecimal("999999999.00")); // 37% bracket over $578,125

        // State tax rates (average effective rates)
        STATE_TAX_RATES.put("CA", new BigDecimal("8.00"));
        STATE_TAX_RATES.put("NY", new BigDecimal("6.85"));
        STATE_TAX_RATES.put("TX", new BigDecimal("0.00")); // No state income tax
        STATE_TAX_RATES.put("FL", new BigDecimal("0.00")); // No state income tax
        STATE_TAX_RATES.put("IL", new BigDecimal("4.95"));
        STATE_TAX_RATES.put("PA", new BigDecimal("3.07"));
        STATE_TAX_RATES.put("OH", new BigDecimal("2.85"));
        STATE_TAX_RATES.put("GA", new BigDecimal("5.75"));
        STATE_TAX_RATES.put("NC", new BigDecimal("5.25"));
        STATE_TAX_RATES.put("MI", new BigDecimal("4.25"));

        // FICA limits (2024)
        FICA_LIMITS.put("SOCIAL_SECURITY", new BigDecimal("168600.00")); // SS wage base
        FICA_LIMITS.put("MEDICARE", new BigDecimal("999999999.00")); // No limit for Medicare
    }

    /**
     * Transforms legacy tax code to Workday standard format (TAX-YYYY-XXX).
     * @param legacyTaxCode The legacy tax code
     * @return Standardized Workday tax code
     * @throws IllegalArgumentException if tax code format is invalid
     */
    public static String transformTaxCode(String legacyTaxCode) {
        if (!DataValidationUtils.isNotEmpty(legacyTaxCode)) {
            throw new IllegalArgumentException("Tax code cannot be null or empty");
        }

        String cleanCode = legacyTaxCode.trim();

        // Handle different legacy formats
        if (TAX_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on tax type
            return generateTaxCodeFromType(cleanCode);
        }
    }

    /**
     * Safely transforms tax code with error handling.
     * @param legacyTaxCode The legacy tax code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday tax code or default
     */
    public static String safeTransformTaxCode(String legacyTaxCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformTaxCode(legacyTaxCode),
            defaultCode,
            "tax code transformation"
        );
    }

    /**
     * Transforms legacy W2 code to Workday standard format (W2-SSSSSSSSS-YYYY).
     * @param legacyW2Code The legacy W2 code
     * @return Standardized Workday W2 code
     * @throws IllegalArgumentException if W2 code format is invalid
     */
    public static String transformW2Code(String legacyW2Code) {
        if (!DataValidationUtils.isNotEmpty(legacyW2Code)) {
            throw new IllegalArgumentException("W2 code cannot be null or empty");
        }

        String cleanCode = legacyW2Code.trim();

        // Handle different legacy formats
        if (W2_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on SSN and year
            return generateW2Code(cleanCode);
        }
    }

    /**
     * Safely transforms W2 code with error handling.
     * @param legacyW2Code The legacy W2 code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday W2 code or default
     */
    public static String safeTransformW2Code(String legacyW2Code, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformW2Code(legacyW2Code),
            defaultCode,
            "W2 code transformation"
        );
    }

    /**
     * Standardizes tax jurisdiction code.
     * @param legacyJurisdiction The legacy jurisdiction code
     * @return Standardized tax jurisdiction
     */
    public static String standardizeTaxJurisdiction(String legacyJurisdiction) {
        if (!DataValidationUtils.isNotEmpty(legacyJurisdiction)) {
            return "Unknown Jurisdiction";
        }

        String cleanJurisdiction = legacyJurisdiction.trim().toUpperCase();

        // Check for exact mappings first
        String mappedJurisdiction = TAX_JURISDICTION_MAPPINGS.get(cleanJurisdiction);
        if (mappedJurisdiction != null) {
            return mappedJurisdiction;
        }

        // Handle state name variations
        switch (cleanJurisdiction) {
            case "CALIFORNIA":
                return TAX_JURISDICTION_MAPPINGS.get("CA");
            case "NEW YORK":
                return TAX_JURISDICTION_MAPPINGS.get("NY");
            case "TEXAS":
                return TAX_JURISDICTION_MAPPINGS.get("TX");
            case "FLORIDA":
                return TAX_JURISDICTION_MAPPINGS.get("FL");
            case "ILLINOIS":
                return TAX_JURISDICTION_MAPPINGS.get("IL");
            case "PENNSYLVANIA":
                return TAX_JURISDICTION_MAPPINGS.get("PA");
            case "OHIO":
                return TAX_JURISDICTION_MAPPINGS.get("OH");
            case "GEORGIA":
                return TAX_JURISDICTION_MAPPINGS.get("GA");
            case "NORTH CAROLINA":
                return TAX_JURISDICTION_MAPPINGS.get("NC");
            case "MICHIGAN":
                return TAX_JURISDICTION_MAPPINGS.get("MI");
            default:
                return cleanJurisdiction;
        }
    }

    /**
     * Safely standardizes tax jurisdiction with error handling.
     * @param legacyJurisdiction The legacy jurisdiction code
     * @param defaultJurisdiction The default jurisdiction to return if standardization fails
     * @return Standardized tax jurisdiction or default
     */
    public static String safeStandardizeTaxJurisdiction(String legacyJurisdiction, String defaultJurisdiction) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeTaxJurisdiction(legacyJurisdiction),
            defaultJurisdiction,
            "tax jurisdiction standardization"
        );
    }

    /**
     * Calculates federal income tax withholding using tax brackets.
     * @param taxableIncome The taxable income amount
     * @param filingStatus The filing status (SINGLE, MARRIED_FILING_JOINTLY, etc.)
     * @return Federal income tax withholding amount
     */
    public static BigDecimal calculateFederalIncomeTax(BigDecimal taxableIncome, String filingStatus) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Simplified calculation using effective rates
        BigDecimal taxAmount;
        if (taxableIncome.compareTo(new BigDecimal("11000")) <= 0) {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.10"));
        } else if (taxableIncome.compareTo(new BigDecimal("44725")) <= 0) {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.12"));
        } else if (taxableIncome.compareTo(new BigDecimal("95375")) <= 0) {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.22"));
        } else if (taxableIncome.compareTo(new BigDecimal("182100")) <= 0) {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.24"));
        } else if (taxableIncome.compareTo(new BigDecimal("231250")) <= 0) {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.32"));
        } else if (taxableIncome.compareTo(new BigDecimal("578125")) <= 0) {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.35"));
        } else {
            taxAmount = taxableIncome.multiply(new BigDecimal("0.37"));
        }

        return taxAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates state income tax withholding.
     * @param taxableIncome The taxable income amount
     * @param stateCode The state code (CA, NY, TX, etc.)
     * @return State income tax withholding amount
     */
    public static BigDecimal calculateStateIncomeTax(BigDecimal taxableIncome, String stateCode) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (!DataValidationUtils.isNotEmpty(stateCode)) {
            return BigDecimal.ZERO;
        }

        BigDecimal stateRate = STATE_TAX_RATES.getOrDefault(stateCode.toUpperCase(), BigDecimal.ZERO);
        BigDecimal taxAmount = taxableIncome.multiply(stateRate.divide(new BigDecimal("100")));

        return taxAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates FICA taxes (Social Security and Medicare).
     * @param grossWages The gross wages amount
     * @param includeAdditionalMedicare Whether to include additional Medicare tax (0.9% for wages over $200,000)
     * @return FICA tax amount
     */
    public static BigDecimal calculateFICATaxes(BigDecimal grossWages, boolean includeAdditionalMedicare) {
        if (grossWages == null || grossWages.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal socialSecurityWages = grossWages.min(FICA_LIMITS.get("SOCIAL_SECURITY"));
        BigDecimal socialSecurityTax = socialSecurityWages.multiply(new BigDecimal("0.062"));

        BigDecimal medicareTax = grossWages.multiply(new BigDecimal("0.0145"));

        BigDecimal additionalMedicareTax = BigDecimal.ZERO;
        if (includeAdditionalMedicare && grossWages.compareTo(new BigDecimal("200000")) > 0) {
            BigDecimal excessWages = grossWages.subtract(new BigDecimal("200000"));
            additionalMedicareTax = excessWages.multiply(new BigDecimal("0.009"));
        }

        return socialSecurityTax.add(medicareTax).add(additionalMedicareTax).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Gets state tax rate for a given state code.
     * @param stateCode The state code (CA, NY, TX, etc.)
     * @return State tax rate as percentage or null if not found
     */
    public static BigDecimal getStateTaxRate(String stateCode) {
        if (!DataValidationUtils.isNotEmpty(stateCode)) {
            return null;
        }
        return STATE_TAX_RATES.get(stateCode.trim().toUpperCase());
    }

    /**
     * Validates if a tax code is in proper Workday format.
     * @param taxCode The tax code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTaxCode(String taxCode) {
        return DataValidationUtils.isNotEmpty(taxCode) && TAX_CODE_PATTERN.matcher(taxCode.trim()).matches();
    }

    /**
     * Validates if a W2 code is in proper Workday format.
     * @param w2Code The W2 code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidW2Code(String w2Code) {
        return DataValidationUtils.isNotEmpty(w2Code) && W2_CODE_PATTERN.matcher(w2Code.trim()).matches();
    }

    /**
     * Validates EIN (Employer Identification Number) format.
     * @param ein The EIN to validate
     * @return true if valid EIN format, false otherwise
     */
    public static boolean isValidEIN(String ein) {
        return DataValidationUtils.isNotEmpty(ein) && EIN_PATTERN.matcher(ein.trim()).matches();
    }

    /**
     * Validates SSN format.
     * @param ssn The SSN to validate
     * @return true if valid SSN format, false otherwise
     */
    public static boolean isValidSSN(String ssn) {
        return DataValidationUtils.isNotEmpty(ssn) && SSN_PATTERN.matcher(ssn.trim()).matches();
    }

    /**
     * Generates a tax summary for reporting purposes.
     * @param taxCode The tax code
     * @param jurisdiction The tax jurisdiction
     * @param grossIncome The gross income amount
     * @param federalTax The federal tax amount
     * @param stateTax The state tax amount
     * @param ficaTax The FICA tax amount
     * @return Formatted tax summary
     */
    public static String generateTaxSummary(String taxCode, String jurisdiction,
                                          BigDecimal grossIncome, BigDecimal federalTax,
                                          BigDecimal stateTax, BigDecimal ficaTax) {
        StringBuilder summary = new StringBuilder();
        summary.append("Tax Code: ").append(transformTaxCode(taxCode)).append("\n");
        summary.append("Jurisdiction: ").append(standardizeTaxJurisdiction(jurisdiction)).append("\n");
        summary.append("Gross Income: ").append(grossIncome != null ? NumberFormattingUtils.formatCurrency(grossIncome.doubleValue()) : "$0.00").append("\n");
        summary.append("Federal Tax: ").append(federalTax != null ? NumberFormattingUtils.formatCurrency(federalTax.doubleValue()) : "$0.00").append("\n");
        summary.append("State Tax: ").append(stateTax != null ? NumberFormattingUtils.formatCurrency(stateTax.doubleValue()) : "$0.00").append("\n");
        summary.append("FICA Tax: ").append(ficaTax != null ? NumberFormattingUtils.formatCurrency(ficaTax.doubleValue()) : "$0.00").append("\n");
        summary.append("Total Tax: ").append(calculateTotalTax(federalTax, stateTax, ficaTax)).append("\n");
        summary.append("Effective Tax Rate: ").append(calculateEffectiveTaxRate(grossIncome, federalTax, stateTax, ficaTax)).append("\n");
        summary.append("Valid Tax Code: ").append(isValidTaxCode(taxCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Calculates total tax amount.
     * @param federalTax The federal tax
     * @param stateTax The state tax
     * @param ficaTax The FICA tax
     * @return Total tax formatted as currency string
     */
    private static String calculateTotalTax(BigDecimal federalTax, BigDecimal stateTax, BigDecimal ficaTax) {
        BigDecimal total = (federalTax != null ? federalTax : BigDecimal.ZERO)
                          .add(stateTax != null ? stateTax : BigDecimal.ZERO)
                          .add(ficaTax != null ? ficaTax : BigDecimal.ZERO);
        return NumberFormattingUtils.formatCurrency(total.doubleValue());
    }

    /**
     * Calculates effective tax rate.
     * @param grossIncome The gross income
     * @param federalTax The federal tax
     * @param stateTax The state tax
     * @param ficaTax The FICA tax
     * @return Effective tax rate formatted as percentage string
     */
    private static String calculateEffectiveTaxRate(BigDecimal grossIncome, BigDecimal federalTax, BigDecimal stateTax, BigDecimal ficaTax) {
        if (grossIncome == null || grossIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return "0.00%";
        }

        BigDecimal totalTax = (federalTax != null ? federalTax : BigDecimal.ZERO)
                             .add(stateTax != null ? stateTax : BigDecimal.ZERO)
                             .add(ficaTax != null ? ficaTax : BigDecimal.ZERO);

        BigDecimal rate = totalTax.divide(grossIncome, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        return NumberFormattingUtils.formatPercentage(rate.doubleValue());
    }

    /**
     * Generates a tax code based on tax type.
     * @param taxType The tax type description
     * @return Generated tax code
     */
    private static String generateTaxCodeFromType(String taxType) {
        String upperType = taxType.toUpperCase();
        String codeSuffix;

        if (upperType.contains("FEDERAL") || upperType.contains("FIT")) {
            codeSuffix = "001";
        } else if (upperType.contains("STATE") || upperType.contains("SIT")) {
            codeSuffix = "002";
        } else if (upperType.contains("SOCIAL") || upperType.contains("SS")) {
            codeSuffix = "003";
        } else if (upperType.contains("MEDICARE") || upperType.contains("MEDI")) {
            codeSuffix = "004";
        } else if (upperType.contains("LOCAL") || upperType.contains("CITY")) {
            codeSuffix = "005";
        } else if (upperType.contains("UNEMPLOYMENT")) {
            codeSuffix = "006";
        } else {
            codeSuffix = "000";
        }

        LocalDate now = LocalDate.now();
        return String.format("TAX-%d-%s", now.getYear(), codeSuffix);
    }

    /**
     * Generates a W2 code based on SSN and tax year.
     * @param input The input string (could be SSN or existing code)
     * @return Generated W2 code
     */
    private static String generateW2Code(String input) {
        String ssn = input.replaceAll("[^0-9]", "");
        if (ssn.length() >= 9) {
            ssn = ssn.substring(0, 9);
        } else {
            ssn = String.format("%09d", Integer.parseInt(ssn));
        }

        LocalDate now = LocalDate.now();
        return String.format("W2-%s-%d", ssn, now.getYear());
    }

    // Copy-paste usage examples:
    // String taxCode = PayrollTaxesUtilities.transformTaxCode(legacyCode);
    // String w2Code = PayrollTaxesUtilities.transformW2Code(legacyW2Code);
    // String jurisdiction = PayrollTaxesUtilities.standardizeTaxJurisdiction(legacyJurisdiction);
    // BigDecimal federalTax = PayrollTaxesUtilities.calculateFederalIncomeTax(taxableIncome, "SINGLE");
    // BigDecimal stateTax = PayrollTaxesUtilities.calculateStateIncomeTax(taxableIncome, "CA");
    // BigDecimal ficaTax = PayrollTaxesUtilities.calculateFICATaxes(grossWages, true);
    // String summary = PayrollTaxesUtilities.generateTaxSummary(code, jurisdiction, grossIncome, federalTax, stateTax, ficaTax);
}