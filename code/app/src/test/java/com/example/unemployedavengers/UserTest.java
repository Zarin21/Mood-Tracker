/**
 * UserTest.java
 *
 * Unit tests for the User model class in the UnemployedAvengers application.
 *
 * This test class verifies:
 * - Proper initialization of User objects through constructors
 * - Correct functioning of all getter and setter methods
 * - Data integrity for core user attributes (ID, username, email, etc.)
 * - Avatar URL handling
 *
 * Testing Methodology:
 * - Uses JUnit 4 testing framework
 * - Follows Arrange-Act-Assert pattern
 * - Tests both object construction and field modification
 * - Verifies all model fields are properly accessible
 *
 */
package com.example.unemployedavengers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.unemployedavengers.models.User;

import org.junit.Before;
import org.junit.Test;

public class UserTest {
    private User user;

    @Before
    public void setUp() {
        user = new User("12345", "testUser", "test@example.com", "password123", "avatar_url");
    }

    @Test
    public void testUserConstructor() {
        assertNotNull(user);
        assertEquals("12345", user.getUserId());
        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getDummyEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("avatar_url", user.getAvatar());
    }

    @Test
    public void testSettersAndGetters() {
        user.setUserId("67890");
        user.setUsername("newUser");
        user.setDummyEmail("new@example.com");
        user.setPassword("newPassword");
        user.setAvatar("new_avatar_url");

        assertEquals("67890", user.getUserId());
        assertEquals("newUser", user.getUsername());
        assertEquals("new@example.com", user.getDummyEmail());
        assertEquals("newPassword", user.getPassword());
        assertEquals("new_avatar_url", user.getAvatar());
    }
}
