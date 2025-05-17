package pl.where2play.w2papi.e2e.framework.constants;

/**
 * Constants for API endpoints.
 * This class externalizes method names to be available for controllers and E2E tests.
 */
public final class ApiEndpoints {

    private ApiEndpoints() {
        // Private constructor to prevent instantiation
    }

    /**
     * Auth controller endpoints.
     */
    public static final class Auth {
        public static final String BASE = "/auth";
        public static final String LOGIN = BASE + "/login";
    }

    /**
     * Calendar event controller endpoints.
     */
    public static final class CalendarEvent {
        public static final String BASE = "/events";
        public static final String GET_EVENT = BASE + "/{id}";
        public static final String UPDATE_EVENT = BASE + "/{id}";
        public static final String DELETE_EVENT = BASE + "/{id}";
        public static final String HARD_DELETE_EVENT = BASE + "/{id}/hard";
        public static final String GET_EVENTS_IN_RANGE = BASE + "/range";
        public static final String SYNC_EVENTS = BASE + "/sync";
        public static final String ADD_ATTENDEE = BASE + "/{eventId}/attendees/{userId}";
        public static final String REMOVE_ATTENDEE = BASE + "/{eventId}/attendees/{userId}";
    }

    /**
     * Google Calendar controller endpoints.
     */
    public static final class GoogleCalendar {
        public static final String BASE = "/google-calendar";
        public static final String AUTH_URL = BASE + "/auth-url";
        public static final String EXCHANGE_CODE = BASE + "/exchange-code";
        public static final String AUTH_STATUS = BASE + "/auth-status";
    }

    /**
     * User controller endpoints.
     */
    public static final class User {
        public static final String BASE = "/users";
        public static final String CURRENT_USER = BASE + "/me";
        public static final String UPDATE_PROFILE = BASE + "/me";
        public static final String DELETE_ACCOUNT = BASE + "/me";
        public static final String SEARCH_USERS = BASE + "/search";
        public static final String GET_USER = BASE + "/{id}";
    }
}