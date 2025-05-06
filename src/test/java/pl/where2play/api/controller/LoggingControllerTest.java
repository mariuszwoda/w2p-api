package pl.where2play.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.where2play.api.config.LoggingConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoggingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoggingConfig loggingConfig;

    @Test
    void testGetLoggingSettings() throws Exception {
        // Reset settings to defaults
        loggingConfig.resetLoggingSettings();

        // Test getting the logging settings
        mockMvc.perform(get("/api/logging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.globalLoggingEnabled").value(true))
                .andExpect(jsonPath("$.endpointSettings").isMap());
    }

    @Test
    void testSetGlobalLogging() throws Exception {
        // Reset settings to defaults
        loggingConfig.resetLoggingSettings();

        // Test disabling global logging
        mockMvc.perform(put("/api/logging/global")
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.globalLoggingEnabled").value(false));

        // Verify that global logging is disabled
        assertFalse(loggingConfig.isGlobalLoggingEnabled());

        // Test enabling global logging
        mockMvc.perform(put("/api/logging/global")
                .param("enabled", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.globalLoggingEnabled").value(true));

        // Verify that global logging is enabled
        assertTrue(loggingConfig.isGlobalLoggingEnabled());
    }

    @Test
    void testSetEndpointLogging() throws Exception {
        // Reset settings to defaults
        loggingConfig.resetLoggingSettings();

        // Test disabling logging for a specific endpoint
        mockMvc.perform(put("/api/logging/endpoint")
                .param("endpoint", "/api/events")
                .param("enabled", "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.endpoint").value("/api/events"))
                .andExpect(jsonPath("$.enabled").value(false));

        // Verify that logging is disabled for the endpoint
        assertFalse(loggingConfig.isLoggingEnabledForUri("/api/events"));

        // Test enabling logging for the endpoint
        mockMvc.perform(put("/api/logging/endpoint")
                .param("endpoint", "/api/events")
                .param("enabled", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.endpoint").value("/api/events"))
                .andExpect(jsonPath("$.enabled").value(true));

        // Verify that logging is enabled for the endpoint
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
    }

    @Test
    void testResetLoggingSettings() throws Exception {
        // Change some settings
        loggingConfig.setGlobalLoggingEnabled(false);
        loggingConfig.setEndpointLoggingEnabled("/api/events", false);

        // Test resetting the logging settings
        mockMvc.perform(post("/api/logging/reset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.globalLoggingEnabled").value(true));

        // Verify that settings are reset
        assertTrue(loggingConfig.isGlobalLoggingEnabled());
        assertTrue(loggingConfig.isLoggingEnabledForUri("/api/events"));
    }
}