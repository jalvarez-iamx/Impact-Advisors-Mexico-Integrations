# Global Tools

This directory contains shared tools and utilities that can be used across all clinic projects in the IAMX repository.

## Structure

- `java-utilities/` - Java utility library for Workday transformations
- `cloverdx-shared/` - Shared CloverDX components and templates
- `docs/` - Global documentation and integration guides

## Java Utilities

The Java utilities provide copy-paste friendly functions for common data transformation patterns in healthcare ERP to Workday migrations.

### Key Features

- **HR Transformations**: Employee data, positions, benefits, compliance
- **Finance Transformations**: GL accounts, transactions, budgets, healthcare finance
- **Payroll Transformations**: Compensation, deductions, taxes, benefits integration
- **Supply Chain**: Inventory, logistics, procurement, supplier management
- **Manufacturing**: Production, quality, maintenance, cost accounting
- **Common Utilities**: Data validation, date formatting, string manipulation, error handling

### Usage

Import the required classes into your CloverDX Java components:

```java
import com.company.workday.common.DataValidationUtils;
import com.company.workday.hr.employee.EmployeeIdTransformer;
// ... other imports
```

## CloverDX Shared Components

Contains reusable CloverDX graphs, jobs, and metadata that can be shared across clinic projects.

## Documentation

- `integration-guides/` - Guides for integrating IAMX utilities
- `api-reference/` - Detailed API documentation

## Building

To build the Java utilities:

```bash
cd java-utilities
mvn clean package
```

This will create a JAR file that can be included in CloverDX projects.