package pl.where2play.w2papi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.CalendarEvent;
import pl.where2play.w2papi.model.RecurrencePattern;
import pl.where2play.w2papi.service.RecurrenceService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the RecurrenceService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecurrenceServiceImpl implements RecurrenceService {

    private static final Pattern RRULE_PATTERN = Pattern.compile("FREQ=(DAILY|WEEKLY|MONTHLY|YEARLY);(?:INTERVAL=(\\d+);)?(?:BYDAY=([^;]+);)?(?:BYMONTHDAY=([^;]+);)?(?:BYMONTH=([^;]+);)?(?:UNTIL=(\\d{8}T\\d{6}Z);)?(?:COUNT=(\\d+);)?");

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> generateOccurrences(CalendarEvent event, LocalDateTime start, LocalDateTime end) {
        log.info("Generating occurrences for event with ID: {} from {} to {}", event.getId(), start, end);
        
        if (event.getRecurrencePattern() == null) {
            // If the event has no recurrence pattern, check if it has a legacy recurrence rule
            if (event.getRecurrenceRule() != null && !event.getRecurrenceRule().isEmpty()) {
                // Convert the legacy recurrence rule to a recurrence pattern
                RecurrencePattern pattern = createPatternFromRule(event.getRecurrenceRule(), event);
                return generateOccurrencesFromPattern(event, pattern, start, end);
            }
            
            // If the event is not recurring, return a list with just the event's start time if it's within the range
            if (event.getStartTime().isAfter(start) && event.getStartTime().isBefore(end)) {
                return List.of(event.getStartTime());
            }
            return Collections.emptyList();
        }
        
        return generateOccurrencesFromPattern(event, event.getRecurrencePattern(), start, end);
    }

    private List<LocalDateTime> generateOccurrencesFromPattern(CalendarEvent event, RecurrencePattern pattern, LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> occurrences = new ArrayList<>();
        LocalDateTime eventStart = event.getStartTime();
        LocalTime eventTime = eventStart.toLocalTime();
        
        // Determine the date range to check
        LocalDate rangeStart = start.toLocalDate();
        LocalDate rangeEnd = end.toLocalDate();
        
        // If the pattern has an until date, limit the range
        if (pattern.getUntilDate() != null && pattern.getUntilDate().isBefore(rangeEnd)) {
            rangeEnd = pattern.getUntilDate();
        }
        
        // Generate occurrences based on the frequency
        switch (pattern.getFrequency()) {
            case DAILY:
                generateDailyOccurrences(occurrences, eventStart, eventTime, rangeStart, rangeEnd, pattern);
                break;
            case WEEKLY:
                generateWeeklyOccurrences(occurrences, eventStart, eventTime, rangeStart, rangeEnd, pattern);
                break;
            case MONTHLY:
                generateMonthlyOccurrences(occurrences, eventStart, eventTime, rangeStart, rangeEnd, pattern);
                break;
            case YEARLY:
                generateYearlyOccurrences(occurrences, eventStart, eventTime, rangeStart, rangeEnd, pattern);
                break;
        }
        
        // If the pattern has a count, limit the number of occurrences
        if (pattern.getCount() != null && occurrences.size() > pattern.getCount()) {
            occurrences = occurrences.subList(0, pattern.getCount());
        }
        
        return occurrences;
    }

    private void generateDailyOccurrences(List<LocalDateTime> occurrences, LocalDateTime eventStart, LocalTime eventTime, LocalDate rangeStart, LocalDate rangeEnd, RecurrencePattern pattern) {
        LocalDate currentDate = eventStart.toLocalDate();
        int interval = pattern.getInterval();
        
        // Skip to the first occurrence within the range
        while (currentDate.isBefore(rangeStart)) {
            currentDate = currentDate.plusDays(interval);
        }
        
        // Generate occurrences within the range
        while (!currentDate.isAfter(rangeEnd)) {
            if (!pattern.getExceptionDates().contains(currentDate)) {
                occurrences.add(LocalDateTime.of(currentDate, eventTime));
            }
            currentDate = currentDate.plusDays(interval);
        }
    }

    private void generateWeeklyOccurrences(List<LocalDateTime> occurrences, LocalDateTime eventStart, LocalTime eventTime, LocalDate rangeStart, LocalDate rangeEnd, RecurrencePattern pattern) {
        LocalDate currentDate = eventStart.toLocalDate();
        int interval = pattern.getInterval();
        Set<DayOfWeek> daysOfWeek = pattern.getDaysOfWeek();
        
        // If no days of week are specified, use the day of the event
        if (daysOfWeek.isEmpty()) {
            daysOfWeek = Set.of(eventStart.getDayOfWeek());
        }
        
        // Skip to the first week within the range
        while (currentDate.plusDays(6).isBefore(rangeStart)) {
            currentDate = currentDate.plusWeeks(interval);
        }
        
        // Generate occurrences within the range
        while (!currentDate.isAfter(rangeEnd)) {
            for (DayOfWeek dayOfWeek : daysOfWeek) {
                LocalDate occurrenceDate = currentDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
                if (!occurrenceDate.isAfter(rangeEnd) && !occurrenceDate.isBefore(rangeStart) && !pattern.getExceptionDates().contains(occurrenceDate)) {
                    occurrences.add(LocalDateTime.of(occurrenceDate, eventTime));
                }
            }
            currentDate = currentDate.plusWeeks(interval);
        }
    }

    private void generateMonthlyOccurrences(List<LocalDateTime> occurrences, LocalDateTime eventStart, LocalTime eventTime, LocalDate rangeStart, LocalDate rangeEnd, RecurrencePattern pattern) {
        LocalDate currentDate = eventStart.toLocalDate();
        int interval = pattern.getInterval();
        Set<Integer> daysOfMonth = pattern.getDaysOfMonth();
        Set<DayOfWeek> daysOfWeek = pattern.getDaysOfWeek();
        Integer dayOfWeekPosition = pattern.getDayOfWeekPosition();
        
        // Skip to the first month within the range
        while (currentDate.plusDays(31).isBefore(rangeStart)) {
            currentDate = currentDate.plusMonths(interval);
        }
        
        // Generate occurrences within the range
        while (!currentDate.isAfter(rangeEnd)) {
            // If days of month are specified
            if (!daysOfMonth.isEmpty()) {
                for (Integer dayOfMonth : daysOfMonth) {
                    try {
                        LocalDate occurrenceDate = currentDate.withDayOfMonth(dayOfMonth);
                        if (!occurrenceDate.isAfter(rangeEnd) && !occurrenceDate.isBefore(rangeStart) && !pattern.getExceptionDates().contains(occurrenceDate)) {
                            occurrences.add(LocalDateTime.of(occurrenceDate, eventTime));
                        }
                    } catch (Exception e) {
                        // Skip invalid days (e.g., February 30)
                    }
                }
            }
            // If days of week and position are specified
            else if (!daysOfWeek.isEmpty() && dayOfWeekPosition != null) {
                for (DayOfWeek dayOfWeek : daysOfWeek) {
                    LocalDate occurrenceDate;
                    if (dayOfWeekPosition > 0) {
                        // Nth occurrence of the day of the week
                        occurrenceDate = currentDate.with(TemporalAdjusters.dayOfWeekInMonth(dayOfWeekPosition, dayOfWeek));
                    } else {
                        // Last occurrence of the day of the week
                        occurrenceDate = currentDate.with(TemporalAdjusters.lastInMonth(dayOfWeek));
                    }
                    if (!occurrenceDate.isAfter(rangeEnd) && !occurrenceDate.isBefore(rangeStart) && !pattern.getExceptionDates().contains(occurrenceDate)) {
                        occurrences.add(LocalDateTime.of(occurrenceDate, eventTime));
                    }
                }
            }
            // If neither is specified, use the day of the event
            else {
                int dayOfMonth = eventStart.getDayOfMonth();
                try {
                    LocalDate occurrenceDate = currentDate.withDayOfMonth(dayOfMonth);
                    if (!occurrenceDate.isAfter(rangeEnd) && !occurrenceDate.isBefore(rangeStart) && !pattern.getExceptionDates().contains(occurrenceDate)) {
                        occurrences.add(LocalDateTime.of(occurrenceDate, eventTime));
                    }
                } catch (Exception e) {
                    // Skip invalid days (e.g., February 30)
                }
            }
            currentDate = currentDate.plusMonths(interval);
        }
    }

    private void generateYearlyOccurrences(List<LocalDateTime> occurrences, LocalDateTime eventStart, LocalTime eventTime, LocalDate rangeStart, LocalDate rangeEnd, RecurrencePattern pattern) {
        LocalDate currentDate = eventStart.toLocalDate();
        int interval = pattern.getInterval();
        Set<Integer> months = pattern.getMonths();
        
        // If no months are specified, use the month of the event
        if (months.isEmpty()) {
            months = Set.of(eventStart.getMonthValue());
        }
        
        // Skip to the first year within the range
        while (currentDate.plusYears(1).isBefore(rangeStart)) {
            currentDate = currentDate.plusYears(interval);
        }
        
        // Generate occurrences within the range
        while (!currentDate.isAfter(rangeEnd)) {
            for (Integer month : months) {
                try {
                    LocalDate occurrenceDate = currentDate.withMonth(month).withDayOfMonth(eventStart.getDayOfMonth());
                    if (!occurrenceDate.isAfter(rangeEnd) && !occurrenceDate.isBefore(rangeStart) && !pattern.getExceptionDates().contains(occurrenceDate)) {
                        occurrences.add(LocalDateTime.of(occurrenceDate, eventTime));
                    }
                } catch (Exception e) {
                    // Skip invalid days (e.g., February 30)
                }
            }
            currentDate = currentDate.plusYears(interval);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDateInPattern(RecurrencePattern pattern, LocalDate date) {
        return pattern.includesDate(date);
    }

    @Override
    @Transactional
    public RecurrencePattern createPatternFromRule(String recurrenceRule, CalendarEvent event) {
        log.info("Creating recurrence pattern from rule: {}", recurrenceRule);
        
        Matcher matcher = RRULE_PATTERN.matcher(recurrenceRule);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid recurrence rule: " + recurrenceRule);
        }
        
        String freqStr = matcher.group(1);
        String intervalStr = matcher.group(2);
        String bydayStr = matcher.group(3);
        String bymonthdayStr = matcher.group(4);
        String bymonthStr = matcher.group(5);
        String untilStr = matcher.group(6);
        String countStr = matcher.group(7);
        
        RecurrencePattern.RecurrenceFrequency frequency = RecurrencePattern.RecurrenceFrequency.valueOf(freqStr);
        int interval = intervalStr != null ? Integer.parseInt(intervalStr) : 1;
        
        RecurrencePattern pattern = RecurrencePattern.builder()
                .event(event)
                .frequency(frequency)
                .interval(interval)
                .build();
        
        // Parse BYDAY
        if (bydayStr != null) {
            Set<DayOfWeek> daysOfWeek = Arrays.stream(bydayStr.split(","))
                    .map(this::parseDayOfWeek)
                    .collect(Collectors.toSet());
            pattern.setDaysOfWeek(daysOfWeek);
        }
        
        // Parse BYMONTHDAY
        if (bymonthdayStr != null) {
            Set<Integer> daysOfMonth = Arrays.stream(bymonthdayStr.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            pattern.setDaysOfMonth(daysOfMonth);
        }
        
        // Parse BYMONTH
        if (bymonthStr != null) {
            Set<Integer> months = Arrays.stream(bymonthStr.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            pattern.setMonths(months);
        }
        
        // Parse UNTIL
        if (untilStr != null) {
            // Parse YYYYMMDDTHHMMSSZ format
            int year = Integer.parseInt(untilStr.substring(0, 4));
            int month = Integer.parseInt(untilStr.substring(4, 6));
            int day = Integer.parseInt(untilStr.substring(6, 8));
            pattern.setUntilDate(LocalDate.of(year, month, day));
        }
        
        // Parse COUNT
        if (countStr != null) {
            pattern.setCount(Integer.parseInt(countStr));
        }
        
        return pattern;
    }

    private DayOfWeek parseDayOfWeek(String dayStr) {
        switch (dayStr) {
            case "MO": return DayOfWeek.MONDAY;
            case "TU": return DayOfWeek.TUESDAY;
            case "WE": return DayOfWeek.WEDNESDAY;
            case "TH": return DayOfWeek.THURSDAY;
            case "FR": return DayOfWeek.FRIDAY;
            case "SA": return DayOfWeek.SATURDAY;
            case "SU": return DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Invalid day of week: " + dayStr);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String convertPatternToRule(RecurrencePattern pattern) {
        log.info("Converting recurrence pattern to rule");
        
        StringBuilder rule = new StringBuilder();
        rule.append("FREQ=").append(pattern.getFrequency().name()).append(";");
        
        if (pattern.getInterval() > 1) {
            rule.append("INTERVAL=").append(pattern.getInterval()).append(";");
        }
        
        if (!pattern.getDaysOfWeek().isEmpty()) {
            rule.append("BYDAY=");
            rule.append(pattern.getDaysOfWeek().stream()
                    .map(this::formatDayOfWeek)
                    .collect(Collectors.joining(",")));
            rule.append(";");
        }
        
        if (!pattern.getDaysOfMonth().isEmpty()) {
            rule.append("BYMONTHDAY=");
            rule.append(pattern.getDaysOfMonth().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
            rule.append(";");
        }
        
        if (!pattern.getMonths().isEmpty()) {
            rule.append("BYMONTH=");
            rule.append(pattern.getMonths().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
            rule.append(";");
        }
        
        if (pattern.getUntilDate() != null) {
            rule.append("UNTIL=");
            rule.append(String.format("%04d%02d%02dT000000Z", 
                    pattern.getUntilDate().getYear(), 
                    pattern.getUntilDate().getMonthValue(), 
                    pattern.getUntilDate().getDayOfMonth()));
            rule.append(";");
        }
        
        if (pattern.getCount() != null) {
            rule.append("COUNT=").append(pattern.getCount()).append(";");
        }
        
        return rule.toString();
    }

    private String formatDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "MO";
            case TUESDAY: return "TU";
            case WEDNESDAY: return "WE";
            case THURSDAY: return "TH";
            case FRIDAY: return "FR";
            case SATURDAY: return "SA";
            case SUNDAY: return "SU";
            default: throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime getNextOccurrence(CalendarEvent event, LocalDateTime after) {
        log.info("Getting next occurrence of event with ID: {} after {}", event.getId(), after);
        
        // Get occurrences for the next year
        LocalDateTime end = after.plus(1, ChronoUnit.YEARS);
        List<LocalDateTime> occurrences = generateOccurrences(event, after, end);
        
        // Find the first occurrence after the specified date
        return occurrences.stream()
                .filter(occurrence -> occurrence.isAfter(after))
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime getPreviousOccurrence(CalendarEvent event, LocalDateTime before) {
        log.info("Getting previous occurrence of event with ID: {} before {}", event.getId(), before);
        
        // Get occurrences for the previous year
        LocalDateTime start = before.minus(1, ChronoUnit.YEARS);
        List<LocalDateTime> occurrences = generateOccurrences(event, start, before);
        
        // Find the last occurrence before the specified date
        return occurrences.stream()
                .filter(occurrence -> occurrence.isBefore(before))
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    @Transactional
    public RecurrencePattern addExceptionDate(RecurrencePattern pattern, LocalDate date) {
        log.info("Adding exception date {} to recurrence pattern", date);
        
        pattern.getExceptionDates().add(date);
        return pattern;
    }

    @Override
    @Transactional
    public RecurrencePattern removeExceptionDate(RecurrencePattern pattern, LocalDate date) {
        log.info("Removing exception date {} from recurrence pattern", date);
        
        pattern.getExceptionDates().remove(date);
        return pattern;
    }
}