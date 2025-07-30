package pl.where2play.w2papi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.CalendarShare;
import pl.where2play.w2papi.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CalendarShare entity.
 */
@Repository
public interface CalendarShareRepository extends JpaRepository<CalendarShare, Long> {

    /**
     * Find all shares for a specific event.
     *
     * @param event the event
     * @return the list of shares
     */
    List<CalendarShare> findByEvent(CalendarEvent event);

    /**
     * Find all shares for a specific user.
     *
     * @param user the user
     * @return the list of shares
     */
    List<CalendarShare> findByUser(User user);

    /**
     * Find all shares by email (for users not registered in the system).
     *
     * @param email the email
     * @return the list of shares
     */
    List<CalendarShare> findBySharedEmail(String email);

    /**
     * Find a share by its token.
     *
     * @param shareToken the share token
     * @return the share, if found
     */
    Optional<CalendarShare> findByShareToken(String shareToken);

    /**
     * Find a share for a specific event and user.
     *
     * @param event the event
     * @param user the user
     * @return the share, if found
     */
    Optional<CalendarShare> findByEventAndUser(CalendarEvent event, User user);

    /**
     * Find a share for a specific event and email.
     *
     * @param event the event
     * @param email the email
     * @return the share, if found
     */
    Optional<CalendarShare> findByEventAndSharedEmail(CalendarEvent event, String email);

    /**
     * Find all events shared with a specific user.
     *
     * @param user the user
     * @return the list of events
     */
    @Query("SELECT cs.event FROM CalendarShare cs WHERE cs.user = :user")
    List<CalendarEvent> findAllEventsSharedWithUser(@Param("user") User user);

    /**
     * Find all events shared with a specific email.
     *
     * @param email the email
     * @return the list of events
     */
    @Query("SELECT cs.event FROM CalendarShare cs WHERE cs.sharedEmail = :email")
    List<CalendarEvent> findAllEventsSharedWithEmail(@Param("email") String email);
}