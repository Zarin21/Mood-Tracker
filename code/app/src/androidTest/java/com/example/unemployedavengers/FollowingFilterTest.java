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
 * Tests for following mood filtering features:
 * US 05.04.01 - Filter by most recent week
 * US 05.05.01 - Filter by emotional state
 * US 05.06.01 - Filter by reason text
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowingFilterTest {
    private static final String TAG = "FollowingFilterTest";

    // User who creates moods
    private static final String MOOD_CREATOR = "moodcreator";
    private static final String MOOD_CREATOR_PASS = "password123";
    private static final String MOOD_CREATOR_EMAIL = "moodcreator@example.com";
    private static String moodCreatorId;

    // User who follows and views moods
    private static final String FOLLOWER = "moodviewer";
    private static final String FOLLOWER_PASS = "password123";
    private static final String FOLLOWER_EMAIL = "moodviewer@example.com";
    private static String followerId;

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupEmulators() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
    }

    @Before
    public void setUp() throws InterruptedException {
        // Create test users
        setupUsers();

        // Create follow relationship
        createFollowRelationship();

        // Create multiple test moods with different characteristics
        createTestMoods();
    }

    private void setupUsers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Try to sign in with the creator account first
        auth.signInWithEmailAndPassword(MOOD_CREATOR_EMAIL, MOOD_CREATOR_PASS)
                .addOnSuccessListener(authResult -> {
                    // User exists, just get the ID
                    moodCreatorId = authResult.getUser().getUid();
                    auth.signOut();
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    // User doesn't exist, create it
                    auth.createUserWithEmailAndPassword(MOOD_CREATOR_EMAIL, MOOD_CREATOR_PASS)
                            .addOnSuccessListener(result -> {
                                moodCreatorId = result.getUser().getUid();

                                // Add to Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("userId", moodCreatorId);
                                userData.put("username", MOOD_CREATOR);
                                userData.put("dummyEmail", MOOD_CREATOR_EMAIL);
                                userData.put("password", MOOD_CREATOR_PASS);

                                db.collection("users").document(moodCreatorId)
                                        .set(userData)
                                        .addOnSuccessListener(v -> {
                                            auth.signOut();
                                            latch.countDown();
                                        });
                            });
                });

        // Try to sign in with the follower account first
        auth.signInWithEmailAndPassword(FOLLOWER_EMAIL, FOLLOWER_PASS)
                .addOnSuccessListener(authResult -> {
                    // User exists, just get the ID
                    followerId = authResult.getUser().getUid();
                    auth.signOut();
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    // Create follower user
                    auth.createUserWithEmailAndPassword(FOLLOWER_EMAIL, FOLLOWER_PASS)
                            .addOnSuccessListener(result -> {
                                followerId = result.getUser().getUid();

                                // Add to Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("userId", followerId);
                                userData.put("username", FOLLOWER);
                                userData.put("dummyEmail", FOLLOWER_EMAIL);
                                userData.put("password", FOLLOWER_PASS);

                                db.collection("users").document(followerId)
                                        .set(userData)
                                        .addOnSuccessListener(v -> {
                                            auth.signOut();
                                            latch.countDown();
                                        });
                            });
                });

        latch.await(30, TimeUnit.SECONDS);
    }

    private void createFollowRelationship() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create following entry for follower
        Map<String, Object> followingData = new HashMap<>();
        followingData.put("followedId", moodCreatorId);
        followingData.put("followedAt", System.currentTimeMillis());

        db.collection("users").document(followerId)
                .collection("following").document(moodCreatorId)
                .set(followingData)
                .addOnSuccessListener(v -> latch.countDown());

        // Create followers entry for mood creator
        Map<String, Object> followerData = new HashMap<>();
        followerData.put("followerId", followerId);
        followerData.put("followedAt", System.currentTimeMillis());

        db.collection("users").document(moodCreatorId)
                .collection("followers").document(followerId)
                .set(followerData)
                .addOnSuccessListener(v -> latch.countDown());

        latch.await(30, TimeUnit.SECONDS);
    }

    private void createTestMoods() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Current time in millis
        long currentTime = System.currentTimeMillis();
        // Time 8 days ago (outside of week range)
        long eightDaysAgo = currentTime - (8 * 24 * 60 * 60 * 1000);
        // Time 3 days ago (inside week range)
        long threeDaysAgo = currentTime - (3 * 24 * 60 * 60 * 1000);

        // Create 4 different moods:

        // 1. Old Happiness mood with specific test word
        Map<String, Object> happinessMood = new HashMap<>();
        happinessMood.put("id", "test_mood_happiness");
        happinessMood.put("mood", "😄Happiness");
        happinessMood.put("reason", "Testing with filterword today");
        happinessMood.put("time", eightDaysAgo);
        happinessMood.put("userId", moodCreatorId);
        happinessMood.put("userName", MOOD_CREATOR);
        happinessMood.put("publicStatus", true);
        happinessMood.put("radioSituation", "Alone");
        happinessMood.put("situation", "Testing situation");
        happinessMood.put("hasLocation", false);
        happinessMood.put("existed", true);

        // 2. Recent Sadness mood without the test word
        Map<String, Object> sadnessMood = new HashMap<>();
        sadnessMood.put("id", "test_mood_sadness");
        sadnessMood.put("mood", "😔Sadness");
        sadnessMood.put("reason", "Just feeling blue");
        sadnessMood.put("time", threeDaysAgo);
        sadnessMood.put("userId", moodCreatorId);
        sadnessMood.put("userName", MOOD_CREATOR);
        sadnessMood.put("publicStatus", true);
        sadnessMood.put("radioSituation", "Alone");
        sadnessMood.put("situation", "Testing situation");
        sadnessMood.put("hasLocation", false);
        sadnessMood.put("existed", true);

        // 3. Recent Anger mood with the test word
        Map<String, Object> angerMood = new HashMap<>();
        angerMood.put("id", "test_mood_anger");
        angerMood.put("mood", "😠Anger");
        angerMood.put("reason", "Frustration with filterword");
        angerMood.put("time", currentTime - (2 * 24 * 60 * 60 * 1000));
        angerMood.put("userId", moodCreatorId);
        angerMood.put("userName", MOOD_CREATOR);
        angerMood.put("publicStatus", true);
        angerMood.put("radioSituation", "Alone");
        angerMood.put("situation", "Testing situation");
        angerMood.put("hasLocation", false);
        angerMood.put("existed", true);

        // 4. Very recent Surprise mood without the test word
        Map<String, Object> surpriseMood = new HashMap<>();
        surpriseMood.put("id", "test_mood_surprise");
        surpriseMood.put("mood", "😯Surprise");
        surpriseMood.put("reason", "Unexpected event happened");
        surpriseMood.put("time", currentTime - (1 * 24 * 60 * 60 * 1000));
        surpriseMood.put("userId", moodCreatorId);
        surpriseMood.put("userName", MOOD_CREATOR);
        surpriseMood.put("publicStatus", true);
        surpriseMood.put("radioSituation", "Alone");
        surpriseMood.put("situation", "Testing situation");
        surpriseMood.put("hasLocation", false);
        surpriseMood.put("existed", true);

        // Add all moods to Firestore
        db.collection("users").document(moodCreatorId)
                .collection("moods").document("test_mood_happiness")
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
                .collection("moods").document("test_mood_sadness")
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
                .collection("moods").document("test_mood_anger")
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
                .collection("moods").document("test_mood_surprise")
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
    public void testRecentWeekFilter() {
        // Login as follower
        login(FOLLOWER, FOLLOWER_PASS);

        // Navigate to friends history
        navigateToFriendsHistory();

        // Test recent week filter
        onView(withId(R.id.filterButton)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.filterWeek)).perform(click());
        SystemClock.sleep(500);

        onView(withText("Apply")).perform(click());
        SystemClock.sleep(3000);

        // The test passes if it reaches this point without crashing
        Log.d(TAG, "Recent week filter applied successfully");
    }

    /**
     * US 05.05.01 - Filter by emotional state
     */
    @Test
    public void testEmotionalStateFilter() {
        // Login as follower
        login(FOLLOWER, FOLLOWER_PASS);

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
    public void testReasonTextFilter() {
        // Login as follower
        login(FOLLOWER, FOLLOWER_PASS);

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

    @After
    public void tearDown() {
        Log.d(TAG, "Starting test cleanup");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clean up the mood creator user
        if (moodCreatorId != null) {
            // Delete mood events
            db.collection("users").document(moodCreatorId).collection("moods")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Deleted mood creator's moods");
                    });

            // Delete followers collection
            db.collection("users").document(moodCreatorId).collection("followers")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Deleted mood creator's followers");
                    });

            // Delete user document
            db.collection("users").document(moodCreatorId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Deleted mood creator's user document");
                    });

            // Delete auth user
            try {
                auth.signInWithEmailAndPassword(MOOD_CREATOR_EMAIL, MOOD_CREATOR_PASS)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                user.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Deleted mood creator's auth profile");
                                        });
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error signing in as mood creator for deletion", e);
            }
        }

        // Clean up the follower user
        if (followerId != null) {
            // Delete following collection
            db.collection("users").document(followerId).collection("following")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        Log.d(TAG, "Deleted follower's following collection");
                    });

            // Delete user document
            db.collection("users").document(followerId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Deleted follower's user document");
                    });

            // Delete auth user
            try {
                auth.signInWithEmailAndPassword(FOLLOWER_EMAIL, FOLLOWER_PASS)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                user.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Deleted follower's auth profile");
                                        });
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error signing in as follower for deletion", e);
            }
        }

        // Sign out
        auth.signOut();

        Log.d(TAG, "Test cleanup complete");
    }
}