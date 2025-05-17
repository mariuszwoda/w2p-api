package pl.where2play.w2papi.constants;

/**
 * Constants for API endpoints.
 * This class externalizes method names to be available for controllers and E2E tests.
 */
public final class ApiEndpoint {

    private ApiEndpoint() {
        // Private constructor to prevent instantiation
    }

    /**
     * Auth controller endpoints.
     */
    public static final class Auth {
        public static final String BASE = "/api/auth";
        public static final String LOGIN = "/login";
    }

    /**
     * Calendar event controller endpoints.
     */
    public static final class CalendarEvent {
        public static final String BASE = "/api/events";
        public static final String CREATE_EVENT = "";
        public static final String GET_ALL_EVENTS = "";
        public static final String GET_EVENT = "/{id}";
        public static final String UPDATE_EVENT = "/{id}";
        public static final String DELETE_EVENT = "/{id}";
        public static final String HARD_DELETE_EVENT = "/{id}/hard";
        public static final String GET_EVENTS_IN_RANGE = "/range";
        public static final String SYNC_EVENTS = "/sync";
        public static final String ADD_ATTENDEE = "/{eventId}/attendees/{userId}";
        public static final String REMOVE_ATTENDEE = "/{eventId}/attendees/{userId}";
    }

    /**
     * Google Calendar controller endpoints.
     */
    public static final class GoogleCalendar {
        public static final String BASE = "/api/google-calendar";
        public static final String AUTH_URL = "/auth-url";
        public static final String EXCHANGE_CODE = "/exchange-code";
        public static final String AUTH_STATUS = "/auth-status";
    }

    /**
     * User controller endpoints.
     */
    public static final class User {
        public static final String BASE = "/api/users";
        public static final String CURRENT_USER = "/me";
        public static final String UPDATE_PROFILE = "/me";
        public static final String DELETE_ACCOUNT = "/me";
        public static final String SEARCH_USERS = "/search";
        public static final String GET_USER = "/{id}";
    }
}
