package com.company.workday.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Date formatting and conversion utilities for healthcare data transformations.
 * Handles multiple date formats commonly found in legacy healthcare systems.
 */
public class DateFormatterUtils {

    // Common date formatters
    private static final DateTimeFormatter WORKDAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter US_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter EUROPEAN_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // List of common input formats to try
    private static final List<DateTimeFormatter> INPUT_FORMATTERS = Arrays.asList(
        WORKDAY_FORMAT,
        US_FORMAT,
        EUROPEAN_FORMAT,
        ISO_FORMAT,
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    /**
     * Converts any common date string to Workday standard format (yyyy-MM-dd).
     * @param dateStr The date string to convert
     * @return Date in yyyy-MM-dd format
     * @throws IllegalArgumentException if date format is invalid
     */
    public static String toWorkdayDateFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        String cleanDate = dateStr.trim();

        // Try each common format
        for (DateTimeFormatter formatter : INPUT_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(cleanDate, formatter);
                return date.format(WORKDAY_FORMAT);
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }

        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }

    /**
     * Safely converts date string to Workday format with fallback.
     * @param dateStr The date string to convert
     * @param defaultDate The default date to return if conversion fails
     * @return Date in yyyy-MM-dd format or default
     */
    public static String safeToWorkdayDateFormat(String dateStr, String defaultDate) {
        try {
            return toWorkdayDateFormat(dateStr);
        } catch (Exception e) {
            return defaultDate;
        }
    }

    /**
     * Converts Workday date format to US format (MM/dd/yyyy).
     * @param workdayDate The date in yyyy-MM-dd format
     * @return Date in MM/dd/yyyy format
     */
    public static String toUSDateFormat(String workdayDate) {
        LocalDate date = LocalDate.parse(workdayDate, WORKDAY_FORMAT);
        return date.format(US_FORMAT);
    }

    /**
     * Converts Workday date format to European format (dd/MM/yyyy).
     * @param workdayDate The date in yyyy-MM-dd format
     * @return Date in dd/MM/yyyy format
     */
    public static String toEuropeanDateFormat(String workdayDate) {
        LocalDate date = LocalDate.parse(workdayDate, WORKDAY_FORMAT);
        return date.format(EUROPEAN_FORMAT);
    }

    /**
     * Calculates age in years from birth date.
     * @param birthDateStr The birth date in any common format
     * @return Age in years
     */
    public static int calculateAge(String birthDateStr) {
        LocalDate birthDate = LocalDate.parse(toWorkdayDateFormat(birthDateStr), WORKDAY_FORMAT);
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    /**
     * Calculates service years from hire date.
     * @param hireDateStr The hire date in any common format
     * @return Service years
     */
    public static int calculateServiceYears(String hireDateStr) {
        LocalDate hireDate = LocalDate.parse(toWorkdayDateFormat(hireDateStr), WORKDAY_FORMAT);
        return (int) ChronoUnit.YEARS.between(hireDate, LocalDate.now());
    }

    /**
     * Calculates service months from hire date.
     * @param hireDateStr The hire date in any common format
     * @return Service months
     */
    public static int calculateServiceMonths(String hireDateStr) {
        LocalDate hireDate = LocalDate.parse(toWorkdayDateFormat(hireDateStr), WORKDAY_FORMAT);
        return (int) ChronoUnit.MONTHS.between(hireDate, LocalDate.now());
    }

    /**
     * Adds days to a date.
     * @param dateStr The date string in any common format
     * @param days The number of days to add (can be negative)
     * @return New date in yyyy-MM-dd format
     */
    public static String addDays(String dateStr, int days) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.plusDays(days).format(WORKDAY_FORMAT);
    }

    /**
     * Adds months to a date.
     * @param dateStr The date string in any common format
     * @param months The number of months to add (can be negative)
     * @return New date in yyyy-MM-dd format
     */
    public static String addMonths(String dateStr, int months) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.plusMonths(months).format(WORKDAY_FORMAT);
    }

    /**
     * Adds years to a date.
     * @param dateStr The date string in any common format
     * @param years The number of years to add (can be negative)
     * @return New date in yyyy-MM-dd format
     */
    public static String addYears(String dateStr, int years) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.plusYears(years).format(WORKDAY_FORMAT);
    }

    /**
     * Gets the current date in Workday format.
     * @return Current date in yyyy-MM-dd format
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(WORKDAY_FORMAT);
    }

    /**
     * Gets the current timestamp in yyyy-MM-dd HH:mm:ss format.
     * @return Current timestamp
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    /**
     * Checks if a date is in the past.
     * @param dateStr The date string in any common format
     * @return true if date is before today, false otherwise
     */
    public static boolean isPastDate(String dateStr) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a date is in the future.
     * @param dateStr The date string in any common format
     * @return true if date is after today, false otherwise
     */
    public static boolean isFutureDate(String dateStr) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.isAfter(LocalDate.now());
    }

    /**
     * Validates if a string represents a valid date.
     * @param dateStr The date string to validate
     * @return true if valid date, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        try {
            toWorkdayDateFormat(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the day of week for a date (1=Monday, 7=Sunday).
     * @param dateStr The date string in any common format
     * @return Day of week as integer
     */
    public static int getDayOfWeek(String dateStr) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.getDayOfWeek().getValue();
    }

    /**
     * Gets the month for a date (1-12).
     * @param dateStr The date string in any common format
     * @return Month as integer
     */
    public static int getMonth(String dateStr) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.getMonthValue();
    }

    /**
     * Gets the year for a date.
     * @param dateStr The date string in any common format
     * @return Year as integer
     */
    public static int getYear(String dateStr) {
        LocalDate date = LocalDate.parse(toWorkdayDateFormat(dateStr), WORKDAY_FORMAT);
        return date.getYear();
    }

    // Copy-paste usage examples:
    // String workdayDate = DateFormatterUtils.toWorkdayDateFormat("01/15/2023");
    // int age = DateFormatterUtils.calculateAge("1990-05-15");
    // int serviceYears = DateFormatterUtils.calculateServiceYears("2020-03-01");
    // String futureDate = DateFormatterUtils.addDays("2023-01-15", 30);
    // boolean isValid = DateFormatterUtils.isValidDate("2023-12-31");
}