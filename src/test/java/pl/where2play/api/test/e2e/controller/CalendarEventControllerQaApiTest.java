package pl.where2play.api.test.e2e.controller;

import org.springframework.test.context.ActiveProfiles;

/**
 * QA environment version of the CalendarEventControllerApiTest.
 * This class extends CalendarEventControllerApiTest but uses the qa profile instead of dev.
 * It allows testing the API with the QA configuration.
 *
 * This demonstrates how to run API tests with different environments as required by the issue description.
 * The same pattern can be used to create test classes for other environments.
 */
@ActiveProfiles({"e2e", "qa"})
public class CalendarEventControllerQaApiTest extends CalendarEventControllerApiTest {
    // Inherits all test methods from CalendarEventControllerApiTest
    // but runs them with the qa profile
}