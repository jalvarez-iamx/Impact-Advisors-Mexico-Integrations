package com.company.week3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonExporter {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void export(List<Map<String, String>> records, List<String> columnOrder, String filePath) throws IOException {
        System.out.println("Starting JSON export to " + filePath);

        ArrayNode arrayNode = mapper.createArrayNode();

        for (Map<String, String> record : records) {
            ObjectNode objectNode = mapper.createObjectNode();

            for (String column : columnOrder) {
                String value = record.getOrDefault(column, "");
                objectNode.put(column, value);
            }

            arrayNode.add(objectNode);
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode));
        }

        System.out.println("JSON export completed: " + records.size() + " records to " + filePath);
    }
}