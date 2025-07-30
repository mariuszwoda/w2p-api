package pl.where2play.w2papi.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for rate limiting.
 * This class provides rate limiting capabilities to protect the API from abuse.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Cache of rate limit buckets per API key or IP address.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Create a bucket for rate limiting with the specified capacity and refill rate.
     *
     * @param key the key to identify the client (API key or IP address)
     * @return the rate limit bucket
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createNewBucket);
    }

    /**
     * Create a new bucket with the specified capacity and refill rate.
     * Default is 20 requests per minute.
     *
     * @param key the key to identify the client (API key or IP address)
     * @return the new bucket
     */
    private Bucket createNewBucket(String key) {
        // For admin users, allow more requests
        if (key.startsWith("ROLE_ADMIN")) {
            return createBucketWithCapacity(100, 1);
        }
        
        // For E2E test users, allow more requests
        if (key.startsWith("ROLE_E2E_TEST")) {
            return createBucketWithCapacity(200, 1);
        }
        
        // For regular users
        return createBucketWithCapacity(20, 1);
    }

    /**
     * Create a bucket with the specified capacity and refill rate.
     *
     * @param capacity the capacity of the bucket
     * @param minutes the time in minutes to refill the bucket
     * @return the bucket
     */
    private Bucket createBucketWithCapacity(int capacity, int minutes) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(minutes)));
        return Bucket4j.builder().addLimit(limit).build();
    }
}