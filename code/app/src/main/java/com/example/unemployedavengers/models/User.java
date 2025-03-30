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

/**
 * Model class representing a user in the mood tracking application.
 * <p>
 * This class encapsulates essential user data, including the user's unique ID, display name,
 * email (used for Firebase authentication), password, and avatar. It implements the Serializable
 * interface to allow passing User objects between components, such as activities or fragments.
 * </p>
 */
public class User implements Serializable {
    // Core user identification and authentication properties
    private String userId;      // Firebase UID for the user
    private String username;    // Display name shown in the app
    private String dummyEmail;  // Email address constructed from username for Firebase Auth
    private String password;    // User's password (note: storing in plaintext is not ideal for security)
    private String avatar; // User's avatar



    /**
     * Default constructor required for Firestore deserialization.
     * <p>
     * This constructor allows Firestore to create a default instance of the User class during
     * deserialization. This constructor should not be used manually.
     * </p>
     */
    public User() {
    }

    /**
     * Constructs a new User with all required fields.
     * <p>
     * This constructor initializes the User object with all necessary data, including the user's
     * unique identifier, username, dummy email for Firebase authentication, password, and avatar.
     * </p>
     *
     * @param userId     The unique identifier for the user (Firebase UID)
     * @param username   The display name for the user in the application
     * @param dummyEmail The email address for Firebase Authentication
     * @param password   The user's password (plaintext, which should be handled securely)
     * @param avatar     The user's avatar image (URL or file reference)
     */
    public User(String userId, String username, String dummyEmail, String password, String avatar) {
        this.userId = userId;
        this.username = username;
        this.dummyEmail = dummyEmail;
        this.password = password;
        this.avatar = avatar;
    }

    /**
     * @return The user's unique identifier (Firebase UID)
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user's unique identifier.
     *
     * @param userId The unique identifier to set for the user (Firebase UID)
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The user's display name in the application
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's display name.
     *
     * @param username The display name to set for the user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return The dummy email used for Firebase authentication
     */
    public String getDummyEmail() {
        return dummyEmail;
    }

    /**
     * Sets the dummy email used for Firebase authentication.
     *
     * @param dummyEmail The dummy email to set for the user
     */
    public void setDummyEmail(String dummyEmail) {
        this.dummyEmail = dummyEmail;
    }

    /**
     * @return The user's password (plaintext)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password The password to set for the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return The user's avatar (URL or file reference)
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Sets the user's avatar.
     *
     * @param avatar The avatar to set for the user (URL or file reference)
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}