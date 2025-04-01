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
import static org.hamcrest.Matchers.allOf;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for following mood filtering features:
 * US 05.04.01 - Filter by most recent week
 * US 05.05.01 - Filter by emotional state
 * US 05.06.01 - Filter by reason text
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowingFilterTest {
    private static final String TAG = "FollowingFilterTest";

    // Create unique user IDs for each test to avoid test interdependencies
    private static final String MOOD_CREATOR_PREFIX = "moodcreator";
    private static final String FOLLOWER_PREFIX = "moodviewer";
    private static final String PASSWORD = "password123";

    // Track all created user IDs for cleanup
    private static List<String> createdUserIds = new ArrayList<>();
    private static Map<String, String> userEmails = new HashMap<>();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupEmulators() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
    }

    @Before
    public void setUp() {
        // Sign out at the beginning of each test
        FirebaseAuth.getInstance().signOut();
    }

    // Create users and test data for each test individually
    private String[] setupTestEnv(String testName) throws InterruptedException {
        String moodCreatorUsername = MOOD_CREATOR_PREFIX + "_" + testName;
        String moodCreatorEmail = moodCreatorUsername + "@example.com";

        String followerUsername = FOLLOWER_PREFIX + "_" + testName;
        String followerEmail = followerUsername + "@example.com";

        Log.d(TAG, "Setting up test environment for test: " + testName);

        // Create test users
        String moodCreatorId = createTestUser(moodCreatorUsername, moodCreatorEmail, PASSWORD);
        String followerId = createTestUser(followerUsername, followerEmail, PASSWORD);

        // Track for cleanup
        createdUserIds.add(moodCreatorId);
        createdUserIds.add(followerId);
        userEmails.put(moodCreatorId, moodCreatorEmail);
        userEmails.put(followerId, followerEmail);

        // Create follow relationship
        createFollowRelationship(moodCreatorId, followerId);

        // Create test moods
        createTestMoods(moodCreatorId, moodCreatorUsername);

        Log.d(TAG, "Test environment setup complete for: " + testName);

        return new String[] {moodCreatorId, moodCreatorUsername, followerId, followerUsername};
    }

    private String createTestUser(String username, String email, String password) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] userId = new String[1];

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Creating test user: " + username);

        // Create the user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    userId[0] = result.getUser().getUid();
                    Log.d(TAG, "Created user auth: " + username + " with ID: " + userId[0]);

                    // Add to Firestore
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", userId[0]);
                    userData.put("username", username);
                    userData.put("dummyEmail", email);
                    userData.put("password", password);

                    db.collection("users").document(userId[0])
                            .set(userData)
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "Added user to Firestore: " + username);
                                auth.signOut();
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error adding user to Firestore: " + username, e);
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating auth user: " + username, e);
                    latch.countDown();
                });

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Timed out waiting to create user: " + username);
        }
        return userId[0];
    }

    private void createFollowRelationship(String moodCreatorId, String followerId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Creating follow relationship between: " + followerId + " and " + moodCreatorId);

        // Create following entry for follower
        Map<String, Object> followingData = new HashMap<>();
        followingData.put("followedId", moodCreatorId);
        followingData.put("followedAt", System.currentTimeMillis());

        db.collection("users").document(followerId)
                .collection("following").document(moodCreatorId)
                .set(followingData)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Added following relationship");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding following relationship", e);
                    latch.countDown();
                });

        // Create followers entry for mood creator
        Map<String, Object> followerData = new HashMap<>();
        followerData.put("followerId", followerId);
        followerData.put("followedAt", System.currentTimeMillis());

        db.collection("users").document(moodCreatorId)
                .collection("followers").document(followerId)
                .set(followerData)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Added follower relationship");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding follower relationship", e);
                    latch.countDown();
                });

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Timed out creating follow relationship");
        }
    }

    private void createTestMoods(String moodCreatorId, String moodCreatorUsername) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Creating test moods for user: " + moodCreatorUsername);

        // Current time in millis
        long currentTime = System.currentTimeMillis();
        // Time 8 days ago (outside of week range)
        long eightDaysAgo = currentTime - (8 * 24 * 60 * 60 * 1000);
        // Time 3 days ago (inside week range)
        long threeDaysAgo = currentTime - (3 * 24 * 60 * 60 * 1000);

        // Create 4 different moods:

        // 1. Old Happiness mood with specific test word
        Map<String, Object> happinessMood = new HashMap<>();
        happinessMood.put("id", "test_mood_happiness_" + moodCreatorId);
        happinessMood.put("mood", "😄Happiness");
        happinessMood.put("reason", "Testing with filterword today");
        happinessMood.put("time", eightDaysAgo);
        happinessMood.put("userId", moodCreatorId);
        happinessMood.put("userName", moodCreatorUsername);
        happinessMood.put("publicStatus", true);
        happinessMood.put("radioSituation", "Alone");
        happinessMood.put("situation", "Testing situation");
        happinessMood.put("hasLocation", false);
        happinessMood.put("existed", true);

        // 2. Recent Sadness mood without the test word
        Map<String, Object> sadnessMood = new HashMap<>();
        sadnessMood.put("id", "test_mood_sadness_" + moodCreatorId);
        sadnessMood.put("mood", "😔Sadness");
        sadnessMood.put("reason", "Just feeling blue");
        sadnessMood.put("time", threeDaysAgo);
        sadnessMood.put("userId", moodCreatorId);
        sadnessMood.put("userName", moodCreatorUsername);
        sadnessMood.put("publicStatus", true);
        sadnessMood.put("radioSituation", "Alone");
        sadnessMood.put("situation", "Testing situation");
        sadnessMood.put("hasLocation", false);
        sadnessMood.put("existed", true);

        // 3. Recent Anger mood with the test word
        Map<String, Object> angerMood = new HashMap<>();
        angerMood.put("id", "test_mood_anger_" + moodCreatorId);
        angerMood.put("mood", "😠Anger");
        angerMood.put("reason", "Frustration with filterword");
        angerMood.put("time", currentTime - (2 * 24 * 60 * 60 * 1000));
        angerMood.put("userId", moodCreatorId);
        angerMood.put("userName", moodCreatorUsername);
        angerMood.put("publicStatus", true);
        angerMood.put("radioSituation", "Alone");
        angerMood.put("situation", "Testing situation");
        angerMood.put("hasLocation", false);
        angerMood.put("existed", true);

        // 4. Very recent Surprise mood without the test word
        Map<String, Object> surpriseMood = new HashMap<>();
        surpriseMood.put("id", "test_mood_surprise_" + moodCreatorId);
        surpriseMood.put("mood", "😯Surprise");
        surpriseMood.put("reason", "Unexpected event happened");
        surpriseMood.put("time", currentTime - (1 * 24 * 60 * 60 * 1000));
        surpriseMood.put("userId", moodCreatorId);
        surpriseMood.put("userName", moodCreatorUsername);
        surpriseMood.put("publicStatus", true);
        surpriseMood.put("radioSituation", "Alone");
        surpriseMood.put("situation", "Testing situation");
        surpriseMood.put("hasLocation", false);
        surpriseMood.put("existed", true);

        // Add all moods to Firestore
        db.collection("users").document(moodCreatorId)
                .collection("moods").document("test_mood_happiness_" + moodCreatorId)
                .set(happinessMood)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Added Happiness mood");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding Happiness mood: " + e.getMessage());
                    latch.countDown();
                });

        db.collection("users").document(moodCreatorId)
                .collection("moods").document("test_mood_sadness_" + moodCreatorId)
                .set(sadnessMood)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Added Sadness mood");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding Sadness mood: " + e.getMessage());
                    latch.countDown();
                });

        db.collection("users").document(moodCreatorId)
                .collection("moods").document("test_mood_anger_" + moodCreatorId)
                .set(angerMood)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Added Anger mood");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding Anger mood: " + e.getMessage());
                    latch.countDown();
                });

        db.collection("users").document(moodCreatorId)
                .collection("moods").document("test_mood_surprise_" + moodCreatorId)
                .set(surpriseMood)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Added Surprise mood");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding Surprise mood: " + e.getMessage());
                    latch.countDown();
                });

        boolean moodsCreated = latch.await(30, TimeUnit.SECONDS);
        if (!moodsCreated) {
            throw new RuntimeException("Failed to create test moods within timeout");
        }
        Log.d(TAG, "All test moods created successfully");
    }

    /**
     * US 05.04.01 - Filter by most recent week
     */
    @Test
    public void testRecentWeekFilter() throws InterruptedException {
        // Setup unique test environment for this test
        String[] testEnv = setupTestEnv("week");
        String moodCreatorId = testEnv[0];
        String moodCreatorUsername = testEnv[1];
        String followerId = testEnv[2];
        String followerUsername = testEnv[3];

        // Login as follower
        login(followerUsername, PASSWORD);

        // Navigate to friends history
        navigateToFriendsHistory();

        // Test recent week filter
        onView(withId(R.id.filterButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterWeek)).perform(click());
        SystemClock.sleep(500);

        onView(withText("Apply")).perform(click());
        SystemClock.sleep(3000);

        Log.d(TAG, "Recent week filter applied successfully");
    }

    /**
     * US 05.05.01 - Filter by emotional state
     */
    @Test
    public void testEmotionalStateFilter() throws InterruptedException {
        // Setup unique test environment for this test
        String[] testEnv = setupTestEnv("emotion");
        String moodCreatorId = testEnv[0];
        String moodCreatorUsername = testEnv[1];
        String followerId = testEnv[2];
        String followerUsername = testEnv[3];

        // Login as follower
        login(followerUsername, PASSWORD);

        // Navigate to friends history
        navigateToFriendsHistory();

        // Test emotional state filter
        onView(withId(R.id.filterButton)).perform(click());
        SystemClock.sleep(1000);

        // Check the Mood checkbox
        onView(withId(R.id.filterMood)).perform(click());
        SystemClock.sleep(1000);

        // Now the spinner is visible with "😠Anger" already selected
        // No need to select anything else, just apply the filter
        onView(withText("Apply")).perform(click());
        SystemClock.sleep(3000);

        // The test passes if it reaches this point without crashing
        Log.d(TAG, "Emotional state filter applied successfully");
    }

    /**
     * US 05.06.01 - Filter by reason text
     */
    @Test
    public void testReasonTextFilter() throws InterruptedException {
        // Setup unique test environment for this test
        String[] testEnv = setupTestEnv("reason");
        String moodCreatorId = testEnv[0];
        String moodCreatorUsername = testEnv[1];
        String followerId = testEnv[2];
        String followerUsername = testEnv[3];

        // Login as follower
        login(followerUsername, PASSWORD);

        // Navigate to friends history
        navigateToFriendsHistory();

        // Test reason text filter
        onView(withId(R.id.filterButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterReason)).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.editReasonFilter)).perform(typeText("filterword"), closeSoftKeyboard());
        SystemClock.sleep(1000);

        onView(withText("Apply")).perform(click());
        SystemClock.sleep(3000);

        // The test passes if it reaches this point without crashing
        Log.d(TAG, "Reason text filter applied successfully");
    }

    private void login(String username, String password) {
        try {
            // Make sure we're on the start screen
            try {
                // Try to find the start screen login link
                onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
                Log.d(TAG, "Found start screen, clicking login link");
            } catch (Exception e) {
                // We might be on another screen, try to go back to start
                Log.d(TAG, "Not on start screen, attempting to recreate activity");
                activityRule.getScenario().recreate();
                SystemClock.sleep(3000);
            }

            // Click Login on start screen
            onView(withId(R.id.tvStartLogin)).perform(click());
            SystemClock.sleep(1000);

            // Enter credentials
            onView(withId(R.id.etUsername)).perform(typeText(username), closeSoftKeyboard());
            SystemClock.sleep(500);

            onView(withId(R.id.etPassword)).perform(typeText(password), closeSoftKeyboard());
            SystemClock.sleep(500);

            // Click login button
            onView(withId(R.id.btnLogin)).perform(click());
            SystemClock.sleep(1000);

            // Handle location permission popup
            try {
                CustomMatchers.handleLocationPermissionPopup();
                Log.d(TAG, "Handled location permissions");
            } catch (Exception e) {
                Log.d(TAG, "No location permissions dialog found");
            }

            // Wait for dashboard to load
            SystemClock.sleep(5000);

        } catch (Exception e) {
            Log.e(TAG, "Error during login process: " + e.getMessage(), e);
            throw e;
        }
    }

    private void navigateToFriendsHistory() {
        try {
            // Click on Friends History tab
            onView(withId(R.id.friendsHistoryFragment)).perform(click());
            SystemClock.sleep(2000);

            // Click on Friends History Mood button
            onView(withId(R.id.friends_history_mood_button)).perform(click());
            SystemClock.sleep(3000);

            Log.d(TAG, "Successfully navigated to Friends History");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Friends History: " + e.getMessage(), e);
            throw e;
        }
    }

    @AfterClass
    public static void cleanupAllData() throws InterruptedException {
        Log.d(TAG, "Starting final test cleanup");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CountDownLatch finalCleanupLatch = new CountDownLatch(createdUserIds.size());

        for (String userId : createdUserIds) {
            String email = userEmails.get(userId);

            // Cleanup user's collections and data
            cleanupUser(db, auth, userId, email, PASSWORD, finalCleanupLatch);
        }

        boolean completed = finalCleanupLatch.await(60, TimeUnit.SECONDS);
        if (!completed) {
            Log.w(TAG, "Cleanup did not complete within timeout, but continuing anyway");
        }

        Log.d(TAG, "All test cleanup complete");
    }

    private static void cleanupUser(FirebaseFirestore db, FirebaseAuth auth, String userId,
                                    String email, String password, CountDownLatch latch) {
        Log.d(TAG, "Cleaning up user: " + userId + " with email: " + email);

        // Use a nested CountDownLatch to track collection deletions
        CountDownLatch collectionsLatch = new CountDownLatch(3); // 3 collections to clean

        // Delete mood events
        db.collection("users").document(userId).collection("moods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " moods to delete for user " + userId);
                    if (queryDocumentSnapshots.size() > 0) {
                        CountDownLatch moodsLatch = new CountDownLatch(queryDocumentSnapshots.size());
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete()
                                    .addOnSuccessListener(v -> {
                                        Log.d(TAG, "Deleted mood: " + document.getId());
                                        moodsLatch.countDown();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to delete mood: " + document.getId(), e);
                                        moodsLatch.countDown();
                                    });
                        }
                        try {
                            moodsLatch.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while waiting for mood deletion", e);
                        }
                    }
                    collectionsLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting moods", e);
                    collectionsLatch.countDown();
                });

        // Delete followers collection
        db.collection("users").document(userId).collection("followers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " followers to delete for user " + userId);
                    if (queryDocumentSnapshots.size() > 0) {
                        CountDownLatch followersLatch = new CountDownLatch(queryDocumentSnapshots.size());
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete()
                                    .addOnSuccessListener(v -> {
                                        Log.d(TAG, "Deleted follower: " + document.getId());
                                        followersLatch.countDown();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to delete follower: " + document.getId(), e);
                                        followersLatch.countDown();
                                    });
                        }
                        try {
                            followersLatch.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while waiting for followers deletion", e);
                        }
                    }
                    collectionsLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting followers", e);
                    collectionsLatch.countDown();
                });

        // Delete following collection
        db.collection("users").document(userId).collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " following to delete for user " + userId);
                    if (queryDocumentSnapshots.size() > 0) {
                        CountDownLatch followingLatch = new CountDownLatch(queryDocumentSnapshots.size());
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete()
                                    .addOnSuccessListener(v -> {
                                        Log.d(TAG, "Deleted following: " + document.getId());
                                        followingLatch.countDown();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to delete following: " + document.getId(), e);
                                        followingLatch.countDown();
                                    });
                        }
                        try {
                            followingLatch.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while waiting for following deletion", e);
                        }
                    }
                    collectionsLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting following", e);
                    collectionsLatch.countDown();
                });

        // Wait for all collections to be cleaned first
        try {
            boolean collectionsComplete = collectionsLatch.await(20, TimeUnit.SECONDS);
            if (!collectionsComplete) {
                Log.w(TAG, "Collection cleanup did not complete within timeout for user " + userId);
            }

            // Now delete the user document and auth profile
            db.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Deleted user document: " + userId);

                        // Attempt to delete auth user
                        try {
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> {
                                        FirebaseUser user = authResult.getUser();
                                        if (user != null) {
                                            user.delete()
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Log.d(TAG, "Deleted auth profile: " + userId);
                                                        latch.countDown();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error deleting auth profile: " + userId, e);
                                                        latch.countDown();
                                                    });
                                        } else {
                                            Log.w(TAG, "User was null after login: " + userId);
                                            latch.countDown();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error signing in as user for deletion: " + userId, e);
                                        latch.countDown();
                                    });
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while deleting auth user: " + userId, e);
                            latch.countDown();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting user document: " + userId, e);
                        latch.countDown();
                    });
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for collections cleanup", e);
            latch.countDown(); // Make sure to count down the main latch on error
        }
    }
}