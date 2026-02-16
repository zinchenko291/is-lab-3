package me.zinch.is.islab3;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String UNIT_NAME = "postgres";

    private static final Properties S3_PROPERTIES = loadRequiredProperties("s3.properties", "S3");
    private static final Properties EMAIL_PROPERTIES = loadOptionalProperties("email.properties");

    public static final String S3_ENDPOINT = getProperty("s3.endpoint");
    public static final String S3_REGION = getProperty("s3.region");
    public static final String S3_ACCESS_KEY = getProperty("s3.access-key");
    public static final String S3_SECRET_KEY = getProperty("s3.secret-key");
    public static final String S3_BUCKET = getProperty("s3.bucket");
    public static final String MAIL_HOST = getOptionalProperty("MAIL_HOST");
    public static final String MAIL_PORT = getOptionalProperty("MAIL_PORT");
    public static final String MAIL_USER = getOptionalProperty("MAIL_USER");
    public static final String MAIL_PASS = getOptionalProperty("MAIL_PASS");
    public static final String MAIL_FROM = getOptionalProperty("MAIL_FROM");

    private Config() {}

    private static Properties loadRequiredProperties(String filename, String name) {
        Properties properties = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + filename + " in classpath");
            }
            properties.load(in);
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + name + " properties", e);
        }
    }

    private static String getProperty(String key) {
        String value = S3_PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required S3 property: " + key);
        }
        return value.trim();
    }

    private static Properties loadOptionalProperties(String filename) {
        Properties properties = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(filename)) {
            if (in != null) {
                properties.load(in);
            }
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + filename, e);
        }
    }

    private static String getOptionalProperty(String key) {
        String value = EMAIL_PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
