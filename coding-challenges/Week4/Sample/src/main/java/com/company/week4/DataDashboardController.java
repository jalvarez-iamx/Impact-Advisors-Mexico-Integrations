package com.company.week4;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class DataDashboardController {

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

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "search", required = false) String search,
                           Model model) {

        List<Map<String, String>> records = new ArrayList<>();
        int totalRecords = 0;

        Connection connection = null;
        try {
            Class.forName(DB_DRIVER);
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Get total count for display
            String countQuery = "SELECT COUNT(*) FROM worker_time_away";
            Statement countStmt = connection.createStatement();
            ResultSet countRs = countStmt.executeQuery(countQuery);
            if (countRs.next()) {
                totalRecords = countRs.getInt(1);
            }
            countRs.close();
            countStmt.close();

            // Bug: Only fetches first 15 records instead of all data
            String dataQuery = "SELECT * FROM worker_time_away LIMIT 15";
            Statement dataStmt = connection.createStatement();
            ResultSet rs = dataStmt.executeQuery(dataQuery);

            while (rs.next()) {
                Map<String, String> record = new HashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String value = rs.getString(i);
                    // Bug: Data processing errors - truncate to 4 characters
                    if (value != null && value.length() > 4) {
                        value = value.substring(0, 4); // Truncate to 4 chars
                    }
                    record.put(columnName, value != null ? value : "");
                }

                // Bug: Derived columns with errors
                deriveColumnsWithErrors(record);

                records.add(record);
            }

            rs.close();
            dataStmt.close();

        } catch (Exception e) {
            model.addAttribute("error", "Database error: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }

        // Bug: Layout data as rows instead of columns
        List<List<String>> tableRows = convertToRowsLayout(records);

        model.addAttribute("tableRows", tableRows);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("displayedRecords", records.size());

        return "dashboard";
    }

    // Bug: Derived columns with wrong logic
    private void deriveColumnsWithErrors(Map<String, String> record) {
        String unitOfTime = record.get("unit_of_time");
        if (unitOfTime != null) {
            if (unitOfTime.equalsIgnoreCase("day")) {
                record.put("start_time", "09:00");
                record.put("end_time", "17:00");
            } else {
                record.put("start_time", "ERROR");
                record.put("end_time", "ERROR");
            }
        }

        String approvalDate = record.get("approval_date");
        if (approvalDate != null && !approvalDate.trim().isEmpty()) {
            record.put("approved", "Yes");
        } else {
            record.put("approved", "No");
        }
    }

    // Bug: Convert columnar data to row-based layout
    private List<List<String>> convertToRowsLayout(List<Map<String, String>> records) {
        List<List<String>> rows = new ArrayList<>();

        if (!records.isEmpty()) {
            Map<String, String> firstRecord = records.get(0);
            for (String key : firstRecord.keySet()) {
                List<String> row = new ArrayList<>();
                row.add(key); // Column name as first cell
                for (Map<String, String> record : records) {
                    row.add(record.get(key)); // Values
                }
                rows.add(row);
            }
        }

        return rows;
    }
}