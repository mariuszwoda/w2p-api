package pl.where2play.api.test.e2e.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration class for E2E tests.
 * Provides environment-specific configuration values from YAML files.
 */
@Slf4j
@Configuration
public class ApiTestConfig {
    // Default values
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_CONFIG_FILE = "application-dev.yml";
    private static final int DEFAULT_TIMEOUT = 10;
    private static final String DEFAULT_ENV = "dev";

    // Configuration file paths
    private static final String ENV_CONFIG_FILE = "test-environments.yml";

    // Cache for parsed YAML data
    private final Map<String, Map<String, Object>> yamlCache = new ConcurrentHashMap<>();

    @Value("${test.environment:" + DEFAULT_ENV + "}")
    private String defaultEnvironment;

    @Autowired
    private Environment springEnvironment;

    private Map<String, Object> environmentConfig;

    /**
     * Get the base URL for the current environment
     *
     * @return The base URL as a string
     */
    public String getBaseUrl() {
        return getEnvironmentProperty("baseUrl", DEFAULT_BASE_URL);
    }

    /**
     * Get the database configuration for the current environment
     *
     * @return A map containing database configuration properties
     */
    public Map<String, String> getDatabaseConfig() {
        // Create a new map for database configuration
        Map<String, String> dbConfig = new HashMap<>();

        try {
            // Get the configuration file name for the current environment
            String configFile = getEnvironmentProperty("configFile", DEFAULT_CONFIG_FILE);

            // Parse the YAML file
            Map<String, Object> yamlMap = parseYamlFile(configFile);

            // Extract database configuration
            if (yamlMap != null && yamlMap.containsKey("spring")) {
                Map<String, Object> spring = (Map<String, Object>) yamlMap.get("spring");

                if (spring.containsKey("datasource")) {
                    Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");

                    // Extract database properties
                    dbConfig.put("url", getStringValue(datasource, "url", ""));
                    dbConfig.put("driverClassName", getStringValue(datasource, "driverClassName", ""));
                    dbConfig.put("username", getStringValue(datasource, "username", ""));
                    dbConfig.put("password", getStringValue(datasource, "password", ""));

                    // Add connection timeout property
                    dbConfig.put("timeout", String.valueOf(getConnectionTimeout()));
                }
            }
        } catch (Exception e) {
            log.error("Error loading database configuration", e);
            throw new RuntimeException("Error loading database configuration", e);
        }

        return dbConfig;
    }

