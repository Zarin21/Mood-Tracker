/*
 * User - Model class representing a user in the mood tracking application.
 *
 * Purpose:
 * - Encapsulates user data including userId, username, email, and password
 * - Implements Serializable to allow passing User objects between components
 * - Provides getters and setters for all properties
 * - Used throughout the app for authentication, profile management, and social features
 */
package com.example.unemployedavengers.models;

import java.io.Serializable;

public class User implements Serializable {
    // Core user identification and authentication properties
    private String userId;      // Firebase UID for the user
    private String username;    // Display name shown in the app
    private String dummyEmail;  // Email address constructed from username for Firebase Auth
    private String password;    // User's password (note: storing in plaintext is not ideal for security)
    private String avatar; // User's avatar



    /**
     * Default constructor required for Firestore deserialization
     */
    public User() {
    }

    /**
     * Constructs a new User with all required fields
     *
     * @param userId     The unique identifier for the user
     * @param username   The display name for the user
     * @param dummyEmail The email address for Firebase Authentication
     * @param password   The user's password
     * @param avatar     The user's avatar
     */
    public User(String userId, String username, String dummyEmail, String password, String avatar) {
        this.userId = userId;
        this.username = username;
        this.dummyEmail = dummyEmail;
        this.password = password;
        this.avatar = avatar;
    }

    /**
     * @return The user's unique identifier
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The user's unique identifier to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The user's display name
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The user's display name to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return The dummy email used for Firebase Authentication
     */
    public String getDummyEmail() {
        return dummyEmail;
    }

    /**
     * @param dummyEmail The dummy email to set
     */
    public void setDummyEmail(String dummyEmail) {
        this.dummyEmail = dummyEmail;
    }

    /**
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The user's password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}