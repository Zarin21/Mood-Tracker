package com.example.unemployedavengers.models;

import java.io.Serializable;

import java.io.Serializable;

/**
 * Represents a user in the system.
 * Includes a unique userId (from Firebase Auth),
 * a username for display, and optionally a dummyEmail for sign-up.
 */
public class User implements Serializable {
    private String userId;
    private String username;
    private String dummyEmail;

    public User() {
        // Required by Firestore for deserialization
    }

    public User(String userId, String username, String dummyEmail) {
        this.userId = userId;
        this.username = username;
        this.dummyEmail = dummyEmail;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDummyEmail() {
        return dummyEmail;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDummyEmail(String dummyEmail) {
        this.dummyEmail = dummyEmail;
    }
}
