package pl.where2play.api.test.e2e.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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
    private static final String ENV_CONFIG_FILE = "test-environments.yml";

    // Cache for parsed YAML data
    private final Map<String, Map<String, Object>> yamlCache = new ConcurrentHashMap<>();

    @Value("${test.environment:" + DEFAULT_ENV + "}")
    private String defaultEnvironment;

    @Autowired
    private Environment springEnvironment;

    @Getter(lazy = true)
    private final Map<String, Object> environmentConfig = loadEnvironmentConfig();

    /**
     * Get the base URL for the current environment
     */
    public String getBaseUrl() {
        return getEnvironmentProperty("baseUrl", DEFAULT_BASE_URL);
    }

    /**
     * Get the database configuration for the current environment
     */
    public Map<String, String> getDatabaseConfig() {
        Map<String, String> dbConfig = new HashMap<>();

        try {
            // Get config file and parse it
            String configFile = getEnvironmentProperty("configFile", DEFAULT_CONFIG_FILE);
            Map<String, Object> yamlMap = parseYamlFile(configFile);

            // Extract database configuration using Optional to handle nulls safely
            Optional.ofNullable(yamlMap)
                .map(map -> (Map<String, Object>) map.get("spring"))
                .map(spring -> (Map<String, Object>) spring.get("datasource"))
                .ifPresent(datasource -> {
                    // Extract database properties
                    dbConfig.put("url", getStringValue(datasource, "url", ""));
                    dbConfig.put("driverClassName", getStringValue(datasource, "driverClassName", ""));
                    dbConfig.put("username", getStringValue(datasource, "username", ""));
                    dbConfig.put("password", getStringValue(datasource, "password", ""));
                    dbConfig.put("timeout", String.valueOf(getConnectionTimeout()));
                });
        } catch (Exception e) {
            log.error("Error loading database configuration", e);
            // Don't throw exception, return empty config instead
        }

        return dbConfig;
    }

    /**
     * Get the current environment name based on active profiles
     */
    public String getEnvironment() {
        String[] activeProfiles = springEnvironment.getActiveProfiles();
        log.debug("Active profiles: {}", Arrays.toString(activeProfiles));

        // If e2e profile is active, look for matching environment profile
        if (Arrays.asList(activeProfiles).contains("e2e")) {
            // Find first profile that matches an environment name
            Optional<String> matchingEnv = Stream.of(activeProfiles)
                .filter(profile -> !profile.equals("e2e"))
                .filter(profile -> getEnvironmentConfig().containsKey(profile))
                .findFirst();

            if (matchingEnv.isPresent()) {
                log.info("Using environment from profile: {}", matchingEnv.get());
                return matchingEnv.get();
            }
        }

        // If no matching profile found, return default
        log.info("Using default environment: {}", defaultEnvironment);
        return defaultEnvironment;
    }

    /**
     * Get the connection timeout for the current environment
     */
    public int getConnectionTimeout() {
        return getEnvironmentProperty("timeout", DEFAULT_TIMEOUT);
    }

    /**
     * Create database connection properties with timeout
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
            props.setProperty("connectTimeout", String.valueOf(timeoutSeconds * 1000));
            props.setProperty("socketTimeout", String.valueOf(timeoutSeconds * 1000));

            log.debug("Created connection properties with timeout: {} seconds", timeoutSeconds);
        } catch (Exception e) {
            log.error("Error creating connection properties", e);
            setDefaultProperties(props);
        }

        return props;
    }

    /**
     * Set default properties for database connection
     */
    private void setDefaultProperties(Properties props) {
        props.setProperty("user", "");
        props.setProperty("password", "");
        props.setProperty("loginTimeout", String.valueOf(DEFAULT_TIMEOUT));
        props.setProperty("connectTimeout", String.valueOf(DEFAULT_TIMEOUT * 1000));
        props.setProperty("socketTimeout", String.valueOf(DEFAULT_TIMEOUT * 1000));
    }

    /**
     * Get a property from the environment configuration
     */
    @SuppressWarnings("unchecked")
    private <T> T getEnvironmentProperty(String property, T defaultValue) {
        try {
            // Get current environment config
            String currentEnv = getEnvironment();
            Map<String, Object> envConfig = (Map<String, Object>) getEnvironmentConfig().get(currentEnv);

            // Try to get from current environment
            if (envConfig != null && envConfig.containsKey(property)) {
                T value = (T) envConfig.get(property);
                log.debug("Found property {} in environment {}: {}", property, currentEnv, value);
                return value;
            }

            // Try to get from default environment
            Map<String, Object> defaultConfig = (Map<String, Object>) getEnvironmentConfig().get("default");
            if (defaultConfig != null && defaultConfig.containsKey(property)) {
                T value = (T) defaultConfig.get(property);
                log.debug("Found property {} in default environment: {}", property, value);
                return value;
            }
        } catch (Exception e) {
            log.warn("Error getting property {}: {}", property, e.getMessage());
        }

        log.debug("Property {} not found, using default: {}", property, defaultValue);
        return defaultValue;
    }

    /**
     * Load the environment configuration from YAML file
     */
    private Map<String, Object> loadEnvironmentConfig() {
        try {
            Map<String, Object> config = parseYamlFile(ENV_CONFIG_FILE);
            log.debug("Loaded environment config with {} environments", 
                    config != null ? config.size() : 0);
            return config;
        } catch (IOException e) {
            log.error("Error loading environment configuration", e);
            return new HashMap<>();
        }
    }

    /**
     * Parse a YAML file and cache the result
     */
    private Map<String, Object> parseYamlFile(String fileName) throws IOException {
        // Check cache first
        return yamlCache.computeIfAbsent(fileName, this::loadYamlFile);
    }

    /**
     * Load a YAML file from classpath
     */
    private Map<String, Object> loadYamlFile(String fileName) {
        try (var inputStream = new ClassPathResource(fileName).getInputStream()) {
            Map<String, Object> yamlMap = new Yaml().load(inputStream);
            return yamlMap != null ? yamlMap : new HashMap<>();
        } catch (IOException e) {
            log.error("Error loading YAML file: {}", fileName, e);
            return new HashMap<>();
        }
    }

    /**
     * Helper method to safely get a string value from a map
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        return Optional.ofNullable(map)
            .map(m -> m.get(key))
            .map(Object::toString)
            .orElse(defaultValue);
    }
}
