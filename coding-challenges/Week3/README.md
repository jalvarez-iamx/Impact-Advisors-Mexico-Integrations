# Week 3 Challenge: Database Data Export Tool

## Challenge Overview

You are provided with a Java application that exports data from the PostgreSQL `worker_time_away` table (same database as Week 2) to multiple formats: CSV, JSON, and Excel. The application includes the derived columns logic from Week 2.

However, upon running the export, you will notice two critical issues:
1. Only the first 100 rows are exported, not the complete dataset
2. The columns appear in reverse order in all output formats

## Your Mission

1. **Investigate**: Understand why only 100 rows are exported and why columns are in reverse order
2. **Analyze**: Find the root causes in the modular codebase
3. **Fix**: Correct both issues to export all data with proper column ordering

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Access to the PostgreSQL database (credentials in .env file)

## Setup Instructions

1. Navigate to the project directory:
   ```bash
   cd coding-challenges/Week3/Sample
   ```

2. Run the export:
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.company.week3.DataExporterApplication" -Dexec.args="csv data.csv"
   ```

   Available formats: csv, json, excel

3. Check the output files to see the issues

## Application Structure

The application is modular with separate classes:
- `DataExporterApplication`: Main entry point
- `DatabaseService`: Database connection and queries
- `DataTransformer`: Column derivation logic
- `CsvExporter`, `JsonExporter`, `ExcelExporter`: Format-specific exporters
- `WorkerTimeAwayRecord`: Data model
- `ExportConfig`: Configuration settings

## Challenge Details

- The application uses the same database connection as Week 2
- Derived columns (start_time, end_time, approved) are calculated as in Week 2
- Exports should include all rows from the table
- Column order should match the defined sequence, not reversed

## Expected Output

- CSV: All rows with columns in correct order
- JSON: Array of objects with proper field ordering
- Excel: Spreadsheet with all data and correct column headers

## Submission

- Fix the code to resolve both issues
- Update this README with your findings and solution approach
- Ensure all export formats work correctly

## Dependencies

- PostgreSQL JDBC Driver
- OpenCSV for CSV export
- Jackson for JSON processing
- Apache POI for Excel files