package com.example.unemployedavengers.DAO;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

import com.example.unemployedavengers.models.User;
import com.google.android.gms.tasks.Task;

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
     *
     * @param username The username to check.
     * @return A Task that resolves to true if the user exists, or false otherwise.
     */
    Task<Boolean> checkUserExists(@NonNull String username);
    /**
     * Changes the password for the user corresponding to the given username.
     *
     * @param user    The current signed in user
     * @param newPassword The new password to set.
     * @return A Task that completes when the password has been successfully updated.
     */
    Task<Void> changePassword(@NonNull User user, @NonNull String newPassword);
    Task<Void> resetPassword(@NonNull String username, @NonNull String newPassword);

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
}
