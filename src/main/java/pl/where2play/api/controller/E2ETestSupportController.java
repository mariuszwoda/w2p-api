package pl.where2play.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.where2play.api.config.E2ETestSupport;
import pl.where2play.api.service.CalendarEventService;

/**
 * Controller that provides endpoints for E2E testing support.
 * These endpoints are only available in local, dev, and sit environments.
 */
@RestController
@RequestMapping("/api/e2e-support")
public class E2ETestSupportController {

    private final CalendarEventService calendarEventService;

    public E2ETestSupportController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    /**
     * Deletes an entity by ID for testing purposes.
     * This endpoint is only available in non-production environments.
     */
    @E2ETestSupport
    @DeleteMapping("/calendar/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        calendarEventService.deleteEventForTesting(id);
        return ResponseEntity.noContent().build();
    }
}