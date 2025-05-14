package pl.where2play.api.test.e2e.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuration class for E2E tests.
 * Provides environment-specific configuration values using Spring's property resolution.
 */
@Slf4j
@Configuration
@PropertySources({
    @PropertySource(value = "classpath:test-environments.yml", factory = YamlPropertySourceFactory.class),
    @PropertySource(value = "classpath:application-${spring.profiles.active:dev}.yml", ignoreResourceNotFound = true, factory = YamlPropertySourceFactory.class),
    @PropertySource(value = "classpath:application-${spring.profiles.active:dev}.properties", ignoreResourceNotFound = true)
    // Note: Spring will automatically load properties for all active profiles
    // For example, if 'staging' profile is active, it will load application-staging.yml/properties
})
public class ApiTestConfig {
    // Default values
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_CONFIG_FILE = "application-dev.yml";
    private static final int DEFAULT_TIMEOUT = 10;
    private static final String DEFAULT_ENV = "dev";

    @Value("${test.environment:" + DEFAULT_ENV + "}")
    private String defaultEnvironment;

    @Autowired
    private Environment springEnvironment;

    /**
     * Get the base URL for the current environment
     */
    public String getBaseUrl() {
        String env = getEnvironment();
        String propertyName = env + ".baseUrl";

        System.out.println("[DEBUG_LOG] Looking for property: " + propertyName);

        // Check if the property exists
        if (springEnvironment.containsProperty(propertyName)) {
            System.out.println("[DEBUG_LOG] Property " + propertyName + " exists");
        } else {
            System.out.println("[DEBUG_LOG] Property " + propertyName + " does not exist");
        }

        String baseUrl = springEnvironment.getProperty(propertyName, DEFAULT_BASE_URL);
        System.out.println("[DEBUG_LOG] Property " + propertyName + " value: " + baseUrl);

        // If not found in environment-specific property, try default
        if (baseUrl.equals(DEFAULT_BASE_URL) && !env.equals("default")) {
            baseUrl = springEnvironment.getProperty("default.baseUrl", DEFAULT_BASE_URL);
        }

        log.info("Using base URL: {}", baseUrl);
        System.out.println("[DEBUG_LOG] Using base URL: " + baseUrl);
        return baseUrl;
    }

    /**
     * Get the database configuration for the current environment
     */
    public Map<String, String> getDatabaseConfig() {
        Map<String, String> dbConfig = new HashMap<>();

        try {
            // Get database properties directly from Spring Environment
            String url = springEnvironment.getProperty("spring.datasource.url", "");
            String driverClassName = springEnvironment.getProperty("spring.datasource.driverClassName", "");
            String username = springEnvironment.getProperty("spring.datasource.username", "");
            String password = springEnvironment.getProperty("spring.datasource.password", "");

            log.info("Database URL: {}", url);
            log.info("Database Driver: {}", driverClassName);
            log.info("Database Username: {}", username);

            // If driver class name is empty, use H2 driver as default
            if (driverClassName == null || driverClassName.isEmpty()) {
                driverClassName = "org.h2.Driver";
                log.info("Using default H2 driver: {}", driverClassName);
            }

            // If URL is empty, use default H2 in-memory database
            if (url == null || url.isEmpty()) {
                url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
                log.info("Using default H2 in-memory database URL: {}", url);
            }

            dbConfig.put("url", url);
            dbConfig.put("driverClassName", driverClassName);
            dbConfig.put("username", username);
            dbConfig.put("password", password);
            dbConfig.put("timeout", String.valueOf(getConnectionTimeout()));

            log.info("Loaded database configuration: {}", dbConfig);
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
        log.info("Active profiles: {}", Arrays.toString(activeProfiles));
        System.out.println("[DEBUG_LOG] Active profiles: " + Arrays.toString(activeProfiles));

        // Check for any known environment profile (dev, prod, staging, qa, etc.)
        // First, check if any of the active profiles is directly defined in test-environments.yml
        for (String profile : activeProfiles) {
            System.out.println("[DEBUG_LOG] Checking profile: " + profile);
            boolean hasBaseUrl = springEnvironment.containsProperty(profile + ".baseUrl");
            System.out.println("[DEBUG_LOG] Profile " + profile + " has baseUrl property: " + hasBaseUrl);

            if (hasBaseUrl) {
                log.info("Using environment from profile: {}", profile);
                System.out.println("[DEBUG_LOG] Using environment from profile: " + profile);
                return profile;
            }
        }

        // If e2e profile is active, look for matching environment profile
        if (Arrays.asList(activeProfiles).contains("e2e")) {
            // If no specific environment profile found, try to find any profile that has a baseUrl property
            Optional<String> matchingEnv = Arrays.stream(activeProfiles)
                .filter(profile -> !profile.equals("e2e"))
                .filter(profile -> springEnvironment.containsProperty(profile + ".baseUrl"))
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
        String env = getEnvironment();
        String propertyName = env + ".timeout";

        // Try environment-specific timeout
        Integer timeout = springEnvironment.getProperty(propertyName, Integer.class);

        // If not found, try default environment
        if (timeout == null && !env.equals("default")) {
            timeout = springEnvironment.getProperty("default.timeout", Integer.class);
        }

        // If still not found, use default value
        if (timeout == null) {
            timeout = DEFAULT_TIMEOUT;
        }

        log.debug("Using connection timeout: {}", timeout);
        return timeout;
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
}
