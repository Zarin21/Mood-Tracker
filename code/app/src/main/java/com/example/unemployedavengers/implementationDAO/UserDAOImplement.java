package com.example.unemployedavengers.implementationDAO;

import androidx.annotation.NonNull;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAOImplement implements IUserDAO {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public UserDAOImplement() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public Task<Void> signUpUser(@NonNull final String username, @NonNull final String password) {
        final String dummyEmail = username.toLowerCase() + "@example.com";

        // Create user with dummyEmail and password
        return auth.createUserWithEmailAndPassword(dummyEmail, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        // If this fails, maybe username is taken, or password is invalid
                        throw task.getException();
                    }

                    // Retrieve the current user
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        throw new Exception("User creation failed: no FirebaseUser returned.");
                    }

                    final String userId = firebaseUser.getUid();
                    // Create a User object
                    User user = new User(userId, username, dummyEmail,password);

                    // Store user in Firestore
                    DocumentReference userDoc = db.collection("users").document(userId);
                    return userDoc.set(user);
                });
    }

    @Override
    public Task<Void> signInUser(@NonNull final String username, @NonNull final String password) {
        // Reconstruct the dummy email
        final String dummyEmail = username.toLowerCase() + "@example.com";
        // Sign in with email/password
        return auth.signInWithEmailAndPassword(dummyEmail, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return Tasks.forResult(null);
                });
    }
    @Override
    public Task<Boolean> checkUserExists(@NonNull String username) {
        // Query the database to check if user exist
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw task.getException() != null
                                ? task.getException()
                                : new Exception("Error fetching user record.");
                    }
                    // If the query returns at least one document, the user exists.
                    return !task.getResult().isEmpty();
                });
    }
    public Task<Void> changePassword(@NonNull User user, @NonNull String newPassword) {
        final String dummyEmail = user.getDummyEmail();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        if (!dummyEmail.equals(currentUser.getEmail())) {
            return Tasks.forException(new Exception("Authenticated user does not match the username provided"));
        }

        // Update the password in Firebase Authentication.
        return currentUser.updatePassword(newPassword)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Update the password in firebase
                    String uid = currentUser.getUid();
                    DocumentReference userDoc = db.collection("users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("password", newPassword);
                    return userDoc.update(updates);
                });
    }

    @Override
    public Task<Void> resetPassword(@NonNull String username, @NonNull String newPassword) {
        final String dummyEmail = username.toLowerCase() + "@example.com";

        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                        throw new Exception("User record not found in Firestore.");
                    }
                    // get password
                    String storedPassword = task.getResult().getDocuments().get(0).getString("password");
                    if (storedPassword == null) {
                        throw new Exception("Stored password not found.");
                    }

                    // sign in first to change password
                    return auth.signInWithEmailAndPassword(dummyEmail, storedPassword);
                })
                .continueWithTask(getUserTask -> getCurrentUserProfile())
                .continueWithTask(profileTask -> {
                    User currentUser = profileTask.getResult();
                    if (currentUser == null) {
                        throw new Exception("Failed to retrieve current user profile.");
                    }

                    return changePassword(currentUser, newPassword);
                });
    }
    @Override
    public Task<User> getCurrentUserProfile() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            return Tasks.forException(new Exception("No user signed in"));
        }
        String userId = firebaseUser.getUid();
        DocumentReference userDoc = db.collection("users").document(userId);

        // Fetch from Firestore and convert to a User
        return userDoc.get().continueWith(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw new Exception("Failed to fetch user profile");
            }
            return task.getResult().toObject(User.class);
        });
    }

    @Override
    public Task<Void> changeUsername(@NonNull String newUsername) {
        final String newDummyEmail = newUsername.toLowerCase() + "@example.com";
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return Tasks.forException(new Exception("No user is signed in."));
        }

        return currentUser.updateEmail(newDummyEmail)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    String uid = currentUser.getUid();
                    DocumentReference userDoc = db.collection("users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("username", newUsername);
                    updates.put("dummyEmail", newDummyEmail);
                    return userDoc.update(updates);
                });
    }

    @Override
    public Task<Void> requestFollow(@NonNull String requesterId, @NonNull String targetId) {
        DocumentReference requestDocRef = db.collection("users")
                .document(targetId)
                .collection("requests")
                .document(requesterId);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("status", "pending");
        requestData.put("requestedAt", System.currentTimeMillis());
        requestData.put("requesterId", requesterId);

        return requestDocRef.set(requestData);
    }

    @Override
    public Task<Void> acceptFollowRequest(@NonNull String requesterId, @NonNull String targetId) {
        // Delete the follow request document
        DocumentReference requestDocRef = db.collection("users")
                .document(targetId)
                .collection("requests")
                .document(requesterId);

        // Create a document in the requester's "following" subcollection for the target user
        DocumentReference followerFollowingRef = db.collection("users")
                .document(requesterId)
                .collection("following")
                .document(targetId);

        // Create a document in the target user's "followers" subcollection for the requester
        DocumentReference followedFollowersRef = db.collection("users")
                .document(targetId)
                .collection("followers")
                .document(requesterId);

        long timestamp = System.currentTimeMillis();

        Map<String, Object> followingData = new HashMap<>();
        followingData.put("followedId", targetId);
        followingData.put("followedAt", timestamp);

        Map<String, Object> followerData = new HashMap<>();
        followerData.put("followerId", requesterId);
        followerData.put("followedAt", timestamp);

        WriteBatch batch = db.batch();
        batch.delete(requestDocRef);
        batch.set(followerFollowingRef, followingData);
        batch.set(followedFollowersRef, followerData);

        return batch.commit();
    }

    @Override
    public Task<Void> rejectFollowRequest(@NonNull String requesterId, @NonNull String targetId) {
        DocumentReference requestDocRef = db.collection("users")
                .document(targetId)
                .collection("requests")
                .document(requesterId);

        return requestDocRef.delete();
    }

    @Override
    public Task<Void> unfollowUser(@NonNull String followerId, @NonNull String followedId) {
        // Remove the follow relationship in both directions.
        DocumentReference followerFollowingRef = db.collection("users")
                .document(followerId)
                .collection("following")
                .document(followedId);

        DocumentReference followedFollowersRef = db.collection("users")
                .document(followedId)
                .collection("followers")
                .document(followerId);

        WriteBatch batch = db.batch();
        batch.delete(followerFollowingRef);
        batch.delete(followedFollowersRef);

        return batch.commit();
    }



    @Override
    public Task<List<User>> searchUsers(@NonNull String userName) {
        // query users first
        Task<QuerySnapshot> queryTask = db.collection("users")
                .orderBy("username")
                .startAt(userName)
                .endAt(userName + "\uf8ff")
                .get();

        // Retrieve the current user's profile.
        Task<User> currentUserTask = getCurrentUserProfile();

        // When all success
        return Tasks.whenAllSuccess(queryTask, currentUserTask)
                .continueWith(task -> {
                    // 2 tasks in one list and extract
                    List<Object> results = task.getResult();
                    QuerySnapshot querySnapshot = (QuerySnapshot) results.get(0);
                    User currentUser = (User) results.get(1);
                    String currentUid = currentUser.getUserId();

                    // logic to get right ones
                    List<User> userList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null && !user.getUserId().equals(currentUid)) {
                            userList.add(user);
                        }
                    }
                    return userList;
                });
    }
    @Override
    public Task<User> getUserByUsername(@NonNull String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                        return null;
                    }
                    return task.getResult().getDocuments().get(0).toObject(User.class);
                });
    }


}
