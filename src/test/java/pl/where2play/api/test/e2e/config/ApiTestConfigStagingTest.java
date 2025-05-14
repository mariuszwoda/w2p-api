package pl.where2play.api.test.e2e.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApiTestConfig with the staging profile.
 * This test class verifies that the ApiTestConfig can properly handle the staging environment.
 */
@SpringJUnitConfig(ApiTestConfig.class)
@ActiveProfiles("staging")
class ApiTestConfigStagingTest {

    @Autowired
    private ApiTestConfig apiTestConfig;

    @Test
    void testGetBaseUrl() {
        // This test verifies that getBaseUrl works correctly for the staging environment
        String baseUrl = apiTestConfig.getBaseUrl();

        // Verify that the base URL was loaded
        assertNotNull(baseUrl);
        assertFalse(baseUrl.isEmpty());

        // Verify that the base URL is the staging URL from test-environments.yml
        assertEquals("http://staging.example.com", baseUrl);

        // Log the base URL for debugging
        System.out.println("[DEBUG_LOG] Staging Base URL: " + baseUrl);
    }

    @Test
    void testGetEnvironment() {
        // This test verifies that getEnvironment correctly identifies the staging profile
        String env = apiTestConfig.getEnvironment();

        // Verify that the environment is "staging"
        assertEquals("staging", env);

        // Log the environment for debugging
        System.out.println("[DEBUG_LOG] Environment: " + env);
    }

    @Test
    void testGetConnectionTimeout() {
        // This test verifies that getConnectionTimeout returns the correct timeout for staging
        int timeout = apiTestConfig.getConnectionTimeout();

        // Verify that the timeout is 30 seconds (as defined in test-environments.yml)
        assertEquals(30, timeout);

        // Log the timeout for debugging
        System.out.println("[DEBUG_LOG] Staging connection timeout: " + timeout);
    }
}