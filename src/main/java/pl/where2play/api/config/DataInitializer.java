package pl.where2play.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import pl.where2play.api.model.CalendarEvent;
import pl.where2play.api.repository.CalendarEventRepository;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * This class is now disabled as we're using Liquibase for data initialization.
 * Kept for reference purposes.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final CalendarEventRepository calendarEventRepository;

    // Disabled as we're now using Liquibase for data initialization
    // @Bean
    // @Profile("dev") // Only run in dev profile
    public CommandLineRunner initDatabase() {
        return args -> {
            // Check if database is already populated
            if (calendarEventRepository.count() > 0) {
                return;
            }

            // Create 10 sample calendar events
            CalendarEvent event1 = new CalendarEvent();
            event1.setTitle("Team Meeting");
            event1.setDescription("Weekly team sync-up");
            event1.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            event1.setEndTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
            event1.setLocation("Conference Room A");
            event1.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event1.setCreatedBy("admin");

            CalendarEvent event2 = new CalendarEvent();
            event2.setTitle("Project Kickoff");
            event2.setDescription("New project kickoff meeting");
            event2.setStartTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
            event2.setEndTime(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0));
            event2.setLocation("Main Hall");
            event2.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event2.setCreatedBy("manager");

            CalendarEvent event3 = new CalendarEvent();
            event3.setTitle("Client Presentation");
            event3.setDescription("Presenting new features to the client");
            event3.setStartTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0));
            event3.setEndTime(LocalDateTime.now().plusDays(3).withHour(12).withMinute(30));
            event3.setLocation("Client Office");
            event3.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event3.setCreatedBy("sales");

            CalendarEvent event4 = new CalendarEvent();
            event4.setTitle("Training Session");
            event4.setDescription("New employee training");
            event4.setStartTime(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0));
            event4.setEndTime(LocalDateTime.now().plusDays(5).withHour(17).withMinute(0));
            event4.setLocation("Training Room");
            event4.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event4.setCreatedBy("hr");

            CalendarEvent event5 = new CalendarEvent();
            event5.setTitle("Code Review");
            event5.setDescription("Review pull requests");
            event5.setStartTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
            event5.setEndTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
            event5.setLocation("Online");
            event5.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event5.setCreatedBy("tech_lead");

            CalendarEvent event6 = new CalendarEvent();
            event6.setTitle("Product Demo");
            event6.setDescription("Demo of new product features");
            event6.setStartTime(LocalDateTime.now().plusDays(7).withHour(13).withMinute(0));
            event6.setEndTime(LocalDateTime.now().plusDays(7).withHour(14).withMinute(0));
            event6.setLocation("Demo Room");
            event6.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event6.setCreatedBy("product_manager");

            CalendarEvent event7 = new CalendarEvent();
            event7.setTitle("Retrospective");
            event7.setDescription("Sprint retrospective meeting");
            event7.setStartTime(LocalDateTime.now().plusDays(10).withHour(16).withMinute(0));
            event7.setEndTime(LocalDateTime.now().plusDays(10).withHour(17).withMinute(0));
            event7.setLocation("Conference Room B");
            event7.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event7.setCreatedBy("scrum_master");

            CalendarEvent event8 = new CalendarEvent();
            event8.setTitle("Lunch and Learn");
            event8.setDescription("Technical presentation during lunch");
            event8.setStartTime(LocalDateTime.now().plusDays(4).withHour(12).withMinute(0));
            event8.setEndTime(LocalDateTime.now().plusDays(4).withHour(13).withMinute(0));
            event8.setLocation("Cafeteria");
            event8.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event8.setCreatedBy("developer");

            CalendarEvent event9 = new CalendarEvent();
            event9.setTitle("Holiday Party");
            event9.setDescription("Annual company holiday celebration");
            event9.setStartTime(LocalDateTime.now().plusMonths(1).withHour(18).withMinute(0));
            event9.setEndTime(LocalDateTime.now().plusMonths(1).withHour(22).withMinute(0));
            event9.setLocation("Grand Hall");
            event9.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event9.setCreatedBy("hr");

            CalendarEvent event10 = new CalendarEvent();
            event10.setTitle("System Maintenance");
            event10.setDescription("Scheduled system downtime for maintenance");
            event10.setStartTime(LocalDateTime.now().plusDays(14).withHour(22).withMinute(0));
            event10.setEndTime(LocalDateTime.now().plusDays(15).withHour(2).withMinute(0));
            event10.setLocation("Server Room");
            event10.setStatus(CalendarEvent.EventStatus.SCHEDULED);
            event10.setCreatedBy("it_admin");

            // Save all events
            calendarEventRepository.saveAll(Arrays.asList(
                    event1, event2, event3, event4, event5, 
                    event6, event7, event8, event9, event10
            ));

            System.out.println("Database initialized with 10 calendar events");
        };
    }
}
