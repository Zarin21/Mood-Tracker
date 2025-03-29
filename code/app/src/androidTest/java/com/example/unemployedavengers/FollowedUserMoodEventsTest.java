package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI test for US 05.03.01:
 * As a participant, I want to view as a list the 3 most recent mood events of the participants
 * I am granted to follow, sorted by date and time, in reverse chronological order
 * (most recent coming first).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowedUserMoodEventsTest {

    private static final String TAG = "FollowedUserMoodTest";

    // Test user credentials - user who will be followed
    private static final String FOLLOWED_USERNAME = "moodcreator";
    private static final String FOLLOWED_PASSWORD = "password123";
    private static final String FOLLOWED_EMAIL = "moodcreator@example.com";
    private static String followedUserId;

    // Test user credentials - user who will follow and view moods
    private static final String FOLLOWER_USERNAME = "moodviewer";
    private static final String FOLLOWER_PASSWORD = "password123";
    private static final String FOLLOWER_EMAIL = "moodviewer@example.com";
    private static String followerUserId;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupEmulators() {
        String androidLocalhost = "10.0.2.2";
        int firestorePort = 8080;
        int authPort = 9099;

        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, firestorePort);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);
    }

    @Before
    public void setUp() throws InterruptedException {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        auth.signOut(); // Ensure no user is signed in before test

        Log.d(TAG, "Setting up test users and data");

        // Set up test users and data
        setupTestUsers();
        createFollowingRelationship();
        createSampleMoods();

        Log.d(TAG, "Test setup completed successfully");
    }

    private void setupTestUsers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);

        // Create mood creator (followed user)
        createTestUser(FOLLOWED_EMAIL, FOLLOWED_PASSWORD, FOLLOWED_USERNAME, userID -> {
            followedUserId = userID;
            Log.d(TAG, "Created followed user with ID: " + followedUserId);
            latch.countDown();
        });

        // Create mood viewer (follower user)
        createTestUser(FOLLOWER_EMAIL, FOLLOWER_PASSWORD, FOLLOWER_USERNAME, userID -> {
            followerUserId = userID;
            Log.d(TAG, "Created follower user with ID: " + followerUserId);
            latch.countDown();
        });

        // Wait for both users to be created
        boolean usersCreated = latch.await(30, TimeUnit.SECONDS);
        if (!usersCreated) {
            throw new RuntimeException("Failed to create test users within timeout");
        }
    }

    private void createTestUser(String email, String password, String username, UserCreatedCallback callback) {
        // Check if user already exists
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // User exists, get ID and sign out
                    String userId = authResult.getUser().getUid();
                    auth.signOut();
                    callback.onUserCreated(userId);
                })
                .addOnFailureListener(e -> {
                    // User doesn't exist, create new user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                String userId = authResult.getUser().getUid();

                                // Add user to Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("userId", userId);
                                userData.put("username", username);
                                userData.put("dummyEmail", email);
                                userData.put("password", password);

                                db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Created user " + username + " with ID " + userId);
                                            auth.signOut();
                                            callback.onUserCreated(userId);
                                        })
                                        .addOnFailureListener(firestoreError -> {
                                            Log.e(TAG, "Error creating user in Firestore", firestoreError);
                                            callback.onUserCreated(userId); // Still return the ID even if Firestore failed
                                        });
                            })
                            .addOnFailureListener(createError -> {
                                Log.e(TAG, "Error creating user " + username, createError);
                                throw new RuntimeException("Failed to create test user: " + createError.getMessage());
                            });
                });
    }

    private void createFollowingRelationship() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // Create following relationship - add follower to followed user's followers collection
        Map<String, Object> followerData = new HashMap<>();
        followerData.put("followerId", followerUserId);
        followerData.put("followedAt", System.currentTimeMillis());

        db.collection("users").document(followedUserId)
                .collection("followers").document(followerUserId)
                .set(followerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Added follower relationship in followed user's document");

                    // Add followed user to follower's following collection
                    Map<String, Object> followingData = new HashMap<>();
                    followingData.put("followedId", followedUserId);
                    followingData.put("followedAt", System.currentTimeMillis());

                    db.collection("users").document(followerUserId)
                            .collection("following").document(followedUserId)
                            .set(followingData)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Added following relationship in follower's document");
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating following relationship", e);
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating follower relationship", e);
                    latch.countDown();
                });

        boolean relationshipCreated = latch.await(30, TimeUnit.SECONDS);
        if (!relationshipCreated) {
            throw new RuntimeException("Failed to create following relationship within timeout");
        }
    }

    private void createSampleMoods() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final int totalMoods = 5; // Creating 5 moods (more than the 3 that should be shown)
        final long baseTime = System.currentTimeMillis();

        // Create mood events with descending timestamps (ensure proper order for test verification)
        for (int i = 0; i < totalMoods; i++) {
            String moodId = "test_mood_" + i;
            String moodName = getMoodNameForIndex(i);
            long timestamp = baseTime - (i * 3600000); // Each mood 1 hour apart

            Map<String, Object> moodData = new HashMap<>();
            moodData.put("id", moodId);
            moodData.put("mood", moodName);
            moodData.put("reason", "Test mood " + (i+1));
            moodData.put("time", timestamp);
            moodData.put("userId", followedUserId);
            moodData.put("userName", FOLLOWED_USERNAME);
            moodData.put("publicStatus", true); // All moods are public
            moodData.put("radioSituation", "Alone");
            moodData.put("situation", "Testing");
            moodData.put("hasLocation", false);

            int finalI = i;
            db.collection("users").document(followedUserId)
                    .collection("moods").document(moodId)
                    .set(moodData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Created mood " + (finalI+1) + " with timestamp " + timestamp);
                        if (finalI == totalMoods - 1) {
                            latch.countDown();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating mood " + (finalI+1), e);
                        if (finalI == totalMoods - 1) {
                            latch.countDown();
                        }
                    });
        }

        boolean moodsCreated = latch.await(30, TimeUnit.SECONDS);
        if (!moodsCreated) {
            throw new RuntimeException("Failed to create sample moods within timeout");
        }
    }

    private String getMoodNameForIndex(int index) {
        // Different moods for each index to easily identify them
        switch (index) {
            case 0: return "😄Happiness"; // Most recent
            case 1: return "😔Sadness";
            case 2: return "😠Anger";
            case 3: return "😯Surprise";
            default: return "😨Fear";     // Oldest
        }
    }

    @Test
    public void testFollowedUserMoodEvents() {
        // Login with follower user who will view the moods
        login(FOLLOWER_USERNAME, FOLLOWER_PASSWORD);

        // Wait for dashboard to load
        SystemClock.sleep(3000);

        // Navigate to Following screen using bottom navigation
        onView(withId(R.id.friendsHistoryFragment)).perform(click());
        SystemClock.sleep(2000);

        // Check that we're on the Following screen
        onView(withId(R.id.friends_list_title)).check(matches(isDisplayed()));

        // Click on the "Friends History Mood" button
        onView(withId(R.id.friends_history_mood_button)).perform(click());
        SystemClock.sleep(2000);

        // Verify Following Mood History title is displayed
        onView(withId(R.id.tvFriendsMoodTitle)).check(matches(withText("Following Mood History")));

        // Verify that the ListView containing mood events is visible
        onView(withId(R.id.followedUsersListView)).check(matches(isDisplayed()));

        // Check first mood (most recent) - should be Happiness with emoji
        onData(anything())
                .inAdapterView(withId(R.id.followedUsersListView))
                .atPosition(0)
                .onChildView(withId(R.id.mood_text))
                .check(matches(withText("😄Happiness")));

        // Check second mood - should be Sadness with emoji
        onData(anything())
                .inAdapterView(withId(R.id.followedUsersListView))
                .atPosition(1)
                .onChildView(withId(R.id.mood_text))
                .check(matches(withText("😔Sadness")));

        // Check third mood - should be Anger with emoji
        onData(anything())
                .inAdapterView(withId(R.id.followedUsersListView))
                .atPosition(2)
                .onChildView(withId(R.id.mood_text))
                .check(matches(withText("😠Anger")));

        // Verify username is displayed in each mood event
        onData(anything())
                .inAdapterView(withId(R.id.followedUsersListView))
                .atPosition(0)
                .onChildView(withId(R.id.usernameText))
                .check(matches(withText(FOLLOWED_USERNAME)));
    }

    private void login(String username, String password) {
        // Navigate to login screen if not already there
        try {
            // Click on login from start screen
            onView(withId(R.id.tvStartLogin)).perform(click());
        } catch (Exception e) {
            // May already be on login screen or need to go back
            Log.d(TAG, "Could not find login link, may already be on login screen");
        }

        // Enter login credentials
        onView(withId(R.id.etUsername)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText(password), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btnLogin)).perform(click());

        // Handle location permissions if needed
        CustomMatchers.handleLocationPermissionPopup();

        // Wait for login to complete
        SystemClock.sleep(5000);
    }

    @After
    public void tearDown() {
        Log.d(TAG, "Starting test cleanup");

        // Clean up test data
        if (followedUserId != null) {
            deleteUserData(followedUserId, FOLLOWED_EMAIL, FOLLOWED_PASSWORD);
        }

        if (followerUserId != null) {
            deleteUserData(followerUserId, FOLLOWER_EMAIL, FOLLOWER_PASSWORD);
        }

        // Sign out
        auth.signOut();
    }

    private void deleteUserData(String userId, String email, String password) {
        try {
            // Sign in as the user to delete
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // Delete mood data
                        db.collection("users").document(userId).collection("moods")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        document.getReference().delete();
                                    }

                                    // Delete followers collection
                                    db.collection("users").document(userId).collection("followers")
                                            .get()
                                            .addOnSuccessListener(followersSnapshot -> {
                                                for (QueryDocumentSnapshot document : followersSnapshot) {
                                                    document.getReference().delete();
                                                }

                                                // Delete following collection
                                                db.collection("users").document(userId).collection("following")
                                                        .get()
                                                        .addOnSuccessListener(followingSnapshot -> {
                                                            for (QueryDocumentSnapshot document : followingSnapshot) {
                                                                document.getReference().delete();
                                                            }

                                                            // Delete user document
                                                            db.collection("users").document(userId)
                                                                    .delete()
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        // Delete auth user
                                                                        FirebaseUser user = auth.getCurrentUser();
                                                                        if (user != null) {
                                                                            user.delete().addOnCompleteListener(task -> {
                                                                                Log.d(TAG, "Deleted user " + email);
                                                                            });
                                                                        }
                                                                    });
                                                        });
                                            });
                                });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to sign in as " + email + " for deletion", e));
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteUserData", e);
        }
    }

    private interface UserCreatedCallback {
        void onUserCreated(String userId);
    }
}