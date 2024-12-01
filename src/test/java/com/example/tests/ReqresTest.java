package com.example.tests;

import com.example.model.User;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Test class to interact with the Reqres API.
 * Demonstrates CRUD operations and additional scenarios with Lombok-powered POJOs.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures test execution order
public class ReqresTest {

    private static String createdUserId; // Store user ID for dependent tests

    static {
        // Base URI for Reqres API
        RestAssured.baseURI = "https://reqres.in/api";
    }

    /**
     * Test creating a new user.
     */
    @Test
    @Order(1)
    public void testCreateUser() {
        User newUser = User.builder()
                .name("John Doe")
                .job("Software Engineer")
                .build();

        User createdUser = given()
                .contentType("application/json")
                .body(newUser.toJson())
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .as(User.class);

        System.out.println("Created User: " + createdUser);

        // Store the user ID for later tests
        createdUserId = createdUser.getId();

        // Validate response fields
        Assertions.assertNotNull(createdUser.getId());
        Assertions.assertNotNull(createdUser.getCreatedAt());
        Assertions.assertEquals("John Doe", createdUser.getName());
        Assertions.assertEquals("Software Engineer", createdUser.getJob());
    }

    /**
     * Test updating an existing user.
     */
    @Test
    @Order(2)
    public void testUpdateUser() {
        Assertions.assertNotNull(createdUserId, "User ID should not be null");

        User updatedUserDetails = User.builder()
                .name("John Doe")
                .job("Senior Software Engineer")
                .build();

        User updatedUser = given()
                .contentType("application/json")
                .body(updatedUserDetails.toJson())
                .when()
                .put("/users/" + createdUserId)
                .then()
                .statusCode(200)
                .extract()
                .as(User.class);

        System.out.println("Updated User: " + updatedUser);

        // Validate updated job
        Assertions.assertEquals("Senior Software Engineer", updatedUser.getJob());
    }

    /**
     * Test fetching a list of users.
     */
    @Test
    @Order(3)
    public void testRetrieveAllUsers() {
        List<User> users = given()
                .queryParam("page", 2) // Fetch page 2 of users
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data", User.class);

        System.out.println("Retrieved Users: " + users);

        // Validate the user list is not empty
        Assertions.assertFalse(users.isEmpty(), "User list should not be empty");
    }

    /**
     * Test deleting an existing user.
     */
    @Test
    @Order(4)
    public void testDeleteUser() {
        Assertions.assertNotNull(createdUserId, "User ID should not be null");

        given()
                .when()
                .delete("/users/" + createdUserId)
                .then()
                .statusCode(204); // 204 means successful deletion

        System.out.println("Deleted User with ID: " + createdUserId);
    }

    /**
     * Test retrieving a non-existent user.
     */
    @Test
    @Order(5)
    public void testRetrieveNonExistentUser() {
        given()
                .when()
                .get("/users/9999") // Invalid user ID
                .then()
                .statusCode(404) // Not Found
                .body("error", equalTo("user not found"));

        System.out.println("Verified non-existent user retrieval returns 404.");
    }

    /**
     * Test creating a user with invalid data.
     */
    @Test
    @Order(6)
    public void testCreateUserInvalidData() {
        User invalidUser = User.builder()
                .name("") // Invalid empty name
                .job("")  // Invalid empty job
                .build();

        given()
                .contentType("application/json")
                .body(invalidUser.toJson())
                .when()
                .post("/users")
                .then()
                .statusCode(400); // Bad Request

        System.out.println("Verified creating user with invalid data fails.");
    }
}
