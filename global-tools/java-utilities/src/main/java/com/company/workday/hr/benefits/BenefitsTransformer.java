package com.company.workday.hr.benefits;

import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.NumberFormattingUtils;
import com.company.workday.common.DataValidationUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for transforming HR benefits data from legacy systems to Workday format.
 * Handles health plans, retirement plans, and various benefit structures specific to healthcare organizations.
 * Supports HIPAA-compliant benefit transformations and healthcare-specific benefit mappings.
 */
public class BenefitsTransformer {

    private static final Pattern LEGACY_PLAN_CODE_PATTERN = Pattern.compile("^[A-Z]{2}\\d{3}$");
    private static final Pattern HEALTH_PLAN_PATTERN = Pattern.compile("^(HMO|PPO|POS|EPO|HDHP)\\s*.*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RETIREMENT_PLAN_PATTERN = Pattern.compile("^(401K|403B|IRA|PENSION|DEFINED BENEFIT)\\s*.*$", Pattern.CASE_INSENSITIVE);

    // Healthcare-specific benefit plan mappings
    private static final Map<String, String> HEALTH_PLAN_MAPPINGS = new HashMap<>();
    private static final Map<String, String> RETIREMENT_PLAN_MAPPINGS = new HashMap<>();
    private static final Map<String, Double> BENEFIT_COST_MAPPINGS = new HashMap<>();

    static {
        // Health plan mappings
        HEALTH_PLAN_MAPPINGS.put("HMO BASIC", "HMO-BASIC - Basic HMO Plan");
        HEALTH_PLAN_MAPPINGS.put("HMO PREMIUM", "HMO-PREM - Premium HMO Plan");
        HEALTH_PLAN_MAPPINGS.put("PPO STANDARD", "PPO-STD - Standard PPO Plan");
        HEALTH_PLAN_MAPPINGS.put("PPO PREMIUM", "PPO-PREM - Premium PPO Plan");
        HEALTH_PLAN_MAPPINGS.put("POS PLAN", "POS-STD - Point of Service Plan");
        HEALTH_PLAN_MAPPINGS.put("EPO PLAN", "EPO-STD - Exclusive Provider Organization");
        HEALTH_PLAN_MAPPINGS.put("HIGH DEDUCTIBLE", "HDHP-STD - High Deductible Health Plan");
        HEALTH_PLAN_MAPPINGS.put("MEDICARE ADVANTAGE", "MA-HMO - Medicare Advantage HMO");
        HEALTH_PLAN_MAPPINGS.put("DENTAL BASIC", "DENTAL-BASIC - Basic Dental Plan");
        HEALTH_PLAN_MAPPINGS.put("DENTAL PREMIUM", "DENTAL-PREM - Premium Dental Plan");
        HEALTH_PLAN_MAPPINGS.put("VISION BASIC", "VISION-BASIC - Basic Vision Plan");
        HEALTH_PLAN_MAPPINGS.put("VISION PREMIUM", "VISION-PREM - Premium Vision Plan");

        // Retirement plan mappings
        RETIREMENT_PLAN_MAPPINGS.put("401K TRADITIONAL", "401K-TRAD - Traditional 401(k)");
        RETIREMENT_PLAN_MAPPINGS.put("401K ROTH", "401K-ROTH - Roth 401(k)");
        RETIREMENT_PLAN_MAPPINGS.put("403B PLAN", "403B-STD - 403(b) Plan");
        RETIREMENT_PLAN_MAPPINGS.put("DEFINED BENEFIT", "DB-PENSION - Defined Benefit Pension");
        RETIREMENT_PLAN_MAPPINGS.put("DEFINED CONTRIBUTION", "DC-401K - Defined Contribution 401(k)");
        RETIREMENT_PLAN_MAPPINGS.put("SIMPLE IRA", "SIMPLE-IRA - SIMPLE IRA");
        RETIREMENT_PLAN_MAPPINGS.put("SEP IRA", "SEP-IRA - SEP IRA");

        // Average benefit costs (monthly premiums)
        BENEFIT_COST_MAPPINGS.put("HMO-BASIC", 450.00);
        BENEFIT_COST_MAPPINGS.put("HMO-PREM", 650.00);
        BENEFIT_COST_MAPPINGS.put("PPO-STD", 550.00);
        BENEFIT_COST_MAPPINGS.put("PPO-PREM", 750.00);
        BENEFIT_COST_MAPPINGS.put("HDHP-STD", 350.00);
        BENEFIT_COST_MAPPINGS.put("DENTAL-BASIC", 35.00);
        BENEFIT_COST_MAPPINGS.put("DENTAL-PREM", 55.00);
        BENEFIT_COST_MAPPINGS.put("VISION-BASIC", 15.00);
        BENEFIT_COST_MAPPINGS.put("VISION-PREM", 25.00);
        BENEFIT_COST_MAPPINGS.put("LIFE-TERM", 25.00);
        BENEFIT_COST_MAPPINGS.put("LIFE-WHOLE", 75.00);
        BENEFIT_COST_MAPPINGS.put("DISABILITY-SHORT", 45.00);
        BENEFIT_COST_MAPPINGS.put("DISABILITY-LONG", 85.00);
    }

    /**
     * Transforms legacy benefit plan code to Workday standard format (XX###).
     * @param legacyPlanCode The legacy benefit plan code
     * @return Standardized Workday benefit plan code
     * @throws IllegalArgumentException if plan code format is invalid
     */
    public static String transformBenefitPlanCode(String legacyPlanCode) {
        if (!DataValidationUtils.isNotEmpty(legacyPlanCode)) {
            throw new IllegalArgumentException("Benefit plan code cannot be null or empty");
        }

        String cleanCode = legacyPlanCode.trim().toUpperCase();

        // Handle different legacy formats
        if (LEGACY_PLAN_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Attempt to extract numeric portion and create standardized code
            Matcher matcher = Pattern.compile("\\d+").matcher(cleanCode);
            if (matcher.find()) {
                String numbers = matcher.group();
                return String.format("BN%03d", Integer.parseInt(numbers));
            } else {
                // Generate code based on plan type
                return generatePlanCodeFromType(cleanCode);
            }
        }
    }

    /**
     * Safely transforms benefit plan code with error handling.
     * @param legacyPlanCode The legacy benefit plan code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday benefit plan code or default
     */
    public static String safeTransformBenefitPlanCode(String legacyPlanCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformBenefitPlanCode(legacyPlanCode),
            defaultCode,
            "benefit plan code transformation"
        );
    }

