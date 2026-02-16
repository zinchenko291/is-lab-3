package me.zinch.is.islab3;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    public static final String UNIT_NAME = "postgres";
    private static final Properties S3_PROPERTIES = loadS3Properties();

    public static final String S3_ENDPOINT = getProperty("s3.endpoint");
    public static final String S3_REGION = getProperty("s3.region");
    public static final String S3_ACCESS_KEY = getProperty("s3.access-key");
    public static final String S3_SECRET_KEY = getProperty("s3.secret-key");
    public static final String S3_BUCKET = getProperty("s3.bucket");

    private Config() {}

    private static Properties loadS3Properties() {
        Properties properties = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("s3.properties")) {
            if (in == null) {
                throw new IllegalStateException("Missing s3.properties in classpath");
            }
            properties.load(in);
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load s3.properties", e);
        }
    }

    private static String getProperty(String key) {
        String value = S3_PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required S3 property: " + key);
        }
        return value.trim();
    }
}
