package com.company.workday.hr.employee;

import com.company.workday.common.StringManipulationUtils;
import com.company.workday.common.ErrorHandlingUtils;
import com.company.workday.common.DateFormatterUtils;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling employee demographic data transformations.
 * Manages gender, ethnicity, disability status, and other demographic information.
 */
public class DemographicDataHandler {

    private static final List<String> VALID_GENDERS = Arrays.asList(
        "Male", "Female", "Non-Binary", "Prefer Not to Say", "Other"
    );

    private static final List<String> ETHNICITY_CATEGORIES = Arrays.asList(
        "White", "Black or African American", "Asian", "Hispanic or Latino",
        "Native American or Alaska Native", "Native Hawaiian or Other Pacific Islander",
        "Two or More Races", "Prefer Not to Say"
    );

    private static final List<String> VETERAN_STATUSES = Arrays.asList(
        "I am not a veteran", "Veteran", "Active Duty", "Reserve/National Guard",
        "Prefer Not to Say"
    );

    private static final List<String> DISABILITY_STATUSES = Arrays.asList(
        "Yes", "No", "Prefer Not to Say"
    );

    /**
     * Standardizes gender from legacy codes to Workday format.
     * @param legacyGender The legacy gender code or description
     * @return Standardized gender or null if invalid
     */
    public static String standardizeGender(String legacyGender) {
        if (legacyGender == null || legacyGender.trim().isEmpty()) {
            return null;
        }

        String normalized = legacyGender.trim().toUpperCase();

        switch (normalized) {
            case "M":
            case "MALE":
            case "MAN":
                return "Male";
            case "F":
            case "FEMALE":
            case "WOMAN":
                return "Female";
            case "NB":
            case "NON-BINARY":
            case "NONBINARY":
                return "Non-Binary";
            case "O":
            case "OTHER":
                return "Other";
            case "P":
            case "PNTS":
            case "PREFER NOT TO SAY":
            case "DECLINE TO STATE":
                return "Prefer Not to Say";
            default:
                return null; // Invalid gender
        }
    }

    /**
     * Standardizes ethnicity from legacy codes to Workday categories.
     * @param legacyEthnicity The legacy ethnicity code or description
     * @return Standardized ethnicity or null if cannot map
     */
    public static String standardizeEthnicity(String legacyEthnicity) {
        if (legacyEthnicity == null || legacyEthnicity.trim().isEmpty()) {
            return null;
        }

        String normalized = StringManipulationUtils.toTitleCase(legacyEthnicity.trim().toLowerCase());

        // Direct matches
        for (String ethnicity : ETHNICITY_CATEGORIES) {
            if (ethnicity.equalsIgnoreCase(normalized)) {
                return ethnicity;
            }
        }

        // Common legacy mappings
        switch (normalized.toUpperCase()) {
            case "CAUCASIAN":
            case "WHITE AMERICAN":
                return "White";
            case "AFRICAN AMERICAN":
            case "BLACK AMERICAN":
                return "Black or African American";
            case "HISPANIC":
            case "LATINO":
            case "MEXICAN":
            case "PUERTO RICAN":
                return "Hispanic or Latino";
            case "ASIAN AMERICAN":
            case "CHINESE":
            case "INDIAN":
            case "KOREAN":
            case "VIETNAMESE":
                return "Asian";
            case "AMERICAN INDIAN":
            case "NATIVE AMERICAN":
                return "Native American or Alaska Native";
            case "PACIFIC ISLANDER":
            case "HAWAIIAN":
                return "Native Hawaiian or Other Pacific Islander";
            case "MULTIRACIAL":
            case "MULTI-RACIAL":
                return "Two or More Races";
            case "DECLINE":
            case "PREFER NOT TO ANSWER":
                return "Prefer Not to Say";
            default:
                return null; // Cannot map
        }
    }

    /**
     * Standardizes veteran status.
     * @param legacyVeteranStatus The legacy veteran status
     * @return Standardized veteran status or null if invalid
     */
    public static String standardizeVeteranStatus(String legacyVeteranStatus) {
        if (legacyVeteranStatus == null || legacyVeteranStatus.trim().isEmpty()) {
            return null;
        }

        String normalized = legacyVeteranStatus.trim().toUpperCase();

        switch (normalized) {
            case "Y":
            case "YES":
            case "VETERAN":
                return "Veteran";
            case "ACTIVE":
            case "ACTIVE DUTY":
                return "Active Duty";
            case "RESERVE":
            case "NATIONAL GUARD":
                return "Reserve/National Guard";
            case "N":
            case "NO":
            case "NOT A VETERAN":
                return "I am not a veteran";
            case "P":
            case "PNTS":
            case "DECLINE":
                return "Prefer Not to Say";
            default:
                return null;
        }
    }

    /**
     * Standardizes disability status.
     * @param legacyDisability The legacy disability status
     * @return Standardized disability status or null if invalid
     */
    public static String standardizeDisabilityStatus(String legacyDisability) {
        if (legacyDisability == null || legacyDisability.trim().isEmpty()) {
            return null;
        }

        String normalized = legacyDisability.trim().toUpperCase();

        switch (normalized) {
            case "Y":
            case "YES":
            case "DISABLED":
                return "Yes";
            case "N":
            case "NO":
            case "NOT DISABLED":
                return "No";
            case "P":
            case "PNTS":
            case "DECLINE":
                return "Prefer Not to Say";
            default:
                return null;
        }
    }

