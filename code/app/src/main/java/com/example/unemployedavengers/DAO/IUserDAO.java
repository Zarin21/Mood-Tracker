/**
 * IUserDAO - Data Access Object (DAO) interface for user-related operations.
 *
 * This interface defines methods for user authentication, profile management,
 * follow system interactions, and user search functionality using Firebase Authentication
 * and Firestore as the backend database.
 *
 * Features:
 * - User registration and authentication
 * - Password and username management
 * - Follow/unfollow functionality
 * - Searching for users
 * - Retrieving user profiles
 *
 * Implementing classes should provide concrete implementations for these methods.
 *
 */

package com.example.unemployedavengers.DAO;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.example.unemployedavengers.models.User;
import java.util.List;

/**
 * Interface defining user-related operations for Firebase Authentication and Firestore.
 */
public interface IUserDAO {

    /**
     * Registers a new user with a username (converted to a dummy email) and password.
     *
     * @param username The desired username for the user.
     * @param password The password to set for the user.
     * @return A {@link Task<Void>} indicating the success or failure of the sign-up process.
     */
    Task<Void> signUpUser(@NonNull String username, @NonNull String password);

    /**
     * Signs in a user using their username and password.
     *
     * @param username The username of the user.
     * @param password The password associated with the user.
     * @return A {@link Task<Void>} indicating the success or failure of the sign-in process.
     */
    Task<Void> signInUser(@NonNull String username, @NonNull String password);

    /**
     * Checks if a user exists in Firestore by querying the database with the given username.
     *
     * @param username The username to check for existence.
     * @return A {@link Task<Boolean>} that returns {@code true} if the user exists, {@code false} otherwise.
     */
    Task<Boolean> checkUserExists(@NonNull String username);

    /**
     * Changes the password for the specified user.
     *
     * @param user The {@link User} object representing the user whose password is being changed.
     * @param newPassword The new password to set.
     * @return A {@link Task<Void>} indicating the success or failure of the password change.
     */
    Task<Void> changePassword(@NonNull User user, @NonNull String newPassword);

    /**
     * Resets the password for a user before they have signed in.
     *
     * @param username The username of the user whose password needs to be reset.
     * @param newPassword The new password to set.
     * @return A {@link Task<Void>} indicating the success or failure of the password reset.
     */
    Task<Void> resetPassword(@NonNull String username, @NonNull String newPassword);

    /**
     * Changes the username of the current user by updating their dummy email and profile information.
     *
     * @param newUsername The new username to be set.
     * @return A {@link Task<Void>} indicating the success or failure of the username change.
     */
    Task<Void> changeUsername(@NonNull String newUsername);

    /**
     * Retrieves the current logged-in user's profile from Firestore.
     *
     * @return A {@link Task<User>} containing the user's profile data.
     */
    Task<User> getCurrentUserProfile();

    /**
     * Sends a follow request from one user to another.
     *
     * @param requesterId The user ID of the user sending the follow request.
     * @param targetId The user ID of the target user receiving the follow request.
     * @return A {@link Task<Void>} indicating the success or failure of the request operation.
     */
    Task<Void> requestFollow(@NonNull String requesterId, @NonNull String targetId);

    /**
     * Accepts a follow request, establishing a follower/following relationship between two users.
     *
     * @param requesterId The user ID of the user who sent the follow request.
     * @param targetId The user ID of the user accepting the follow request.
     * @return A {@link Task<Void>} indicating the success or failure of the operation.
     */
    Task<Void> acceptFollowRequest(@NonNull String requesterId, @NonNull String targetId);

    /**
     * Rejects a follow request by deleting it from Firestore.
     *
     * @param requesterId The user ID of the user who sent the follow request.
     * @param targetId The user ID of the user rejecting the follow request.
     * @return A {@link Task<Void>} indicating the success or failure of the rejection process.
     */
    Task<Void> rejectFollowRequest(@NonNull String requesterId, @NonNull String targetId);

    /**
     * Unfollows a user by removing the follow relationship from Firestore.
     *
     * @param followerId The user ID of the user who wants to unfollow.
     * @param followedId The user ID of the user being unfollowed.
     * @return A {@link Task<Void>} indicating the success or failure of the unfollow operation.
     */
    Task<Void> unfollowUser(@NonNull String followerId, @NonNull String followedId);

    /**
     * Searches for users in Firestore based on a search query.
     *
     * @param query The search term to find matching usernames.
     * @return A {@link Task<List<User>>} containing a list of matching users.
     */
    Task<List<User>> searchUsers(@NonNull String query);

    /**
     * Retrieves a user from Firestore based on their username.
     *
     * @param username The username of the user to be retrieved.
     * @return A {@link Task<User>} containing the user data if found, or {@code null} if no user exists.
     */
    Task<User> getUserByUsername(@NonNull String username);

    /**
     * Updates the avatar URL of the currently logged-in user in Firestore.
     *
     * @param avatarUrl The new avatar URL to be set.
     * @return A {@link Task<Void>} indicating the success or failure of the update operation.
     */
    Task<Void> updateUserAvatar(@NonNull String avatarUrl);
}
