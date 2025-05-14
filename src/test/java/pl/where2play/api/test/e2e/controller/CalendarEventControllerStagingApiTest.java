package pl.where2play.api.test.e2e.controller;

import org.springframework.test.context.ActiveProfiles;

/**
 * Staging environment version of the CalendarEventControllerApiTest.
 * This class extends CalendarEventControllerApiTest but uses the staging profile instead of dev.
 * It allows testing the API with the staging configuration.
 *
 * This demonstrates how to run API tests with different environments as required by the issue description.
 * The same pattern can be used to create test classes for other environments (qa, etc.).
 */
@ActiveProfiles({"e2e", "staging"})
public class CalendarEventControllerStagingApiTest extends CalendarEventControllerApiTest {
    // Inherits all test methods from CalendarEventControllerApiTest
    // but runs them with the staging profile
}