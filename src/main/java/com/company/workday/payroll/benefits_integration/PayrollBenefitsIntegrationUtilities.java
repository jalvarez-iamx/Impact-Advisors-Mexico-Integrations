package com.company.workday.payroll.benefits_integration;

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
 * Utility class for transforming payroll benefits integration data from legacy systems to Workday format.
 * Handles COBRA administration, retirement plan matching, benefit elections, and benefits integration specific to healthcare organizations.
 * Supports HIPAA compliance, ACA requirements, and benefits enrollment processing.
 */
public class PayrollBenefitsIntegrationUtilities {

    private static final Pattern BENEFIT_ELECTION_CODE_PATTERN = Pattern.compile("^BEN-ELEC-\\d{4}-\\d{4}$");
    private static final Pattern COBRA_CODE_PATTERN = Pattern.compile("^COBRA-\\d{8}-\\d{4}$");
    private static final Pattern RETIREMENT_MATCH_CODE_PATTERN = Pattern.compile("^RET-MATCH-\\d{4}-\\d{3}$");

    // Benefits integration mappings
    private static final Map<String, String> BENEFIT_ELECTION_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> COBRA_STATUS_MAPPINGS = new HashMap<>();
    private static final Map<String, String> RETIREMENT_MATCH_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> COBRA_PREMIUM_RATES = new HashMap<>();
    private static final Map<String, BigDecimal> RETIREMENT_MATCH_RATES = new HashMap<>();

    static {
        // Benefit election type mappings
        BENEFIT_ELECTION_TYPE_MAPPINGS.put("ENROLL", "Enroll - Initial enrollment in benefit plan");
        BENEFIT_ELECTION_TYPE_MAPPINGS.put("CHANGE", "Change - Change in benefit coverage");
        BENEFIT_ELECTION_TYPE_MAPPINGS.put("CANCEL", "Cancel - Cancellation of benefit coverage");
        BENEFIT_ELECTION_TYPE_MAPPINGS.put("WAIVE", "Waive - Waiver of benefit coverage");
        BENEFIT_ELECTION_TYPE_MAPPINGS.put("OPEN_ENROLLMENT", "Open Enrollment - Annual enrollment period");
        BENEFIT_ELECTION_TYPE_MAPPINGS.put("QUALIFYING_EVENT", "Qualifying Event - Life event change");

        // COBRA status mappings
        COBRA_STATUS_MAPPINGS.put("ACTIVE", "Active - Currently enrolled in COBRA");
        COBRA_STATUS_MAPPINGS.put("PENDING", "Pending - COBRA election pending");
        COBRA_STATUS_MAPPINGS.put("EXPIRED", "Expired - COBRA coverage expired");
        COBRA_STATUS_MAPPINGS.put("TERMINATED", "Terminated - COBRA coverage terminated");
        COBRA_STATUS_MAPPINGS.put("WAIVED", "Waived - COBRA coverage waived");

        // Retirement match type mappings
        RETIREMENT_MATCH_TYPE_MAPPINGS.put("FIXED_MATCH", "Fixed Match - Fixed percentage employer match");
        RETIREMENT_MATCH_TYPE_MAPPINGS.put("GRADUATED_MATCH", "Graduated Match - Tiered employer match");
        RETIREMENT_MATCH_TYPE_MAPPINGS.put("NON_ELECTIVE", "Non-Elective - Automatic employer contribution");
        RETIREMENT_MATCH_TYPE_MAPPINGS.put("SAFE_HARBOR", "Safe Harbor - Safe harbor employer match");
        RETIREMENT_MATCH_TYPE_MAPPINGS.put("PROFIT_SHARING", "Profit Sharing - Performance-based employer contribution");

        // COBRA premium rates (multipliers of regular premium)
        COBRA_PREMIUM_RATES.put("SELF_ONLY", new BigDecimal("2.00")); // 200% of regular premium
        COBRA_PREMIUM_RATES.put("SELF_FAMILY", new BigDecimal("2.00")); // 200% of regular premium
        COBRA_PREMIUM_RATES.put("MAX_PERIOD", new BigDecimal("36")); // Maximum 36 months

        // Retirement match rates
        RETIREMENT_MATCH_RATES.put("FIXED_50_PERCENT", new BigDecimal("0.50")); // 50% match
        RETIREMENT_MATCH_RATES.put("DOLLAR_FOR_DOLLAR", new BigDecimal("1.00")); // $1 for $1 match
        RETIREMENT_MATCH_RATES.put("UP_TO_6_PERCENT", new BigDecimal("0.06")); // Up to 6% match
        RETIREMENT_MATCH_RATES.put("GRADUATED_TIER1", new BigDecimal("1.00")); // 100% on first 3%
        RETIREMENT_MATCH_RATES.put("GRADUATED_TIER2", new BigDecimal("0.50")); // 50% on next 3%
    }

