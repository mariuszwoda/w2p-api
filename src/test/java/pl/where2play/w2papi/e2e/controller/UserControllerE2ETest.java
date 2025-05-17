package pl.where2play.w2papi.e2e.controller;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pl.where2play.w2papi.e2e.framework.BaseApiTest;
import pl.where2play.w2papi.e2e.framework.RequestConfig;
import pl.where2play.w2papi.constants.ApiEndpoint;
import pl.where2play.w2papi.model.User;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static pl.where2play.w2papi.constants.ApiEndpoint.User.BASE;

/**
 * E2E tests for the User Controller.
 */
public class UserControllerE2ETest extends BaseApiTest {

    @Test
    @DisplayName("Test getting current user profile")
    void testGetCurrentUserProfile() {
        // Test getting current user profile
        Response response = get(
                BASE+ApiEndpoint.User.CURRENT_USER,
                RequestConfig.empty()
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testUser.getId().intValue()))
                .body("email", equalTo(testUser.getEmail()))
                .body("name", equalTo(testUser.getName()));
    }

    @Test
    @DisplayName("Test updating user profile")
    void testUpdateUserProfile() {
        // Test updating user profile
        String newName = "Updated E2E Test User " + UUID.randomUUID();
        String newPictureUrl = "https://example.com/updated-picture.jpg";
        
        Response response = put(
                BASE+ApiEndpoint.User.UPDATE_PROFILE,
                RequestConfig.withQueryParams(Map.of(
                        "name", newName,
                        "pictureUrl", newPictureUrl
                ))
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testUser.getId().intValue()))
                .body("email", equalTo(testUser.getEmail()))
                .body("name", equalTo(newName))
                .body("pictureUrl", equalTo(newPictureUrl));
        
        // Verify the update by getting the user profile again
        Response verifyResponse = get(
                BASE+ApiEndpoint.User.CURRENT_USER,
                RequestConfig.empty()
        );
        
        verifyResponse.then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo(newName))
                .body("pictureUrl", equalTo(newPictureUrl));
    }

    @Test
    @DisplayName("Test searching for users")
    void testSearchUsers() {
        // Create a test user with a unique name for searching
        String uniqueName = "SearchableUser" + UUID.randomUUID();
        User searchableUser = User.builder()
                .email("searchable-" + UUID.randomUUID() + "@example.com")
                .name(uniqueName)
                .provider(User.AuthProvider.GOOGLE)
                .providerId("search-test-" + UUID.randomUUID())
                .build();
        userRepository.save(searchableUser);
        
        // Test searching for users
        Response response = get(
                BASE+ApiEndpoint.User.SEARCH_USERS,
                RequestConfig.withQueryParams(Map.of("query", uniqueName))
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == " + searchableUser.getId() + " }.name", equalTo(uniqueName));
    }

    @Test
    @DisplayName("Test getting user by ID")
    void testGetUserById() {
        // Test getting user by ID
        Response response = get(
                BASE+ApiEndpoint.User.GET_USER,
                RequestConfig.withPathParams(Map.of("id", testUser.getId()))
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testUser.getId().intValue()))
                .body("email", equalTo(testUser.getEmail()))
                .body("name", equalTo(testUser.getName()));
    }

    @Test
    @DisplayName("Test getting non-existent user")
    void testGetNonExistentUser() {
        // Test getting non-existent user
        Response response = get(
                BASE+ApiEndpoint.User.GET_USER,
                RequestConfig.withPathParams(Map.of("id", 999999L))
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Test unauthorized access")
    void testUnauthorizedAccess() {
        // Try to access protected endpoint without authentication
        Response response = requestWithoutAuth(
                BASE+ApiEndpoint.User.CURRENT_USER,
                "GET",
                RequestConfig.empty()
        );
        
        // Verify response
        response.then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    // Note: We're not testing the delete account endpoint in E2E tests
    // as it would permanently delete the test user
}