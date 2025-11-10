package com.company.workday.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

class DataValidationUtilsTest {

    @Test
    void testIsNotEmpty() {
        assertTrue(DataValidationUtils.isNotEmpty("test"));
        assertTrue(DataValidationUtils.isNotEmpty(" test "));
        assertFalse(DataValidationUtils.isNotEmpty(""));
        assertFalse(DataValidationUtils.isNotEmpty("   "));
        assertFalse(DataValidationUtils.isNotEmpty(null));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(DataValidationUtils.isValidEmail("test@example.com"));
        assertTrue(DataValidationUtils.isValidEmail("user.name+tag@example.co.uk"));
        assertFalse(DataValidationUtils.isValidEmail("invalid-email"));
        assertFalse(DataValidationUtils.isValidEmail(""));
        assertFalse(DataValidationUtils.isValidEmail(null));
    }

    @Test
    void testIsValidPhoneNumber() {
        assertTrue(DataValidationUtils.isValidPhoneNumber("123-456-7890"));
        assertTrue(DataValidationUtils.isValidPhoneNumber("(123) 456-7890"));
        assertTrue(DataValidationUtils.isValidPhoneNumber("1234567890"));
        assertTrue(DataValidationUtils.isValidPhoneNumber("11234567890"));
        assertFalse(DataValidationUtils.isValidPhoneNumber("123-45-678"));
        assertFalse(DataValidationUtils.isValidPhoneNumber(""));
        assertFalse(DataValidationUtils.isValidPhoneNumber(null));
    }

    @Test
    void testIsValidSSN() {
        assertTrue(DataValidationUtils.isValidSSN("123-45-6789"));
        assertTrue(DataValidationUtils.isValidSSN("123456789"));
        assertFalse(DataValidationUtils.isValidSSN("000-00-0000"));
        assertFalse(DataValidationUtils.isValidSSN("666-45-6789"));
        assertFalse(DataValidationUtils.isValidSSN("123-45-678"));
        assertFalse(DataValidationUtils.isValidSSN(""));
        assertFalse(DataValidationUtils.isValidSSN(null));
    }

    @Test
    void testIsValidZipCode() {
        assertTrue(DataValidationUtils.isValidZipCode("12345"));
        assertTrue(DataValidationUtils.isValidZipCode("12345-6789"));
        assertFalse(DataValidationUtils.isValidZipCode("1234"));
        assertFalse(DataValidationUtils.isValidZipCode("123456"));
        assertFalse(DataValidationUtils.isValidZipCode(""));
        assertFalse(DataValidationUtils.isValidZipCode(null));
    }

    @Test
    void testIsValidDate() {
        assertTrue(DataValidationUtils.isValidDate("2023-01-15"));
        assertTrue(DataValidationUtils.isValidDate("01/15/2023"));
        assertTrue(DataValidationUtils.isValidDate("15/01/2023"));
        assertTrue(DataValidationUtils.isValidDate("01-15-2023"));
        assertFalse(DataValidationUtils.isValidDate("2023-13-45"));
        assertFalse(DataValidationUtils.isValidDate(""));
        assertFalse(DataValidationUtils.isValidDate(null));
    }

    @Test
    void testIsInRange() {
        assertTrue(DataValidationUtils.isInRange(5.0, 0.0, 10.0));
        assertTrue(DataValidationUtils.isInRange(0.0, 0.0, 10.0));
        assertTrue(DataValidationUtils.isInRange(10.0, 0.0, 10.0));
        assertFalse(DataValidationUtils.isInRange(-1.0, 0.0, 10.0));
        assertFalse(DataValidationUtils.isInRange(11.0, 0.0, 10.0));
    }

    @Test
    void testIsRequiredFieldPresent() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("empty", "");
        data.put("null", null);

        assertTrue(DataValidationUtils.isRequiredFieldPresent(data, "name"));
        assertFalse(DataValidationUtils.isRequiredFieldPresent(data, "empty"));
        assertFalse(DataValidationUtils.isRequiredFieldPresent(data, "null"));
        assertFalse(DataValidationUtils.isRequiredFieldPresent(data, "missing"));
        assertFalse(DataValidationUtils.isRequiredFieldPresent(null, "name"));
    }

    @Test
    void testValidateEmployeeData() {
        Map<String, Object> validData = new HashMap<>();
        validData.put("employeeId", "12345");
        validData.put("firstName", "John");
        validData.put("lastName", "Doe");
        validData.put("email", "john.doe@example.com");
        validData.put("phone", "123-456-7890");
        validData.put("ssn", "123-45-6789");
        validData.put("hireDate", "2023-01-15");

        Map<String, String> errors = DataValidationUtils.validateEmployeeData(validData);
        assertTrue(errors.isEmpty());

        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("email", "invalid-email");
        invalidData.put("phone", "123");
        invalidData.put("ssn", "000-00-0000");
        invalidData.put("hireDate", "invalid-date");

        errors = DataValidationUtils.validateEmployeeData(invalidData);
        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("employeeId"));
        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("lastName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
        assertTrue(errors.containsKey("ssn"));
        assertTrue(errors.containsKey("hireDate"));
    }
}