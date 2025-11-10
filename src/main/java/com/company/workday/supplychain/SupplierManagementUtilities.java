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
 * Utility class for transforming supplier management data from legacy systems to Workday format.
 * Handles scorecards, certifications, diversity, and risk management.
 * Supports healthcare-specific supplier evaluation, compliance monitoring, and supplier relationship management.
 */
public class SupplierManagementUtilities {

    private static final Pattern SUPPLIER_SCORECARD_CODE_PATTERN = Pattern.compile("^SCORE-\\d{4}-\\d{4}$");
    private static final Pattern CERTIFICATION_CODE_PATTERN = Pattern.compile("^CERT-\\d{8}-\\d{3}$");
    private static final Pattern DIVERSITY_CODE_PATTERN = Pattern.compile("^DIVERSITY-\\d{4}-\\d{3}$");
    private static final Pattern RISK_ASSESSMENT_CODE_PATTERN = Pattern.compile("^RISK-\\d{4}-\\d{6}$");

    // Supplier management mappings
    private static final Map<String, String> SCORECARD_RATING_MAPPINGS = new HashMap<>();
    private static final Map<String, String> CERTIFICATION_TYPE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> DIVERSITY_CATEGORY_MAPPINGS = new HashMap<>();
    private static final Map<String, String> RISK_LEVEL_MAPPINGS = new HashMap<>();
    private static final Map<String, BigDecimal> SCORECARD_WEIGHTINGS = new HashMap<>();
    private static final Map<String, BigDecimal> RISK_SCORE_THRESHOLDS = new HashMap<>();

    static {
        // Scorecard rating mappings
        SCORECARD_RATING_MAPPINGS.put("EXCELLENT", "Excellent - Outstanding performance (90-100%)");
        SCORECARD_RATING_MAPPINGS.put("GOOD", "Good - Strong performance (80-89%)");
        SCORECARD_RATING_MAPPINGS.put("SATISFACTORY", "Satisfactory - Acceptable performance (70-79%)");
        SCORECARD_RATING_MAPPINGS.put("NEEDS_IMPROVEMENT", "Needs Improvement - Below standard (60-69%)");
        SCORECARD_RATING_MAPPINGS.put("UNSATISFACTORY", "Unsatisfactory - Poor performance (<60%)");

        // Certification type mappings
        CERTIFICATION_TYPE_MAPPINGS.put("ISO_9001", "ISO 9001 - Quality Management Systems");
        CERTIFICATION_TYPE_MAPPINGS.put("ISO_14001", "ISO 14001 - Environmental Management");
        CERTIFICATION_TYPE_MAPPINGS.put("ISO_45001", "ISO 45001 - Occupational Health & Safety");
        CERTIFICATION_TYPE_MAPPINGS.put("FDA_REGISTRATION", "FDA Registration - Medical Device Regulation");
        CERTIFICATION_TYPE_MAPPINGS.put("CE_MARK", "CE Mark - European Conformity");

        // Diversity category mappings
        DIVERSITY_CATEGORY_MAPPINGS.put("MBE", "Minority Business Enterprise");
        DIVERSITY_CATEGORY_MAPPINGS.put("WBE", "Women Business Enterprise");
        DIVERSITY_CATEGORY_MAPPINGS.put("DBE", "Disadvantaged Business Enterprise");
        DIVERSITY_CATEGORY_MAPPINGS.put("VBE", "Veteran Business Enterprise");
        DIVERSITY_CATEGORY_MAPPINGS.put("LGBTBE", "LGBT Business Enterprise");

        // Risk level mappings
        RISK_LEVEL_MAPPINGS.put("LOW", "Low Risk - Minimal concerns");
        RISK_LEVEL_MAPPINGS.put("MEDIUM", "Medium Risk - Moderate concerns");
        RISK_LEVEL_MAPPINGS.put("HIGH", "High Risk - Significant concerns");
        RISK_LEVEL_MAPPINGS.put("CRITICAL", "Critical Risk - Immediate action required");

        // Scorecard weightings (percentages)
        SCORECARD_WEIGHTINGS.put("QUALITY", new BigDecimal("0.30")); // 30%
        SCORECARD_WEIGHTINGS.put("DELIVERY", new BigDecimal("0.25")); // 25%
        SCORECARD_WEIGHTINGS.put("COST", new BigDecimal("0.20")); // 20%
        SCORECARD_WEIGHTINGS.put("SERVICE", new BigDecimal("0.15")); // 15%
        SCORECARD_WEIGHTINGS.put("COMPLIANCE", new BigDecimal("0.10")); // 10%

        // Risk score thresholds
        RISK_SCORE_THRESHOLDS.put("LOW_THRESHOLD", new BigDecimal("25.00"));
        RISK_SCORE_THRESHOLDS.put("MEDIUM_THRESHOLD", new BigDecimal("50.00"));
        RISK_SCORE_THRESHOLDS.put("HIGH_THRESHOLD", new BigDecimal("75.00"));
    }

