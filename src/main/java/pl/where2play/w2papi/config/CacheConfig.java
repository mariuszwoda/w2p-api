package pl.where2play.w2papi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.time.Duration;

/**
 * Configuration for caching.
 * This class enables caching in the application and configures the cache manager.
 * For development, it uses an in-memory cache.
 * For production, it uses Redis as a distributed cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.cache.redis.time-to-live:3600}")
    private long timeToLive;

    /**
     * Redis connection factory bean.
     * This bean is used to connect to Redis.
     *
     * @return the Redis connection factory
     */
    @Bean
    @Profile("!dev")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        if (!redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * Redis cache manager bean for production.
     * This bean is used to manage caches in the application using Redis.
     *
     * @param redisConnectionFactory the Redis connection factory
     * @return the Redis cache manager
     */
    @Bean
    @Profile("!dev")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeToLive))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("events", cacheConfig)
                .withCacheConfiguration("eventsInDateRange", cacheConfig)
                .withCacheConfiguration("event", cacheConfig)
                .withCacheConfiguration("userEvents", cacheConfig)
                .build();
    }

    /**
     * In-memory cache manager bean for development.
     * This bean is used to manage caches in the application using an in-memory cache.
     *
     * @return the in-memory cache manager
     */
    @Bean
    @Profile("dev")
    public CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager(
                "events",
                "eventsInDateRange",
                "event",
                "userEvents"
        );
    }
}
