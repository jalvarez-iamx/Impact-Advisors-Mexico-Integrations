package com.company.workday.common;

import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * String manipulation utilities for healthcare data transformations.
 * Handles common string operations needed for data cleansing and standardization.
 */
public class StringManipulationUtils {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9\\s]");
    private static final Pattern NAME_SUFFIXES = Pattern.compile("\\b(jr|sr|ii|iii|iv|v|md|do|phd|rn|lpn|cna)\\b\\.?", Pattern.CASE_INSENSITIVE);

    /**
     * Normalizes whitespace by trimming and collapsing multiple spaces to single spaces.
     * @param input The input string
     * @return Normalized string
     */
    public static String normalizeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return MULTIPLE_SPACES.matcher(input.trim()).replaceAll(" ");
    }

    /**
     * Converts string to title case (first letter of each word capitalized).
     * @param input The input string
     * @return Title case string
     */
    public static String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        return Arrays.stream(input.trim().toLowerCase().split("\\s+"))
                .map(word -> word.isEmpty() ? word :
                    word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    /**
     * Converts string to proper case for names, handling suffixes appropriately.
     * @param input The name string
     * @return Properly formatted name
     */
    public static String toProperNameCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String result = toTitleCase(input);

        // Handle Roman numerals and suffixes - keep them uppercase
        result = NAME_SUFFIXES.matcher(result).replaceAll(match ->
            match.group().toUpperCase());

        return result;
    }

    /**
     * Removes all non-alphanumeric characters except spaces.
     * @param input The input string
     * @return Cleaned string
     */
    public static String removeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }
        return NON_ALPHANUMERIC.matcher(input).replaceAll("");
    }

    /**
     * Extracts numeric portion from a string.
     * @param input The input string
     * @return Numeric string or empty string if no numbers found
     */
    public static String extractNumbers(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[^0-9]", "");
    }

    /**
     * Extracts alphabetic portion from a string.
     * @param input The input string
     * @return Alphabetic string
     */
    public static String extractLetters(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * Pads a string with leading zeros to reach specified length.
     * @param input The input string
     * @param length The desired length
     * @return Zero-padded string
     */
    public static String padWithZeros(String input, int length) {
        if (input == null) {
            input = "";
        }
        return StringUtils.leftPad(input, length, '0');
    }

    /**
     * Truncates a string to specified length with ellipsis if needed.
     * @param input The input string
     * @param maxLength The maximum length
     * @return Truncated string
     */
    public static String truncateWithEllipsis(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }

    /**
     * Removes common prefixes from addresses (e.g., "Apt", "Suite", "Unit").
     * @param address The address string
     * @return Address with prefixes removed
     */
    public static String removeAddressPrefixes(String address) {
        if (address == null) {
            return null;
        }

        String[] prefixes = {"apt", "apartment", "suite", "ste", "unit", "bldg", "building", "floor", "fl", "room", "rm"};
        String lowerAddress = address.toLowerCase();

        for (String prefix : prefixes) {
            if (lowerAddress.startsWith(prefix + " ") || lowerAddress.startsWith(prefix + ".")) {
                return address.substring(prefix.length()).trim().replaceFirst("^[^a-zA-Z0-9]*", "");
            }
        }

        return address;
    }

    /**
     * Standardizes phone number format to (XXX) XXX-XXXX.
     * @param phone The phone number string
     * @return Standardized phone number
     */
    public static String standardizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }

        String digits = extractNumbers(phone);
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6));
        } else if (digits.length() == 11 && digits.startsWith("1")) {
            // Handle 1 + 10 digits
            return String.format("(%s) %s-%s",
                digits.substring(1, 4),
                digits.substring(4, 7),
                digits.substring(7));
        }

        return phone; // Return original if can't standardize
    }

    /**
     * Converts string to uppercase.
     * @param input The input string
     * @return Uppercase string
     */
    public static String toUpperCase(String input) {
        return input == null ? null : input.toUpperCase();
    }

    /**
     * Converts string to lowercase.
     * @param input The input string
     * @return Lowercase string
     */
    public static String toLowerCase(String input) {
        return input == null ? null : input.toLowerCase();
    }

    /**
     * Checks if string contains only numeric characters.
     * @param input The input string
     * @return true if numeric, false otherwise
     */
    public static boolean isNumeric(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("\\d+");
    }

    /**
     * Checks if string contains only alphabetic characters.
     * @param input The input string
     * @return true if alphabetic, false otherwise
     */
    public static boolean isAlphabetic(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("[a-zA-Z]+");
    }

    /**
     * Reverses a string.
     * @param input The input string
     * @return Reversed string
     */
    public static String reverse(String input) {
        return input == null ? null : new StringBuilder(input).reverse().toString();
    }

    // Copy-paste usage examples:
    // String cleanName = StringManipulationUtils.toProperNameCase(dirtyName);
    // String phone = StringManipulationUtils.standardizePhoneNumber(rawPhone);
    // String address = StringManipulationUtils.removeAddressPrefixes(fullAddress);
}