package com.company.workday.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Number formatting and conversion utilities for financial and healthcare data transformations.
 * Handles currency, percentages, and various numeric formats.
 */
public class NumberFormattingUtils {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.00%");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");

    static {
        CURRENCY_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
        PERCENTAGE_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * Formats a number as currency (USD).
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Formats a number as currency from string input.
     * @param amountStr The amount string to format
     * @return Formatted currency string or original string if parsing fails
     */
    public static String formatCurrency(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr.replaceAll("[^0-9.-]", ""));
            return formatCurrency(amount);
        } catch (NumberFormatException e) {
            return amountStr;
        }
    }

    /**
     * Formats a number as percentage.
     * @param value The decimal value to format (e.g., 0.15 for 15%)
     * @return Formatted percentage string
     */
    public static String formatPercentage(double value) {
        return PERCENTAGE_FORMAT.format(value);
    }

    /**
     * Formats a number with decimal places.
     * @param value The value to format
     * @param decimalPlaces Number of decimal places
     * @return Formatted decimal string
     */
    public static String formatDecimal(double value, int decimalPlaces) {
        String pattern = "#,##0." + "0".repeat(decimalPlaces);
        DecimalFormat formatter = new DecimalFormat(pattern);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(value);
    }

    /**
     * Formats a number as integer with commas.
     * @param value The value to format
     * @return Formatted integer string
     */
    public static String formatInteger(long value) {
        return INTEGER_FORMAT.format(value);
    }

    /**
     * Parses a currency string to double.
     * @param currencyStr The currency string to parse
     * @return Parsed double value
     * @throws NumberFormatException if parsing fails
     */
    public static double parseCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return 0.0;
        }
        String cleanStr = currencyStr.replaceAll("[^0-9.-]", "");
        return Double.parseDouble(cleanStr);
    }

    /**
     * Parses a percentage string to decimal.
     * @param percentageStr The percentage string to parse
     * @return Parsed decimal value (e.g., "15%" becomes 0.15)
     * @throws NumberFormatException if parsing fails
     */
    public static double parsePercentage(String percentageStr) {
        if (percentageStr == null || percentageStr.trim().isEmpty()) {
            return 0.0;
        }
        String cleanStr = percentageStr.replaceAll("[^0-9.]", "");
        return Double.parseDouble(cleanStr) / 100.0;
    }

    /**
     * Safely parses a string to double with default value.
     * @param valueStr The string to parse
     * @param defaultValue The default value if parsing fails
     * @return Parsed double or default value
     */
    public static double safeParseDouble(String valueStr, double defaultValue) {
        try {
            return Double.parseDouble(valueStr.replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Safely parses a string to integer with default value.
     * @param valueStr The string to parse
     * @param defaultValue The default value if parsing fails
     * @return Parsed integer or default value
     */
    public static int safeParseInt(String valueStr, int defaultValue) {
        try {
            return Integer.parseInt(valueStr.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Rounds a double to specified decimal places.
     * @param value The value to round
     * @param decimalPlaces Number of decimal places
     * @return Rounded value
     */
    public static double round(double value, int decimalPlaces) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Calculates percentage of a total.
     * @param part The part value
     * @param total The total value
     * @return Percentage as decimal (e.g., 0.15 for 15%)
     */
    public static double calculatePercentage(double part, double total) {
        if (total == 0) {
            return 0.0;
        }
        return part / total;
    }

    /**
     * Applies a percentage increase/decrease to a value.
     * @param baseValue The base value
     * @param percentage The percentage change (e.g., 0.10 for 10% increase)
     * @return New value after percentage change
     */
    public static double applyPercentage(double baseValue, double percentage) {
        return baseValue * (1 + percentage);
    }

    /**
     * Formats a number for healthcare billing (charges, payments).
     * @param amount The amount to format
     * @return Formatted amount with 2 decimal places
     */
    public static String formatHealthcareAmount(double amount) {
        return formatDecimal(amount, 2);
    }

    /**
     * Formats FTE (Full-Time Equivalent) values.
     * @param fte The FTE value (0.0 to 1.0)
     * @return Formatted FTE string
     */
    public static String formatFTE(double fte) {
        if (fte >= 0 && fte <= 1) {
            return formatDecimal(fte, 2);
        }
        return formatDecimal(fte, 2) + " (Invalid FTE)";
    }

    /**
     * Converts hours to FTE based on standard 40-hour workweek.
     * @param hoursPerWeek Hours worked per week
     * @return FTE value
     */
    public static double hoursToFTE(double hoursPerWeek) {
        return Math.min(hoursPerWeek / 40.0, 1.0);
    }

    /**
     * Converts FTE to hours based on standard 40-hour workweek.
     * @param fte The FTE value
     * @return Hours per week
     */
    public static double fteToHours(double fte) {
        return fte * 40.0;
    }

    /**
     * Formats a rate (hourly, daily, etc.) with currency.
     * @param rate The rate amount
     * @param period The period (e.g., "hour", "day", "year")
     * @return Formatted rate string
     */
    public static String formatRate(double rate, String period) {
        return formatCurrency(rate) + "/" + period.toLowerCase();
    }

    /**
     * Calculates annual salary from hourly rate.
     * @param hourlyRate The hourly rate
     * @param hoursPerWeek Hours worked per week (default 40)
     * @param weeksPerYear Weeks worked per year (default 52)
     * @return Annual salary
     */
    public static double calculateAnnualSalary(double hourlyRate, double hoursPerWeek, double weeksPerYear) {
        return hourlyRate * hoursPerWeek * weeksPerYear;
    }

    /**
     * Calculates annual salary from hourly rate using defaults.
     * @param hourlyRate The hourly rate
     * @return Annual salary (assuming 40 hours/week, 52 weeks/year)
     */
    public static double calculateAnnualSalary(double hourlyRate) {
        return calculateAnnualSalary(hourlyRate, 40, 52);
    }

    // Copy-paste usage examples:
    // String salary = NumberFormattingUtils.formatCurrency(75000.50);
    // double parsedAmount = NumberFormattingUtils.safeParseDouble("$1,234.56", 0.0);
    // String fte = NumberFormattingUtils.formatFTE(0.75);
    // double annual = NumberFormattingUtils.calculateAnnualSalary(25.00);
}