package com.company.workday.supplychain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class LogisticsUtilitiesTest {

    @Test
    void testTransformShippingOrderCode() {
        // Test valid format
        String validCode = "SHIP-2024-123456";
        assertEquals(validCode, LogisticsUtilities.transformShippingOrderCode(validCode));

        // Test invalid input
        assertThrows(IllegalArgumentException.class, () -> LogisticsUtilities.transformShippingOrderCode(null));
        assertThrows(IllegalArgumentException.class, () -> LogisticsUtilities.transformShippingOrderCode(""));
    }

    @Test
    void testSafeTransformShippingOrderCode() {
        assertEquals("SHIP-2024-123456", LogisticsUtilities.safeTransformShippingOrderCode("SHIP-2024-123456", "DEFAULT"));
        assertEquals("DEFAULT", LogisticsUtilities.safeTransformShippingOrderCode(null, "DEFAULT"));
    }

    @Test
    void testStandardizeShippingMethod() {
        assertEquals("Ground Shipping - Standard delivery", LogisticsUtilities.standardizeShippingMethod("GROUND"));
        assertEquals("Air Freight - Expedited delivery", LogisticsUtilities.standardizeShippingMethod("AIR"));
        assertEquals("Sea Freight - International shipping", LogisticsUtilities.standardizeShippingMethod("SEA"));
        assertEquals("Express Courier - Overnight delivery", LogisticsUtilities.standardizeShippingMethod("EXPRESS"));
        assertEquals("Temperature Controlled - Medical supplies", LogisticsUtilities.standardizeShippingMethod("TEMPERATURE_CONTROLLED"));
    }

    @Test
    void testCalculateShippingCost() {
        BigDecimal weight = new BigDecimal("10.0");
        BigDecimal distance = new BigDecimal("100.0");

        BigDecimal groundCost = LogisticsUtilities.calculateShippingCost(weight, distance, "GROUND");
        assertTrue(groundCost.compareTo(BigDecimal.ZERO) > 0);

        BigDecimal airCost = LogisticsUtilities.calculateShippingCost(weight, distance, "AIR");
        assertTrue(airCost.compareTo(groundCost) > 0); // Air should be more expensive

        assertEquals(BigDecimal.ZERO, LogisticsUtilities.calculateShippingCost(null, distance, "GROUND"));
    }

    @Test
    void testCalculateDeliveryTime() {
        BigDecimal distance = new BigDecimal("500.0");

        int groundTime = LogisticsUtilities.calculateDeliveryTime(distance, "GROUND");
        int airTime = LogisticsUtilities.calculateDeliveryTime(distance, "AIR");
        int expressTime = LogisticsUtilities.calculateDeliveryTime(distance, "EXPRESS");

        assertTrue(groundTime > airTime); // Air should be faster
        assertTrue(airTime >= expressTime); // Express should be fastest or equal
    }

    @Test
    void testDetermineFreightAuditStatus() {
        BigDecimal billed = new BigDecimal("100.00");
        BigDecimal actual = new BigDecimal("100.00");

        assertEquals("Approved - Charges verified", LogisticsUtilities.determineFreightAuditStatus(billed, actual));

        BigDecimal overBilled = new BigDecimal("120.00");
        assertEquals("Disputed - Charges contested", LogisticsUtilities.determineFreightAuditStatus(overBilled, actual));

        assertEquals("Pending - Audit in progress", LogisticsUtilities.determineFreightAuditStatus(null, actual));
    }

    @Test
    void testIsValidShippingOrderCode() {
        assertTrue(LogisticsUtilities.isValidShippingOrderCode("SHIP-2024-123456"));
        assertFalse(LogisticsUtilities.isValidShippingOrderCode("INVALID"));
        assertFalse(LogisticsUtilities.isValidShippingOrderCode(""));
        assertFalse(LogisticsUtilities.isValidShippingOrderCode(null));
    }

    @Test
    void testGenerateLogisticsOperationsSummary() {
        String summary = LogisticsUtilities.generateLogisticsOperationsSummary(
            "SHIP-2024-123456", "RECV-2024-123456", "TRANS-2024-1234",
            "GROUND", new BigDecimal("150.00"), 5, "Approved - Charges verified"
        );

        assertTrue(summary.contains("Shipping Code: SHIP-2024-123456"));
        assertTrue(summary.contains("Shipping Cost: $150.00"));
        assertTrue(summary.contains("Delivery Time: 5 days"));
        assertTrue(summary.contains("Freight Audit Status: Approved - Charges verified"));
    }
}