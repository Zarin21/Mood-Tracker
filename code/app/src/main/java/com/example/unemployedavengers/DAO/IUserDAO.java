package com.example.unemployedavengers.DAO;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

import com.example.unemployedavengers.models.User;
import com.google.android.gms.tasks.Task;

import java.util.List;

public interface IUserDAO {

    /**
     * Signs up a user with a username (converted to a dummy email) and password.
     */
    Task<Void> signUpUser(@NonNull String username, @NonNull String password);

    /**
     * Signs in a user using username-only (dummy email behind the scenes).
     */
    Task<Void> signInUser(@NonNull String username, @NonNull String password);
    /**
     * Checks if a user exists by converting the username into a dummy email
     * and querying Firebase Authentication.
     */
    Task<Boolean> checkUserExists(@NonNull String username);
    /**
     * Changes the password for the user corresponding to the given username.
     */
    Task<Void> changePassword(@NonNull User user, @NonNull String newPassword);

    /**
     * Reset Password of the user before signed in
     */
    Task<Void> resetPassword(@NonNull String username, @NonNull String newPassword);


    /**
     * Changes the username by updating the dummy email (username@example.com) in Firebase Authentication
     * and the user record in Firestore.
     *
     * @param newUsername The new username to be set.
     * @return A Task that completes when both updates are successful.
     */
    Task<Void> changeUsername(@NonNull String newUsername);


    /**
     * Gets the current logged-in User object from firebase.
     */
    Task<User> getCurrentUserProfile();

    /**
     * User A requests to follow User B.
     */
    Task<Void> requestFollow(@NonNull String requesterId, @NonNull String targetId);

    /**
     * Accept a follow request (B accepts A's request),
     * creating a real follow relationship.
     */
    Task<Void> acceptFollowRequest(@NonNull String requesterId, @NonNull String targetId);

    /**
     * Reject a follow request (B rejects A's request),
     */
    Task<Void> rejectFollowRequest(@NonNull String requesterId, @NonNull String targetId);

    Task<Void> unfollowUser(@NonNull String followerId, @NonNull String followedId);

    /**
     * Search user functionality
     */
    Task<List<User>> searchUsers(@NonNull String query);

    /**
     * get user by user name
     */
    Task<User> getUserByUsername(@NonNull String username);
}
