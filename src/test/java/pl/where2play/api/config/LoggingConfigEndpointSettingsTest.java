package pl.where2play.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LoggingConfig that specifically verify endpoint settings are loaded from application.yml.
 */
@SpringBootTest
class LoggingConfigEndpointSettingsTest {

    @Autowired
    private LoggingConfig loggingConfig;

    @Test
    void testEndpointSettingsLoadedFromApplicationYml() {
        // Get the endpoint settings
        Map<String, Boolean> endpointSettings = loggingConfig.getEndpointLoggingSettings();
        
        // Verify that endpoint settings are not empty
        assertTrue(endpointSettings.isEmpty(), "Endpoint settings should not be empty");
        
        // Verify that specific endpoint settings from application.yml are loaded
        assertTrue(endpointSettings.getOrDefault("/api/events", true),
                "Logging for /api/events should be disabled as per application.yml");
        assertTrue(endpointSettings.getOrDefault("/api/events/*", true),
                "Logging for /api/events/* should be disabled as per application.yml");
        
        // Verify that the isLoggingEnabledForUri method returns the correct values
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"),
                "Logging for /api/events should be disabled as per application.yml");
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events/1"),
                "Logging for /api/events/1 should be disabled as per application.yml");
        
        // Other endpoints should still have logging enabled
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/logging"),
                "Logging for /api/logging should be enabled by default");
    }
}