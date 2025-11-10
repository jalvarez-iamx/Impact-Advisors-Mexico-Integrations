package com.company.workday.hr.employee;

import com.company.workday.common.DataValidationUtils;
import com.company.workday.common.StringManipulationUtils;
import com.company.workday.common.ErrorHandlingUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for validating and standardizing employee contact information.
 * Handles phone numbers, email addresses, and emergency contact data.
 */
public class ContactInfoValidator {

    private static final Pattern US_PHONE_PATTERN = Pattern.compile(
        "^(?:\\+?1[-.\\s]?)?\\(?([2-9]\\d{2})\\)?[-.\\s]?([2-9]\\d{2})[-.\\s]?(\\d{4})$"
    );
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(
        "^\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$"
    );
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("(?:ext|extension|x)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Validates and standardizes email address.
     * @param email The email address to validate
     * @return Standardized email or null if invalid
     */
    public static String validateAndStandardizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String cleaned = email.trim().toLowerCase();

        if (DataValidationUtils.isValidEmail(cleaned)) {
            return cleaned;
        }

        return null; // Invalid email
    }

    /**
     * Validates and standardizes US phone number to (XXX) XXX-XXXX format.
     * @param phone The phone number to validate
     * @return Standardized phone number or null if invalid
     */
    public static String validateAndStandardizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String cleaned = phone.replaceAll("[^0-9+()\\s.-]", "");

        // Extract extension if present
        String extension = null;
        Matcher extMatcher = EXTENSION_PATTERN.matcher(cleaned);
        if (extMatcher.find()) {
            extension = extMatcher.group(1);
            cleaned = cleaned.substring(0, extMatcher.start()).trim();
        }

        // Validate phone number
        if (!US_PHONE_PATTERN.matcher(cleaned).matches() &&
            !INTERNATIONAL_PHONE_PATTERN.matcher(cleaned).matches()) {
            return null;
        }

        // Format US numbers
        if (US_PHONE_PATTERN.matcher(cleaned).matches()) {
            Matcher matcher = US_PHONE_PATTERN.matcher(cleaned);
            if (matcher.find()) {
                String formatted = String.format("(%s) %s-%s",
                    matcher.group(1), matcher.group(2), matcher.group(3));
                return extension != null ? formatted + " x" + extension : formatted;
            }
        }

        // Return international numbers as-is if valid
        return extension != null ? cleaned + " x" + extension : cleaned;
    }

    /**
     * Validates and standardizes mobile phone number.
     * @param mobile The mobile phone number
     * @return Standardized mobile number or null if invalid
     */
    public static String validateAndStandardizeMobile(String mobile) {
        // Mobile numbers follow same validation as regular phones
        return validateAndStandardizePhone(mobile);
    }

    /**
     * Validates emergency contact information.
     * @param contactName The emergency contact name
     * @param relationship The relationship to employee
     * @param phone The emergency contact phone
     * @return true if all required fields are valid, false otherwise
     */
    public static boolean validateEmergencyContact(String contactName, String relationship, String phone) {
        return contactName != null && !contactName.trim().isEmpty() &&
               relationship != null && !relationship.trim().isEmpty() &&
               validateAndStandardizePhone(phone) != null;
    }

    /**
     * Standardizes emergency contact name.
     * @param contactName The emergency contact name
     * @return Standardized contact name
     */
    public static String standardizeEmergencyContactName(String contactName) {
        return NameParser.standardizeName(contactName);
    }

    /**
     * Standardizes relationship description.
     * @param relationship The relationship description
     * @return Standardized relationship
     */
    public static String standardizeRelationship(String relationship) {
        if (relationship == null || relationship.trim().isEmpty()) {
            return null;
        }

        String cleaned = StringManipulationUtils.normalizeWhitespace(relationship.trim());
        return StringManipulationUtils.toTitleCase(cleaned.toLowerCase());
    }

    /**
     * Safely validates and standardizes contact info with error handling.
     * @param email Email address
     * @param phone Phone number
     * @param mobile Mobile number
     * @return String array [email, phone, mobile] with standardized values or nulls
     */
    public static String[] safeValidateContactInfo(String email, String phone, String mobile) {
        return ErrorHandlingUtils.safeExecute(
            () -> new String[]{
                validateAndStandardizeEmail(email),
                validateAndStandardizePhone(phone),
                validateAndStandardizeMobile(mobile)
            },
            new String[]{email, phone, mobile},
            "contact info validation"
        );
    }

    /**
     * Checks if phone number is a mobile number (basic heuristic).
     * @param phone The phone number
     * @return true if likely mobile, false otherwise
     */
    public static boolean isLikelyMobileNumber(String phone) {
        if (phone == null) {
            return false;
        }

        String digits = phone.replaceAll("\\D", "");

        // US mobile numbers typically start with certain area codes
        // This is a basic heuristic - actual mobile detection would require carrier lookup
        if (digits.length() == 10) {
            String areaCode = digits.substring(0, 3);
            // Common mobile area code patterns (simplified)
            return areaCode.matches("^[2-9][0-8][0-9]$"); // Avoid 0xx and 1xx patterns
        }

        return false;
    }

    /**
     * Extracts phone extension from phone string.
     * @param phone The phone number with possible extension
     * @return Extension number or null if none found
     */
    public static String extractPhoneExtension(String phone) {
        if (phone == null) {
            return null;
        }

        Matcher matcher = EXTENSION_PATTERN.matcher(phone);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Formats phone number for display without extension.
     * @param phone The phone number
     * @return Phone number without extension
     */
    public static String formatPhoneForDisplay(String phone) {
        if (phone == null) {
            return null;
        }

        String extension = extractPhoneExtension(phone);
        if (extension != null) {
            return phone.substring(0, phone.toLowerCase().indexOf("ext")).trim();
        }

        return phone;
    }

    /**
     * Validates if contact information is complete for employee records.
     * @param email Email address
     * @param phone Phone number
     * @return true if at least one contact method is valid, false otherwise
     */
    public static boolean hasValidContactInfo(String email, String phone) {
        return validateAndStandardizeEmail(email) != null ||
               validateAndStandardizePhone(phone) != null;
    }

    /**
     * Creates a standardized contact info summary.
     * @param email Email address
     * @param phone Phone number
     * @param mobile Mobile number
     * @return Formatted contact summary
     */
    public static String createContactSummary(String email, String phone, String mobile) {
        StringBuilder summary = new StringBuilder();

        if (email != null && !email.isEmpty()) {
            summary.append("Email: ").append(email);
        }

        if (phone != null && !phone.isEmpty()) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("Phone: ").append(phone);
        }

        if (mobile != null && !mobile.isEmpty()) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("Mobile: ").append(mobile);
        }

        return summary.toString();
    }

    // Copy-paste usage examples:
    // String email = ContactInfoValidator.validateAndStandardizeEmail(rawEmail);
    // String phone = ContactInfoValidator.validateAndStandardizePhone(rawPhone);
    // boolean hasContact = ContactInfoValidator.hasValidContactInfo(email, phone);
    // String[] contacts = ContactInfoValidator.safeValidateContactInfo(email, phone, mobile);
}