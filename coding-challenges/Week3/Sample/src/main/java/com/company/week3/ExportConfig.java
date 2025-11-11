package com.company.week3;

import io.github.cdimascio.dotenv.Dotenv;

public class ExportConfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getDbUrl() {
        return dotenv.get("DB_URL");
    }

    public static String getDbUsername() {
        return dotenv.get("DB_USERNAME");
    }

    public static String getDbPassword() {
        return dotenv.get("DB_PASSWORD");
    }

    public static String getDbDriver() {
        return dotenv.get("DB_DRIVER");
    }

    public static String getTableName() {
        return "worker_time_away";
    }
}