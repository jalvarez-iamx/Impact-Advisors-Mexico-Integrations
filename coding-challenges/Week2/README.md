# Week 2 Challenge: Database Viewer Enhancement

## Challenge Overview

You are provided with a Spring Boot application that displays data from a PostgreSQL database table called `worker_time_away`. The application successfully connects to the database and displays various columns related to employee time away requests.

However, upon running the application, you will notice that three specific columns are not populated: `start_time`, `end_time`, and `approved`.

## Your Mission

1. **Investigate**: Understand why the `start_time`, `end_time`, and `approved` columns are not populated in the displayed data.

2. **Analyze**: Determine if these columns can be populated based on the existing data or application logic.

3. **Fix (if possible)**: Implement a solution to populate these columns appropriately. If it's not possible to fix, explain why.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Access to the PostgreSQL database (credentials provided in the code)

## Setup Instructions

1. Navigate to the project directory:
   ```bash
   cd coding-challenges/Week2/Sample
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Open your browser to `http://localhost:8080` to view the data.

## Challenge Details

- The application uses hardcoded column definitions in `DatabaseViewer.java`.
- The table `worker_time_away` contains employee time away data.
- Columns like `time_away_date`, `unit_of_time`, `approval_date` may contain relevant information.
- Consider business logic: for time away requests, start/end times might be derivable, and approval status from approval_date.

## Submission

- Modify the code as needed to address the issue.
- Update this README with your findings and solution approach.
- If no fix is possible, document the reasons.

## Dependencies

- PostgreSQL JDBC Driver (org.postgresql:postgresql:42.7.3)