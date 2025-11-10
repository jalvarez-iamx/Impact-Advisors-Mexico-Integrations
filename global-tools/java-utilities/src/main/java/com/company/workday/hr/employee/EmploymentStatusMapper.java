package com.company.workday.hr.employee;

import com.company.workday.common.StringManipulationUtils;
import com.company.workday.common.ErrorHandlingUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping employment status from legacy ERP systems to Workday.
 * Handles various employment status codes and classifications.
 */
public class EmploymentStatusMapper {

    private static final Map<String, String> STATUS_MAPPING = new HashMap<>();
    private static final Map<String, String> EMPLOYEE_TYPE_MAPPING = new HashMap<>();
    private static final Map<String, String> WORKER_TYPE_MAPPING = new HashMap<>();

    static {
        // Employment Status Mappings (Legacy -> Workday)
        STATUS_MAPPING.put("A", "Active");
        STATUS_MAPPING.put("ACTIVE", "Active");
        STATUS_MAPPING.put("1", "Active");
        STATUS_MAPPING.put("T", "Terminated");
        STATUS_MAPPING.put("TERMINATED", "Terminated");
        STATUS_MAPPING.put("2", "Terminated");
        STATUS_MAPPING.put("L", "Leave of Absence");
        STATUS_MAPPING.put("LOA", "Leave of Absence");
        STATUS_MAPPING.put("LEAVE", "Leave of Absence");
        STATUS_MAPPING.put("3", "Leave of Absence");
        STATUS_MAPPING.put("I", "Inactive");
        STATUS_MAPPING.put("INACTIVE", "Inactive");
        STATUS_MAPPING.put("4", "Inactive");
        STATUS_MAPPING.put("R", "Retired");
        STATUS_MAPPING.put("RETIRED", "Retired");
        STATUS_MAPPING.put("5", "Retired");
        STATUS_MAPPING.put("D", "Deceased");
        STATUS_MAPPING.put("DECEASED", "Deceased");
        STATUS_MAPPING.put("6", "Deceased");

        // Employee Type Mappings
        EMPLOYEE_TYPE_MAPPING.put("FT", "Full-time");
        EMPLOYEE_TYPE_MAPPING.put("FULLTIME", "Full-time");
        EMPLOYEE_TYPE_MAPPING.put("FULL_TIME", "Full-time");
        EMPLOYEE_TYPE_MAPPING.put("PT", "Part-time");
        EMPLOYEE_TYPE_MAPPING.put("PARTTIME", "Part-time");
        EMPLOYEE_TYPE_MAPPING.put("PART_TIME", "Part-time");
        EMPLOYEE_TYPE_MAPPING.put("C", "Contract");
        EMPLOYEE_TYPE_MAPPING.put("CONTRACT", "Contract");
        EMPLOYEE_TYPE_MAPPING.put("TEMP", "Temporary");
        EMPLOYEE_TYPE_MAPPING.put("TEMPORARY", "Temporary");
        EMPLOYEE_TYPE_MAPPING.put("S", "Seasonal");
        EMPLOYEE_TYPE_MAPPING.put("SEASONAL", "Seasonal");
        EMPLOYEE_TYPE_MAPPING.put("P", "Per Diem");
        EMPLOYEE_TYPE_MAPPING.put("PER_DIEM", "Per Diem");
        EMPLOYEE_TYPE_MAPPING.put("PRN", "Per Diem");

        // Worker Type Mappings (for Workday)
        WORKER_TYPE_MAPPING.put("EMPLOYEE", "Employee");
        WORKER_TYPE_MAPPING.put("CONTINGENT", "Contingent Worker");
        WORKER_TYPE_MAPPING.put("CONTRACTOR", "Contingent Worker");
        WORKER_TYPE_MAPPING.put("CONSULTANT", "Contingent Worker");
        WORKER_TYPE_MAPPING.put("INTERN", "Contingent Worker");
    }

