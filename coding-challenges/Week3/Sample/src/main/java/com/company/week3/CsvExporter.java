package com.company.week3;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CsvExporter {

    public static void export(List<Map<String, String>> records, List<String> columnOrder, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            String[] header = columnOrder.toArray(new String[0]);
            writer.writeNext(header);

            // Write data
            for (Map<String, String> record : records) {
                String[] row = new String[columnOrder.size()];
                for (int i = 0; i < columnOrder.size(); i++) {
                    String column = columnOrder.get(i);
                    row[i] = record.getOrDefault(column, "");
                }
                writer.writeNext(row);
            }
        }

        System.out.println("CSV export completed: " + records.size() + " records to " + filePath);
    }
}