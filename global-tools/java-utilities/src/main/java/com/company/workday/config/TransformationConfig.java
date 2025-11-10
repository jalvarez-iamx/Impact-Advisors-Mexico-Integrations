package com.company.workday.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Configuration management for IAMX transformation utilities.
 * Provides centralized configuration for transformation rules, mappings, and settings.
 */
public class TransformationConfig {

    private static final String CONFIG_FILE = "iamx-config.properties";
    private static TransformationConfig instance;
    private Properties properties;
    private Map<String, String> customMappings;

    private TransformationConfig() {
        properties = new Properties();
        customMappings = new HashMap<>();
        loadDefaultConfig();
        loadConfigFile();
    }

    /**
     * Gets the singleton instance of the configuration.
     */
    public static synchronized TransformationConfig getInstance() {
        if (instance == null) {
            instance = new TransformationConfig();
        }
        return instance;
    }

    /**
     * Loads default configuration values.
     */
    private void loadDefaultConfig() {
        // Default transformation settings
        properties.setProperty("date.format.input", "MM/dd/yyyy,dd/MM/yyyy,yyyy-MM-dd");
        properties.setProperty("date.format.output", "yyyy-MM-dd");
        properties.setProperty("employee.id.prefix", "EM");
        properties.setProperty("employee.id.length", "8");
        properties.setProperty("batch.size.default", "1000");
        properties.setProperty("thread.pool.size", String.valueOf(Runtime.getRuntime().availableProcessors()));
        properties.setProperty("error.handling.mode", "SAFE"); // SAFE or STRICT
        properties.setProperty("logging.level", "INFO");

        // Healthcare-specific defaults
        properties.setProperty("healthcare.fte.standard", "40");
        properties.setProperty("healthcare.pay.periods", "26");
        properties.setProperty("healthcare.currency", "USD");
    }

    /**
     * Loads configuration from properties file if available.
     */
    private void loadConfigFile() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // Use defaults if config file not found
        }
    }

    /**
     * Gets a configuration property as string.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a configuration property as string with default value.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets a configuration property as integer.
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a configuration property as boolean.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Sets a configuration property.
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Adds a custom mapping for transformations.
     */
    public void addCustomMapping(String key, String value) {
        customMappings.put(key, value);
    }

    /**
     * Gets a custom mapping value.
     */
    public String getCustomMapping(String key) {
        return customMappings.get(key);
    }

    /**
     * Gets all custom mappings.
     */
    public Map<String, String> getAllCustomMappings() {
        return new HashMap<>(customMappings);
    }

    /**
     * Gets the default batch size for processing.
     */
    public int getDefaultBatchSize() {
        return getIntProperty("batch.size.default", 1000);
    }

    /**
     * Gets the default thread pool size.
     */
    public int getDefaultThreadPoolSize() {
        return getIntProperty("thread.pool.size", Runtime.getRuntime().availableProcessors());
    }

    /**
     * Checks if error handling should be in safe mode.
     */
    public boolean isSafeMode() {
        return "SAFE".equalsIgnoreCase(getProperty("error.handling.mode", "SAFE"));
    }

    /**
     * Gets the standard FTE hours.
     */
    public double getStandardFTEHours() {
        return getIntProperty("healthcare.fte.standard", 40);
    }

    /**
     * Gets the number of pay periods per year.
     */
    public int getPayPeriodsPerYear() {
        return getIntProperty("healthcare.pay.periods", 26);
    }

    /**
     * Gets the default currency code.
     */
    public String getDefaultCurrency() {
        return getProperty("healthcare.currency", "USD");
    }

    /**
     * Reloads configuration from file.
     */
    public void reload() {
        properties.clear();
        customMappings.clear();
        loadDefaultConfig();
        loadConfigFile();
    }

    /**
     * Gets configuration summary for debugging.
     */
    public String getConfigSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== IAMX Configuration Summary ===\n");
        summary.append("Batch Size: ").append(getDefaultBatchSize()).append("\n");
        summary.append("Thread Pool Size: ").append(getDefaultThreadPoolSize()).append("\n");
        summary.append("Safe Mode: ").append(isSafeMode()).append("\n");
        summary.append("Default Currency: ").append(getDefaultCurrency()).append("\n");
        summary.append("FTE Hours: ").append(getStandardFTEHours()).append("\n");
        summary.append("Pay Periods/Year: ").append(getPayPeriodsPerYear()).append("\n");
        summary.append("Custom Mappings: ").append(customMappings.size()).append("\n");
        return summary.toString();
    }
}