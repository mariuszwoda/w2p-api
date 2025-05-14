package pl.where2play.api.test.e2e.controller;

import org.springframework.test.context.ActiveProfiles;

/**
 * Production environment version of the CalendarEventControllerApiTest.
 * This class extends CalendarEventControllerApiTest but uses the prod profile instead of dev.
 * It allows testing the API with the production configuration.
 */
@ActiveProfiles({"e2e", "prod"})
public class CalendarEventControllerProdApiTest extends CalendarEventControllerApiTest {
    // Inherits all test methods from CalendarEventControllerApiTest
    // but runs them with the prod profile
}