# E2E Test Configuration Guide

This guide explains how to configure and run E2E tests with different environments.

## Overview

The E2E test framework is designed to be flexible and support multiple environments (dev, prod, staging, qa, etc.). It uses Spring profiles to determine which environment configuration to use.

## How It Works

1. The `ApiTestConfig` class loads configuration from:
   - `test-environments.yml` - Contains environment-specific settings like baseUrl and timeout
   - `application-{profile}.yml` - Contains Spring application settings for the active profile
   - `application-{profile}.properties` - Additional properties for the active profile

2. When running tests, you can specify which environment to use by setting the active profiles:
   - `@ActiveProfiles({"e2e", "dev"})` - Run tests with dev environment
   - `@ActiveProfiles({"e2e", "prod"})` - Run tests with prod environment
   - `@ActiveProfiles({"e2e", "staging"})` - Run tests with staging environment
   - `@ActiveProfiles({"e2e", "qa"})` - Run tests with qa environment

## Adding a New Environment

To add a new environment (e.g., "uat"):

1. Add the environment configuration to `test-environments.yml`:
   ```yaml
   uat:
     baseUrl: http://uat.example.com
     configFile: application-uat.yml
     timeout: 20
   ```

2. Create application configuration files for the new environment:
   - `application-uat.yml` or `application-uat.properties`

3. Create a test class that uses the new environment:
   ```java
   @ActiveProfiles({"e2e", "uat"})
   public class CalendarEventControllerUatApiTest extends CalendarEventControllerApiTest {
       // Inherits all test methods from CalendarEventControllerApiTest
       // but runs them with the uat profile
   }
   ```

## Running Tests with Different Environments

You can run tests with different environments in several ways:

1. Using test classes with specific profiles:
   - `CalendarEventControllerApiTest` - Uses dev environment by default
   - `CalendarEventControllerProdApiTest` - Uses prod environment
   - `CalendarEventControllerStagingApiTest` - Uses staging environment
   - `CalendarEventControllerQaApiTest` - Uses qa environment

2. Using Maven command line:
   ```
   mvn test -Dspring.profiles.active=e2e,staging
   ```

3. Using JUnit run configuration in your IDE:
   - Add `-Dspring.profiles.active=e2e,qa` to the VM options

## Environment Properties

Each environment in `test-environments.yml` can have the following properties:

- `baseUrl` - The base URL for API requests (e.g., http://localhost:8080)
- `configFile` - The configuration file to use (e.g., application-dev.yml)
- `timeout` - Database connection timeout in seconds

## Testing Environment Configuration

You can verify that your environment configuration is working correctly by running the environment-specific test classes:

- `ApiTestConfigTest` - Tests the dev environment configuration
- `ApiTestConfigProdTest` - Tests the prod environment configuration
- `ApiTestConfigStagingTest` - Tests the staging environment configuration
- `ApiTestConfigQaTest` - Tests the qa environment configuration