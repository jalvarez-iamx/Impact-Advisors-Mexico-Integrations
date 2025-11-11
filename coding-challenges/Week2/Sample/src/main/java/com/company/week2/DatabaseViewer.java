package com.company.week2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Controller
public class DatabaseViewer {

    // Database configuration
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;

    static {
        Dotenv dotenv = Dotenv.load();
        DB_URL = dotenv.get("DB_URL");
        DB_USERNAME = dotenv.get("DB_USERNAME");
        DB_PASSWORD = dotenv.get("DB_PASSWORD");
        DB_DRIVER = dotenv.get("DB_DRIVER");
    }

    // Hardcoded column definitions for worker_time_away table
    private static final List<ColumnDefinition> COLUMN_DEFINITIONS = List.of(
        new ColumnDefinition("time_away_entry", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("supervisory_organization", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("worker", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("request_type", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("time_away_absence_table", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("type", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("entered_on", java.sql.Types.DATE, "date"),
        new ColumnDefinition("approval_date", java.sql.Types.DATE, "date"),
        new ColumnDefinition("time_away_date", java.sql.Types.DATE, "date"),
        new ColumnDefinition("start_time", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("end_time", java.sql.Types.VARCHAR, "varchar"),
        new ColumnDefinition("approved", java.sql.Types.DATE, "date"),
        new ColumnDefinition("unit_of_time", java.sql.Types.VARCHAR, "varchar")
    );

    // Typesafe data classes
    public static class ColumnDefinition {
        public final String name;
        public final int type;
        public final String typeName;

        public ColumnDefinition(String name, int type, String typeName) {
            this.name = name;
            this.type = type;
            this.typeName = typeName;
        }
    }

    public static class RowData {
        public final List<String> cells;

        public RowData(List<String> cells) {
            this.cells = cells;
        }
    }
    public static record TableData(String name, List<ColumnDefinition> columns, List<RowData> rows, int rowCount) {}

    // Custom exception for validation failures
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(DatabaseViewer.class, args);
    }

    @GetMapping("/")
    public String displayDatabaseContents(Model model) {
        List<TableData> tables = new ArrayList<>();

        Connection connection = null;
        try {
            // Load the JDBC driver
            Class.forName(DB_DRIVER);
            System.out.println("JDBC Driver loaded successfully");

            // Establish connection
            System.out.println("Attempting to connect to database...");
            System.out.println("URL: " + DB_URL);
            System.out.println("Username: " + DB_USERNAME);
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Database connection established successfully");

            // Get database metadata to explore available databases
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("Connected to: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("Database URL: " + metaData.getURL());
            System.out.println("Username: " + metaData.getUserName());

            // List all available databases/schemas
            ResultSet schemas = metaData.getCatalogs();
            System.out.println("\nAvailable databases:");
            while (schemas.next()) {
                String schemaName = schemas.getString("TABLE_CAT");
                System.out.println("- " + schemaName);
            }
            schemas.close();

            // Focus on the worker_time_away table specifically
            String targetTable = "worker_time_away";
            System.out.println("\nQuerying table: " + targetTable);

            TableData tableData = getTableData(connection, targetTable);
            tables.add(tableData);

        } catch (ClassNotFoundException e) {
            String errorMsg = "JDBC Driver not found: " + e.getMessage();
            System.err.println(errorMsg);
            model.addAttribute("error", errorMsg);
            return "error";
        } catch (SQLException e) {
            String errorMsg = "Database connection error: " + e.getMessage();
            System.err.println(errorMsg);
            model.addAttribute("error", errorMsg);
            return "error";
        } catch (Exception e) {
            String errorMsg = "Unexpected error: " + e.getMessage();
            System.err.println(errorMsg);
            model.addAttribute("error", errorMsg);
            return "error";
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed");
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }

        model.addAttribute("tables", tables);
        return "database";
    }

    private List<String> getTableNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }
        tables.close();
        return tableNames;
    }

    private TableData getTableData(Connection connection, String tableName) throws SQLException {
        // Query table contents
        String query = "SELECT * FROM " + tableName;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        List<RowData> validRows = new ArrayList<>();
        while (resultSet.next()) {
            List<String> rowData = new ArrayList<>();
            for (ColumnDefinition col : COLUMN_DEFINITIONS) {
                try {
                    String formattedValue = getValidatedValue(resultSet, col.name, col.type);
                    rowData.add(formattedValue);
                } catch (ValidationException e) {
                    rowData.add(""); // Don't display invalid data
                }
            }
            validRows.add(new RowData(rowData));
        }

        resultSet.close();
        statement.close();

        return new TableData(tableName, COLUMN_DEFINITIONS, validRows, validRows.size());
    }

    private String getValidatedValue(ResultSet resultSet, String columnName, int sqlType) throws ValidationException, SQLException {
        Object obj = resultSet.getObject(columnName);
        if (obj == null) {
            return "NULL";
        }

        switch (sqlType) {
            case java.sql.Types.DATE:
            case java.sql.Types.TIMESTAMP:
                try {
                    java.sql.Date date = resultSet.getDate(columnName);
                    if (date == null) {
                        throw new ValidationException("Date parsing failed for column " + columnName);
                    }
                    return date.toString();
                } catch (SQLException e) {
                    throw new ValidationException("Invalid date value for column " + columnName + ": " + e.getMessage());
                }

            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                try {
                    java.math.BigDecimal decimal = resultSet.getBigDecimal(columnName);
                    if (decimal == null) {
                        throw new ValidationException("Decimal parsing failed for column " + columnName);
                    }
                    return decimal.toString();
                } catch (SQLException e) {
                    throw new ValidationException("Invalid decimal value for column " + columnName + ": " + e.getMessage());
                }

            case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
                try {
                    long longVal = resultSet.getLong(columnName);
                    if (resultSet.wasNull()) {
                        throw new ValidationException("Integer parsing failed for column " + columnName);
                    }
                    return String.valueOf(longVal);
                } catch (SQLException e) {
                    throw new ValidationException("Invalid integer value for column " + columnName + ": " + e.getMessage());
                }

            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
                try {
                    double doubleVal = resultSet.getDouble(columnName);
                    if (resultSet.wasNull()) {
                        throw new ValidationException("Double parsing failed for column " + columnName);
                    }
                    return String.valueOf(doubleVal);
                } catch (SQLException e) {
                    throw new ValidationException("Invalid double value for column " + columnName + ": " + e.getMessage());
                }

            default:
                // For VARCHAR, TEXT, etc. - strings are always valid
                String strVal = resultSet.getString(columnName);
                return strVal != null ? strVal : "NULL";
        }
    }
}
