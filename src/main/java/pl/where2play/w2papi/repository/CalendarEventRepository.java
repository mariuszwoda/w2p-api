package pl.where2play.w2papi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CalendarEvent entity.
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    /**
     * Find all non-deleted events owned by a user.
     *
     * @param owner the owner
     * @return the list of events
     */
    List<CalendarEvent> findByOwnerAndDeletedFalse(User owner);

    /**
     * Find all non-deleted events where a user is an attendee.
     *
     * @param attendee the attendee
     * @return the list of events
     */
    @Query("SELECT e FROM CalendarEvent e JOIN e.attendees a WHERE a = :attendee AND e.deleted = false")
    List<CalendarEvent> findByAttendeeAndDeletedFalse(@Param("attendee") User attendee);

    /**
     * Find all non-deleted events for a user (owned or attending).
     *
     * @param user the user
     * @return the list of events
     */
    @Query("SELECT e FROM CalendarEvent e WHERE (e.owner = :user OR :user MEMBER OF e.attendees) AND e.deleted = false")
    List<CalendarEvent> findAllEventsForUser(@Param("user") User user);

    /**
     * Find all non-deleted events for a user in a date range.
     *
     * @param user the user
     * @param start the start date
     * @param end the end date
     * @return the list of events
     */
    @Query("SELECT e FROM CalendarEvent e WHERE (e.owner = :user OR :user MEMBER OF e.attendees) AND e.startTime >= :start AND e.endTime <= :end AND e.deleted = false")
    List<CalendarEvent> findAllEventsForUserInDateRange(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find a non-deleted event by external ID and provider.
     *
     * @param externalId the external ID
     * @param provider the provider
     * @return the event
     */
    Optional<CalendarEvent> findByExternalIdAndProviderAndDeletedFalse(String externalId, CalendarEvent.CalendarProvider provider);

    /**
     * Find a non-deleted event by ID.
     *
     * @param id the event ID
     * @return the event
     */
    Optional<CalendarEvent> findByIdAndDeletedFalse(Long id);

    /**
     * Find all events owned by a user (including deleted ones).
     *
     * @param owner the owner
     * @return the list of events
     */
    List<CalendarEvent> findByOwner(User owner);
}
