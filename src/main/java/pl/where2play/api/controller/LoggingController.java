package pl.where2play.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.where2play.api.config.LoggingConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing request/response logging settings at runtime.
 */
@RestController
@RequestMapping("/api/logging")
@RequiredArgsConstructor
public class LoggingController {

    private final LoggingConfig loggingConfig;

    /**
     * Get the current logging settings.
     *
     * @return a map containing the global logging status and endpoint-specific settings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLoggingSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("globalLoggingEnabled", loggingConfig.isGlobalLoggingEnabled());
        settings.put("endpointSettings", loggingConfig.getEndpointLoggingSettings());
        return ResponseEntity.ok(settings);
    }

    /**
     * Enable or disable global logging.
     *
     * @param enabled whether global logging should be enabled
     * @return a success message
     */
    @PutMapping("/global")
    public ResponseEntity<Map<String, Object>> setGlobalLogging(@RequestParam boolean enabled) {
        loggingConfig.setGlobalLoggingEnabled(enabled);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Global logging " + (enabled ? "enabled" : "disabled"));
        response.put("globalLoggingEnabled", loggingConfig.isGlobalLoggingEnabled());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Enable or disable logging for a specific endpoint.
     *
     * @param endpoint the endpoint pattern (e.g., "/api/events", "/api/events/*")
     * @param enabled whether logging should be enabled for this endpoint
     * @return a success message
     */
    @PutMapping("/endpoint")
    public ResponseEntity<Map<String, Object>> setEndpointLogging(
            @RequestParam String endpoint,
            @RequestParam boolean enabled) {
        
        loggingConfig.setEndpointLoggingEnabled(endpoint, enabled);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logging for endpoint '" + endpoint + "' " + (enabled ? "enabled" : "disabled"));
        response.put("endpoint", endpoint);
        response.put("enabled", enabled);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Reset all logging settings to their default values.
     *
     * @return a success message
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetLoggingSettings() {
        loggingConfig.resetLoggingSettings();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logging settings reset to defaults");
        response.put("globalLoggingEnabled", loggingConfig.isGlobalLoggingEnabled());
        response.put("endpointSettings", loggingConfig.getEndpointLoggingSettings());
        
        return ResponseEntity.ok(response);
    }
}