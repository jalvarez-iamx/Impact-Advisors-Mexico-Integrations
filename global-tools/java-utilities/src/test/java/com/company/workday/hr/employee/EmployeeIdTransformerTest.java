package com.company.workday.hr.employee;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeIdTransformerTest {

    @Test
    void testTransformEmployeeId() {
        // Test valid Workday format
        assertEquals("EM123456", EmployeeIdTransformer.transformEmployeeId("EM123456"));

        // Test 8-digit numeric conversion
        assertEquals("EM12345678", EmployeeIdTransformer.transformEmployeeId("12345678"));

        // Test alpha-numeric normalization
        assertEquals("ABC012345", EmployeeIdTransformer.transformEmployeeId("ABC12345"));

        // Test numeric extraction
        assertEquals("EM000123", EmployeeIdTransformer.transformEmployeeId("ABC123"));
    }

    @Test
    void testTransformEmployeeIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> EmployeeIdTransformer.transformEmployeeId(null));
        assertThrows(IllegalArgumentException.class, () -> EmployeeIdTransformer.transformEmployeeId(""));
        assertThrows(IllegalArgumentException.class, () -> EmployeeIdTransformer.transformEmployeeId("   "));
    }

    @Test
    void testSafeTransformEmployeeId() {
        assertEquals("EM123456", EmployeeIdTransformer.safeTransformEmployeeId("EM123456", "DEFAULT"));
        assertEquals("DEFAULT", EmployeeIdTransformer.safeTransformEmployeeId(null, "DEFAULT"));
        assertEquals("DEFAULT", EmployeeIdTransformer.safeTransformEmployeeId("", "DEFAULT"));
    }

    @Test
    void testIsValidEmployeeId() {
        assertTrue(EmployeeIdTransformer.isValidEmployeeId("EM123456"));
        assertTrue(EmployeeIdTransformer.isValidEmployeeId("AB123456"));
        assertFalse(EmployeeIdTransformer.isValidEmployeeId("EM12345"));
        assertFalse(EmployeeIdTransformer.isValidEmployeeId("EM1234567"));
        assertFalse(EmployeeIdTransformer.isValidEmployeeId(""));
        assertFalse(EmployeeIdTransformer.isValidEmployeeId(null));
    }

    @Test
    void testGenerateTemporaryEmployeeId() {
        String tempId = EmployeeIdTransformer.generateTemporaryEmployeeId(123);
        assertTrue(tempId.startsWith("TMP"));
        assertTrue(tempId.length() == 9); // TMP + 6 digits
        assertTrue(tempId.matches("TMP\\d{6}"));
    }

    @Test
    void testToNumericValue() {
        assertEquals(123456, EmployeeIdTransformer.toNumericValue("EM123456"));
        assertEquals(-1, EmployeeIdTransformer.toNumericValue("INVALID"));
        assertEquals(-1, EmployeeIdTransformer.toNumericValue(null));
    }

    @Test
    void testIsTemporaryEmployee() {
        assertTrue(EmployeeIdTransformer.isTemporaryEmployee("TMP123456"));
        assertFalse(EmployeeIdTransformer.isTemporaryEmployee("EM123456"));
        assertFalse(EmployeeIdTransformer.isTemporaryEmployee(null));
    }
}