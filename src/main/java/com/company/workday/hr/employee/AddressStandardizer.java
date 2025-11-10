package com.company.workday.hr.employee;

import com.company.workday.common.StringManipulationUtils;
import com.company.workday.common.ErrorHandlingUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for standardizing employee addresses from legacy systems.
 * Handles various address formats and geocoding preparation for Workday.
 */
public class AddressStandardizer {

    private static final Pattern STREET_ADDRESS_PATTERN = Pattern.compile("^(\\d+)\\s+(.+)$");
    private static final Pattern PO_BOX_PATTERN = Pattern.compile("^(?:P\\.?O\\.?\\s*)?BOX\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUITE_PATTERN = Pattern.compile("(?:suite|ste|apt|apartment|#)\\s*([A-Za-z0-9-]+)", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> STATE_ABBREVIATIONS = new HashMap<>();
    private static final Map<String, String> STREET_SUFFIXES = new HashMap<>();

    static {
        // State abbreviations
        STATE_ABBREVIATIONS.put("ALABAMA", "AL");
        STATE_ABBREVIATIONS.put("ALASKA", "AK");
        STATE_ABBREVIATIONS.put("ARIZONA", "AZ");
        STATE_ABBREVIATIONS.put("ARKANSAS", "AR");
        STATE_ABBREVIATIONS.put("CALIFORNIA", "CA");
        STATE_ABBREVIATIONS.put("COLORADO", "CO");
        STATE_ABBREVIATIONS.put("CONNECTICUT", "CT");
        STATE_ABBREVIATIONS.put("DELAWARE", "DE");
        STATE_ABBREVIATIONS.put("FLORIDA", "FL");
        STATE_ABBREVIATIONS.put("GEORGIA", "GA");
        STATE_ABBREVIATIONS.put("HAWAII", "HI");
        STATE_ABBREVIATIONS.put("IDAHO", "ID");
        STATE_ABBREVIATIONS.put("ILLINOIS", "IL");
        STATE_ABBREVIATIONS.put("INDIANA", "IN");
        STATE_ABBREVIATIONS.put("IOWA", "IA");
        STATE_ABBREVIATIONS.put("KANSAS", "KS");
        STATE_ABBREVIATIONS.put("KENTUCKY", "KY");
        STATE_ABBREVIATIONS.put("LOUISIANA", "LA");
        STATE_ABBREVIATIONS.put("MAINE", "ME");
        STATE_ABBREVIATIONS.put("MARYLAND", "MD");
        STATE_ABBREVIATIONS.put("MASSACHUSETTS", "MA");
        STATE_ABBREVIATIONS.put("MICHIGAN", "MI");
        STATE_ABBREVIATIONS.put("MINNESOTA", "MN");
        STATE_ABBREVIATIONS.put("MISSISSIPPI", "MS");
        STATE_ABBREVIATIONS.put("MISSOURI", "MO");
        STATE_ABBREVIATIONS.put("MONTANA", "MT");
        STATE_ABBREVIATIONS.put("NEBRASKA", "NE");
        STATE_ABBREVIATIONS.put("NEVADA", "NV");
        STATE_ABBREVIATIONS.put("NEW HAMPSHIRE", "NH");
        STATE_ABBREVIATIONS.put("NEW JERSEY", "NJ");
        STATE_ABBREVIATIONS.put("NEW MEXICO", "NM");
        STATE_ABBREVIATIONS.put("NEW YORK", "NY");
        STATE_ABBREVIATIONS.put("NORTH CAROLINA", "NC");
        STATE_ABBREVIATIONS.put("NORTH DAKOTA", "ND");
        STATE_ABBREVIATIONS.put("OHIO", "OH");
        STATE_ABBREVIATIONS.put("OKLAHOMA", "OK");
        STATE_ABBREVIATIONS.put("OREGON", "OR");
        STATE_ABBREVIATIONS.put("PENNSYLVANIA", "PA");
        STATE_ABBREVIATIONS.put("RHODE ISLAND", "RI");
        STATE_ABBREVIATIONS.put("SOUTH CAROLINA", "SC");
        STATE_ABBREVIATIONS.put("SOUTH DAKOTA", "SD");
        STATE_ABBREVIATIONS.put("TENNESSEE", "TN");
        STATE_ABBREVIATIONS.put("TEXAS", "TX");
        STATE_ABBREVIATIONS.put("UTAH", "UT");
        STATE_ABBREVIATIONS.put("VERMONT", "VT");
        STATE_ABBREVIATIONS.put("VIRGINIA", "VA");
        STATE_ABBREVIATIONS.put("WASHINGTON", "WA");
        STATE_ABBREVIATIONS.put("WEST VIRGINIA", "WV");
        STATE_ABBREVIATIONS.put("WISCONSIN", "WI");
        STATE_ABBREVIATIONS.put("WYOMING", "WY");

        // Street suffixes
        STREET_SUFFIXES.put("AVENUE", "AVE");
        STREET_SUFFIXES.put("BOULEVARD", "BLVD");
        STREET_SUFFIXES.put("CIRCLE", "CIR");
        STREET_SUFFIXES.put("COURT", "CT");
        STREET_SUFFIXES.put("DRIVE", "DR");
        STREET_SUFFIXES.put("LANE", "LN");
        STREET_SUFFIXES.put("PLACE", "PL");
        STREET_SUFFIXES.put("ROAD", "RD");
        STREET_SUFFIXES.put("STREET", "ST");
        STREET_SUFFIXES.put("TERRACE", "TER");
        STREET_SUFFIXES.put("WAY", "WAY");
    }

    /**
     * Standardizes a complete address for Workday format.
     * @param addressLine1 Street address
     * @param addressLine2 Additional address info (apt, suite, etc.)
     * @param city City name
     * @param state State name or abbreviation
     * @param zipCode ZIP code
     * @return Standardized address components as String array [line1, line2, city, state, zip]
     */
    public static String[] standardizeAddress(String addressLine1, String addressLine2,
                                            String city, String state, String zipCode) {
        String[] result = new String[5];

        // Standardize address line 1
        result[0] = standardizeStreetAddress(addressLine1);

        // Standardize address line 2
        result[1] = standardizeAddressLine2(addressLine2);

        // Standardize city
        result[2] = standardizeCity(city);

        // Standardize state
        result[3] = standardizeState(state);

        // Standardize ZIP code
        result[4] = standardizeZipCode(zipCode);

        return result;
    }

    /**
     * Standardizes street address line 1.
     * @param streetAddress The street address
     * @return Standardized street address
     */
    public static String standardizeStreetAddress(String streetAddress) {
        if (streetAddress == null || streetAddress.trim().isEmpty()) {
            return null;
        }

        String cleaned = StringManipulationUtils.normalizeWhitespace(streetAddress.trim().toUpperCase());

        // Handle PO Box
        Matcher poBoxMatcher = PO_BOX_PATTERN.matcher(cleaned);
        if (poBoxMatcher.find()) {
            return "PO BOX " + poBoxMatcher.group(1);
        }

        // Standardize street suffixes
        for (Map.Entry<String, String> entry : STREET_SUFFIXES.entrySet()) {
            cleaned = cleaned.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }

        return StringManipulationUtils.toTitleCase(cleaned.toLowerCase());
    }

    /**
     * Standardizes address line 2 (apartment, suite, etc.).
     * @param addressLine2 The second address line
     * @return Standardized address line 2
     */
    public static String standardizeAddressLine2(String addressLine2) {
        if (addressLine2 == null || addressLine2.trim().isEmpty()) {
            return null;
        }

        String cleaned = StringManipulationUtils.normalizeWhitespace(addressLine2.trim());

        // Standardize common prefixes
        cleaned = cleaned.replaceAll("(?i)\\bapartment\\b", "APT");
        cleaned = cleaned.replaceAll("(?i)\\bsuite\\b", "STE");
        cleaned = cleaned.replaceAll("(?i)\\bunit\\b", "UNIT");
        cleaned = cleaned.replaceAll("(?i)\\bbuilding\\b", "BLDG");

        return cleaned.toUpperCase();
    }

    /**
     * Standardizes city name.
     * @param city The city name
     * @return Standardized city name
     */
    public static String standardizeCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return null;
        }

        String cleaned = StringManipulationUtils.normalizeWhitespace(city.trim());
        return StringManipulationUtils.toTitleCase(cleaned.toLowerCase());
    }

