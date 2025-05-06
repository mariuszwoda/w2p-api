package pl.where2play.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Filter to log request and response bodies for all API calls.
 * This is particularly useful for debugging purposes.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private final LoggingConfig loggingConfig;

    private static final List<String> VISIBLE_TYPES = Arrays.asList(
            "application/json",
            "application/xml",
            "text/plain",
            "text/html"
    );

    // Constants for request attributes
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String REQUEST_API_INFO_ATTRIBUTE = "apiInfo";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip logging for non-API requests
        if (!request.getRequestURI().startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate a unique request ID
        String requestId = UUID.randomUUID().toString();

        // Store API endpoint information
        String apiInfo = request.getMethod() + " " + request.getRequestURI();

        // Store in MDC for logging
        MDC.put(REQUEST_ID_ATTRIBUTE, requestId);
        MDC.put(REQUEST_API_INFO_ATTRIBUTE, apiInfo);

        // Store as request attributes
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(REQUEST_API_INFO_ATTRIBUTE, apiInfo);

        // Add request ID to response header
        response.setHeader("X-Request-ID", requestId);

        // Wrap request and response to cache their content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Proceed with the filter chain
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Check if logging is enabled for this URI
            String uri = wrappedRequest.getRequestURI();
            boolean loggingEnabled = loggingConfig.isLoggingEnabledForUri(uri);

            if (loggingEnabled) {
                // Log request and response after the request has been processed
                logRequest(wrappedRequest);
                logResponse(wrappedResponse);
            }

            // Copy content to the original response
            wrappedResponse.copyBodyToResponse();

            // Clear MDC
            MDC.remove(REQUEST_ID_ATTRIBUTE);
            MDC.remove(REQUEST_API_INFO_ATTRIBUTE);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
        if (requestBody.isEmpty()) {
            return;
        }

        String contentType = request.getContentType();
        if (shouldLog(contentType)) {
            String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
            String apiInfo = (String) request.getAttribute(REQUEST_API_INFO_ATTRIBUTE);

            log.info("REQUEST: [ID: {}] [API: {}] {} {} [Body: {}]",
                    requestId,
                    apiInfo,
                    request.getMethod(),
                    request.getRequestURI(),
                    requestBody);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        String responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
        if (responseBody.isEmpty()) {
            return;
        }

        String contentType = response.getContentType();
        if (shouldLog(contentType)) {
            // Get request ID and API info from MDC (they should still be available)
            String requestId = MDC.get(REQUEST_ID_ATTRIBUTE);
            String apiInfo = MDC.get(REQUEST_API_INFO_ATTRIBUTE);

            log.info("RESPONSE: [ID: {}] [API: {}] Status {} [Body: {}]",
                    requestId,
                    apiInfo,
                    response.getStatus(),
                    responseBody);
        }
    }

    private String getContentAsString(byte[] content, String encoding) {
        if (content.length == 0) {
            return "";
        }

        try {
            return new String(content, encoding != null ? encoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to parse request/response content", e);
            return "Error parsing content";
        }
    }

    private boolean shouldLog(String contentType) {
        if (contentType == null) {
            return false;
        }

        return VISIBLE_TYPES.stream()
                .anyMatch(visibleType -> contentType.toLowerCase().contains(visibleType));
    }
}