    /**
     * Parse a YAML file and cache the result
     *
     * @param fileName The name of the YAML file to parse
     * @return The parsed YAML as a Map
     * @throws IOException If the file cannot be read
     */
    private Map<String, Object> parseYamlFile(String fileName) throws IOException {
        // Check if the file is already cached
        if (yamlCache.containsKey(fileName)) {
            return yamlCache.get(fileName);
        }

        // Load and parse the YAML file
        Resource resource = new ClassPathResource(fileName);
        Yaml yaml = new Yaml();

        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Object> yamlMap = yaml.load(inputStream);

            // Cache the result
            if (yamlMap != null) {
                yamlCache.put(fileName, yamlMap);
            }

            return yamlMap;
        }
    }

    /**
     * Helper method to safely get a string value from a map
     *
     * @param map          The map to get the value from
     * @param key          The key to look up
     * @param defaultValue The default value to return if the key is not found or the value is null
     * @return The string value
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        return (String) map.get(key);
    }

    /**
     * Get the current environment name based on active profiles
     *
     * @return The environment name
     */
    public String getEnvironment() {
        // Get active profiles
        String[] activeProfiles = springEnvironment.getActiveProfiles();

        log.debug("Active profiles: {}", Arrays.toString(activeProfiles));

        // If e2e profile is active, look for other profiles that match environment names
        if (Arrays.asList(activeProfiles).contains("e2e")) {
            // Load environment config to get available environment names
            loadEnvironmentConfig();

            if (environmentConfig != null) {
                // Check each active profile against available environments
                for (String profile : activeProfiles) {
                    if (profile.equals("e2e")) {
                        continue; // Skip the e2e profile itself
                    }

                    // If profile matches an environment name, use it
                    if (environmentConfig.containsKey(profile)) {
                        log.info("Using environment from profile: {}", profile);
                        return profile;
                    }
                }
            }
        }

        // If no matching profile found, return the default environment
        log.info("Using default environment: {}", defaultEnvironment);
        return defaultEnvironment;
    }

    /**
     * Get the connection timeout for the current environment
     *
     * @return The connection timeout in seconds
     */
    public int getConnectionTimeout() {
        return getEnvironmentProperty("timeout", DEFAULT_TIMEOUT);
    }

    /**
     * Get a property from the environment configuration
     *
     * @param property     The property name
     * @param defaultValue The default value if the property is not found
     * @return The property value
     */
    @SuppressWarnings("unchecked")
    private <T> T getEnvironmentProperty(String property, T defaultValue) {
        try {
            loadEnvironmentConfig();

            if (environmentConfig == null) {
                log.warn("Environment configuration is null, using default value for {}", property);
                return defaultValue;
            }

            // Get the current environment name
            String currentEnv = getEnvironment();

            // Try to get from the current environment
            Map<String, Object> envConfig = (Map<String, Object>) environmentConfig.get(currentEnv);

            if (envConfig != null && envConfig.containsKey(property)) {
                T value = (T) envConfig.get(property);
                log.debug("Found property {} in environment {}: {}", property, currentEnv, value);
                return value;
            }

            // Try to get from the default environment
            Map<String, Object> defaultConfig = (Map<String, Object>) environmentConfig.get("default");

            if (defaultConfig != null && defaultConfig.containsKey(property)) {
                T value = (T) defaultConfig.get(property);
                log.debug("Found property {} in default environment: {}", property, value);
                return value;
            }

            log.debug("Property {} not found, using default value: {}", property, defaultValue);
            return defaultValue;
        } catch (Exception e) {
            log.warn("Error getting property " + property, e);
            return defaultValue;
        }
    }

    /**
     * Load the environment configuration from the YAML file
     */
    private void loadEnvironmentConfig() {
        if (environmentConfig != null) {
            return;
        }

        try {
            environmentConfig = parseYamlFile(ENV_CONFIG_FILE);
            log.debug("Loaded environment configuration with {} environments",
                    environmentConfig != null ? environmentConfig.size() : 0);
        } catch (IOException e) {
            log.error("Error loading environment configuration", e);
            throw new RuntimeException("Error loading environment configuration", e);
        }
    }

    /**
     * Create database connection properties with timeout
     *
     * @return Properties object with connection settings
     */
    public Properties getConnectionProperties() {
        Properties props = new Properties();

        try {
            // Get database configuration
            Map<String, String> dbConfig = getDatabaseConfig();

            // Set username and password
            props.setProperty("user", dbConfig.getOrDefault("username", ""));
            props.setProperty("password", dbConfig.getOrDefault("password", ""));

            // Set connection timeout
            int timeoutSeconds = getConnectionTimeout();
            props.setProperty("loginTimeout", String.valueOf(timeoutSeconds));
            props.setProperty("connectTimeout", String.valueOf(timeoutSeconds * 1000)); // milliseconds
            props.setProperty("socketTimeout", String.valueOf(timeoutSeconds * 1000)); // milliseconds

            log.debug("Created database connection properties with timeout: {} seconds", timeoutSeconds);
        } catch (Exception e) {
            log.error("Error creating connection properties", e);
            // Set default properties if there's an error
            props.setProperty("user", "");
            props.setProperty("password", "");
            props.setProperty("loginTimeout", String.valueOf(DEFAULT_TIMEOUT));
            props.setProperty("connectTimeout", String.valueOf(DEFAULT_TIMEOUT * 1000));
            props.setProperty("socketTimeout", String.valueOf(DEFAULT_TIMEOUT * 1000));
        }

        return props;
    }
}