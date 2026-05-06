package com.pharmacy.shared.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class AppConfig {

    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE_NAME = "application.properties";

    static {
        // 1. Check if there is an external config file (at the same level as the working directory)
        File externalConfigFile = new File(CONFIG_FILE_NAME);

        if (externalConfigFile.exists() && !externalConfigFile.isDirectory()) {
            // IF EXTERNAL FILE EXISTS: Prioritize loading from the File System
            try (InputStream input = new FileInputStream(externalConfigFile)) {
                properties.load(input);
                log.info("Configuration loaded from EXTERNAL file: {}", externalConfigFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("Failed to load configuration from external file.", e);
            }
        } else {
            // IF NOT EXISTS: Fallback to loading the default file packed inside the .jar (Classpath)
            try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
                if (input == null) {
                    log.error("Configuration file '{}' was not found on the classpath either.", CONFIG_FILE_NAME);
                } else {
                    properties.load(input);
                    log.info("Configuration loaded from INTERNAL classpath (inside .jar).");
                }
            } catch (Exception e) {
                log.error("Failed to load configuration from classpath.", e);
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public static long getLong(String key, long defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Long.parseLong(value) : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}