/*
You likely **don't need this class** in your project if:
- Your application properties files already contain the necessary database configuration
- You're relying on Spring Boot's auto-configuration
- You don't have any custom database initialization or configuration logic that's not covered by properties

If you want to remove this class, ensure your property files contain all necessary database configuration parameters like:
 */

//package pl.where2play.w2papi.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.env.Environment;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//import javax.sql.DataSource;
//
///**
// * Database configuration for different environments.
// * Uses H2 for local development and PostgreSQL for production.
// */
//@Configuration
//@Slf4j
//public class DatabaseConfig {
//
//    private final Environment env;
//
//    public DatabaseConfig(Environment env) {
//        this.env = env;
//    }
//
//    /**
//     * DataSource configuration for development environment (H2).
//     *
//     * @return H2 DataSource
//     */
//    @Bean
//    @Profile("dev")
//    public DataSource h2DataSource() {
//        log.info("Setting up H2 datasource for DEV environment");
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
//        dataSource.setUrl(env.getProperty("spring.datasource.url"));
//        dataSource.setUsername(env.getProperty("spring.datasource.username"));
//        dataSource.setPassword(env.getProperty("spring.datasource.password"));
//        return dataSource;
//    }
//
//    /**
//     * DataSource configuration for production environment (PostgreSQL).
//     *
//     * @return PostgreSQL DataSource
//     */
//    @Bean
//    @Profile("prod")
//    public DataSource postgresqlDataSource() {
//        log.info("Setting up PostgreSQL datasource for PROD environment");
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
//        dataSource.setUrl(env.getProperty("spring.datasource.url"));
//        dataSource.setUsername(env.getProperty("spring.datasource.username"));
//        dataSource.setPassword(env.getProperty("spring.datasource.password"));
//        return dataSource;
//    }
//}