    /**
     * Transforms legacy supplier scorecard code to Workday standard format (SCORE-YYYY-NNNN).
     * @param legacyScoreCode The legacy supplier scorecard code
     * @return Standardized Workday supplier scorecard code
     * @throws IllegalArgumentException if scorecard code format is invalid
     */
    public static String transformSupplierScorecardCode(String legacyScoreCode) {
        if (!DataValidationUtils.isNotEmpty(legacyScoreCode)) {
            throw new IllegalArgumentException("Supplier scorecard code cannot be null or empty");
        }

        String cleanCode = legacyScoreCode.trim();

        // Handle different legacy formats
        if (SUPPLIER_SCORECARD_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateSupplierScorecardCode();
        }
    }

    /**
     * Safely transforms supplier scorecard code with error handling.
     * @param legacyScoreCode The legacy supplier scorecard code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday supplier scorecard code or default
     */
    public static String safeTransformSupplierScorecardCode(String legacyScoreCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformSupplierScorecardCode(legacyScoreCode),
            defaultCode,
            "supplier scorecard code transformation"
        );
    }

    /**
     * Transforms legacy certification code to Workday standard format (CERT-YYYYMMDD-NNN).
     * @param legacyCertCode The legacy certification code
     * @return Standardized Workday certification code
     * @throws IllegalArgumentException if certification code format is invalid
     */
    public static String transformCertificationCode(String legacyCertCode) {
        if (!DataValidationUtils.isNotEmpty(legacyCertCode)) {
            throw new IllegalArgumentException("Certification code cannot be null or empty");
        }

        String cleanCode = legacyCertCode.trim();

        // Handle different legacy formats
        if (CERTIFICATION_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateCertificationCode();
        }
    }

    /**
     * Safely transforms certification code with error handling.
     * @param legacyCertCode The legacy certification code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday certification code or default
     */
    public static String safeTransformCertificationCode(String legacyCertCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformCertificationCode(legacyCertCode),
            defaultCode,
            "certification code transformation"
        );
    }

    /**
     * Transforms legacy diversity code to Workday standard format (DIVERSITY-YYYY-NNN).
     * @param legacyDiversityCode The legacy diversity code
     * @return Standardized Workday diversity code
     * @throws IllegalArgumentException if diversity code format is invalid
     */
    public static String transformDiversityCode(String legacyDiversityCode) {
        if (!DataValidationUtils.isNotEmpty(legacyDiversityCode)) {
            throw new IllegalArgumentException("Diversity code cannot be null or empty");
        }

        String cleanCode = legacyDiversityCode.trim();

        // Handle different legacy formats
        if (DIVERSITY_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on diversity category
            return generateDiversityCode(cleanCode);
        }
    }

    /**
     * Safely transforms diversity code with error handling.
     * @param legacyDiversityCode The legacy diversity code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday diversity code or default
     */
    public static String safeTransformDiversityCode(String legacyDiversityCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformDiversityCode(legacyDiversityCode),
            defaultCode,
            "diversity code transformation"
        );
    }

    /**
     * Transforms legacy risk assessment code to Workday standard format (RISK-YYYY-NNNNNN).
     * @param legacyRiskCode The legacy risk assessment code
     * @return Standardized Workday risk assessment code
     * @throws IllegalArgumentException if risk code format is invalid
     */
    public static String transformRiskAssessmentCode(String legacyRiskCode) {
        if (!DataValidationUtils.isNotEmpty(legacyRiskCode)) {
            throw new IllegalArgumentException("Risk assessment code cannot be null or empty");
        }

        String cleanCode = legacyRiskCode.trim();

        // Handle different legacy formats
        if (RISK_ASSESSMENT_CODE_PATTERN.matcher(cleanCode).matches()) {
            return cleanCode;
        } else {
            // Generate code based on current date
            return generateRiskAssessmentCode();
        }
    }

    /**
     * Safely transforms risk assessment code with error handling.
     * @param legacyRiskCode The legacy risk assessment code
     * @param defaultCode The default code to return if transformation fails
     * @return Standardized Workday risk assessment code or default
     */
    public static String safeTransformRiskAssessmentCode(String legacyRiskCode, String defaultCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> transformRiskAssessmentCode(legacyRiskCode),
            defaultCode,
            "risk assessment code transformation"
        );
    }

    /**
     * Standardizes scorecard rating description.
     * @param legacyRating The legacy scorecard rating
     * @return Standardized scorecard rating
     */
    public static String standardizeScorecardRating(String legacyRating) {
        if (!DataValidationUtils.isNotEmpty(legacyRating)) {
            return "Unknown Rating";
        }

        String cleanRating = legacyRating.trim().toUpperCase().replaceAll("[^A-Z_]", "_");

        // Check for exact mappings first
        String mappedRating = SCORECARD_RATING_MAPPINGS.get(cleanRating);
        if (mappedRating != null) {
            return mappedRating;
        }

        // Handle common variations
        if (cleanRating.contains("EXCELLENT") || cleanRating.contains("OUTSTANDING")) {
            return SCORECARD_RATING_MAPPINGS.get("EXCELLENT");
        } else if (cleanRating.contains("GOOD") || cleanRating.contains("STRONG")) {
            return SCORECARD_RATING_MAPPINGS.get("GOOD");
        } else if (cleanRating.contains("SATISFACTORY") || cleanRating.contains("ACCEPTABLE")) {
            return SCORECARD_RATING_MAPPINGS.get("SATISFACTORY");
        } else if (cleanRating.contains("NEEDS") || cleanRating.contains("IMPROVEMENT")) {
            return SCORECARD_RATING_MAPPINGS.get("NEEDS_IMPROVEMENT");
        } else if (cleanRating.contains("UNSATISFACTORY") || cleanRating.contains("POOR")) {
            return SCORECARD_RATING_MAPPINGS.get("UNSATISFACTORY");
        }

        return cleanRating;
    }

    /**
     * Safely standardizes scorecard rating with error handling.
     * @param legacyRating The legacy scorecard rating
     * @param defaultRating The default rating to return if standardization fails
     * @return Standardized scorecard rating or default
     */
    public static String safeStandardizeScorecardRating(String legacyRating, String defaultRating) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeScorecardRating(legacyRating),
            defaultRating,
            "scorecard rating standardization"
        );
    }

    /**
     * Calculates overall supplier scorecard rating based on weighted criteria.
     * @param qualityScore Quality score (0-100)
     * @param deliveryScore Delivery score (0-100)
     * @param costScore Cost score (0-100)
     * @param serviceScore Service score (0-100)
     * @param complianceScore Compliance score (0-100)
     * @return Overall scorecard rating (0-100)
     */
    public static BigDecimal calculateOverallScorecardRating(BigDecimal qualityScore, BigDecimal deliveryScore,
                                                            BigDecimal costScore, BigDecimal serviceScore,
                                                            BigDecimal complianceScore) {
        if (qualityScore == null || deliveryScore == null || costScore == null ||
            serviceScore == null || complianceScore == null) {
            return BigDecimal.ZERO;
        }

        // Apply weightings to each score
        BigDecimal weightedQuality = qualityScore.multiply(SCORECARD_WEIGHTINGS.get("QUALITY"));
        BigDecimal weightedDelivery = deliveryScore.multiply(SCORECARD_WEIGHTINGS.get("DELIVERY"));
        BigDecimal weightedCost = costScore.multiply(SCORECARD_WEIGHTINGS.get("COST"));
        BigDecimal weightedService = serviceScore.multiply(SCORECARD_WEIGHTINGS.get("SERVICE"));
        BigDecimal weightedCompliance = complianceScore.multiply(SCORECARD_WEIGHTINGS.get("COMPLIANCE"));

        BigDecimal totalScore = weightedQuality.add(weightedDelivery).add(weightedCost)
                .add(weightedService).add(weightedCompliance);

        return totalScore.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines scorecard rating category based on overall score.
     * @param overallScore The overall scorecard score
     * @return Rating category (EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT, UNSATISFACTORY)
     */
    public static String determineScorecardRatingCategory(BigDecimal overallScore) {
        if (overallScore == null) {
            return "UNKNOWN";
        }

        if (overallScore.compareTo(new BigDecimal("90.00")) >= 0) {
            return "EXCELLENT";
        } else if (overallScore.compareTo(new BigDecimal("80.00")) >= 0) {
            return "GOOD";
        } else if (overallScore.compareTo(new BigDecimal("70.00")) >= 0) {
            return "SATISFACTORY";
        } else if (overallScore.compareTo(new BigDecimal("60.00")) >= 0) {
            return "NEEDS_IMPROVEMENT";
        } else {
            return "UNSATISFACTORY";
        }
    }

    /**
     * Calculates supplier risk score based on multiple risk factors.
     * @param financialRisk Financial stability risk (0-100)
     * @param operationalRisk Operational capability risk (0-100)
     * @param complianceRisk Regulatory compliance risk (0-100)
     * @param geopoliticalRisk Geopolitical exposure risk (0-100)
     * @return Overall risk score (0-100)
     */
    public static BigDecimal calculateSupplierRiskScore(BigDecimal financialRisk, BigDecimal operationalRisk,
                                                       BigDecimal complianceRisk, BigDecimal geopoliticalRisk) {
        if (financialRisk == null || operationalRisk == null || complianceRisk == null || geopoliticalRisk == null) {
            return BigDecimal.ZERO;
        }

        // Equal weighting for all risk factors
        BigDecimal averageRisk = financialRisk.add(operationalRisk).add(complianceRisk).add(geopoliticalRisk)
                .divide(new BigDecimal("4"), 2, BigDecimal.ROUND_HALF_UP);

        return averageRisk.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Determines risk level based on risk score.
     * @param riskScore The calculated risk score
     * @return Risk level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    public static String determineRiskLevel(BigDecimal riskScore) {
        if (riskScore == null) {
            return "UNKNOWN";
        }

        if (riskScore.compareTo(RISK_SCORE_THRESHOLDS.get("HIGH_THRESHOLD")) >= 0) {
            return "CRITICAL";
        } else if (riskScore.compareTo(RISK_SCORE_THRESHOLDS.get("MEDIUM_THRESHOLD")) >= 0) {
            return "HIGH";
        } else if (riskScore.compareTo(RISK_SCORE_THRESHOLDS.get("LOW_THRESHOLD")) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Validates if a supplier scorecard code is in proper Workday format.
     * @param scoreCode The supplier scorecard code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSupplierScorecardCode(String scoreCode) {
        return DataValidationUtils.isNotEmpty(scoreCode) && SUPPLIER_SCORECARD_CODE_PATTERN.matcher(scoreCode.trim()).matches();
    }

    /**
     * Validates if a certification code is in proper Workday format.
     * @param certCode The certification code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCertificationCode(String certCode) {
        return DataValidationUtils.isNotEmpty(certCode) && CERTIFICATION_CODE_PATTERN.matcher(certCode.trim()).matches();
    }

    /**
     * Validates if a diversity code is in proper Workday format.
     * @param diversityCode The diversity code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDiversityCode(String diversityCode) {
        return DataValidationUtils.isNotEmpty(diversityCode) && DIVERSITY_CODE_PATTERN.matcher(diversityCode.trim()).matches();
    }

    /**
     * Validates if a risk assessment code is in proper Workday format.
     * @param riskCode The risk assessment code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRiskAssessmentCode(String riskCode) {
        return DataValidationUtils.isNotEmpty(riskCode) && RISK_ASSESSMENT_CODE_PATTERN.matcher(riskCode.trim()).matches();
    }

    /**
     * Generates a supplier management summary for reporting purposes.
     * @param scoreCode The supplier scorecard code
     * @param certCode The certification code
     * @param diversityCode The diversity code
     * @param riskCode The risk assessment code
     * @param overallRating The overall scorecard rating
     * @param ratingCategory The rating category
     * @param riskScore The risk score
     * @param riskLevel The risk level
     * @return Formatted supplier management summary
     */
    public static String generateSupplierManagementSummary(String scoreCode, String certCode, String diversityCode,
                                                         String riskCode, BigDecimal overallRating,
                                                         String ratingCategory, BigDecimal riskScore,
                                                         String riskLevel) {
        StringBuilder summary = new StringBuilder();
        summary.append("Scorecard Code: ").append(safeTransformSupplierScorecardCode(scoreCode, "Not specified")).append("\n");
        summary.append("Certification Code: ").append(safeTransformCertificationCode(certCode, "Not certified")).append("\n");
        summary.append("Diversity Code: ").append(safeTransformDiversityCode(diversityCode, "Not specified")).append("\n");
        summary.append("Risk Assessment Code: ").append(safeTransformRiskAssessmentCode(riskCode, "Not assessed")).append("\n");
        summary.append("Overall Rating: ").append(overallRating != null ? overallRating.toString() + "%" : "Not rated").append("\n");
        summary.append("Rating Category: ").append(ratingCategory != null ? ratingCategory : "Not categorized").append("\n");
        summary.append("Risk Score: ").append(riskScore != null ? riskScore.toString() : "Not calculated").append("\n");
        summary.append("Risk Level: ").append(riskLevel != null ? riskLevel : "Not assessed").append("\n");
        summary.append("Valid Scorecard Code: ").append(isValidSupplierScorecardCode(scoreCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Certification Code: ").append(isValidCertificationCode(certCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Diversity Code: ").append(isValidDiversityCode(diversityCode) ? "Yes" : "No").append("\n");
        summary.append("Valid Risk Code: ").append(isValidRiskAssessmentCode(riskCode) ? "Yes" : "No");

        return summary.toString();
    }

    /**
     * Generates a supplier scorecard code based on current date.
     * @return Generated supplier scorecard code
     */
    private static String generateSupplierScorecardCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%04d", (int)(Math.random() * 10000));
        return String.format("SCORE-%d-%s", now.getYear(), sequencePart);
    }

    /**
     * Generates a certification code based on current date.
     * @return Generated certification code
     */
    private static String generateCertificationCode() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequencePart = String.format("%03d", (int)(Math.random() * 1000));
        return "CERT-" + datePart + "-" + sequencePart;
    }

    /**
     * Generates a diversity code based on diversity category.
     * @param diversityCategory The diversity category description
     * @return Generated diversity code
     */
    private static String generateDiversityCode(String diversityCategory) {
        String upperCategory = diversityCategory.toUpperCase();
        String codeSuffix;

        if (upperCategory.contains("MINORITY") || upperCategory.contains("MBE")) {
            codeSuffix = "001";
        } else if (upperCategory.contains("WOMEN") || upperCategory.contains("WBE")) {
            codeSuffix = "002";
        } else if (upperCategory.contains("DISADVANTAGED") || upperCategory.contains("DBE")) {
            codeSuffix = "003";
        } else if (upperCategory.contains("VETERAN") || upperCategory.contains("VBE")) {
            codeSuffix = "004";
        } else if (upperCategory.contains("LGBT")) {
            codeSuffix = "005";
        } else {
            codeSuffix = "000";
        }

        LocalDate now = LocalDate.now();
        return String.format("DIVERSITY-%d-%s", now.getYear(), codeSuffix);
    }

    /**
     * Generates a risk assessment code based on current date.
     * @return Generated risk assessment code
     */
    private static String generateRiskAssessmentCode() {
        LocalDate now = LocalDate.now();
        String sequencePart = String.format("%06d", (int)(Math.random() * 1000000));
        return String.format("RISK-%d-%s", now.getYear(), sequencePart);
    }

    // Copy-paste usage examples:
    // String scoreCode = SupplierManagementUtilities.transformSupplierScorecardCode(legacyScoreCode);
    // String certCode = SupplierManagementUtilities.transformCertificationCode(legacyCertCode);
    // String diversityCode = SupplierManagementUtilities.transformDiversityCode(legacyDiversityCode);
    // String riskCode = SupplierManagementUtilities.transformRiskAssessmentCode(legacyRiskCode);
    // String rating = SupplierManagementUtilities.standardizeScorecardRating(legacyRating);
    // BigDecimal overallRating = SupplierManagementUtilities.calculateOverallScorecardRating(qualityScore, deliveryScore, costScore, serviceScore, complianceScore);
    // String ratingCategory = SupplierManagementUtilities.determineScorecardRatingCategory(overallRating);
    // BigDecimal riskScore = SupplierManagementUtilities.calculateSupplierRiskScore(financialRisk, operationalRisk, complianceRisk, geopoliticalRisk);
    // String riskLevel = SupplierManagementUtilities.determineRiskLevel(riskScore);
    // String summary = SupplierManagementUtilities.generateSupplierManagementSummary(scoreCode, certCode, diversityCode, riskCode, overallRating, ratingCategory, riskScore, riskLevel);
}