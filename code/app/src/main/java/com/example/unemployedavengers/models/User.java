/*
 * User - Represents a user entity in the Unemployed Avengers application.
 *
 * This class models a user with essential attributes such as:
 * - A unique userId (retrieved from Firebase Authentication).
 * - A username for display and identification purposes.
 * - A dummyEmail used for Firebase Authentication (generated from the username).
 * - A password (stored temporarily for local operations, not recommended for long-term storage).
 *
 */
package com.example.unemployedavengers.models;

import java.io.Serializable;

import java.io.Serializable;

/**
 * Represents a user in the system.
 * <p>
 * This class includes:
 * - A unique userId (from Firebase Auth).
 * - A username for display.
 * - A dummyEmail used for Firebase Authentication.
 * - A password field (used for authentication, should not be stored long-term).
 * </p>
 */
public class User implements Serializable {
    private String userId;
    private String username;
    private String dummyEmail;
    private String password;

    /**
     * Default constructor required for Firestore serialization.
     */
    public User() {
    }

    /**
     * Constructs a new {@link User} with the given attributes.
     *
     * @param userId     The unique ID assigned to the user.
     * @param username   The display username of the user.
     * @param dummyEmail The dummy email used for Firebase authentication.
     * @param password   The password associated with the user.
     */
    public User(String userId, String username, String dummyEmail, String password) {
        this.userId = userId;
        this.username = username;
        this.dummyEmail = dummyEmail;
        this.password = password;
    }

    /**
     * Retrieves the user ID.
     *
     * @return The unique user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The unique user ID to be set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the username.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The new username to be assigned to the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the dummy email used for Firebase authentication.
     *
     * @return The dummy email associated with the user.
     */
    public String getDummyEmail() {
        return dummyEmail;
    }

    /**
     * Sets the dummy email.
     *
     * @param dummyEmail The new dummy email to be assigned to the user.
     */
    public void setDummyEmail(String dummyEmail) {
        this.dummyEmail = dummyEmail;
    }

    /**
     * Retrieves the user's password.
     *
     * @return The user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password The new password to be assigned to the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}