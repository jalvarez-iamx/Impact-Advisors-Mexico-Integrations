# CloverDX Integration Guide

This guide explains how to integrate the IAMX Workday Transformation Utilities into your CloverDX ETL processes.

## Overview

IAMX utilities are designed as copy-paste friendly functions that can be directly inserted into CloverDX Java components. Each utility class contains static methods that handle common data transformation patterns in healthcare ERP to Workday migrations.

## Quick Start

### 1. Add IAMX JAR to Your Project

Copy the compiled `workday-transformations.jar` file to your CloverDX project's `lib` directory, or add it to your classpath.

### 2. Import Required Classes

In your CloverDX Java component, add the necessary imports:

```java
import com.company.workday.common.DataValidationUtils;
import com.company.workday.common.DateFormatterUtils;
import com.company.workday.common.StringManipulationUtils;
import com.company.workday.hr.employee.EmployeeIdTransformer;
import com.company.workday.hr.employee.NameParser;
import com.company.workday.hr.employee.AddressStandardizer;
// Add other imports as needed
```

### 3. Use Utilities in Transformation Logic

Here's a complete example of a CloverDX Java component for employee data transformation:

```java
// Input: record from your data source
// Output: transformed record for Workday

// Transform Employee ID
String legacyId = record.getString("employee_id");
String workdayId = EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN");
output.setString("employee_id", workdayId);

// Transform Name
String fullName = record.getString("full_name");
String firstName = NameParser.extractFirstName(fullName);
String lastName = NameParser.extractLastName(fullName);
String standardizedName = NameParser.standardizeName(fullName);

output.setString("first_name", firstName);
output.setString("last_name", lastName);
output.setString("full_name", standardizedName);

// Transform Address
String rawAddress = record.getString("address");
String standardizedAddress = AddressStandardizer.standardizeStreetAddress(rawAddress);
output.setString("address", standardizedAddress);

// Transform Date
String hireDateStr = record.getString("hire_date");
String workdayHireDate = DateFormatterUtils.safeToWorkdayDateFormat(hireDateStr, "1900-01-01");
output.setString("hire_date", workdayHireDate);

// Validate Data
java.util.Map<String, Object> employeeData = new java.util.HashMap<>();
employeeData.put("employeeId", workdayId);
employeeData.put("firstName", firstName);
employeeData.put("lastName", lastName);
employeeData.put("hireDate", workdayHireDate);

java.util.Map<String, String> validationErrors = DataValidationUtils.validateEmployeeData(employeeData);
if (!validationErrors.isEmpty()) {
    // Handle validation errors - log or route to error handling
    output.setString("validation_errors", validationErrors.toString());
}
```

## Common Transformation Patterns

### Employee Data Transformation

```java
// Standard employee transformation
String legacyId = input.getString("emp_id");
String workdayId = EmployeeIdTransformer.transformEmployeeId(legacyId);

String fullName = input.getString("name");
output.setString("employee_id", workdayId);
output.setString("first_name", NameParser.extractFirstName(fullName));
output.setString("last_name", NameParser.extractLastName(fullName));
output.setString("hire_date", DateFormatterUtils.toWorkdayDateFormat(input.getString("hire_date")));
```

### Address Standardization

```java
// Address transformation
String rawAddress = input.getString("address");
String city = input.getString("city");
String state = input.getString("state");
String zip = input.getString("zip_code");

output.setString("street_address", AddressStandardizer.standardizeStreetAddress(rawAddress));
output.setString("city", StringManipulationUtils.toTitleCase(city));
output.setString("state", state.toUpperCase());
output.setString("postal_code", zip);
```

### Date Handling

```java
// Date transformations
String hireDate = DateFormatterUtils.toWorkdayDateFormat(input.getString("hire_date"));
String birthDate = DateFormatterUtils.toWorkdayDateFormat(input.getString("birth_date"));

output.setString("hire_date", hireDate);
output.setString("birth_date", birthDate);
output.setInteger("age", DateFormatterUtils.calculateAge(birthDate));
output.setInteger("service_years", DateFormatterUtils.calculateServiceYears(hireDate));
```

### Financial Data Transformation

```java
import com.company.workday.finance.transactions.TransactionTransformer;
import com.company.workday.common.NumberFormattingUtils;

// Transaction transformation
String legacyTxnId = input.getString("transaction_id");
String workdayTxnId = TransactionTransformer.safeTransformTransactionId(legacyTxnId, "UNKNOWN");

BigDecimal amount = new BigDecimal(input.getString("amount"));
String currency = input.getString("currency");

output.setString("transaction_id", workdayTxnId);
output.setString("amount", NumberFormattingUtils.formatCurrency(amount.doubleValue()));
output.setString("currency", TransactionTransformer.standardizeCurrencyCode(currency));
```

## Error Handling

### Safe Execution Pattern

