/**
 * FollowFunctionalityFlowTest - End-to-end test suite for follow request functionality.
 *
 * Design Pattern:
 * - Implements Page Object Model pattern for test organization
 * - Uses Test Data Builder pattern for test user creation
 * - Follows Test Fixture pattern for setup/teardown
 * - Implements Flow Testing pattern for multi-step user scenarios
 *
 * Key Responsibilities:
 * 1. Test Coverage:
 *    - Tests complete follow request flow (US 05.01.01)
 *    - Verifies follow request acceptance (US 05.02.01)
 *    - Validates request visibility (US 05.02.02)
 *
 * 2. Test Infrastructure:
 *    - Manages Firebase emulator configuration
 *    - Handles test user lifecycle (creation/deletion)
 *    - Implements robust login/logout flows
 *    - Provides detailed logging for debugging
 *
 * 3. Test Scenarios:
 *    - test1_SendFollowRequest: Validates request sending
 *    - test2_ViewAndAcceptFollowRequest: Tests request acceptance
 *
 * Technical Implementation:
 * - Uses Espresso for UI testing
 * - Leverages Firebase emulator for isolated testing
 * - Implements CountDownLatch for async operations
 * - Uses JUnit 4 with fixed test ordering
 *
 * Dependencies:
 * - AndroidX Test (Espresso)
 * - Firebase Emulator Suite
 * - JUnit 4
 * - CustomMatchers helper class
 *
 * Lifecycle Notes:
 * - Uses @BeforeClass for one-time setup
 * - @Test methods run in fixed order (@FixMethodOrder)
 * - @AfterClass handles comprehensive cleanup
 * - Each test maintains independent state
 *
 * @see CustomMatchers
 */
