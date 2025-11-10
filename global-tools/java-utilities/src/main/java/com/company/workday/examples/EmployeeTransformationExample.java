package com.company.workday.examples;

import com.company.workday.hr.employee.EmployeeIdTransformer;
import com.company.workday.hr.employee.NameParser;
import com.company.workday.hr.employee.AddressStandardizer;
import com.company.workday.common.DateFormatterUtils;
import com.company.workday.common.DataValidationUtils;
import com.company.workday.common.StringManipulationUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Example application demonstrating employee data transformation utilities.
 * Shows how to use the IAMX utilities for typical healthcare data migration scenarios.
 */
public class EmployeeTransformationExample {

    public static void main(String[] args) {
        System.out.println("=== IAMX Employee Transformation Example ===\n");

        // Sample legacy employee data
        Map<String, Object> legacyEmployee = createSampleEmployeeData();

        System.out.println("Original Legacy Data:");
        legacyEmployee.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println();

        // Transform the data using IAMX utilities
        Map<String, Object> transformedEmployee = transformEmployeeData(legacyEmployee);

        System.out.println("Transformed Workday Data:");
        transformedEmployee.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println();

        // Validate the transformed data
        Map<String, String> validationErrors = DataValidationUtils.validateEmployeeData(transformedEmployee);

        if (validationErrors.isEmpty()) {
            System.out.println("✓ All data validation passed!");
        } else {
            System.out.println("✗ Validation errors found:");
            validationErrors.forEach((field, error) -> System.out.println("  " + field + ": " + error));
        }
    }

    /**
     * Creates sample legacy employee data for demonstration.
     */
    private static Map<String, Object> createSampleEmployeeData() {
        Map<String, Object> employee = new HashMap<>();
        employee.put("legacyId", "123456");
        employee.put("fullName", "john q. public jr.");
        employee.put("address", "123 main st apt 4b");
        employee.put("city", "anytown");
        employee.put("state", "ca");
        employee.put("zipCode", "12345");
        employee.put("hireDate", "01/15/2023");
        employee.put("birthDate", "05/20/1985");
        employee.put("email", "john.public@example.com");
        employee.put("phone", "123-456-7890");
        employee.put("ssn", "123-45-6789");
        return employee;
    }

    /**
     * Transforms legacy employee data to Workday format using IAMX utilities.
     */
    private static Map<String, Object> transformEmployeeData(Map<String, Object> legacyData) {
        Map<String, Object> transformed = new HashMap<>();

        // Transform Employee ID
        String legacyId = (String) legacyData.get("legacyId");
        transformed.put("employeeId", EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN"));

        // Transform Name
        String fullName = (String) legacyData.get("fullName");
        transformed.put("firstName", NameParser.extractFirstName(fullName));
        transformed.put("middleName", NameParser.extractMiddleName(fullName));
        transformed.put("lastName", NameParser.extractLastName(fullName));
        transformed.put("fullName", NameParser.standardizeName(fullName));

        // Transform Address
        String streetAddress = (String) legacyData.get("address");
        transformed.put("streetAddress", AddressStandardizer.standardizeStreetAddress(streetAddress));
        transformed.put("city", StringManipulationUtils.toTitleCase((String) legacyData.get("city")));
        transformed.put("state", ((String) legacyData.get("state")).toUpperCase());
        transformed.put("zipCode", legacyData.get("zipCode"));

        // Transform Dates
        String hireDate = (String) legacyData.get("hireDate");
        String birthDate = (String) legacyData.get("birthDate");
        transformed.put("hireDate", DateFormatterUtils.safeToWorkdayDateFormat(hireDate, "1900-01-01"));
        transformed.put("birthDate", DateFormatterUtils.safeToWorkdayDateFormat(birthDate, "1900-01-01"));

        // Calculate derived fields
        transformed.put("age", DateFormatterUtils.calculateAge(birthDate));
        transformed.put("serviceYears", DateFormatterUtils.calculateServiceYears(hireDate));

        // Transform Contact Info
        transformed.put("email", legacyData.get("email"));
        transformed.put("phone", StringManipulationUtils.standardizePhoneNumber((String) legacyData.get("phone")));
        transformed.put("ssn", legacyData.get("ssn"));

        return transformed;
    }
}