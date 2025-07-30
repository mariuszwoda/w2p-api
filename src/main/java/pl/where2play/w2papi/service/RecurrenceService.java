package pl.where2play.w2papi.service;

import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.RecurrencePattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for handling recurring events.
 */
public interface RecurrenceService {

    /**
     * Generate occurrences of a recurring event within a date range.
     *
     * @param event the recurring event
     * @param start the start date
     * @param end the end date
     * @return the list of occurrence dates
     */
    List<LocalDateTime> generateOccurrences(CalendarEvent event, LocalDateTime start, LocalDateTime end);

    /**
     * Check if a date is part of a recurrence pattern.
     *
     * @param pattern the recurrence pattern
     * @param date the date to check
     * @return true if the date is part of the recurrence pattern, false otherwise
     */
    boolean isDateInPattern(RecurrencePattern pattern, LocalDate date);

    /**
     * Create a recurrence pattern from a recurrence rule string.
     * This is used for backward compatibility with the legacy recurrenceRule field.
     *
     * @param recurrenceRule the recurrence rule string
     * @param event the event
     * @return the recurrence pattern
     */
    RecurrencePattern createPatternFromRule(String recurrenceRule, CalendarEvent event);

    /**
     * Convert a recurrence pattern to a recurrence rule string.
     * This is used for backward compatibility with external calendar providers.
     *
     * @param pattern the recurrence pattern
     * @return the recurrence rule string
     */
    String convertPatternToRule(RecurrencePattern pattern);

    /**
     * Get the next occurrence of a recurring event after a specified date.
     *
     * @param event the recurring event
     * @param after the date after which to find the next occurrence
     * @return the next occurrence date, or null if there are no more occurrences
     */
    LocalDateTime getNextOccurrence(CalendarEvent event, LocalDateTime after);

    /**
     * Get the previous occurrence of a recurring event before a specified date.
     *
     * @param event the recurring event
     * @param before the date before which to find the previous occurrence
     * @return the previous occurrence date, or null if there are no previous occurrences
     */
    LocalDateTime getPreviousOccurrence(CalendarEvent event, LocalDateTime before);

    /**
     * Add an exception date to a recurrence pattern.
     * This is used to exclude specific dates from the recurrence pattern.
     *
     * @param pattern the recurrence pattern
     * @param date the date to exclude
     * @return the updated recurrence pattern
     */
    RecurrencePattern addExceptionDate(RecurrencePattern pattern, LocalDate date);

    /**
     * Remove an exception date from a recurrence pattern.
     *
     * @param pattern the recurrence pattern
     * @param date the date to remove from exceptions
     * @return the updated recurrence pattern
     */
    RecurrencePattern removeExceptionDate(RecurrencePattern pattern, LocalDate date);
}