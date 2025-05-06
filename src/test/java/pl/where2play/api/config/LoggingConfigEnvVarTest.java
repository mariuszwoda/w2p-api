package pl.where2play.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LoggingConfig that specifically verify environment variable functionality.
 * This test class uses @TestPropertySource to simulate environment variables.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "REQUEST_RESPONSE_LOGGING_GLOBAL_ENABLED=false"
})
class LoggingConfigEnvVarTest {

    @Autowired
    private LoggingConfig loggingConfig;

    @Test
    void testEnvironmentVariableOverridesApplicationYml() {
        // The environment variable REQUEST_RESPONSE_LOGGING_GLOBAL_ENABLED is set to false
        // This should override the application.yml setting which is true
        assertFalse(loggingConfig.isGlobalLoggingEnabled());
        
        // Since global logging is disabled, all endpoints should have logging disabled
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events"));
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events/1"));
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/logging"));
    }
}