package pl.where2play.api.test.e2e.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApiTestConfig with the prod profile.
 */
@SpringJUnitConfig(ApiTestConfig.class)
@ActiveProfiles("prod")
class ApiTestConfigProdTest {

    @Autowired
    private ApiTestConfig apiTestConfig;

    @Test
    void testGetDatabaseConfig() {
        // This test verifies that getDatabaseConfig can load configuration from both YAML and properties files
        Map<String, String> dbConfig = apiTestConfig.getDatabaseConfig();

        // Verify that the database configuration was loaded
        assertNotNull(dbConfig);
        assertFalse(dbConfig.isEmpty());

        // Log the configuration for debugging
        System.out.println("[DEBUG_LOG] Database configuration: " + dbConfig);
    }

    @Test
    void testGetBaseUrl() {
        // This test verifies that getBaseUrl works correctly
        String baseUrl = apiTestConfig.getBaseUrl();

        // Verify that the base URL was loaded
        assertNotNull(baseUrl);
        assertFalse(baseUrl.isEmpty());

        // Verify that the base URL is the prod URL
        assertEquals("http://azure.com", baseUrl);

        // Log the base URL for debugging
        System.out.println("[DEBUG_LOG] Base URL: " + baseUrl);
    }
}