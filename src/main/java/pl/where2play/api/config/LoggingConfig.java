package pl.where2play.api.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration class for managing request/response logging settings.
 * Allows enabling/disabling logging for specific API endpoints on the fly.
 */
@Component
public class LoggingConfig {

    // Global flag to enable/disable all logging
    @Getter
    private volatile boolean globalLoggingEnabled = true;

    // Map to store endpoint-specific logging settings
    // Key: endpoint pattern (e.g., "/api/events", "/api/events/*")
    // Value: whether logging is enabled for this endpoint
    private final Map<String, Boolean> endpointLoggingSettings = new ConcurrentHashMap<>();

    // Read from environment variable REQUEST_RESPONSE_LOGGING_GLOBAL_ENABLED with fallback to application.yml
    @Value("${REQUEST_RESPONSE_LOGGING_GLOBAL_ENABLED:${request-response-logging.global-enabled:true}}")
    private boolean globalEnabled;

    /**
     * Initialize settings from environment variables or application.yml
     * Environment variables take precedence over application.yml properties.
     * For Azure Web App, use the environment variable REQUEST_RESPONSE_LOGGING_GLOBAL_ENABLED.
     */
    @PostConstruct
    public void init() {
        // Set global logging enabled from properties
        this.globalLoggingEnabled = this.globalEnabled;

        // Endpoint settings will be configured programmatically
    }

    /**
     * Checks if logging is enabled for the given URI.
     *
     * @param uri the request URI to check
     * @return true if logging is enabled for this URI, false otherwise
     */
    public boolean isLoggingEnabledForUri(String uri) {
        // If global logging is disabled, return false
        if (!globalLoggingEnabled) {
            return false;
        }

        // Check for exact endpoint match
        if (endpointLoggingSettings.containsKey(uri)) {
            return endpointLoggingSettings.get(uri);
        }

        // Check for pattern matches (e.g., "/api/events/*")
        for (Map.Entry<String, Boolean> entry : endpointLoggingSettings.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                if (uri.startsWith(prefix)) {
                    return entry.getValue();
                }
            }
        }

        // Default to true if no specific setting is found
        return true;
    }

    /**
     * Enables or disables logging for a specific endpoint.
     *
     * @param endpoint the endpoint pattern (e.g., "/api/events", "/api/events/*")
     * @param enabled whether logging should be enabled for this endpoint
     */
    public void setEndpointLoggingEnabled(String endpoint, boolean enabled) {
        endpointLoggingSettings.put(endpoint, enabled);
    }

    /**
     * Enables or disables global logging.
     *
     * @param enabled whether global logging should be enabled
     */
    public void setGlobalLoggingEnabled(boolean enabled) {
        this.globalLoggingEnabled = enabled;
    }

    /**
     * Resets all logging settings to their default values.
     * Global logging is enabled, and all endpoint-specific settings are removed.
     */
    public void resetLoggingSettings() {
        globalLoggingEnabled = true;
        endpointLoggingSettings.clear();
    }

    /**
     * Gets the current endpoint-specific logging settings.
     *
     * @return a map of endpoint patterns to their logging enabled status
     */
    public Map<String, Boolean> getEndpointLoggingSettings() {
        return new ConcurrentHashMap<>(endpointLoggingSettings);
    }
}
