# Workday Transformation Utilities

A comprehensive Java utility library for transforming healthcare ERP data to Workday format. Designed for junior integration engineers with copy-paste friendly functions.

## Repository Structure

```
workday-transformations/
├── common/                    # Shared utilities
│   ├── DataValidationUtils.java
│   ├── DateFormatterUtils.java
│   ├── StringManipulationUtils.java
│   ├── ErrorHandlingUtils.java
│   └── NumberFormattingUtils.java
├── hr/                        # Human Resources transformations
│   ├── employee/             # Employee data transformations
│   │   ├── EmployeeIdTransformer.java
│   │   ├── NameParser.java
│   │   ├── AddressStandardizer.java
│   │   ├── ContactInfoValidator.java
│   │   ├── EmploymentStatusMapper.java
│   │   ├── HireDateConverter.java
│   │   └── DemographicDataHandler.java
│   ├── position/             # Position data transformations
│   ├── benefits/             # Benefits data transformations
│   └── compliance/           # Compliance data transformations
├── finance/                   # Financial data transformations
├── payroll/                   # Payroll data transformations
└── README.md
```

## Quick Start

### 1. Add to Your CloverDX Project

Copy the required utility classes directly into your CloverDX Java components.

### 2. Common Usage Patterns

#### Employee ID Transformation
```java
// Copy this entire block into your CloverDX Java component
import com.company.workday.hr.employee.EmployeeIdTransformer;

String legacyId = "123456"; // From your data source
String workdayId = EmployeeIdTransformer.transformEmployeeId(legacyId);
// Result: "EM123456"
```

#### Name Standardization
```java
// Copy this entire block into your CloverDX Java component
import com.company.workday.hr.employee.NameParser;

String fullName = "john q. public jr."; // From your data source
String standardized = NameParser.standardizeName(fullName);
// Result: "John Q. Public Jr."
```

#### Address Standardization
```java
// Copy this entire block into your CloverDX Java component
import com.company.workday.hr.employee.AddressStandardizer;

String rawAddress = "123 main st apt 4b"; // From your data source
String standardized = AddressStandardizer.standardizeStreetAddress(rawAddress);
// Result: "123 Main St Apt 4b"
```

#### Date Formatting
```java
// Copy this entire block into your CloverDX Java component
import com.company.workday.common.DateFormatterUtils;

String legacyDate = "01/15/2023"; // From your data source
String workdayDate = DateFormatterUtils.toWorkdayDateFormat(legacyDate);
// Result: "2023-01-15"
```

#### Employment Status Mapping
```java
// Copy this entire block into your CloverDX Java component
import com.company.workday.hr.employee.EmploymentStatusMapper;

String legacyStatus = "A"; // From your data source
String workdayStatus = EmploymentStatusMapper.mapEmploymentStatus(legacyStatus);
// Result: "Active"
```

## Module Documentation

### Common Utilities

#### DataValidationUtils
- Email validation
- Phone number validation
- SSN validation
- Date validation
- Address validation

#### DateFormatterUtils
- Multiple date format conversion
- Age calculation
- Service year calculation
- Date arithmetic

#### StringManipulationUtils
- Whitespace normalization
- Case conversion
- Special character removal
- Address prefix removal

#### ErrorHandlingUtils
- Safe execution with fallbacks
- Data quality warnings
- Transformation error logging

#### NumberFormattingUtils
- Currency formatting
- Percentage handling
- Healthcare amount formatting
- FTE calculations

### HR Module

#### Employee Transformations
- **EmployeeIdTransformer**: Standardizes employee IDs to Workday format
- **NameParser**: Parses and standardizes employee names
- **AddressStandardizer**: Standardizes addresses for Workday
- **ContactInfoValidator**: Validates and formats contact information
- **EmploymentStatusMapper**: Maps legacy status codes to Workday
- **HireDateConverter**: Converts and validates hire dates
- **DemographicDataHandler**: Handles gender, ethnicity, veteran status

## Error Handling

All utilities include safe execution methods that won't break your transformations:

```java
// Safe execution with fallback
String result = EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN");
```

## Testing

Run tests with Maven:
```bash
mvn test
```

## Building

Build the JAR:
```bash
mvn clean package
```

## Contributing

1. Create individual utility classes for new transformations
2. Include comprehensive Javadoc
3. Add copy-paste usage examples
4. Include safe execution methods
5. Add unit tests

## License

This project is part of the healthcare data transformation toolkit.