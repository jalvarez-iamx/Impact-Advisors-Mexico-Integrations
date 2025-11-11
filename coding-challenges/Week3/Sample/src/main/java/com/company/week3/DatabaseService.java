package com.company.week3;

import java.sql.*;
import java.util.*;

public class DatabaseService {

    private Connection connection;
    private List<String> columnNames;

    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName(ExportConfig.getDbDriver());
        connection = DriverManager.getConnection(
            ExportConfig.getDbUrl(),
            ExportConfig.getDbUsername(),
            ExportConfig.getDbPassword()
        );
        System.out.println("Database connection established");
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public List<Map<String, String>> fetchAllRecords() throws SQLException {
        List<Map<String, String>> records = new ArrayList<>();

        // Bug: LIMIT 100 in query
        String query = "SELECT * FROM " + ExportConfig.getTableName() + " LIMIT 100";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        // Get column names dynamically
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        while (resultSet.next()) {
            Map<String, String> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String value = resultSet.getString(i);
                row.put(columnName, value != null ? value : "");
            }
            records.add(row);
        }

        resultSet.close();
        statement.close();

        return records;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public int getTotalRecordCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM " + ExportConfig.getTableName();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        int count = resultSet.getInt(1);
        resultSet.close();
        statement.close();
        return count;
    }
}