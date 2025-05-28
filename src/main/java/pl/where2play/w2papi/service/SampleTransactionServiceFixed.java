package pl.where2play.w2papi.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.impl.GoogleCalendarServiceImpl;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleTransactionServiceFixed {
    private final UserRepository userRepository;
    private final GoogleCalendarServiceImpl googleCalendarService;
    private SampleTransactionServiceFixed self;

    /**
     * Sets a reference to self using Spring's @Lazy annotation.
     * This pattern is used to ensure that transactional behavior works correctly
     * for internal method calls. When methods are called directly (this.method()),
     * Spring's transaction proxies don't apply. Using self.method() instead
     * ensures that the method is called through Spring's proxy, activating
     * transactional behavior defined by @Transactional annotations.
     *
     * This implementation was updated to address Sonar findings related to
     * transaction management. Sonar detects when transactional methods are called
     * directly within the same class, which bypasses the Spring proxy and
     * results in transactions not being applied correctly.
     *
     * @param self Lazy-initialized proxy to this service
     */
    @Autowired
    public void setSelf(@Lazy SampleTransactionServiceFixed self) {
        this.self = self;
    }

    public User performTransaction() {
        User user = User.builder().email("mail@test.com").build();
        self.saveUserData(user);
        return user;
    }

    @Transactional
    public User saveUserData(User user) {
        userRepository.save(user);
        saveCalendarEvent(user);
        return user;
    }

    public void saveCalendarEvent(User user) {
        CalendarEvent calendarEvent = new CalendarEvent();
        googleCalendarService.createEvent(calendarEvent, user);
    }
}