    /**
     * Maps legacy employment status to Workday employment status.
     * @param legacyStatus The legacy status code or description
     * @return Workday employment status or "Unknown" if not mapped
     */
    public static String mapEmploymentStatus(String legacyStatus) {
        if (legacyStatus == null || legacyStatus.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = legacyStatus.trim().toUpperCase();
        return STATUS_MAPPING.getOrDefault(normalized, "Unknown");
    }

    /**
     * Maps legacy employee type to Workday employee type.
     * @param legacyType The legacy employee type
     * @return Workday employee type or "Unknown" if not mapped
     */
    public static String mapEmployeeType(String legacyType) {
        if (legacyType == null || legacyType.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = legacyType.trim().toUpperCase().replace("-", "_");
        return EMPLOYEE_TYPE_MAPPING.getOrDefault(normalized, "Unknown");
    }

    /**
     * Maps to Workday worker type based on employee type and status.
     * @param employeeType The employee type
     * @param employmentStatus The employment status
     * @return Workday worker type
     */
    public static String mapWorkerType(String employeeType, String employmentStatus) {
        String empType = mapEmployeeType(employeeType);

        // Contingent workers
        if ("Contract".equals(empType) || "Temporary".equals(empType) || "Per Diem".equals(empType)) {
            return "Contingent Worker";
        }

        // Employees
        return "Employee";
    }

    /**
     * Determines if employee is considered active based on status.
     * @param employmentStatus The employment status
     * @return true if active, false otherwise
     */
    public static boolean isActiveEmployee(String employmentStatus) {
        String mappedStatus = mapEmploymentStatus(employmentStatus);
        return "Active".equals(mappedStatus);
    }

    /**
     * Determines if employee is on leave.
     * @param employmentStatus The employment status
     * @return true if on leave, false otherwise
     */
    public static boolean isOnLeave(String employmentStatus) {
        String mappedStatus = mapEmploymentStatus(employmentStatus);
        return "Leave of Absence".equals(mappedStatus);
    }

    /**
     * Determines if employee is terminated.
     * @param employmentStatus The employment status
     * @return true if terminated, false otherwise
     */
    public static boolean isTerminated(String employmentStatus) {
        String mappedStatus = mapEmploymentStatus(employmentStatus);
        return "Terminated".equals(mappedStatus);
    }

    /**
     * Safely maps employment status with error handling.
     * @param legacyStatus The legacy status
     * @param defaultStatus The default status if mapping fails
     * @return Mapped status or default
     */
    public static String safeMapEmploymentStatus(String legacyStatus, String defaultStatus) {
        return ErrorHandlingUtils.safeExecute(
            () -> mapEmploymentStatus(legacyStatus),
            defaultStatus,
            "employment status mapping"
        );
    }

    /**
     * Gets all valid Workday employment statuses.
     * @return Array of valid employment statuses
     */
    public static String[] getValidEmploymentStatuses() {
        return new String[]{"Active", "Terminated", "Leave of Absence", "Inactive", "Retired", "Deceased"};
    }

    /**
     * Gets all valid Workday employee types.
     * @return Array of valid employee types
     */
    public static String[] getValidEmployeeTypes() {
        return new String[]{"Full-time", "Part-time", "Contract", "Temporary", "Seasonal", "Per Diem"};
    }

    /**
     * Validates if employment status is valid for Workday.
     * @param status The status to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmploymentStatus(String status) {
        if (status == null) {
            return false;
        }

        for (String validStatus : getValidEmploymentStatuses()) {
            if (validStatus.equals(status.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if employee type is valid for Workday.
     * @param employeeType The employee type to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmployeeType(String employeeType) {
        if (employeeType == null) {
            return false;
        }

        for (String validType : getValidEmployeeTypes()) {
            if (validType.equals(employeeType.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Maps legacy pay status to employment status.
     * @param payStatus The pay status (e.g., "PAY", "NO PAY", "SEVERANCE")
     * @return Mapped employment status
     */
    public static String mapPayStatusToEmploymentStatus(String payStatus) {
        if (payStatus == null || payStatus.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = payStatus.trim().toUpperCase();

        switch (normalized) {
            case "PAY":
            case "ACTIVE PAY":
                return "Active";
            case "NO PAY":
            case "INACTIVE PAY":
                return "Inactive";
            case "SEVERANCE":
            case "TERMINATION PAY":
                return "Terminated";
            case "LEAVE PAY":
                return "Leave of Absence";
            default:
                return "Unknown";
        }
    }

    /**
     * Determines if employee is eligible for benefits based on status and type.
     * @param employmentStatus The employment status
     * @param employeeType The employee type
     * @return true if eligible for benefits, false otherwise
     */
    public static boolean isEligibleForBenefits(String employmentStatus, String employeeType) {
        if (!isActiveEmployee(employmentStatus)) {
            return false;
        }

        String empType = mapEmployeeType(employeeType);
        // Full-time and part-time employees are typically eligible
        return "Full-time".equals(empType) || "Part-time".equals(empType);
    }

    // Copy-paste usage examples:
    // String status = EmploymentStatusMapper.mapEmploymentStatus(legacyStatus);
    // String empType = EmploymentStatusMapper.mapEmployeeType(legacyType);
    // boolean active = EmploymentStatusMapper.isActiveEmployee(status);
    // boolean benefitsEligible = EmploymentStatusMapper.isEligibleForBenefits(status, empType);
}