    /**
     * Calculates age from birth date.
     * @param birthDateStr The birth date string
     * @return Age in years or 0 if invalid
     */
    public static int calculateAge(String birthDateStr) {
        return DateFormatterUtils.calculateAge(birthDateStr);
    }

    /**
     * Determines age group category.
     * @param birthDateStr The birth date string
     * @return Age group category
     */
    public static String getAgeGroup(String birthDateStr) {
        int age = calculateAge(birthDateStr);

        if (age < 18) return "Under 18";
        if (age < 25) return "18-24";
        if (age < 35) return "25-34";
        if (age < 45) return "35-44";
        if (age < 55) return "45-54";
        if (age < 65) return "55-64";
        return "65+";
    }

    /**
     * Validates citizenship status.
     * @param citizenship The citizenship status
     * @return Standardized citizenship status
     */
    public static String standardizeCitizenship(String citizenship) {
        if (citizenship == null || citizenship.trim().isEmpty()) {
            return null;
        }

        String normalized = citizenship.trim().toUpperCase();

        switch (normalized) {
            case "US":
            case "USA":
            case "UNITED STATES":
            case "CITIZEN":
            case "US CITIZEN":
                return "US Citizen";
            case "GREEN CARD":
            case "PERMANENT RESIDENT":
            case "PR":
                return "Permanent Resident";
            case "H1B":
            case "WORK VISA":
            case "VISA":
                return "Work Visa";
            case "OTHER":
            case "FOREIGN":
                return "Other";
            default:
                return null;
        }
    }

    /**
     * Safely standardizes demographic data with error handling.
     * @param gender Gender data
     * @param ethnicity Ethnicity data
     * @param veteranStatus Veteran status
     * @param disabilityStatus Disability status
     * @return String array [gender, ethnicity, veteranStatus, disabilityStatus] or nulls
     */
    public static String[] safeStandardizeDemographics(String gender, String ethnicity,
                                                     String veteranStatus, String disabilityStatus) {
        return ErrorHandlingUtils.safeExecute(
            () -> new String[]{
                standardizeGender(gender),
                standardizeEthnicity(ethnicity),
                standardizeVeteranStatus(veteranStatus),
                standardizeDisabilityStatus(disabilityStatus)
            },
            new String[]{gender, ethnicity, veteranStatus, disabilityStatus},
            "demographic data standardization"
        );
    }

    /**
     * Validates if gender is valid for Workday.
     * @param gender The gender to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidGender(String gender) {
        return gender != null && VALID_GENDERS.contains(gender.trim());
    }

    /**
     * Validates if ethnicity is valid for Workday.
     * @param ethnicity The ethnicity to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEthnicity(String ethnicity) {
        return ethnicity != null && ETHNICITY_CATEGORIES.contains(ethnicity.trim());
    }

    /**
     * Validates if veteran status is valid for Workday.
     * @param veteranStatus The veteran status to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidVeteranStatus(String veteranStatus) {
        return veteranStatus != null && VETERAN_STATUSES.contains(veteranStatus.trim());
    }

    /**
     * Validates if disability status is valid for Workday.
     * @param disabilityStatus The disability status to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDisabilityStatus(String disabilityStatus) {
        return disabilityStatus != null && DISABILITY_STATUSES.contains(disabilityStatus.trim());
    }

    /**
     * Checks if employee is of working age (14-70).
     * @param birthDateStr The birth date string
     * @return true if working age, false otherwise
     */
    public static boolean isWorkingAge(String birthDateStr) {
        int age = calculateAge(birthDateStr);
        return age >= 14 && age <= 70;
    }

    /**
     * Determines if employee qualifies for senior worker protections.
     * @param birthDateStr The birth date string
     * @return true if 40+ years old, false otherwise
     */
    public static boolean isProtectedAge(String birthDateStr) {
        return calculateAge(birthDateStr) >= 40;
    }

    /**
     * Generates a demographic summary for reporting.
     * @param gender Gender
     * @param ethnicity Ethnicity
     * @param age Age in years
     * @return Formatted demographic summary
     */
    public static String createDemographicSummary(String gender, String ethnicity, int age) {
        StringBuilder summary = new StringBuilder();

        if (gender != null) {
            summary.append(gender);
        }

        if (ethnicity != null) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(ethnicity);
        }

        if (age > 0) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Age ").append(age);
        }

        return summary.toString();
    }

    // Copy-paste usage examples:
    // String gender = DemographicDataHandler.standardizeGender(legacyGender);
    // String ethnicity = DemographicDataHandler.standardizeEthnicity(legacyEthnicity);
    // int age = DemographicDataHandler.calculateAge(birthDate);
    // String[] demographics = DemographicDataHandler.safeStandardizeDemographics(gender, ethnicity, veteran, disability);
}