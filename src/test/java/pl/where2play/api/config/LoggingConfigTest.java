package pl.where2play.api.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LoggingConfigTest {

    @Autowired
    private LoggingConfig loggingConfig;

    @BeforeEach
    void setUp() {
//        // Disable loading endpoint settings from application.yml for tests
//        loggingConfig.disableLoadingEndpointSettingsFromConfig();
        loggingConfig.resetLoggingSettings();
    }

    @AfterEach
    void cleanUpAfterTest() {
        // Reset logging settings after each test
        loggingConfig.resetLoggingSettings();
    }

    @Test
    void testDefaultSettings() {
        // By default, global logging should be enabled
        assertTrue(loggingConfig.isGlobalLoggingEnabled());

        // By default, logging should be enabled for all endpoints
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"));
    }

    @Test
    void testDisableGlobalLogging() {
        // Initially, global logging is enabled
        assertTrue(loggingConfig.isGlobalLoggingEnabled());
//        assertFalse(loggingConfig.isGlobalLoggingEnabled());

        // Disable global logging
        loggingConfig.setGlobalLoggingEnabled(false);
//        loggingConfig.setGlobalLoggingEnabled(true);

        // Now, logging should be disabled for all endpoints
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/logging"));
//        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
//        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
//        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"));

        // Reset for other tests
        loggingConfig.setGlobalLoggingEnabled(false);
    }

    @Test
    void testEndpointSpecificSettings() {
        // Initially, logging is enabled for all endpoints
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));

        // Disable logging for a specific endpoint
        loggingConfig.setEndpointLoggingEnabled("/api/events", false);

        // Now, logging should be disabled for that endpoint
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events"));

        // But still enabled for other endpoints
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"));

        // Enable logging for the endpoint again
        loggingConfig.setEndpointLoggingEnabled("/api/events", true);

        // Now, logging should be enabled for all endpoints again
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"));
    }

    @Test
    void testWildcardPatterns() {
        // Initially, logging is enabled for all endpoints
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/search"));

        // Disable logging for all events endpoints using a wildcard
        loggingConfig.setEndpointLoggingEnabled("/api/events/*", false);

        // Now, logging should be disabled for endpoints matching the pattern
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events")); // Not affected by wildcard
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events/search"));

        // But still enabled for other endpoints
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"));

        // Reset for other tests
        loggingConfig.resetLoggingSettings();
    }

    @Test
    void testResetSettings() {
        // Change some settings
        loggingConfig.setGlobalLoggingEnabled(false);
        loggingConfig.setEndpointLoggingEnabled("/api/events", false);
        loggingConfig.setEndpointLoggingEnabled("/api/events/*", false);

        // Reset all settings
        loggingConfig.resetLoggingSettings();

        // Now, global logging should be enabled again
        assertTrue(loggingConfig.isGlobalLoggingEnabled());

        // And all endpoint-specific settings should be cleared
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"));
    }
}
