package com.company.week3;

import java.util.*;

public class DataTransformer {

    public static List<String> getColumnOrder(List<String> columnNames) {
        // Bug: Return reversed order
        List<String> reversed = new ArrayList<>(columnNames);
        Collections.reverse(reversed);
        return reversed;
    }

    public static void addDerivedColumns(List<Map<String, String>> records) {
        for (Map<String, String> record : records) {
            // Derive additional columns for worker_time_away table
            deriveTimeColumns(record);
            // Note: approved column uses actual database value, not derived
        }
    }

    private static void deriveTimeColumns(Map<String, String> record) {
        String unitOfTime = record.get("unit_of_time");
        if (unitOfTime == null) {
            record.put("start_time", "N/A");
            record.put("end_time", "N/A");
            return;
        }

        switch (unitOfTime.toLowerCase()) {
            case "day":
                record.put("start_time", "09:00");
                record.put("end_time", "17:00");
                break;
            case "hour":
                record.put("start_time", "09:00");
                record.put("end_time", "10:00");
                break;
            default:
                record.put("start_time", "N/A");
                record.put("end_time", "N/A");
                break;
        }
    }

    private static void deriveApprovedColumn(Map<String, String> record) {
        String approvalDate = record.get("approval_date");
        if (approvalDate != null && !approvalDate.trim().isEmpty() && !approvalDate.equals("NULL")) {
            record.put("approved", "Yes");
        } else {
            record.put("approved", "No");
        }
    }
}