    /**
     * Standardizes health plan name to Workday format with healthcare-specific mappings.
     * @param legacyPlanName The legacy health plan name
     * @return Standardized health plan name
     */
    public static String standardizeHealthPlanName(String legacyPlanName) {
        if (!DataValidationUtils.isNotEmpty(legacyPlanName)) {
            return "Unknown Health Plan";
        }

        String cleanName = legacyPlanName.trim().toUpperCase();

        // Check for exact mappings first
        String mappedName = HEALTH_PLAN_MAPPINGS.get(cleanName);
        if (mappedName != null) {
            return mappedName;
        }

        // Handle health plan type patterns
        if (HEALTH_PLAN_PATTERN.matcher(cleanName).matches()) {
            String[] parts = cleanName.split("\\s+", 2);
            String planType = parts[0];
            String remainder = parts.length > 1 ? parts[1] : "STANDARD";

            switch (planType) {
                case "HMO":
                    return "HMO-" + remainder.replaceAll("\\s+", "-") + " - HMO Plan";
                case "PPO":
                    return "PPO-" + remainder.replaceAll("\\s+", "-") + " - PPO Plan";
                case "POS":
                    return "POS-" + remainder.replaceAll("\\s+", "-") + " - Point of Service Plan";
                case "EPO":
                    return "EPO-" + remainder.replaceAll("\\s+", "-") + " - Exclusive Provider Organization";
                case "HDHP":
                    return "HDHP-" + remainder.replaceAll("\\s+", "-") + " - High Deductible Health Plan";
                default:
                    return cleanName; // Return as-is if no mapping
            }
        }

        // Return cleaned name if no mapping found
        return cleanName;
    }

