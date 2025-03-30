/**
 * MoodCommentsViewTest.java
 *
 * This class contains instrumented UI tests for the mood commenting functionality in the Unemployed Avengers app.
 * It tests user stories related to posting and viewing comments on mood events (US 05.07.01 and US 05.07.02).
 *
 * The test follows a three-user scenario:
 * 1. A mood poster creates a public mood event
 * 2. A comment user views the mood and posts a comment
 * 3. A viewer user views the mood and verifies the comment is visible
 *
 * Key Features Tested:
 * - Comment creation and submission
 * - Comment visibility to other users
 * - Integration with Firebase Authentication and Firestore
 * - Following relationships between users
 * Note: This test requires Firebase emulators to be running locally.
 */
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

import androidx.test.espresso.Espresso;
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
 * UI test for:
 * US 05.07.01: As a participant, I want to be able to comment on a specific mood event.
 * US 05.07.02: As a participant, I want to view all comments on a given mood event.
 *
 * This test involves three users:
 * 1. MOOD_POSTER: Posts a mood
 * 2. COMMENT_USER: Views the mood and posts a comment
 * 3. VIEWER_USER: Views the mood and sees the comment made by COMMENT_USER
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodCommentsViewTest {

    private static final String TAG = "MoodCommentsViewTest";

    // User 1 - Posts the mood
    private static final String MOOD_POSTER_USERNAME = "moodposter";
    private static final String MOOD_POSTER_PASSWORD = "password123";
    private static final String MOOD_POSTER_EMAIL = "moodposter@example.com";
    private static String moodPosterId;

    // User 2 - Views the mood and posts a comment
    private static final String COMMENT_USER_USERNAME = "commentuser";
    private static final String COMMENT_USER_PASSWORD = "password123";
    private static final String COMMENT_USER_EMAIL = "commentuser@example.com";
    private static String commentUserId;

    // User 3 - Views the mood and sees the comment
    private static final String VIEWER_USER_USERNAME = "vieweruser";
    private static final String VIEWER_USER_PASSWORD = "password123";
    private static final String VIEWER_USER_EMAIL = "vieweruser@example.com";
    private static String viewerUserId;

    // Sample mood event and comment content
    private static final String MOOD_ID = "test_mood_comment";
    private static final String MOOD_NAME = "😄Happiness";
    private static final String MOOD_REASON = "Feeling great today!";
    private static final String COMMENT_TEXT = "Nice mood! Hope you have a great day!";

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
        createFollowingRelationships();
        createSampleMood();

        Log.d(TAG, "Test setup completed successfully");
    }

    private void setupTestUsers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);

        Log.d(TAG, "Creating test users...");

        // Create mood poster user
        createTestUser(MOOD_POSTER_EMAIL, MOOD_POSTER_PASSWORD, MOOD_POSTER_USERNAME, userID -> {
            moodPosterId = userID;
            Log.d(TAG, "Created mood poster user with ID: " + moodPosterId);
            latch.countDown();
        });

        // Create comment user
        createTestUser(COMMENT_USER_EMAIL, COMMENT_USER_PASSWORD, COMMENT_USER_USERNAME, userID -> {
            commentUserId = userID;
            Log.d(TAG, "Created comment user with ID: " + commentUserId);
            latch.countDown();
        });

        // Create viewer user
        createTestUser(VIEWER_USER_EMAIL, VIEWER_USER_PASSWORD, VIEWER_USER_USERNAME, userID -> {
            viewerUserId = userID;
            Log.d(TAG, "Created viewer user with ID: " + viewerUserId);
            latch.countDown();
        });

        // Wait for all users to be created with a timeout (60 seconds)
        boolean usersCreated = latch.await(60, TimeUnit.SECONDS);
        if (!usersCreated) {
            Log.e(TAG, "Failed to create test users within timeout");
            throw new RuntimeException("Failed to create test users within timeout");
        }

        Log.d(TAG, "All test users created successfully");
    }

    private void createTestUser(String email, String password, String username, UserCreatedCallback callback) {
        Log.d(TAG, "Creating user: " + username + " with email: " + email);

        // Check if user already exists
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // User exists, get ID and sign out
                    String userId = authResult.getUser().getUid();
                    Log.d(TAG, "User " + username + " already exists with ID: " + userId);
                    auth.signOut();
                    callback.onUserCreated(userId);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "User " + username + " doesn't exist, creating new user");
                    // User doesn't exist, create new user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                String userId = authResult.getUser().getUid();
                                Log.d(TAG, "Authentication created for " + username + " with ID: " + userId);

                                // Add user to Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("userId", userId);
                                userData.put("username", username);
                                userData.put("dummyEmail", email);
                                userData.put("password", password);
                                userData.put("avatar", ""); // Add empty avatar field

                                db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Firestore entry created for user " + username + " with ID " + userId);
                                            auth.signOut();
                                            callback.onUserCreated(userId);
                                        })
                                        .addOnFailureListener(firestoreError -> {
                                            Log.e(TAG, "Error creating user " + username + " in Firestore", firestoreError);
                                            callback.onUserCreated(userId); // Still return the ID even if Firestore failed
                                        });
                            })
                            .addOnFailureListener(createError -> {
                                Log.e(TAG, "Error creating user " + username + " in Authentication", createError);
                                // Don't throw an exception here, just log the error and continue
                                // This will cause the test to time out if user creation fails
                            });
                });
    }

    private void createFollowingRelationships() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);

        Log.d(TAG, "Creating following relationships");

        // Both comment user and viewer user follow the mood poster

        // 1. Comment User follows Mood Poster
        addFollowingRelationship(commentUserId, moodPosterId, latch);

        // 2. Viewer User follows Mood Poster
        addFollowingRelationship(viewerUserId, moodPosterId, latch);

        boolean relationshipsCreated = latch.await(60, TimeUnit.SECONDS); // Increased timeout
        if (!relationshipsCreated) {
            Log.e(TAG, "Failed to create following relationships within timeout");
            throw new RuntimeException("Failed to create following relationships within timeout");
        }

        Log.d(TAG, "Following relationships created successfully");
    }

    private void addFollowingRelationship(String followerId, String followedId, CountDownLatch latch) {
        // Add follower to followed user's followers collection
        Map<String, Object> followerData = new HashMap<>();
        followerData.put("followerId", followerId);
        followerData.put("followedAt", System.currentTimeMillis());

        db.collection("users").document(followedId)
                .collection("followers").document(followerId)
                .set(followerData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Added follower relationship: " + followerId + " follows " + followedId);
                    latch.countDown();

                    // Add followed user to follower's following collection
                    Map<String, Object> followingData = new HashMap<>();
                    followingData.put("followedId", followedId);
                    followingData.put("followedAt", System.currentTimeMillis());

                    db.collection("users").document(followerId)
                            .collection("following").document(followedId)
                            .set(followingData)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Added following relationship: " + followerId + " follows " + followedId);
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
                    latch.countDown(); // Count down twice since we won't create the second relationship
                });
    }

    private void createSampleMood() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Log.d(TAG, "Creating sample mood event for user: " + moodPosterId);

        // Create a sample mood event for the first user
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("id", MOOD_ID);
        moodData.put("mood", MOOD_NAME);
        moodData.put("reason", MOOD_REASON);
        moodData.put("time", System.currentTimeMillis());
        moodData.put("userId", moodPosterId);
        moodData.put("userName", MOOD_POSTER_USERNAME);
        moodData.put("publicStatus", true); // Must be public to be seen
        moodData.put("radioSituation", "Alone");
        moodData.put("situation", "Testing");
        moodData.put("hasLocation", false);
        moodData.put("existed", true); // Ensure the mood is marked as existing

        // First sign in as the mood poster to ensure we have proper permissions
        auth.signInWithEmailAndPassword(MOOD_POSTER_EMAIL, MOOD_POSTER_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Signed in as mood poster to create mood event");

                    // Now add the mood to their collection
                    db.collection("users").document(moodPosterId)
                            .collection("moods").document(MOOD_ID)
                            .set(moodData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Successfully created mood event with ID: " + MOOD_ID);

                                // Verify the mood was added properly
                                db.collection("users").document(moodPosterId)
                                        .collection("moods").document(MOOD_ID)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                Log.d(TAG, "Verified mood event exists with data: " + documentSnapshot.getData());
                                                auth.signOut();
                                                latch.countDown();
                                            } else {
                                                Log.e(TAG, "Mood event was not found after creation");
                                                auth.signOut();
                                                latch.countDown();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error verifying mood event", e);
                                            auth.signOut();
                                            latch.countDown();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating mood event", e);
                                auth.signOut();
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sign in as mood poster to create mood", e);
                    latch.countDown();
                });

        boolean moodCreated = latch.await(60, TimeUnit.SECONDS); // Increased timeout
        if (!moodCreated) {
            Log.e(TAG, "Failed to create sample mood within timeout");
            throw new RuntimeException("Failed to create sample mood within timeout");
        }

        Log.d(TAG, "Sample mood creation completed");
    }

    @Test
    public void testMoodCommentAndView() {
        try {
            // Part 1: Second user posts a comment on the mood
            loginAndPostComment();

            // Successfully posted comment, log out
            logOut();
            SystemClock.sleep(2000);

            // Part 2: Third user views the mood and sees the comment
            loginAndViewComment();

        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception: " + e.getMessage(), e);
            throw e;
        }
    }

    private void loginAndPostComment() {
        Log.d(TAG, "Login as comment user and post a comment");

        // Login with the comment user
        login(COMMENT_USER_USERNAME, COMMENT_USER_PASSWORD);

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

        // Wait longer for the data to load and ensure the list is visible
        SystemClock.sleep(5000);

        // Check if the list has items by looking for the ListView itself rather than the empty state
        try {
            // Check if the ListView is visible
            onView(withId(R.id.followedUsersListView)).check(matches(isDisplayed()));
            Log.d(TAG, "Found ListView - checking for mood items");

            // Try to click on the first mood
            try {
                onData(anything())
                        .inAdapterView(withId(R.id.followedUsersListView))
                        .atPosition(0)
                        .perform(click());
                Log.d(TAG, "Successfully clicked on the mood at position 0");
            } catch (Exception e) {
                Log.e(TAG, "Failed to click on mood: " + e.getMessage());

                // Instead of failing, check if emptyStateMessage is visible
                try {
                    onView(withId(R.id.emptyStateMessage)).check(matches(isDisplayed()));
                    Log.d(TAG, "Empty state message is displayed - no moods found");
                    throw new RuntimeException("No mood events found for the followed user. Test cannot continue.");
                } catch (Exception emptyCheck) {
                    // If neither works, the data might still be loading
                    Log.e(TAG, "Neither mood list items nor empty state message found. Data may still be loading.");
                    throw new RuntimeException("Could not click on mood and empty state not visible. ListView may not be properly populated.", e);
                }
            }
        } catch (Exception listCheck) {
            Log.e(TAG, "ListView not visible: " + listCheck.getMessage());
            throw new RuntimeException("ListView not visible or accessible", listCheck);
        }

        SystemClock.sleep(2000);

        // Verify we're on the mood detail page
        onView(withId(R.id.tvMoodUsername)).check(matches(withText(MOOD_POSTER_USERNAME)));
        onView(withId(R.id.tvMoodType)).check(matches(withText(MOOD_NAME)));

        // Wait a bit for the comment section to fully load
        SystemClock.sleep(2000);

        // Enter a comment
        try {
            onView(withId(R.id.comment_input)).perform(typeText(COMMENT_TEXT), closeSoftKeyboard());
            Log.d(TAG, "Successfully entered comment text");
        } catch (Exception e) {
            Log.e(TAG, "Failed to enter comment text: " + e.getMessage());
            throw new RuntimeException("Could not enter comment text. Comment input may not be visible.", e);
        }

        // Submit the comment
        try {
            onView(withId(R.id.submit_comment)).perform(click());
            Log.d(TAG, "Successfully clicked submit comment button");
        } catch (Exception e) {
            Log.e(TAG, "Failed to click submit comment button: " + e.getMessage());
            throw new RuntimeException("Could not click submit comment button. Button may not be visible.", e);
        }

        // Wait longer for the comment to be processed and displayed
        SystemClock.sleep(5000);

        // Verify the comment is displayed
        onView(withId(R.id.comment_username)).check(matches(withText(COMMENT_USER_USERNAME)));
        onView(withId(R.id.comment_content)).check(matches(withText(COMMENT_TEXT)));

        // Go back to login page (sign out)
        onView(withId(R.id.btnBack)).perform(click());
        SystemClock.sleep(1000);
        logOut();
    }

    private void loginAndViewComment() {
        Log.d(TAG, "Login as viewer user and check the comment");

        // Login with the viewer user
        login(VIEWER_USER_USERNAME, VIEWER_USER_PASSWORD);

        // Wait for dashboard to load
        SystemClock.sleep(3000);

        // Navigate to Following screen using bottom navigation
        onView(withId(R.id.friendsHistoryFragment)).perform(click());
        SystemClock.sleep(2000);

        // Click on the "Friends History Mood" button
        onView(withId(R.id.friends_history_mood_button)).perform(click());
        SystemClock.sleep(2000);

        // Wait longer for the data to load and ensure the list is visible
        SystemClock.sleep(5000);

        // Try to click on the first mood
        try {
            onData(anything())
                    .inAdapterView(withId(R.id.followedUsersListView))
                    .atPosition(0)
                    .perform(click());
            Log.d(TAG, "Successfully clicked on the mood at position 0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to click on mood: " + e.getMessage());
            throw new RuntimeException("Could not click on mood. ListView may be empty or not visible.", e);
        }
        SystemClock.sleep(2000);

        // Verify the mood details are correct
        onView(withId(R.id.tvMoodUsername)).check(matches(withText(MOOD_POSTER_USERNAME)));
        onView(withId(R.id.tvMoodType)).check(matches(withText(MOOD_NAME)));

        // Wait for comments to load
        SystemClock.sleep(3000);

        // Verify the comment from the second user is displayed
        try {
            onView(withId(R.id.comment_username)).check(matches(withText(COMMENT_USER_USERNAME)));
            Log.d(TAG, "Found comment username");

            onView(withId(R.id.comment_content)).check(matches(withText(COMMENT_TEXT)));
            Log.d(TAG, "Found comment content");
        } catch (Exception e) {
            Log.e(TAG, "Failed to verify comment is displayed: " + e.getMessage());
            throw new RuntimeException("Comment not visible to third user", e);
        }

        // Take a screenshot if needed
        // Espresso.takeScreenshot("viewer_sees_comment");

        // Log out
        onView(withId(R.id.btnBack)).perform(click());
        SystemClock.sleep(1000);
        logOut();
    }

    private void login(String username, String password) {
        Log.d(TAG, "Logging in as " + username);

        // Sign out from Firebase first to ensure we're starting fresh
        auth.signOut();
        SystemClock.sleep(1000);

        try {
            // Navigate to login screen if not already there

            // First check if we're already on the login screen
            try {
                onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
                Log.d(TAG, "Already on login screen");
            } catch (Exception e) {
                Log.d(TAG, "Not on login screen, looking for start screen");

                // Try to find the start screen login link
                try {
                    onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                    Log.d(TAG, "Found start screen, clicking login link");
                    onView(withId(R.id.tvStartLogin)).perform(click());
                    SystemClock.sleep(1000);
                } catch (Exception e2) {
                    Log.d(TAG, "Not on start screen either, attempting to navigate back");

                    // Try to navigate back to start screen
                    try {
                        Espresso.pressBack();
                        SystemClock.sleep(1000);
                        Espresso.pressBack();
                        SystemClock.sleep(1000);
                    } catch (Exception e3) {
                        Log.d(TAG, "Back navigation failed, attempting to restart activity");
                    }

                    // As a last resort, recreate the activity
                    activityScenarioRule.getScenario().recreate();
                    SystemClock.sleep(3000);

                    try {
                        onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                        Log.d(TAG, "Found start screen after recreation, clicking login link");
                        onView(withId(R.id.tvStartLogin)).perform(click());
                        SystemClock.sleep(1000);
                    } catch (Exception e4) {
                        Log.e(TAG, "Still can't find login link after activity recreation");
                        throw new RuntimeException("Unable to navigate to login screen");
                    }
                }
            }

            // Now we should be on login screen, enter credentials
            try {
                onView(withId(R.id.etUsername)).perform(click(), typeText(username), closeSoftKeyboard());
                Log.d(TAG, "Entered username: " + username);
            } catch (Exception e) {
                Log.e(TAG, "Error entering username: " + e.getMessage());
                throw e;
            }

            SystemClock.sleep(500); // Short pause between fields

            try {
                onView(withId(R.id.etPassword)).perform(click(), typeText(password), closeSoftKeyboard());
                Log.d(TAG, "Entered password");
            } catch (Exception e) {
                Log.e(TAG, "Error entering password: " + e.getMessage());
                throw e;
            }

            SystemClock.sleep(500); // Short pause before clicking button

            // Also directly authenticate with Firebase just to be sure
            auth.signInWithEmailAndPassword(username + "@example.com", password)
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "Firebase auth successful for " + username);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firebase auth failed for " + username + ": " + e.getMessage());
                    });

            // Click login button in UI
            try {
                onView(withId(R.id.btnLogin)).perform(click());
                Log.d(TAG, "Clicked login button");
            } catch (Exception e) {
                Log.e(TAG, "Error clicking login button: " + e.getMessage());
                throw e;
            }

            // Handle location permissions if needed (multiple attempts)
            for (int i = 0; i < 3; i++) {
                try {
                    CustomMatchers.handleLocationPermissionPopup();
                    Log.d(TAG, "Handled location permissions");
                    break;
                } catch (Exception e) {
                    Log.d(TAG, "No location permissions dialog on attempt " + (i+1));
                }
                SystemClock.sleep(1000);
            }

            // Wait longer for login to complete and dashboard to load
            SystemClock.sleep(7000);

            // Verify login was successful by checking for dashboard elements
            try {
                onView(withId(R.id.add_mood_button)).check(matches(isDisplayed()));
                Log.d(TAG, "Login successful - verified dashboard is displayed");
            } catch (Exception e) {
                Log.d(TAG, "Could not verify dashboard, checking other elements");
                try {
                    // Try other dashboard elements
                    onView(withId(R.id.dashboard_text)).check(matches(isDisplayed()));
                    Log.d(TAG, "Login successful - verified dashboard text is displayed");
                } catch (Exception e2) {
                    Log.e(TAG, "Login verification failed, dashboard not displayed");
                    throw new RuntimeException("Login failed - dashboard not displayed");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Login process failed: " + e.getMessage(), e);
            throw new RuntimeException("Login process failed for user " + username, e);
        }
    }

    private void logOut() {
        // Sign out from Firebase
        auth.signOut();

        // Navigate back to start screen
        for (int i = 0; i < 3; i++) {
            try {
                Espresso.pressBack();
                SystemClock.sleep(1000);
            } catch (Exception e) {
                Log.d(TAG, "Error during back navigation: " + e.getMessage());
            }
        }

        // Wait for UI to update
        SystemClock.sleep(2000);
    }

    @After
    public void tearDown() {
        Log.d(TAG, "Starting test cleanup");

        // Clean up test data
        if (moodPosterId != null) {
            deleteUserData(moodPosterId, MOOD_POSTER_EMAIL, MOOD_POSTER_PASSWORD);
        }

        if (commentUserId != null) {
            deleteUserData(commentUserId, COMMENT_USER_EMAIL, COMMENT_USER_PASSWORD);
        }

        if (viewerUserId != null) {
            deleteUserData(viewerUserId, VIEWER_USER_EMAIL, VIEWER_USER_PASSWORD);
        }

        // Cleanup comments collection
        db.collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        document.getReference().delete();
                    }
                    Log.d(TAG, "Deleted all test comments");
                });

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