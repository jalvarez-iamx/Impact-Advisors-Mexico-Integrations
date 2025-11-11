package com.company.week3;

import java.util.List;
import java.util.Map;

public class DataExporterApplication {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar data-exporter.jar <format> <output-file>");
            System.err.println("Formats: csv, json, excel");
            System.exit(1);
        }

        String format = args[0].toLowerCase();
        String outputFile = args[1];

        DatabaseService dbService = new DatabaseService();

        try {
            // Connect to database
            dbService.connect();

            // Get total count for verification
            int totalCount = dbService.getTotalRecordCount();
            System.out.println("Total records in database: " + totalCount);

            // Fetch records (with bug: only 100)
            List<Map<String, String>> records = dbService.fetchAllRecords();
            System.out.println("Fetched records: " + records.size());

            // Add derived columns
            DataTransformer.addDerivedColumns(records);

            // Get column order (with bug: reversed)
            List<String> columnOrder = DataTransformer.getColumnOrder(dbService.getColumnNames());

            // Export based on format
            switch (format) {
                case "csv":
                    CsvExporter.export(records, columnOrder, outputFile);
                    break;
                case "json":
                    JsonExporter.export(records, columnOrder, outputFile);
                    break;
                case "excel":
                    ExcelExporter.export(records, columnOrder, outputFile);
                    break;
                default:
                    System.err.println("Unsupported format: " + format);
                    System.exit(1);
            }

            System.out.println("Export completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbService.disconnect();
        }
    }
}