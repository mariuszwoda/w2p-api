package pl.where2play.api.test.e2e.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApiTestConfig with the qa profile.
 * This test class verifies that the ApiTestConfig can properly handle the qa environment.
 */
@SpringJUnitConfig(ApiTestConfig.class)
@ActiveProfiles("qa")
class ApiTestConfigQaTest {

    @Autowired
    private ApiTestConfig apiTestConfig;

    @Test
    void testGetBaseUrl() {
        // This test verifies that getBaseUrl works correctly for the qa environment
        String baseUrl = apiTestConfig.getBaseUrl();

        // Verify that the base URL was loaded
        assertNotNull(baseUrl);
        assertFalse(baseUrl.isEmpty());

        // Verify that the base URL is the qa URL from test-environments.yml
        assertEquals("http://qa.example.com", baseUrl);

        // Log the base URL for debugging
        System.out.println("[DEBUG_LOG] QA Base URL: " + baseUrl);
    }

    @Test
    void testGetEnvironment() {
        // This test verifies that getEnvironment correctly identifies the qa profile
        String env = apiTestConfig.getEnvironment();

        // Verify that the environment is "qa"
        assertEquals("qa", env);

        // Log the environment for debugging
        System.out.println("[DEBUG_LOG] Environment: " + env);
    }

    @Test
    void testGetConnectionTimeout() {
        // This test verifies that getConnectionTimeout returns the correct timeout for qa
        int timeout = apiTestConfig.getConnectionTimeout();

        // Verify that the timeout is 25 seconds (as defined in test-environments.yml)
        assertEquals(25, timeout);

        // Log the timeout for debugging
        System.out.println("[DEBUG_LOG] QA connection timeout: " + timeout);
    }
}