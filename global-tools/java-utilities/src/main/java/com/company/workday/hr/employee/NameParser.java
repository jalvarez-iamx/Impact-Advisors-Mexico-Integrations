package com.company.workday.hr.employee;

import com.company.workday.common.StringManipulationUtils;
import com.company.workday.common.ErrorHandlingUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing and standardizing employee names from legacy systems.
 * Handles various name formats common in healthcare ERPs.
 */
public class NameParser {

    private static final Pattern NAME_WITH_MIDDLE_INITIAL = Pattern.compile("^([^\\s]+)\\s+([A-Z])\\.?\\s+([^\\s]+)$");
    private static final Pattern NAME_WITH_MIDDLE_NAME = Pattern.compile("^([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)$");
    private static final Pattern NAME_WITH_SUFFIX = Pattern.compile("^(.+?)\\s+(jr|sr|ii|iii|iv|v|md|do|phd|rn|lpn|cna)\\b\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTIPLE_NAMES = Pattern.compile("\\s+");

    private static final List<String> SUFFIXES = Arrays.asList("jr", "sr", "ii", "iii", "iv", "v", "md", "do", "phd", "rn", "lpn", "cna");

    /**
     * Parses full name into first, middle, and last name components.
     * @param fullName The full name string
     * @return String array [firstName, middleName, lastName] or null values if parsing fails
     */
    public static String[] parseFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{null, null, null};
        }

        String cleanName = StringManipulationUtils.normalizeWhitespace(fullName.trim());

        // Handle suffixes first
        String suffix = extractSuffix(cleanName);
        String nameWithoutSuffix = suffix != null ?
            cleanName.substring(0, cleanName.toLowerCase().lastIndexOf(suffix.toLowerCase())).trim() :
            cleanName;

        String[] nameParts = MULTIPLE_NAMES.split(nameWithoutSuffix);

        switch (nameParts.length) {
            case 1:
                // Only last name
                return new String[]{null, null, nameParts[0]};
            case 2:
                // First and last name
                return new String[]{nameParts[0], null, nameParts[1]};
            case 3:
                // First, middle, last name
                return new String[]{nameParts[0], nameParts[1], nameParts[2]};
            default:
                // More than 3 parts - assume first, middle initial(s), last
                String firstName = nameParts[0];
                String lastName = nameParts[nameParts.length - 1];
                String middleName = String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length - 1));
                return new String[]{firstName, middleName, lastName};
        }
    }

    /**
     * Standardizes name format for Workday (First Middle Last).
     * @param fullName The full name to standardize
     * @return Standardized name or original if parsing fails
     */
    public static String standardizeName(String fullName) {
        String[] nameParts = parseFullName(fullName);
        if (nameParts[0] == null && nameParts[1] == null && nameParts[2] == null) {
            return fullName;
        }

        StringBuilder standardized = new StringBuilder();

        if (nameParts[0] != null) {
            standardized.append(StringManipulationUtils.toProperNameCase(nameParts[0]));
        }

        if (nameParts[1] != null) {
            if (standardized.length() > 0) standardized.append(" ");
            standardized.append(StringManipulationUtils.toProperNameCase(nameParts[1]));
        }

        if (nameParts[2] != null) {
            if (standardized.length() > 0) standardized.append(" ");
            standardized.append(StringManipulationUtils.toProperNameCase(nameParts[2]));
        }

        return standardized.toString();
    }

    /**
     * Safely standardizes name with error handling.
     * @param fullName The full name to standardize
     * @param defaultName The default name if standardization fails
     * @return Standardized name or default
     */
    public static String safeStandardizeName(String fullName, String defaultName) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeName(fullName),
            defaultName,
            "name standardization"
        );
    }

    /**
     * Extracts suffix from name (Jr, Sr, MD, etc.).
     * @param fullName The full name
     * @return The suffix or null if none found
     */
    public static String extractSuffix(String fullName) {
        if (fullName == null) {
            return null;
        }

        Matcher matcher = NAME_WITH_SUFFIX.matcher(fullName.toLowerCase());
        if (matcher.find()) {
            return matcher.group(2).toUpperCase();
        }

        return null;
    }

    /**
     * Checks if name contains a professional suffix.
     * @param fullName The full name
     * @return true if contains professional suffix, false otherwise
     */
    public static boolean hasProfessionalSuffix(String fullName) {
        String suffix = extractSuffix(fullName);
        return suffix != null && Arrays.asList("MD", "DO", "PHD", "RN", "LPN", "CNA").contains(suffix);
    }

    /**
     * Generates preferred name from full name (typically first name).
     * @param fullName The full name
     * @return Preferred name or null if cannot determine
     */
    public static String generatePreferredName(String fullName) {
        String[] nameParts = parseFullName(fullName);
        return nameParts[0]; // First name
    }

    /**
     * Formats name for display (Last, First M.).
     * @param fullName The full name
     * @return Formatted name for display
     */
    public static String formatNameForDisplay(String fullName) {
        String[] nameParts = parseFullName(fullName);
        if (nameParts[2] == null) {
            return fullName; // Return original if can't parse
        }

        StringBuilder displayName = new StringBuilder(nameParts[2]); // Last name

        if (nameParts[0] != null) {
            displayName.append(", ").append(nameParts[0]); // First name
        }

        if (nameParts[1] != null && nameParts[1].length() == 1) {
            displayName.append(" ").append(nameParts[1]).append("."); // Middle initial
        } else if (nameParts[1] != null && nameParts[1].length() > 1) {
            displayName.append(" ").append(nameParts[1].charAt(0)).append("."); // First letter of middle name
        }

        return displayName.toString();
    }

    /**
     * Validates name format and content.
     * @param fullName The full name to validate
     * @return true if valid name format, false otherwise
     */
    public static boolean isValidName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }

        // Check for minimum requirements
        String[] nameParts = parseFullName(fullName);
        return nameParts[2] != null && nameParts[2].length() >= 2; // Must have last name of at least 2 characters
    }

    /**
     * Removes special characters and extra spaces from names.
     * @param fullName The full name to clean
     * @return Cleaned name
     */
    public static String cleanName(String fullName) {
        if (fullName == null) {
            return null;
        }

        String cleaned = StringManipulationUtils.removeSpecialCharacters(fullName);
        return StringManipulationUtils.normalizeWhitespace(cleaned);
    }

    // Copy-paste usage examples:
    // String[] nameParts = NameParser.parseFullName(fullName);
    // String standardized = NameParser.standardizeName(dirtyName);
    // String displayName = NameParser.formatNameForDisplay(fullName);
    // String preferred = NameParser.generatePreferredName(fullName);
}