    /**
     * Transforms legacy benefit election code to Workday standard format (BEN-ELEC-YYYY-XXXX).
     * @param legacyElectionCode The legacy benefit election code
     * @return Standardized Workday benefit election code
     * @throws IllegalArgumentException if election code format is invalid
     */
    public static String transformBenefitElectionCode(String legacyElectionCode) {
        if (!DataValidationUtils.isNotEmpty(legacyElectionCode)) {
            throw new IllegalArgumentException("Benefit election code cannot be null or empty");
        }

        String cleanCode = legacyElectionCode.trim();

        // Handle different legacy formats
        if (BENEFIT_ELECTION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateBenefitElectionCode();
        }
    }

    /**
     * Safely transforms benefit election code with error handling.
     * @param legacyElectionCode The legacy benefit election code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday benefit election code or default
     */
    public static String safeTransformBenefitElectionCode(String legacyElectionCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformBenefitElectionCode(legacyElectionCode),
            defaultCode,
            "benefit election code transformation"
        );
    }

    /**
     * Transforms legacy COBRA code to Workday standard format (COBRA-YYYYMMDD-XXXX).
     * @param legacyCobraCode The legacy COBRA code
     * @return Standardized Workday COBRA code
     * @throws IllegalArgumentException if COBRA code format is invalid
     */
    public static String transformCobraCode(String legacyCobraCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCobraCode)) {
            throw new IllegalArgumentException("COBRA code cannot be null or empty");
        }

        String cleanCode = legacyCobraCode.trim();

        // Handle different legacy formats
        if (COBRA_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCobraCode();
        }
    }

    /**
     * Safely transforms COBRA code with error handling.
     * @param legacyCobraCode The legacy COBRA code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday COBRA code or default
     */
    public static String safeTransformCobraCode(String legacyCobraCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCobraCode(legacyCobraCode),
            defaultCode,
            "COBRA code transformation"
        );
    }

    /**
     * Transforms legacy retirement match code to Workday standard format (RET-MATCH-YYYY-XXX).
     * @param legacyMatchCode The legacy retirement match code
     * @return Standardized Workday retirement match code
     * @throws IllegalArgumentException if match code format is invalid
     */
    public static String transformRetirementMatchCode(String legacyMatchCode) {
        if (!DataValidationUtils.isNotEmpty(legacyMatchCode)) {
            throw new IllegalArgumentException("Retirement match code cannot be null or empty");
        }

        String cleanCode = legacyMatchCode.trim();

        // Handle different legacy formats
        if (RETIREMENT_MATCH_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on match type
            return generateRetirementMatchCodeFromType(cleanCode);
        }
    }

    /**
     * Safely transforms retirement match code with error handling.
     * @param legacyMatchCode The legacy retirement match code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday retirement match code or default
     */
    public static String safeTransformRetirementMatchCode(String legacyMatchCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformRetirementMatchCode(legacyMatchCode),
            defaultCode,
            "retirement match code transformation"
        );
    }

    /**
     * Standardizes benefit election type description.
     * @param legacyElectionType The legacy benefit election type
     * @return Standardized benefit election type
     */
    public static String standardizeBenefitElectionType(String legacyElectionType) {
        if (!DataValidationUtils.isNotEmpty(legacyElectionType)) {
            return "Unknown Election Type";
        }

        String cleanType = legacyElectionType.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedType = BENEFIT_ELECTION_TYPE_MAPPINGS.get(cleanType);
        if (mappedType != null) {
            return mappedType;
        }

        // Handle common variations
        if (cleanType.contains("ENROLL") || cleanType.contains("NEW")) {
            return BENEFIT_ELECTION_TYPE_MAPPINGS.get("ENROLL");
        } else if (cleanType.contains("CHANGE") || cleanType.contains("MODIFY")) {
            return BENEFIT_ELECTION_TYPE_MAPPINGS.get("CHANGE");
        } else if (cleanType.contains("CANCEL") || cleanType.contains("TERMINATE")) {
            return BENEFIT_ELECTION_TYPE_MAPPINGS.get("CANCEL");
        } else if (cleanType.contains("WAIVE") || cleanType.contains("DECLINE")) {
            return BENEFIT_ELECTION_TYPE_MAPPINGS.get("WAIVE");
        } else if (cleanType.contains("OPEN") || cleanType.contains("ANNUAL")) {
            return BENEFIT_ELECTION_TYPE_MAPPINGS.get("OPEN_ENROLLMENT");
        } else if (cleanType.contains("QUALIFY") || cleanType.contains("LIFE_EVENT")) {
            return BENEFIT_ELECTION_TYPE_MAPPINGS.get("QUALIFYING_EVENT");
        }

        return cleanType;
    }

    /**
     * Safely standardizes benefit election type with error handling.
     * @param legacyElectionType The legacy benefit election type
     * @param defaultType The default type to return if standardization fails
     * @return Standardized benefit election type or default
     */
    public static String safeStandardizeBenefitElectionType(String legacyElectionType, String defaultType) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeBenefitElectionType(legacyElectionType),
            defaultType,
            "benefit election type standardization"
        );
    }

    /**
     * Calculates COBRA premium amount based on regular premium and coverage type.
     * @param regularPremium The regular monthly premium amount
     * @param coverageType The coverage type (SELF_ONLY, SELF_FAMILY)
     * @return COBRA premium amount
     */
    public static BigDecimal calculateCobraPremium(BigDecimal regularPremium, String coverageType) {
        if (regularPremium == null || regularPremium.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal multiplier = COBRA_PREMIUM_RATES.getOrDefault(
            coverageType != null ? coverageType.toUpperCase() : "SELF_ONLY",
            COBRA_PREMIUM_RATES.get("SELF_ONLY"));

        BigDecimal cobraPremium = regularPremium.multiply(multiplier);
        return cobraPremium.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates employer retirement match contribution.
     * @param employeeContribution The employee contribution amount
     * @param matchType The match type (FIXED_50_PERCENT, DOLLAR_FOR_DOLLAR, etc.)
     * @param employeeContributionPercent The employee contribution percentage (0-100)
     * @return Employer match amount
     */
    public static BigDecimal calculateRetirementMatch(BigDecimal employeeContribution, String matchType, BigDecimal employeeContributionPercent) {
        if (employeeContribution == null || employeeContribution.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (!DataValidationUtils.isNotEmpty(matchType)) {
            matchType = "FIXED_50_PERCENT";
        }

        BigDecimal matchAmount;
        String upperMatchType = matchType.toUpperCase();

        switch (upperMatchType) {
            case "DOLLAR_FOR_DOLLAR":
                matchAmount = employeeContribution;
                break;
            case "FIXED_50_PERCENT":
                matchAmount = employeeContribution.multiply(new BigDecimal("0.50"));
                break;
            case "UP_TO_6_PERCENT":
                // Assume employee contributes up to 6%, employer matches up to 6%
                if (employeeContributionPercent != null && employeeContributionPercent.compareTo(new BigDecimal("6.00")) <= 0) {
                    matchAmount = employeeContribution;
                } else {
                    matchAmount = employeeContribution.multiply(new BigDecimal("6.00").divide(employeeContributionPercent, 4, BigDecimal.ROUND_HALF_UP));
                }
                break;
            case "GRADUATED_MATCH":
                // Simplified graduated match: 100% on first 3%, 50% on next 3%
                BigDecimal contributionPercent = employeeContributionPercent != null ? employeeContributionPercent : BigDecimal.ZERO;
                if (contributionPercent.compareTo(new BigDecimal("3.00")) <= 0) {
                    matchAmount = employeeContribution;
                } else if (contributionPercent.compareTo(new BigDecimal("6.00")) <= 0) {
                    BigDecimal firstTierMatch = employeeContribution.multiply(new BigDecimal("3.00").divide(contributionPercent, 4, BigDecimal.ROUND_HALF_UP));
                    BigDecimal remainingContribution = employeeContribution.subtract(firstTierMatch);
                    BigDecimal secondTierMatch = remainingContribution.multiply(new BigDecimal("0.50"));
                    matchAmount = firstTierMatch.add(secondTierMatch);
                } else {
                    matchAmount = employeeContribution.multiply(new BigDecimal("0.045")); // 4.5% average match
                }
                break;
            default:
                matchAmount = employeeContribution.multiply(new BigDecimal("0.50"));
        }

        return matchAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines COBRA eligibility based on qualifying event.
     * @param qualifyingEvent The qualifying event (TERMINATION, REDUCTION, DEATH, etc.)
     * @param employmentEndDate The employment end date
     * @return COBRA eligibility period in months
     */
    public static int determineCobraEligibility(String qualifyingEvent, LocalDate employmentEndDate) {
        if (!DataValidationUtils.isNotEmpty(qualifyingEvent)) {
            return 0;
        }

        String upperEvent = qualifyingEvent.toUpperCase();

        if (upperEvent.contains("TERMINATION") || upperEvent.contains("LAYOFF") || upperEvent.contains("REDUCTION")) {
            return 18; // 18 months for termination
        } else if (upperEvent.contains("DEATH") || upperEvent.contains("DIVORCE") || upperEvent.contains("DEPENDENT")) {
            return 36; // 36 months for other qualifying events
        } else if (upperEvent.contains("MEDICARE") || upperEvent.contains("DISABLED")) {
            return 29; // 29 months for Medicare/disability
        } else {
            return 18; // Default to 18 months
        }
    }

    /**
     * Validates if a benefit election code is in proper Workday format.
     * @param electionCode The benefit election code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBenefitElectionCode(String electionCode) {
        return DataValidationUtils.isNotEmpty(electionCode) && BENEFIT_ELECTION_CODE_PATTERN.matcher(electionCode.trim()).matches();
    }

    /**
     * Validates if a COBRA code is in proper Workday format.
     * @param cobraCode The COBRA code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCobraCode(String cobraCode) {
        return DataValidationUtils.isNotEmpty(cobraCode) && COBRA_CODE_PATTERN.matcher(cobraCode.trim()).matches();
    }

    /**
     * Validates if a retirement match code is in proper Workday format.
     * @param matchCode The retirement match code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRetirementMatchCode(String matchCode) {
        return DataValidationUtils.isNotEmpty(matchCode) && RETIREMENT_MATCH_CODE_PATTERN.matcher(matchCode.trim()).matches();
    }

    /**
     * Generates a benefits integration summary for reporting purposes.
     * @param electionCode The benefit election code
     * @param cobraCode The COBRA code
     * @param matchCode The retirement match code
     * @param electionType The benefit election type
     * @param cobraPremium The COBRA premium amount
     * @param employerMatch The employer match amount
     * @return Formatted benefits integration summary
     */
    public static String generateBenefitsIntegrationSummary(String electionCode, String cobraCode, String matchCode,
                                                          String electionType, BigDecimal cobraPremium,
                                                          BigDecimal employerMatch) {
        StringBuilder summary = new StringBuilder();
        summary.append("Election Code: ").append(safeTransformBenefitElectionCode(electionCode, "Not specified")).append("\n");
        summary.append("COBRA Code: ").append(safeTransformCobraCode(cobraCode, "Not applicable")).append("\n");
        summary.append("Match Code: ").append(safeTransformRetirementMatchCode(matchCode, "Not specified")).append("\n");
        summary.append("Election Type: ").append(standardizeBenefitElectionType(electionType)).append("\n");
        summary.append("COBRA Premium: ").append(cobraPremium != null ? NumberFormattingUtils.formatCurrency(cobraPremium.doubleValue()) : "$0.00").append("\n");
        summary.append("Employer Match: ").append(employerMatch != null ? NumberFormattingUtils.formatCurrency(employerMatch.doubleValue()) : "$0.00").append("\n");
        summary.append("Valid Election Code: ").append(isValidBenefitElectionCode(electionCode) ? "Yes" : "No").append("\n");
        summary.append("Valid COBRA Code: ").append(isValidCobraCode(cobraCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Match Code: ").append(isValidRetirementMatchCode(matchCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a benefit election code based on current date.
     * @return Generated benefit election code
     */
    private static String generateBenefitElectionCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("BEN-ELEC-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a COBRA code based on current date.
     * @return Generated COBRA code
     */
    private static String generateCobraCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return "COBRA-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates a retirement match code based on match type.
     * @param matchType The match type description
     * @return Generated retirement match code
     */
    private static String generateRetirementMatchCodeFromType(String matchType) {
        String upperType = matchType.toUpperCase();
        String codeSuffix;

        if (upperType.contains("FIXED") || upperType.contains("FLAT")) {
            codeSuffix = "001";
        } else if (upperType.contains("GRADUATED") || upperType.contains("TIER")) {
            codeSuffix = "002";
        } else if (upperType.contains("NON") || upperType.contains("AUTO")) {
            codeSuffix = "003";
        } else if (upperType.contains("SAFE") || upperType.contains("HARBOR")) {
            codeSuffix = "004";
        } else if (upperType.contains("PROFIT") || upperType.contains("PERFORMANCE")) {
            codeSuffix = "005";
        } else {
            codeSuffix = "000";
        }

        LocalDate now = LocalDate.now();
        return String.format("RET-MATCH-%d-%s", now.getYear(), codeSuffix);
    }

    // Copy-paste usage examples:
    // String electionCode = PayrollBenefitsIntegrationUtilities.transformBenefitElectionCode(legacyCode);
    // String cobraCode = PayrollBenefitsIntegrationUtilities.transformCobraCode(legacyCobraCode);
    // String matchCode = PayrollBenefitsIntegrationUtilities.transformRetirementMatchCode(legacyMatchCode);
    // String electionType = PayrollBenefitsIntegrationUtilities.standardizeBenefitElectionType(legacyType);
    // BigDecimal cobraPremium = PayrollBenefitsIntegrationUtilities.calculateCobraPremium(regularPremium, "SELF_FAMILY");
    // BigDecimal employerMatch = PayrollBenefitsIntegrationUtilities.calculateRetirementMatch(employeeContribution, "FIXED_50_PERCENT", new BigDecimal("6.00"));
    // int cobraMonths = PayrollBenefitsIntegrationUtilities.determineCobraEligibility("TERMINATION", employmentEndDate);
    // String summary = PayrollBenefitsIntegrationUtilities.generateBenefitsIntegrationSummary(electionCode, cobraCode, matchCode, electionType, cobraPremium, employerMatch);
}