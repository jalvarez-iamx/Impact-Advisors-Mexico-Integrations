package com.company.week3;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public static void export(List<Map<String, String>> records, List<String> columnOrder, String filePath) throws IOException {
        System.out.println("Starting Excel export to " + filePath);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data Export");

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnOrder.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnOrder.get(i));
            }

            // Create data rows
            for (int i = 0; i < records.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, String> record = records.get(i);

                for (int j = 0; j < columnOrder.size(); j++) {
                    String column = columnOrder.get(j);
                    String value = record.getOrDefault(column, "");
                    Cell cell = row.createCell(j);
                    cell.setCellValue(value);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columnOrder.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }

        System.out.println("Excel export completed: " + records.size() + " records to " + filePath);
    }
}