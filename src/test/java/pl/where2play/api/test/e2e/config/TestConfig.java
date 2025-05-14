package pl.where2play.api.test.e2e.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@PropertySource("classpath:test-config.properties")
@Getter

@EnableJpaRepositories(basePackages = "pl.where2play.api.repository")
@EntityScan(basePackages = "pl.where2play.api.model")
public class TestConfig {
    
    @Value("${test.baseUrl.dev}")
    private String devBaseUrl;
    
    @Value("${test.baseUrl.prod}")
    private String prodBaseUrl;
    
    public String getBaseUrlForProfile(String profile) {
        return switch (profile.toLowerCase()) {
            case "dev" -> devBaseUrl;
            case "prod" -> prodBaseUrl;
            default -> throw new IllegalArgumentException("Unknown profile: " + profile);
        };
    }
}