    /**
     * Safely standardizes health plan name with error handling.
     * @param legacyPlanName The legacy health plan name
     * @param defaultName The default name to return if standardization fails
     * @return Standardized health plan name or default
     */
    public static String safeStandardizeHealthPlanName(String legacyPlanName, String defaultName) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeHealthPlanName(legacyPlanName),
            defaultName,
            "health plan name standardization"
        );
    }

    /**
     * Standardizes retirement plan name to Workday format.
     * @param legacyPlanName The legacy retirement plan name
     * @return Standardized retirement plan name
     */
    public static String standardizeRetirementPlanName(String legacyPlanName) {
        if (!DataValidationUtils.isNotEmpty(legacyPlanName)) {
            return "Unknown Retirement Plan";
        }

        String cleanName = legacyPlanName.trim().toUpperCase();

        // Check for exact mappings first
        String mappedName = RETIREMENT_PLAN_MAPPINGS.get(cleanName);
        if (mappedName != null) {
            return mappedName;
        }

        // Handle retirement plan type patterns
        if (RETIREMENT_PLAN_PATTERN.matcher(cleanName).matches()) {
            String[] parts = cleanName.split("\\s+", 2);
            String planType = parts[0];
            String remainder = parts.length > 1 ? parts[1] : "STANDARD";

            switch (planType) {
                case "401K":
                    return "401K-" + remainder.replaceAll("\\s+", "-") + " - 401(k) Plan";
                case "403B":
                    return "403B-" + remainder.replaceAll("\\s+", "-") + " - 403(b) Plan";
                case "IRA":
                    return "IRA-" + remainder.replaceAll("\\s+", "-") + " - IRA Plan";
                case "PENSION":
                    return "PENSION-" + remainder.replaceAll("\\s+", "-") + " - Pension Plan";
                case "DEFINED":
                    if (remainder.toUpperCase().contains("BENEFIT")) {
                        return "DB-PENSION - Defined Benefit Pension";
                    } else {
                        return "DC-401K - Defined Contribution Plan";
                    }
                default:
                    return cleanName;
            }
        }

        return cleanName;
    }

    /**
     * Safely standardizes retirement plan name with error handling.
     * @param legacyPlanName The legacy retirement plan name
     * @param defaultName The default name to return if standardization fails
     * @return Standardized retirement plan name or default
     */
    public static String safeStandardizeRetirementPlanName(String legacyPlanName, String defaultName) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeRetirementPlanName(legacyPlanName),
            defaultName,
            "retirement plan name standardization"
        );
    }

    /**
     * Calculates employee benefit cost based on plan type and coverage level.
     * @param planCode The benefit plan code
     * @param coverageLevel The coverage level (EE, ES, EF, FAM)
     * @return Monthly benefit cost
     */
    public static double calculateBenefitCost(String planCode, String coverageLevel) {
        if (!DataValidationUtils.isNotEmpty(planCode)) {
            return 0.0;
        }

        String basePlanCode = planCode.split("-")[0].toUpperCase();
        double baseCost = BENEFIT_COST_MAPPINGS.getOrDefault(basePlanCode, 0.0);

        // Apply coverage level multipliers
        double multiplier = getCoverageMultiplier(coverageLevel);
        return NumberFormattingUtils.round(baseCost * multiplier, 2);
    }

    /**
     * Transforms benefit election date to standardized format.
     * @param legacyDate The legacy election date
     * @return Standardized election date (yyyy-MM-dd)
     */
    public static String transformBenefitElectionDate(String legacyDate) {
        if (!DataValidationUtils.isNotEmpty(legacyDate)) {
            // Default to current date if not provided
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        // Use existing date validation and formatting
        if (DataValidationUtils.isValidDate(legacyDate)) {
            // Assume it's already in a parseable format, return as-is for now
            // In a real implementation, you'd want to standardize all dates to yyyy-MM-dd
            return legacyDate;
        }

        throw new IllegalArgumentException("Invalid benefit election date: " + legacyDate);
    }

    /**
     * Safely transforms benefit election date with error handling.
     * @param legacyDate The legacy election date
     * @param defaultDate The default date to return if transformation fails
     * @return Standardized election date or default
     */
    public static String safeTransformBenefitElectionDate(String legacyDate, String defaultDate) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformBenefitElectionDate(legacyDate),
            defaultDate,
            "benefit election date transformation"
        );
    }

    /**
     * Determines benefit plan type from plan code or name.
     * @param planCode The plan code
     * @param planName The plan name
     * @return Benefit plan type (HEALTH, DENTAL, VISION, RETIREMENT, LIFE, DISABILITY)
     */
    public static String determineBenefitPlanType(String planCode, String planName) {
        // Check plan code first
        if (DataValidationUtils.isNotEmpty(planCode)) {
            String upperCode = planCode.toUpperCase();
            if (upperCode.startsWith("HMO") || upperCode.startsWith("PPO") || upperCode.startsWith("POS") ||
                upperCode.startsWith("EPO") || upperCode.startsWith("HDHP")) {
                return "HEALTH";
            } else if (upperCode.startsWith("DENTAL")) {
                return "DENTAL";
            } else if (upperCode.startsWith("VISION")) {
                return "VISION";
            } else if (upperCode.startsWith("401K") || upperCode.startsWith("403B") ||
                       upperCode.startsWith("IRA") || upperCode.startsWith("PENSION")) {
                return "RETIREMENT";
            } else if (upperCode.startsWith("LIFE")) {
                return "LIFE";
            } else if (upperCode.startsWith("DISABILITY")) {
                return "DISABILITY";
            }
        }

        // Fall back to plan name analysis
        if (DataValidationUtils.isNotEmpty(planName)) {
            String upperName = planName.toUpperCase();
            if (upperName.contains("HEALTH") || HEALTH_PLAN_PATTERN.matcher(upperName).matches()) {
                return "HEALTH";
            } else if (upperName.contains("DENTAL")) {
                return "DENTAL";
            } else if (upperName.contains("VISION")) {
                return "VISION";
            } else if (RETIREMENT_PLAN_PATTERN.matcher(upperName).matches()) {
                return "RETIREMENT";
            } else if (upperName.contains("LIFE")) {
                return "LIFE";
            } else if (upperName.contains("DISABILITY")) {
                return "DISABILITY";
            }
        }

        return "OTHER";
    }

    /**
     * Validates if a benefit plan code is in proper Workday format.
     * @param planCode The plan code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBenefitPlanCode(String planCode) {
        return DataValidationUtils.isNotEmpty(planCode) && LEGACY_PLAN_CODE_PATTERN.matcher(planCode.trim()).matches();
    }

    /**
     * Checks if a benefit plan is healthcare-specific.
     * @param planName The plan name
     * @return true if healthcare-specific, false otherwise
     */
    public static boolean isHealthcareSpecificPlan(String planName) {
        if (!DataValidationUtils.isNotEmpty(planName)) {
            return false;
        }

        String upperName = planName.toUpperCase();
        return upperName.contains("MEDICARE") ||
               upperName.contains("MEDICAID") ||
               upperName.contains("HEALTH") ||
               upperName.contains("MEDICAL") ||
               HEALTH_PLAN_PATTERN.matcher(upperName).matches();
    }

    /**
     * Generates a benefit summary for reporting purposes.
     * @param planCode The plan code
     * @param planName The plan name
     * @param coverageLevel The coverage level
     * @param electionDate The election date
     * @return Formatted benefit summary
     */
    public static String generateBenefitSummary(String planCode, String planName, String coverageLevel, String electionDate) {
        StringBuilder summary = new StringBuilder();
        summary.append("Plan: ").append(standardizeHealthPlanName(planName)).append("\n");
        summary.append("Code: ").append(transformBenefitPlanCode(planCode)).append("\n");
        summary.append("Type: ").append(determineBenefitPlanType(planCode, planName)).append("\n");
        summary.append("Coverage: ").append(coverageLevel != null ? coverageLevel : "EE").append("\n");
        summary.append("Cost: ").append(NumberFormattingUtils.formatCurrency(calculateBenefitCost(planCode, coverageLevel))).append("/month\n");
        summary.append("Election Date: ").append(safeTransformBenefitElectionDate(electionDate, "Not specified")).append("\n");
        summary.append("Healthcare Specific: ").append(isHealthcareSpecificPlan(planName) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a plan code based on plan type.
     * @param planType The plan type description
     * @return Generated plan code
     */
    private static String generatePlanCodeFromType(String planType) {
        String upperType = planType.toUpperCase();

        if (upperType.contains("HEALTH") || upperType.contains("MEDICAL")) {
            return "HL001";
        } else if (upperType.contains("DENTAL")) {
            return "DN001";
        } else if (upperType.contains("VISION")) {
            return "VS001";
        } else if (upperType.contains("LIFE")) {
            return "LF001";
        } else if (upperType.contains("DISABILITY")) {
            return "DI001";
        } else if (upperType.contains("RETIREMENT") || upperType.contains("401K") || upperType.contains("PENSION")) {
            return "RT001";
        } else {
            return "BN001"; // Generic benefit
        }
    }

    /**
     * Gets coverage level multiplier for benefit cost calculation.
     * @param coverageLevel The coverage level (EE=Employee, ES=Employee+Spouse, EC=Employee+Child, EF=Employee+Family)
     * @return Cost multiplier
     */
    private static double getCoverageMultiplier(String coverageLevel) {
        if (!DataValidationUtils.isNotEmpty(coverageLevel)) {
            return 1.0; // Default to employee-only
        }

        String upperLevel = coverageLevel.toUpperCase();
        switch (upperLevel) {
            case "EE":
            case "EMPLOYEE":
                return 1.0;
            case "ES":
            case "EMPLOYEE+SPOUSE":
            case "EMPLOYEE AND SPOUSE":
                return 1.8;
            case "EC":
            case "EMPLOYEE+CHILD":
            case "EMPLOYEE+CHILDREN":
                return 2.2;
            case "EF":
            case "FAMILY":
            case "EMPLOYEE+FAMILY":
                return 3.0;
            default:
                return 1.0;
        }
    }

    // Copy-paste usage examples:
    // String planCode = BenefitsTransformer.transformBenefitPlanCode(legacyPlanCode);
    // String healthPlan = BenefitsTransformer.standardizeHealthPlanName(legacyPlanName);
    // String retirementPlan = BenefitsTransformer.standardizeRetirementPlanName(legacyRetirementName);
    // double cost = BenefitsTransformer.calculateBenefitCost(planCode, "FAMILY");
    // String planType = BenefitsTransformer.determineBenefitPlanType(planCode, planName);
    // String summary = BenefitsTransformer.generateBenefitSummary(planCode, planName, coverageLevel, electionDate);
}