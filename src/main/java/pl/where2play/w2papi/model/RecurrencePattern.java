package pl.where2play.w2papi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a recurrence pattern for calendar events.
 * This allows for complex recurring events like "every second Tuesday of the month" or "every weekday except holidays".
 */
@Entity
@Table(name = "recurrence_patterns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrencePattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "recurrencePattern")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CalendarEvent event;

    /**
     * The frequency of the recurrence.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency frequency;

    /**
     * The interval of the recurrence.
     * For example, if frequency is WEEKLY and interval is 2, the event occurs every 2 weeks.
     */
    @Column(nullable = false)
    private int interval;

    /**
     * The days of the week on which the event occurs.
     * Used for WEEKLY and MONTHLY frequencies.
     */
    @ElementCollection
    @CollectionTable(name = "recurrence_days_of_week", joinColumns = @JoinColumn(name = "recurrence_pattern_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> daysOfWeek = new HashSet<>();

    /**
     * The days of the month on which the event occurs.
     * Used for MONTHLY frequency.
     * For example, [1, 15] means the event occurs on the 1st and 15th of each month.
     */
    @ElementCollection
    @CollectionTable(name = "recurrence_days_of_month", joinColumns = @JoinColumn(name = "recurrence_pattern_id"))
    @Column(name = "day_of_month")
    @Builder.Default
    private Set<Integer> daysOfMonth = new HashSet<>();

    /**
     * The months of the year in which the event occurs.
     * Used for YEARLY frequency.
     * For example, [1, 7] means the event occurs in January and July.
     */
    @ElementCollection
    @CollectionTable(name = "recurrence_months", joinColumns = @JoinColumn(name = "recurrence_pattern_id"))
    @Column(name = "month")
    @Builder.Default
    private Set<Integer> months = new HashSet<>();

    /**
     * The position of the day of the week in the month.
     * Used for MONTHLY frequency with daysOfWeek.
     * For example, 2 means the second occurrence of the day of the week in the month.
     * -1 means the last occurrence.
     */
    private Integer dayOfWeekPosition;

    /**
     * The date until which the event recurs.
     * If null, the event recurs indefinitely.
     */
    private LocalDate untilDate;

    /**
     * The number of occurrences of the event.
     * If null, the event recurs indefinitely or until untilDate.
     */
    private Integer count;

    /**
     * The dates to exclude from the recurrence.
     * For example, holidays or other exceptions.
     */
    @ElementCollection
    @CollectionTable(name = "recurrence_exceptions", joinColumns = @JoinColumn(name = "recurrence_pattern_id"))
    @Column(name = "exception_date")
    @Builder.Default
    private Set<LocalDate> exceptionDates = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum representing the frequency of a recurrence pattern.
     */
    public enum RecurrenceFrequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    /**
     * Check if a date is part of this recurrence pattern.
     *
     * @param date the date to check
     * @return true if the date is part of the recurrence pattern, false otherwise
     */
    public boolean includesDate(LocalDate date) {
        // Check if the date is an exception
        if (exceptionDates.contains(date)) {
            return false;
        }

        // Check if the date is after the until date
        if (untilDate != null && date.isAfter(untilDate)) {
            return false;
        }

        // Check based on frequency
        switch (frequency) {
            case DAILY:
                return isDailyRecurrence(date);
            case WEEKLY:
                return isWeeklyRecurrence(date);
            case MONTHLY:
                return isMonthlyRecurrence(date);
            case YEARLY:
                return isYearlyRecurrence(date);
            default:
                return false;
        }
    }

    private boolean isDailyRecurrence(LocalDate date) {
        // For daily recurrence, check if the number of days since the start date is divisible by the interval
        LocalDate startDate = event.getStartTime().toLocalDate();
        long daysBetween = startDate.until(date).getDays();
        return daysBetween % interval == 0;
    }

    private boolean isWeeklyRecurrence(LocalDate date) {
        // For weekly recurrence, check if the day of the week is in the set of days of the week
        // and if the number of weeks since the start date is divisible by the interval
        if (!daysOfWeek.contains(date.getDayOfWeek())) {
            return false;
        }

        LocalDate startDate = event.getStartTime().toLocalDate();
        long daysBetween = startDate.until(date).getDays();
        long weeksBetween = daysBetween / 7;
        return weeksBetween % interval == 0;
    }

    private boolean isMonthlyRecurrence(LocalDate date) {
        // For monthly recurrence, check if the day of the month is in the set of days of the month
        // or if the day of the week and position match
        // and if the number of months since the start date is divisible by the interval
        LocalDate startDate = event.getStartTime().toLocalDate();
        int monthsBetween = (date.getYear() - startDate.getYear()) * 12 + date.getMonthValue() - startDate.getMonthValue();
        if (monthsBetween % interval != 0) {
            return false;
        }

        // Check if the day of the month is in the set of days of the month
        if (!daysOfMonth.isEmpty() && daysOfMonth.contains(date.getDayOfMonth())) {
            return true;
        }

        // Check if the day of the week and position match
        if (!daysOfWeek.isEmpty() && daysOfWeek.contains(date.getDayOfWeek()) && dayOfWeekPosition != null) {
            int position = getDayOfWeekPosition(date);
            return position == dayOfWeekPosition;
        }

        return false;
    }

    private boolean isYearlyRecurrence(LocalDate date) {
        // For yearly recurrence, check if the month is in the set of months
        // and if the day of the month matches
        // and if the number of years since the start date is divisible by the interval
        LocalDate startDate = event.getStartTime().toLocalDate();
        int yearsBetween = date.getYear() - startDate.getYear();
        if (yearsBetween % interval != 0) {
            return false;
        }

        // Check if the month is in the set of months
        if (!months.isEmpty() && !months.contains(date.getMonthValue())) {
            return false;
        }

        // Check if the day of the month matches
        return date.getDayOfMonth() == startDate.getDayOfMonth();
    }

    /**
     * Get the position of the day of the week in the month.
     * For example, the 2nd Tuesday or the last Friday.
     *
     * @param date the date
     * @return the position (1-based, negative for counting from the end)
     */
    private int getDayOfWeekPosition(LocalDate date) {
        int dayOfMonth = date.getDayOfMonth();
        int position = (dayOfMonth - 1) / 7 + 1;

        // Check if it's the last occurrence of this day of the week in the month
        LocalDate nextWeek = date.plusDays(7);
        if (nextWeek.getMonthValue() != date.getMonthValue()) {
            return -1; // Last occurrence
        }

        return position;
    }
}