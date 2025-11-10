package com.company.workday.hr.employee;

import com.company.workday.common.DateFormatterUtils;
import com.company.workday.common.ErrorHandlingUtils;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Utility class for converting and validating hire dates from legacy systems.
 * Handles various date formats and business rules for hire dates.
 */
public class HireDateConverter {

    /**
     * Converts hire date from legacy format to Workday format (yyyy-MM-dd).
     * @param legacyHireDate The legacy hire date string
     * @return Standardized hire date or null if invalid
     */
    public static String convertHireDate(String legacyHireDate) {
        return DateFormatterUtils.toWorkdayDateFormat(legacyHireDate);
    }

    /**
     * Safely converts hire date with error handling.
     * @param legacyHireDate The legacy hire date
     * @param defaultDate The default date if conversion fails
     * @return Converted date or default
     */
    public static String safeConvertHireDate(String legacyHireDate, String defaultDate) {
        return ErrorHandlingUtils.safeExecute(
            () -> convertHireDate(legacyHireDate),
            defaultDate,
            "hire date conversion"
        );
    }

    /**
     * Validates if hire date is reasonable (not in future, not too far in past).
     * @param hireDateStr The hire date string
     * @return true if valid hire date, false otherwise
     */
    public static boolean isValidHireDate(String hireDateStr) {
        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate now = LocalDate.now();
            LocalDate earliestValid = now.minusYears(100); // No hires over 100 years ago
            LocalDate latestValid = now.plusDays(30); // Allow future hires up to 30 days

            return !hireDate.isBefore(earliestValid) && !hireDate.isAfter(latestValid);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Calculates years of service from hire date.
     * @param hireDateStr The hire date string
     * @return Years of service as integer
     */
    public static int calculateYearsOfService(String hireDateStr) {
        return DateFormatterUtils.calculateYearsOfService(hireDateStr);
    }

    /**
     * Calculates months of service from hire date.
     * @param hireDateStr The hire date string
     * @return Months of service as integer
     */
    public static int calculateMonthsOfService(String hireDateStr) {
        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            return 0;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate now = LocalDate.now();

            return (int) java.time.temporal.ChronoUnit.MONTHS.between(hireDate, now);
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    /**
     * Determines if employee is a new hire (hired within last 30 days).
     * @param hireDateStr The hire date string
     * @return true if new hire, false otherwise
     */
    public static boolean isNewHire(String hireDateStr) {
        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

            return hireDate.isAfter(thirtyDaysAgo) || hireDate.isEqual(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Determines if employee is eligible for probationary period review.
     * @param hireDateStr The hire date string
     * @param probationMonths Number of probation months (default 3)
     * @return true if eligible for review, false otherwise
     */
    public static boolean isEligibleForProbationReview(String hireDateStr, int probationMonths) {
        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate reviewDate = hireDate.plusMonths(probationMonths);
            LocalDate now = LocalDate.now();

            // Eligible if review date is within +/- 7 days of today
            return Math.abs(java.time.temporal.ChronoUnit.DAYS.between(reviewDate, now)) <= 7;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Determines if employee is eligible for probationary period review (3 months default).
     * @param hireDateStr The hire date string
     * @return true if eligible for review, false otherwise
     */
    public static boolean isEligibleForProbationReview(String hireDateStr) {
        return isEligibleForProbationReview(hireDateStr, 3);
    }

    /**
     * Calculates next work anniversary date.
     * @param hireDateStr The hire date string
     * @return Next anniversary date in yyyy-MM-dd format
     */
    public static String getNextWorkAnniversary(String hireDateStr) {
        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            return null;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate now = LocalDate.now();

            LocalDate thisYearAnniversary = hireDate.withYear(now.getYear());

            if (thisYearAnniversary.isBefore(now) || thisYearAnniversary.isEqual(now)) {
                // Anniversary already passed this year, get next year
                return thisYearAnniversary.plusYears(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                // Anniversary coming up this year
                return thisYearAnniversary.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Determines if hire date indicates a rehire.
     * @param hireDateStr The hire date string
     * @param originalHireDateStr The original hire date string
     * @return true if rehire, false otherwise
     */
    public static boolean isRehire(String hireDateStr, String originalHireDateStr) {
        if (hireDateStr == null || originalHireDateStr == null) {
            return false;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate originalHireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(originalHireDateStr));

            return hireDate.isAfter(originalHireDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Calculates service milestone (5, 10, 15, etc. years).
     * @param hireDateStr The hire date string
     * @return Next service milestone year or 0 if none approaching
     */
    public static int getNextServiceMilestone(String hireDateStr) {
        int yearsOfService = calculateYearsOfService(hireDateStr);

        if (yearsOfService < 5) {
            return 5;
        } else if (yearsOfService < 10) {
            return 10;
        } else if (yearsOfService < 15) {
            return 15;
        } else if (yearsOfService < 20) {
            return 20;
        } else if (yearsOfService < 25) {
            return 25;
        } else if (yearsOfService < 30) {
            return 30;
        } else {
            return 0; // No standard milestone
        }
    }

    /**
     * Formats hire date for display purposes.
     * @param hireDateStr The hire date string
     * @return Formatted date for display
     */
    public static String formatHireDateForDisplay(String hireDateStr) {
        return DateFormatterUtils.toUSDateFormat(hireDateStr);
    }

    /**
     * Validates hire date against birth date (employee must be at least 14 years old).
     * @param hireDateStr The hire date string
     * @param birthDateStr The birth date string
     * @return true if valid age at hire, false otherwise
     */
    public static boolean validateAgeAtHire(String hireDateStr, String birthDateStr) {
        if (hireDateStr == null || birthDateStr == null) {
            return false;
        }

        try {
            LocalDate hireDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(hireDateStr));
            LocalDate birthDate = LocalDate.parse(DateFormatterUtils.toWorkdayDateFormat(birthDateStr));

            int ageAtHire = (int) java.time.temporal.ChronoUnit.YEARS.between(birthDate, hireDate);
            return ageAtHire >= 14; // Minimum working age
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Copy-paste usage examples:
    // String workdayHireDate = HireDateConverter.convertHireDate(legacyHireDate);
    // int yearsOfService = HireDateConverter.calculateYearsOfService(hireDate);
    // boolean newHire = HireDateConverter.isNewHire(hireDate);
    // String anniversary = HireDateConverter.getNextWorkAnniversary(hireDate);
}