package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for Follow Functionality Flow:
 *
 * This test follows a complete user flow:
 * 1. User A sends a follow request to User B
 * 2. User B checks for the request
 * 3. User B accepts the request
 *
 * This covers all user stories:
 * - US 05.01.01: As a participant, I want to ask another participant to follow their most recent mood event profile.
 * - US 05.02.01: As a participant, I want to grant another participant permission to follow my most recent mood event.
 * - US 05.02.02: As a participant, I want to view all users who have requested to follow me.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FollowFunctionalityFlowTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    // User 1 - Will send follow requests and be a follower
    private static final String USER1_USERNAME = "flowsender";
    private static final String USER1_PASSWORD = "123456";
    private static final String USER1_EMAIL = "flowsender@example.com";
    private static String user1Id;

    // User 2 - Will receive follow requests and be followed
    private static final String USER2_USERNAME = "flowreceiver";
    private static final String USER2_PASSWORD = "123456";
    private static final String USER2_EMAIL = "flowreceiver@example.com";
    private static String user2Id;

    private static final String TAG = "FollowFlowTest";

    // Use AtomicBoolean to track if setup is complete
    private static AtomicBoolean setupComplete = new AtomicBoolean(false);

    @BeforeClass
    public static void setup() throws InterruptedException {
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        int authPort = 9099;

        Log.d(TAG, "Starting test setup");

        // Configure Firebase emulators
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        auth.signOut(); // Ensure no user is signed in before test

        // Create both test users
        setupUser1();
        setupUser2();

        // Create a sample mood event for user 2
        createSampleMoodEvent();

        setupComplete.set(true);
        Log.d(TAG, "Test setup complete");
    }

    private static void setupUser1() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        // Try to sign in with test account to see if it exists
        auth.signInWithEmailAndPassword(USER1_EMAIL, USER1_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    // User exists, sign out and proceed
                    user1Id = authResult.getUser().getUid();
                    auth.signOut();
                    success[0] = true;
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    // User doesn't exist, create it
                    createNewTestUser(USER1_EMAIL, USER1_PASSWORD, USER1_USERNAME, latch, success, 1);
                });

        boolean completed = latch.await(30, TimeUnit.SECONDS);

        if (!completed) {
            Log.e(TAG, "Timed out waiting for user 1 setup");
            throw new RuntimeException("Timed out waiting for user 1 setup");
        }

        if (!success[0]) {
            Log.e(TAG, "Failed to set up user 1");
            throw new RuntimeException("Failed to set up user 1");
        }
    }

    private static void setupUser2() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        // Try to sign in with test account to see if it exists
        auth.signInWithEmailAndPassword(USER2_EMAIL, USER2_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    // User exists, sign out and proceed
                    user2Id = authResult.getUser().getUid();
                    auth.signOut();
                    success[0] = true;
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    // User doesn't exist, create it
                    createNewTestUser(USER2_EMAIL, USER2_PASSWORD, USER2_USERNAME, latch, success, 2);
                });

        boolean completed = latch.await(30, TimeUnit.SECONDS);

        if (!completed) {
            Log.e(TAG, "Timed out waiting for user 2 setup");
            throw new RuntimeException("Timed out waiting for user 2 setup");
        }

        if (!success[0]) {
            Log.e(TAG, "Failed to set up user 2");
            throw new RuntimeException("Failed to set up user 2");
        }
    }

    private static void createNewTestUser(String email, String password, String username, CountDownLatch latch, boolean[] success, int userNumber) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();
                        if (userNumber == 1) {
                            user1Id = userId;
                        } else {
                            user2Id = userId;
                        }
                        Log.d(TAG, "Created auth user " + userNumber + " with ID: " + userId);

                        // Add user to Firestore
                        Map<String, Object> testUser = new HashMap<>();
                        testUser.put("userId", userId);
                        testUser.put("username", username);
                        testUser.put("dummyEmail", email);
                        testUser.put("password", password);

                        db.collection("users")
                                .document(userId)
                                .set(testUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Test user " + userNumber + " created successfully in Firestore");
                                    success[0] = true;
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating test user " + userNumber + " in Firestore", e);
                                    latch.countDown();
                                });
                    } else {
                        Log.e(TAG, "Error creating authentication for user " + userNumber, task.getException());
                        latch.countDown();
                    }
                });
    }

    private static void createSampleMoodEvent() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        // Create a sample mood event
        Map<String, Object> moodEvent = new HashMap<>();
        moodEvent.put("mood", "Happiness");
        moodEvent.put("reason", "Sample mood for testing");
        moodEvent.put("time", System.currentTimeMillis());
        moodEvent.put("userId", user2Id);
        moodEvent.put("userName", USER2_USERNAME);
        moodEvent.put("publicStatus", true);
        String moodId = "test_mood_" + System.currentTimeMillis();
        moodEvent.put("id", moodId);

        // Add the mood event to Firestore
        db.collection("users")
                .document(user2Id)
                .collection("moods")
                .document(moodId)
                .set(moodEvent)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sample mood event created successfully");
                    success[0] = true;
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating sample mood event", e);
                    latch.countDown();
                });

        boolean completed = latch.await(30, TimeUnit.SECONDS);

        if (!completed) {
            Log.e(TAG, "Timed out waiting for sample mood event creation");
            throw new RuntimeException("Timed out waiting for sample mood event creation");
        }

        if (!success[0]) {
            Log.e(TAG, "Failed to create sample mood event");
            throw new RuntimeException("Failed to create sample mood event");
        }
    }

    private void login(String username, String password) {
        Log.d(TAG, "Starting login process for " + username);

        try {
            // First check what screen we're on
            boolean onLoginScreen = false;
            boolean onStartScreen = false;

            try {
                // Check if we're already on the login screen
                onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
                onLoginScreen = true;
                Log.d(TAG, "Already on login screen");
            } catch (Exception e) {
                // Not on login screen
                try {
                    // Check if we're on the start screen
                    onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                    onStartScreen = true;
                    Log.d(TAG, "On start screen, navigating to login");
                } catch (Exception e2) {
                    // Not on start screen either
                    Log.d(TAG, "Not on start or login screen, trying to return to start screen");
                    // Try to go back to start screen
                    for (int i = 0; i < 3; i++) {
                        try {
                            androidx.test.espresso.Espresso.pressBack();
                            SystemClock.sleep(1000);
                            // See if we reached start screen
                            onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                            onStartScreen = true;
                            break;
                        } catch (Exception backException) {
                            // Continue trying
                        }
                    }

                    if (!onStartScreen) {
                        // Restart activity as last resort
                        Log.d(TAG, "Restarting activity to reach start screen");
                        activityScenarioRule.getScenario().recreate();
                        SystemClock.sleep(3000);
                        try {
                            onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                            onStartScreen = true;
                        } catch (Exception restartException) {
                            throw new RuntimeException("Failed to reach start screen after restart");
                        }
                    }
                }
            }

            // Navigate to login page if needed
            if (onStartScreen) {
                onView(withId(R.id.tvStartLogin)).perform(click());
                SystemClock.sleep(2000);
            }

            // Now we should be on login screen, enter credentials
            onView(withId(R.id.etUsername)).perform(clearText(), typeText(username), closeSoftKeyboard());
            SystemClock.sleep(500); // Short pause between fields
            onView(withId(R.id.etPassword)).perform(clearText(), typeText(password), closeSoftKeyboard());
            SystemClock.sleep(500); // Short pause before clicking button

            // Click login
            onView(withId(R.id.btnLogin)).perform(click());
            Log.d(TAG, "Clicked login button");

            // Handle location permissions - attempt multiple times with increasing delay
            for (int i = 0; i < 3; i++) {
                SystemClock.sleep(1000 * (i + 1)); // Increasing delay: 1s, 2s, 3s
                try {
                    CustomMatchers.handleLocationPermissionPopup();
                    Log.d(TAG, "Handled location permissions attempt " + (i + 1));
                } catch (Exception e) {
                    Log.d(TAG, "No location permission dialog found on attempt " + (i + 1));
                }
            }

            // Wait for login to complete and for dashboard to load
            SystemClock.sleep(7000); // Increased wait time

            // Debug what's on screen
            Log.d(TAG, "Checking what's on screen after login");

            // Verify we're on the dashboard with several retries
            boolean dashboardFound = false;
            for (int i = 0; i < 5; i++) { // Increased retries
                try {
                    // Try different dashboard elements
                    try {
                        onView(withId(R.id.add_mood_button)).check(matches(isDisplayed()));
                        dashboardFound = true;
                        Log.d(TAG, "Found add_mood_button");
                        break;
                    } catch (Exception e1) {
                        try {
                            onView(withId(R.id.dashboard_text)).check(matches(isDisplayed()));
                            dashboardFound = true;
                            Log.d(TAG, "Found dashboard_text");
                            break;
                        } catch (Exception e2) {
                            try {
                                onView(withId(R.id.activity_list)).check(matches(isDisplayed()));
                                dashboardFound = true;
                                Log.d(TAG, "Found activity_list");
                                break;
                            } catch (Exception e3) {
                                Log.d(TAG, "Couldn't find any dashboard elements on attempt " + (i+1));
                            }
                        }
                    }

                    // If we reach here, we haven't found any dashboard elements yet
                    Log.d(TAG, "Dashboard not loaded yet, retrying... (attempt " + (i+1) + ")");
                    SystemClock.sleep(2000);
                } catch (Exception e) {
                    Log.e(TAG, "Error checking for dashboard: " + e.getMessage());
                    SystemClock.sleep(2000);
                }
            }

            if (!dashboardFound) {
                // Log the current screen state for debugging
                Log.e(TAG, "Failed to reach dashboard after login - reporting visible elements");

                // Try to identify what's on screen instead
                try {
                    onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
                    Log.e(TAG, "Still on login screen - login failed");
                } catch (Exception e) {
                    // Not on login
                }

                try {
                    onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                    Log.e(TAG, "On start screen - login failed");
                } catch (Exception e) {
                    // Not on start screen
                }

                throw new RuntimeException("Dashboard not found after login - check logs for details");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during login: " + e.getMessage(), e);
            throw e;
        }
    }

    private void logout() {
        // Perform sign out from Firebase
        auth.signOut();

        // Navigate back to start screen
        try {
            // Press back until we get to the start screen
            for (int i = 0; i < 5; i++) {  // limit to 5 attempts to avoid infinite loop
                try {
                    // Check if we're already at the start screen
                    onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                    Log.d(TAG, "Reached start screen");
                    break;
                } catch (Exception e) {
                    // Still need to go back
                    androidx.test.espresso.Espresso.pressBack();
                    SystemClock.sleep(1000);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage());
            // If navigation failed, restart the activity
            activityScenarioRule.getScenario().recreate();
            SystemClock.sleep(2000);
        }
    }

    //5.5.1
    @Test
    public void test1_SendFollowRequest() {
        // Ensure setup is complete
        if (!setupComplete.get()) {
            throw new RuntimeException("Test setup did not complete successfully");
        }

        // Direct Firebase auth check
        auth.signInWithEmailAndPassword(USER1_EMAIL, USER1_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Firebase auth succeeded for user1 direct check");
                    auth.signOut();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase auth failed for user1 direct check: " + e.getMessage());
                });
        SystemClock.sleep(2000);

        // Login with user 1 (sender)
        login(USER1_USERNAME, USER1_PASSWORD);

        // Navigate to Friends/User Search
        onView(withId(R.id.friendsButton)).perform(click());
        SystemClock.sleep(2000);

        // Set text in search field
        onView(withId(R.id.etUsername)).perform(typeText(USER2_USERNAME), closeSoftKeyboard());
        SystemClock.sleep(1000);

        // Click search button
        onView(withId(R.id.search_button)).perform(click());
        SystemClock.sleep(3000);

        // Click on the found user to view their profile
        // Using ID to avoid ambiguity with the search text field
        onView(withId(R.id.username_text)).perform(click());
        SystemClock.sleep(2000);

        try {
            // Verify follow button is visible and click it
            onView(withId(R.id.follow_button)).check(matches(isDisplayed()));
            onView(withId(R.id.follow_button)).perform(click());
            SystemClock.sleep(2000);

            // Verify the button text changed to "Requested"
            onView(withId(R.id.follow_button)).check(matches(withText("Requested")));

            // Toast messages are unreliable to test with Espresso
            // Instead of checking for toast, verify the button is disabled which indicates request was sent
            onView(withId(R.id.follow_button)).check(matches(not(isEnabled())));

            Log.d(TAG, "Successfully sent follow request from " + USER1_USERNAME + " to " + USER2_USERNAME);
        } catch (Exception e) {
            Log.e(TAG, "Error during follow process: " + e.getMessage(), e);
            throw e;
        }

        // Logout to prepare for the next test
        logout();
        SystemClock.sleep(2000);
    }

    //5.2.1
    @Test
    public void test2_ViewAndAcceptFollowRequest() {
        // Ensure the first test has run
        if (!setupComplete.get()) {
            throw new RuntimeException("Test setup did not complete successfully");
        }

        // Direct Firebase auth check
        auth.signInWithEmailAndPassword(USER2_EMAIL, USER2_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Firebase auth succeeded for user2 direct check");
                    auth.signOut();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase auth failed for user2 direct check: " + e.getMessage());
                });
        SystemClock.sleep(2000);

        // Login as the user who will view and accept the request (user 2)
        login(USER2_USERNAME, USER2_PASSWORD);

        // Navigate to notifications
        try {
            // Click on notifications icon in the top toolbar
            onView(withId(R.id.notificationsFragment)).perform(click());
            SystemClock.sleep(3000);

            // Verify we're on the notifications screen
            onView(withId(R.id.notifications_list)).check(matches(isDisplayed()));

            // Verify the requesting username is displayed
            // Use a more specific matcher to avoid ambiguity
            onView(allOf(withId(R.id.username_text), withText(USER1_USERNAME)))
                    .check(matches(isDisplayed()));

            Log.d(TAG, "Successfully verified follow request is visible");

            // Click the add button next to the request
            onView(withId(R.id.add_button)).perform(click());
            SystemClock.sleep(2000);

            Log.d(TAG, "Successfully accepted follow request from " + USER1_USERNAME);

            // Verify the follower relationship was created in Firestore
            verifyFollowerRelationshipCreated();

            // Logout after the test
            logout();

        } catch (Exception e) {
            Log.e(TAG, "Error in view and accept follow request: " + e.getMessage(), e);
            // Take screenshot or other diagnostics here
            throw e;
        }
    }

    private void verifyFollowerRelationshipCreated() {
        final CountDownLatch latch = new CountDownLatch(2);
        final boolean[] followerExists = {false};
        final boolean[] followingExists = {false};

        // Check if user1 is in user2's followers collection
        db.collection("users")
                .document(user2Id)
                .collection("followers")
                .document(user1Id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        followerExists[0] = true;
                        Log.d(TAG, "Follower relationship verified in Firestore");
                    } else {
                        Log.e(TAG, "Follower relationship not found in Firestore");
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying follower relationship", e);
                    latch.countDown();
                });

        // Check if user2 is in user1's following collection
        db.collection("users")
                .document(user1Id)
                .collection("following")
                .document(user2Id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        followingExists[0] = true;
                        Log.d(TAG, "Following relationship verified in Firestore");
                    } else {
                        Log.e(TAG, "Following relationship not found in Firestore");
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying following relationship", e);
                    latch.countDown();
                });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for relationship verification", e);
        }

        if (!followerExists[0] || !followingExists[0]) {
            throw new AssertionError("Follow relationship was not properly created in Firestore");
        }
    }

    @AfterClass
    public static void tearDown() {
        Log.d(TAG, "Starting test cleanup");

        // Clean up all test users
        cleanUpUser(user1Id, 1);
        cleanUpUser(user2Id, 2);

        Log.d(TAG, "Test cleanup complete");
    }

    private static void cleanUpUser(String userId, int userNumber) {
        if (userId == null) {
            Log.d(TAG, "User ID " + userNumber + " is null, nothing to clean up");
            return;
        }

        // Delete user data directly
        deleteMoodsAndUserData(userId, userNumber);
    }

    private static void deleteMoodsAndUserData(String uid, int userNumber) {
        // Clean up following collection
        db.collection("users").document(uid).collection("following")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Following collection cleared for user " + userNumber);
                    }
                });

        // Clean up followers collection
        db.collection("users").document(uid).collection("followers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Followers collection cleared for user " + userNumber);
                    }
                });

        // Clean up requests collection
        db.collection("users").document(uid).collection("requests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Requests collection cleared for user " + userNumber);
                    }
                });

        // Clean up moods collection
        db.collection("users").document(uid).collection("moods")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Moods collection cleared for user " + userNumber);

                        // After cleaning collections, delete the user document
                        db.collection("users").document(uid).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User " + userNumber + " document deleted");

                                    // Finally try to delete the auth user
                                    deleteAuthUser(userNumber);
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting user " + userNumber + " document", e));
                    }
                });
    }

    private static void deleteAuthUser(int userNumber) {
        String email = (userNumber == 1) ? USER1_EMAIL : USER2_EMAIL;
        String password = (userNumber == 1) ? USER1_PASSWORD : USER2_PASSWORD;

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        user.delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "User " + userNumber + " auth profile deleted"))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting user " + userNumber + " auth profile", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Could not sign in as user " + userNumber + " to delete them", e));
    }
}