```java
// Use safe methods to prevent transformation failures
String employeeId = EmployeeIdTransformer.safeTransformEmployeeId(
    input.getString("emp_id"), "UNKNOWN"
);

String hireDate = DateFormatterUtils.safeToWorkdayDateFormat(
    input.getString("hire_date"), "1900-01-01"
);
```

### Validation and Error Logging

```java
import com.company.workday.common.ErrorHandlingUtils;

// Validate data and log errors
try {
    String email = input.getString("email");
    if (!DataValidationUtils.isValidEmail(email)) {
        ErrorHandlingUtils.logDataQualityWarning("Invalid email format", email);
        // Route to error handling or set default
        output.setString("email", "invalid@example.com");
    } else {
        output.setString("email", email);
    }
} catch (Exception e) {
    ErrorHandlingUtils.logTransformationError("Email transformation failed", input, e);
    output.setString("email", "error@example.com");
}
```

## Batch Processing

For large datasets, use the batch processing utilities:

```java
import com.company.workday.batch.BatchProcessingUtils;
import java.util.List;
import java.util.ArrayList;

// In a larger transformation context, you might collect records and process in batches
// This is more relevant for custom Java components that handle multiple records

public class BatchEmployeeTransformer {
    private List<Map<String, Object>> batch = new ArrayList<>();

    public void addRecord(Map<String, Object> record) {
        batch.add(record);
        if (batch.size() >= 1000) { // Process in batches of 1000
            processBatch();
        }
    }

    private void processBatch() {
        List<Map<String, Object>> results = BatchProcessingUtils.processBatch(
            batch,
            this::transformSingleEmployee,
            100,
            (current, total) -> System.out.println("Processed: " + current + "/" + total)
        );
        // Handle results
        batch.clear();
    }

    private Map<String, Object> transformSingleEmployee(Map<String, Object> employee) {
        // Your transformation logic here
        return employee; // transformed
    }
}
```

## Best Practices

### 1. Use Safe Methods for Production

Always prefer safe methods (with default values) in production environments:

```java
// Good for production
String id = EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN");

// Avoid in production (can throw exceptions)
String id = EmployeeIdTransformer.transformEmployeeId(legacyId);
```

### 2. Validate Data Early

Validate incoming data and handle errors gracefully:

```java
// Validate required fields
if (!DataValidationUtils.isNotEmpty(input.getString("employee_id"))) {
    // Route to error handling
    errorOutput.setString("error_message", "Missing employee ID");
    return;
}
```

### 3. Log Transformation Errors

Implement comprehensive error logging:

```java
try {
    // transformation logic
} catch (Exception e) {
    ErrorHandlingUtils.logTransformationError(
        "Employee transformation failed for ID: " + employeeId,
        input,
        e
    );
    // Continue processing or route to error handling
}
```

### 4. Handle Null Values

Always check for null values before transformation:

```java
String phone = input.getString("phone");
if (phone != null && !phone.trim().isEmpty()) {
    output.setString("phone", StringManipulationUtils.standardizePhoneNumber(phone));
} else {
    output.setString("phone", "");
}
```

## Performance Considerations

### Memory Management

- Process large datasets in batches to avoid memory issues
- Use streaming approaches for very large files
- Monitor JVM memory usage in production

### Parallel Processing

For CPU-intensive transformations, consider parallel processing:

```java
// Use parallel processing for computationally intensive tasks
List<TransformedRecord> results = BatchProcessingUtils.processParallel(
    inputRecords,
    record -> expensiveTransformation(record),
    Runtime.getRuntime().availableProcessors()
);
```

### Caching

Cache frequently used reference data:

```java
// Cache state codes, cost centers, etc. in memory for fast lookups
private static final Map<String, String> STATE_CACHE = loadStateMappings();

private static Map<String, String> loadStateMappings() {
    // Load from database or file
    Map<String, String> mappings = new HashMap<>();
    // populate mappings
    return mappings;
}
```

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**: Ensure the IAMX JAR is in your classpath
2. **NullPointerException**: Check for null inputs before transformation
3. **DateParseException**: Use safe date formatting methods
4. **Memory Issues**: Process large datasets in smaller batches

### Debugging

Enable detailed logging to troubleshoot issues:

```java
// Add logging to your transformation
System.out.println("Processing employee: " + employeeId);
System.out.println("Input data: " + input.toString());

// Use ErrorHandlingUtils for structured logging
ErrorHandlingUtils.logTransformationError("Debug info", input, null);
```

## Support

For additional support or questions about IAMX integration:

1. Check the Javadoc documentation for detailed method signatures
2. Review the example applications in the `examples` package
3. Test transformations with sample data before production deployment
4. Monitor error logs and validation failures in production

## Version Compatibility

- **CloverDX**: Compatible with CloverDX 5.x and later
- **Java**: Requires Java 11 or later
- **Dependencies**: See pom.xml for required dependencies

The IAMX utilities are designed to be lightweight and have minimal external dependencies to ensure easy integration with existing CloverDX projects.