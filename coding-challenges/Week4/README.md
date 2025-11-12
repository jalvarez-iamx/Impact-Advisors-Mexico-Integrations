# Week 4 Challenge: Data Dashboard with Search and Pagination

## Challenge Overview

You are provided with an enhanced Spring Boot web application that displays worker_time_away data in a dashboard format with search functionality and pagination. The application includes the derived columns from Week 2. However, upon running the application, you will notice multiple display and functionality issues that make the dashboard unusable.

## Your Mission

1. **Investigate**: Understand why the dashboard displays data incorrectly and why pagination/search don't work properly.

2. **Analyze**: Find the root causes in the backend data processing, frontend rendering, and pagination logic.

3. **Fix**: Correct all issues to ensure:
   - Data displays correctly without truncation, concatenation, or type errors
   - Table layout shows columns properly (not as rows)
   - Pagination works for all results (not just first 15)
   - Search functionality filters results accurately
   - Derived columns appear correctly

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Access to the PostgreSQL database (credentials in .env file)

## Setup Instructions

1. Navigate to the project directory:
   ```bash
   cd coding-challenges/Week4/Sample
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Open your browser to `http://localhost:8080` to view the dashboard issues.

## Application Structure

The application builds on Week 2 with additional components:
- `DataDashboardController`: Enhanced controller with search and pagination
- `DataService`: Service layer for data retrieval and processing
- `PaginationHelper`: Utility for pagination logic
- `SearchFilter`: Search functionality
- `dashboard.html`: Thymeleaf template with table display
- `WorkerTimeAwayRecord`: Data model with derived fields

## Challenge Details

- The application uses the same database as Weeks 2-3
- Derived columns (start_time, end_time, approved) should display correctly
- Search should work on multiple fields (worker, type, dates)
- Pagination should allow browsing through all records
- Table should display in proper columnar format

## Issues to Address

- **Data Display Errors**: All text content is truncated to only 4 characters, making data unreadable
- **Layout Problems**: Columns are rendered as rows instead of proper table structure
- **Incomplete Data Loading**: Only first 15 records shown despite over 700 existing in database
- **Search Malfunction**: Search queries return incorrect or no results

## Expected Behavior

- Clean table display with proper columns and all data visible
- All records from the database displayed on a single page
- Working search that filters results in real-time
- All derived columns populated and displayed correctly
- Responsive design that works on different screen sizes

## Submission

- Fix all display, pagination, and search issues
- Update this README with your findings and solution approach
- Ensure the dashboard works correctly for the full dataset
- Include any frontend/backend optimizations made

## Dependencies

- Spring Boot Web Starter
- Thymeleaf templating
- PostgreSQL JDBC Driver