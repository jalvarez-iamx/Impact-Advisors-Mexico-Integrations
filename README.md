# IAMX Repository

A comprehensive platform for healthcare data integration and transformation to Workday format. Designed for managing multiple clinic and hospital projects with shared utilities and training resources.

## Repository Structure

```
iamx-repository/
├── global-tools/              # Shared utilities and frameworks
│   ├── java-utilities/       # Java utility library
│   ├── cloverdx-shared/      # Shared CloverDX components
│   └── docs/                 # Global documentation
├── projects/                 # Clinic/hospital-specific projects
│   ├── clinic-template/      # Template for new clinics
│   ├── hospital-a/           # Hospital A project
│   ├── hospital-b/           # Hospital B project
│   ├── clinic-x/             # Clinic X project
│   └── specialty-center/     # Specialty Center project
├── training/                 # Educational resources
│   └── cloverdx-exercises/   # CloverDX training materials
├── scripts/                  # Build and deployment scripts
└── docs/                     # Repository-level documentation
```

## Quick Start

### For New Clinic Projects

1. **Copy the clinic template**:
   ```bash
   cp -r projects/clinic-template projects/your-clinic-name
   ```

2. **Set up your CloverDX workspace** in the new clinic directory

3. **Leverage global tools** from `global-tools/java-utilities/`

### For Development

1. **Build Java utilities**:
   ```bash
   cd global-tools/java-utilities
   mvn clean package
   ```

2. **Import CloverDX exercises** from `training/cloverdx-exercises/`

3. **Use shared components** from `global-tools/cloverdx-shared/`

### Common Usage Patterns

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

## Key Components

### Global Tools (`global-tools/`)

#### Java Utilities
A comprehensive library with modules for:
- **Common Utilities**: Data validation, date formatting, string manipulation, error handling, number formatting
- **HR Module**: Employee, position, benefits, and compliance transformations
- **Finance Module**: GL accounts, transactions, budgets, healthcare finance
- **Payroll Module**: Compensation, deductions, taxes, benefits integration
- **Supply Chain**: Inventory, logistics, procurement, supplier management
- **Manufacturing**: Production, quality, maintenance, cost accounting

#### CloverDX Shared Components
Reusable ETL components, graphs, and job templates that can be shared across clinic projects.

### Projects (`projects/`)

Each clinic/hospital project contains:
- **CloverDX Workspace**: ETL processes specific to the clinic
- **Custom Transformations**: Clinic-specific business logic
- **Data Mappings**: Configuration for data source mappings
- **Documentation**: Clinic-specific implementation details

### Training (`training/`)

Educational resources including:
- **CloverDX Exercises**: Hands-on training materials
- **Integration Examples**: Real-world usage patterns
- **Best Practices**: Guidelines for development and deployment

## Development Workflow

### 1. Setting Up a New Clinic Project

1. Copy the clinic template:
   ```bash
   cp -r projects/clinic-template projects/new-clinic-name
   cd projects/new-clinic-name
   ```

2. Update the README.md with clinic-specific information

3. Set up your CloverDX workspace

### 2. Using Global Tools

1. Build the Java utilities:
   ```bash
   cd global-tools/java-utilities
   mvn clean package
   ```

2. Copy the resulting JAR to your CloverDX project's lib directory

3. Import required classes in your Java components

### 3. Error Handling

All utilities include safe execution methods:

```java
// Safe execution with fallback
String result = EmployeeIdTransformer.safeTransformEmployeeId(legacyId, "UNKNOWN");
```

## Testing & Building

### Java Utilities
```bash
cd global-tools/java-utilities
mvn test                    # Run tests
mvn clean package          # Build JAR
```

### CloverDX Projects
- Import workspaces from clinic directories
- Use the training exercises for learning
- Follow integration guides in `global-tools/docs/`

## Contributing

### For Global Tools
1. Create utility classes in appropriate modules
2. Include comprehensive Javadoc
3. Add copy-paste usage examples
4. Include safe execution methods
5. Add unit tests

### For Clinic Projects
1. Follow the established project structure
2. Document clinic-specific customizations
3. Share reusable components back to global-tools when appropriate
4. Include data mapping documentation

## License

This project is part of the healthcare data transformation toolkit.