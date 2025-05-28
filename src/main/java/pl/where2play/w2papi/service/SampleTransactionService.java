package pl.where2play.w2papi.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.impl.GoogleCalendarServiceImpl;

@AllArgsConstructor
@Slf4j
@Service
public class SampleTransactionService {
    private UserRepository userRepository;
    private GoogleCalendarServiceImpl googleCalendarService;

    public User performTransaction() {
        User user = User.builder().email("mail@test.com").build();
        saveUserData(user);
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
