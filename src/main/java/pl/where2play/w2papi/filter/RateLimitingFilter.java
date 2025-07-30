package pl.where2play.w2papi.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.where2play.w2papi.config.RateLimitingConfig;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Filter for rate limiting API requests.
 * This filter applies rate limiting based on the user's role or IP address.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip rate limiting for certain paths
        String path = request.getRequestURI();
        if (shouldSkipRateLimiting(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get the key for rate limiting (user role or IP address)
        String key = getKeyForRateLimiting(request);
        
        // Get the bucket for this key
        Bucket bucket = rateLimitingConfig.resolveBucket(key);
        
        // Try to consume a token from the bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Request is allowed, add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            
            // Continue with the request
            filterChain.doFilter(request, response);
        } else {
            // Request is denied due to rate limiting
            log.warn("Rate limit exceeded for key: {}", key);
            
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            
            // Return 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }
    }

    /**
     * Determine if rate limiting should be skipped for this path.
     *
     * @param path the request path
     * @return true if rate limiting should be skipped, false otherwise
     */
    private boolean shouldSkipRateLimiting(String path) {
        // Skip rate limiting for authentication endpoints
        if (path.startsWith("/api/auth")) {
            return true;
        }
        
        // Skip rate limiting for Swagger/OpenAPI documentation
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return true;
        }
        
        // Skip rate limiting for actuator endpoints
        if (path.startsWith("/actuator")) {
            return true;
        }
        
        return false;
    }

    /**
     * Get the key for rate limiting.
     * This can be the user's role or IP address.
     *
     * @param request the HTTP request
     * @return the key for rate limiting
     */
    private String getKeyForRateLimiting(HttpServletRequest request) {
        // Try to get the authenticated user's roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getAuthorities().isEmpty()) {
            // Use the user's roles as the key
            return authentication.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        
        // Fall back to IP address if no authenticated user
        return getClientIP(request);
    }

    /**
     * Get the client's IP address.
     *
     * @param request the HTTP request
     * @return the client's IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the X-Forwarded-For header (client's original IP)
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Fall back to the remote address
        return request.getRemoteAddr();
    }
}