    /**
     * Standardizes state to 2-letter abbreviation.
     * @param state The state name or abbreviation
     * @return 2-letter state abbreviation
     */
    public static String standardizeState(String state) {
        if (state == null || state.trim().isEmpty()) {
            return null;
        }

        String cleaned = state.trim().toUpperCase();

        // If already 2 letters, validate it
        if (cleaned.length() == 2) {
            return STATE_ABBREVIATIONS.containsValue(cleaned) ? cleaned : null;
        }

        // Look up full state name
        return STATE_ABBREVIATIONS.get(cleaned);
    }

    /**
     * Standardizes ZIP code to 5-digit or 5+4 format.
     * @param zipCode The ZIP code
     * @return Standardized ZIP code
     */
    public static String standardizeZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return null;
        }

        String digits = zipCode.replaceAll("\\D", "");

        if (digits.length() >= 5) {
            String base = digits.substring(0, 5);
            if (digits.length() >= 9) {
                String extension = digits.substring(5, Math.min(9, digits.length()));
                return base + "-" + extension;
            }
            return base;
        }

        return null; // Invalid ZIP
    }

    /**
     * Safely standardizes address with error handling.
     * @param addressLine1 Street address
     * @param addressLine2 Additional address info
     * @param city City name
     * @param state State name or abbreviation
     * @param zipCode ZIP code
     * @return Standardized address components or null if standardization fails
     */
    public static String[] safeStandardizeAddress(String addressLine1, String addressLine2,
                                                String city, String state, String zipCode) {
        return ErrorHandlingUtils.safeExecute(
            () -> standardizeAddress(addressLine1, addressLine2, city, state, zipCode),
            new String[]{addressLine1, addressLine2, city, state, zipCode},
            "address standardization"
        );
    }

    /**
     * Validates if address is complete for Workday requirements.
     * @param addressLine1 Street address
     * @param city City name
     * @param state State name or abbreviation
     * @param zipCode ZIP code
     * @return true if address is valid and complete, false otherwise
     */
    public static boolean isValidAddress(String addressLine1, String city, String state, String zipCode) {
        return addressLine1 != null && !addressLine1.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               zipCode != null && !zipCode.trim().isEmpty() &&
               standardizeState(state) != null &&
               standardizeZipCode(zipCode) != null;
    }

    /**
     * Determines if address is a PO Box.
     * @param addressLine1 The street address
     * @return true if PO Box, false otherwise
     */
    public static boolean isPOBox(String addressLine1) {
        return addressLine1 != null && PO_BOX_PATTERN.matcher(addressLine1.toUpperCase()).find();
    }

    // Copy-paste usage examples:
    // String[] standardized = AddressStandardizer.standardizeAddress(line1, line2, city, state, zip);
    // String streetAddress = AddressStandardizer.standardizeStreetAddress(rawAddress);
    // String stateAbbrev = AddressStandardizer.standardizeState(stateName);
    // boolean valid = AddressStandardizer.isValidAddress(line1, city, state, zip);
}