package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static java.lang.Thread.sleep;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OfflineTest {
    private static FirebaseAuth auth;
    private static FirebaseFirestore db;
    private static SharedPreferences sharedPreferences;
    private String userId;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupClass() {
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        int authPort = 9099;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);

        // Get SharedPreferences instance
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
    }

    @Before
    public void setUp() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        auth.signOut(); // Ensure no user is signed in before each test

    }


    @Before
    public void seedDatabase() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1); // Initialize latch with 1

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword("testuser@example.com", "123456")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(task.getResult().getUser()).getUid(); // Get UID

                        Log.d("Testing test", "User created in Firebase Authentication emulator with UID: " + userId);

                        // Add user details to Firestore
                        Map<String, Object> testUser = new HashMap<>();
                        testUser.put("userId", userId);  // Store the user ID
                        testUser.put("username", "testUser");
                        testUser.put("email", "testuser@example.com");
                        testUser.put("password", "123456");

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users")
                                .document(userId)  // Use UID as the document ID
                                .set(testUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Testing test", "User added to Firestore");
                                    // Add mood document under the moods collection
                                    Map<String, Object> testMood = new HashMap<>();
                                    testMood.put("existed", true);
                                    testMood.put("hasLocation", false);
                                    testMood.put("imageUri", "");
                                    //testMood.put("latitude",null);
                                    //testMood.put("longitude",null);
                                    testMood.put("mood", "\uD83D\uDE04Happiness");
                                    testMood.put("publicStatus", true);
                                    testMood.put("radioSituation", "Alone");
                                    testMood.put("reason", "code pls work");
                                    testMood.put("situation", "code pls work");
                                    testMood.put("time", 1742402187646L);
                                    testMood.put("userId", userId);
                                    testUser.put("userName", "testUser");

                                    // Add mood to Firestore under the user's mood collection
                                    db.collection("users")
                                            .document(userId)
                                            .collection("moods")
                                            .add(testMood)
                                            .addOnSuccessListener(documentReference -> {
                                                String moodId = documentReference.getId();
                                                Map<String, Object> updatedMood = new HashMap<>(testMood);
                                                updatedMood.put("id", moodId);  // Add the ID field to the mood document

                                                // Set the updated document with the 'id' field
                                                documentReference.set(updatedMood)
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            Log.d("Testing test", "Mood added to Firestore with ID: " + moodId);
                                                            latch.countDown(); // Decrement latch when mood is added
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.w("Firebase", "Error adding 'id' to mood in Firestore", e);
                                                            latch.countDown(); // Ensure latch is decremented even on failure
                                                        });
                                                Log.d("Testing test", "Mood added to Firestore for user: " + userId);

                                                latch.countDown(); // Decrement latch when mood is added
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w("Firebase", "Error adding mood to Firestore", e);
                                                latch.countDown(); // Ensure latch is decremented even on failure
                                            });
                                    latch.countDown(); // Decrement latch when Firestore operation is complete
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("Firebase", "Error adding user to Firestore", e);
                                    latch.countDown(); // Ensure latch is decremented even on failure
                                });
                    } else {
                        Log.e("Testing test", "Failed to create user", task.getException());
                        latch.countDown(); // Ensure latch is decremented even on failure
                    }
                });

        latch.await();  // Wait for the operation to finish before continuing

    }

    /*

    @Before
    public void setUp() throws InterruptedException {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        auth.signOut();

        // Clear SharedPreferences to start fresh
        sharedPreferences.edit().clear().commit();

        final CountDownLatch latch = new CountDownLatch(1);

        auth.createUserWithEmailAndPassword("testuser@example.com", "123456")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userId = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        Log.d("Testing", "User created with UID: " + userId);

                        Map<String, Object> testUser = new HashMap<>();
                        testUser.put("userId", userId);
                        testUser.put("username", "testUser");
                        testUser.put("email", "testuser@example.com");
                        testUser.put("password", "123456");

                        db.collection("users").document(userId).set(testUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Testing", "User added to Firestore");

                                    // SAVE CREDENTIALS FOR OFFLINE USE
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("username", "testUser");
                                    editor.putString("password_hash", hashPassword("123456"));
                                    editor.putBoolean("is_logged_in", true);
                                    editor.putLong("last_login_time", System.currentTimeMillis());
                                    editor.putString("userID", userId);
                                    editor.commit(); // Use commit() to ensure immediate write

                                    Log.d("editor", "are you storing correctly");

                                    latch.countDown();
                                });
                    } else {
                        latch.countDown();
                    }
                });
        latch.await();
    }
*/
    private String hashPassword(String password) {
        // IMPORTANT: This must match your app's hashing algorithm exactly
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password + "_salt_for_security"; // Fallback must match app
        }
    }

    private static void executeAdbCommand(String command) {
        try {
            InstrumentationRegistry.getInstrumentation().getUiAutomation()
                    .executeShellCommand(command);
            sleep(2000); // Give time for the network change to take effect
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login() {
        onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));

        onView(withId(R.id.tvStartLogin)).perform(click());
        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("testUser"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        CustomMatchers.handleLocationPermissionPopup();
    }


    @Test
    public void stage1_login() {
        // First verify we're starting clean
        assertFalse(sharedPreferences.getBoolean("is_logged_in", false));

        // Perform login
        login();

        // Verify credentials were saved
        assertTrue(sharedPreferences.getBoolean("is_logged_in", false));
        assertNotNull(sharedPreferences.getString("userID", null));
    }

    @Test
    public void stage2_testOfflineModeAdd() throws InterruptedException {

        // Verify we have credentials first
        assertNotNull("No userID in SharedPreferences",
                sharedPreferences.getString("userID", null));

        // Now go offline
        executeAdbCommand("svc wifi disable");
        executeAdbCommand("svc data disable");

        Log.d("offline testing", "stage1_testOfflineModeAdd: " + sharedPreferences.getString("userID", null));

        Thread.sleep(5000);

        onView(withId(R.id.tvStartLogin)).perform(click());

        // Verify offline mode indicator is shown
        onView(withText("Offline Mode - Limited functionality available"))
                .check(matches(isDisplayed()));

        // Perform offline login

        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("testUser"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        CustomMatchers.handleLocationPermissionPopup();

        Thread.sleep(1000);

        // Perform an action (e.g., add a mood event)
        onView(withId(R.id.add_mood_button)).perform(click());
        onView(withId(R.id.editSocialSituation)).perform(ViewActions.typeText("I hate unit testing"), closeSoftKeyboard());

        onView(withId(R.id.scrollView)).perform(swipeUp());
        Espresso.onIdle();
        onView(withId(R.id.buttonConfirm))
                .check(matches(isDisplayed()))  // Check if button is visible
                .check(matches(isClickable())) // Check if button is clickable
                .perform(click()); // Click the button

        Thread.sleep(10000);
        onView(withText("\uD83D\uDE20Anger")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE20Anger")).perform(click());

        onView(withText("\uD83D\uDE20Anger")).check(matches(isDisplayed()));
        onView(withId(R.id.editSocialSituation)).check(matches(withText("I hate unit testing")));

        // Simulate going online
        executeAdbCommand("svc wifi enable");
        executeAdbCommand("svc data enable");

        Thread.sleep(5000);

    }

    @Test
    public void stage3_testOfflineModeAddSync() throws InterruptedException {
        // First ensure we're online
        executeAdbCommand("svc wifi enable");
        executeAdbCommand("svc data enable");
        Thread.sleep(5000); // Wait for connection

        // Manually trigger Firestore sync
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.enableNetwork().addOnCompleteListener(task -> {
            Log.d("Testing", "Network enabled, sync triggered");
        });

        // Wait for sync to complete
        Thread.sleep(3000);

        // Now verify the mood exists
        login();
        Thread.sleep(3000);

        // Check if mood appears in the list
        onView(withText("\uD83D\uDE20Anger"))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.editSocialSituation))
                .check(matches(withText("I hate unit testing")));


    }

    @Test
    public void stage4_OfflineEdit() throws InterruptedException{
        // Verify we have credentials first
        assertNotNull("No userID in SharedPreferences",
                sharedPreferences.getString("userID", null));

        // Now go offline
        executeAdbCommand("svc wifi disable");
        executeAdbCommand("svc data disable");

        Log.d("offline testing", "stage1_testOfflineModeAdd: " + sharedPreferences.getString("userID", null));

        Thread.sleep(5000);

        onView(withId(R.id.tvStartLogin)).perform(click());

        // Verify offline mode indicator is shown
        onView(withText("Offline Mode - Limited functionality available"))
                .check(matches(isDisplayed()));

        // Perform offline login

        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("testUser"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        CustomMatchers.handleLocationPermissionPopup();

        Thread.sleep(10000);

        onView(withText("\uD83D\uDE04Happiness"))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.editSocialSituation)).perform(clearText());
        onView(withId(R.id.editSocialSituation)).perform(ViewActions.typeText("I am alone"), closeSoftKeyboard());

        onView(withId(R.id.scrollView)).perform(swipeUp());
        Espresso.onIdle();
        onView(withId(R.id.buttonConfirm))
                .check(matches(isDisplayed()))  // Check if button is visible
                .check(matches(isClickable())) // Check if button is clickable
                .perform(click()); // Click the button

        Thread.sleep(10000);
        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE04Happiness")).perform(click());
        onView(withId(R.id.scrollView)).perform(swipeUp());
        Espresso.onIdle();
        onView(withId(R.id.buttonCancel))
                .check(matches(isDisplayed()))  // Check if button is visible
                .check(matches(isClickable())) // Check if button is clickable
                .perform(click()); // Click the button

        Thread.sleep(5000);
        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE04Happiness")).perform(click());
        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withId(R.id.editSocialSituation)).check(matches(withText("I am alone")));
        Thread.sleep(5000);

        // Simulate going online
        executeAdbCommand("svc wifi enable");
        executeAdbCommand("svc data enable");

        Thread.sleep(5000);

    }

    @Test
    public void stage5_testOfflineEditSync() throws InterruptedException {
        // First ensure we're online
        executeAdbCommand("svc wifi enable");
        executeAdbCommand("svc data enable");
        Thread.sleep(5000); // Wait for connection

        // Manually trigger Firestore sync
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.enableNetwork().addOnCompleteListener(task -> {
            Log.d("Testing", "Network enabled, sync triggered");
        });

        // Wait for sync to complete
        Thread.sleep(3000);

        // Now verify the mood exists
        login();
        Thread.sleep(3000);

        // Check if mood appears in the list
        onView(withText("\uD83D\uDE04Happiness"))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.editSocialSituation))
                .check(matches(withText("I am alone")));

    }

    @Test
    public void stage6_testOfflineDelete() throws InterruptedException {

        // Verify we have credentials first
        assertNotNull("No userID in SharedPreferences",
                sharedPreferences.getString("userID", null));

        // Now go offline
        executeAdbCommand("svc wifi disable");
        executeAdbCommand("svc data disable");

        Log.d("offline testing", "stage1_testOfflineModeAdd: " + sharedPreferences.getString("userID", null));

        Thread.sleep(5000);

        onView(withId(R.id.tvStartLogin)).perform(click());

        // Verify offline mode indicator is shown
        onView(withText("Offline Mode - Limited functionality available"))
                .check(matches(isDisplayed()));

        // Perform offline login

        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("testUser"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        CustomMatchers.handleLocationPermissionPopup();

        Thread.sleep(10000);

        onView(withText("\uD83D\uDE04Happiness"))
                .check(matches(isDisplayed()))
                .perform(longClick());

        onView(withText("Delete")).perform(click());

        Thread.sleep(5000);

        onView(withText("\uD83D\uDE04Happiness")).check(doesNotExist());

        // Simulate going online
        executeAdbCommand("svc wifi enable");
        executeAdbCommand("svc data enable");

        Thread.sleep(5000);

    }


    @Test
    public void stage7_testOfflineDeleteSync(){
        login();
        onView(withText("\uD83D\uDE04Happiness")).check(doesNotExist());

    }
    @AfterClass
    public static void tearDown() throws InterruptedException {
        // Ensure the user is signed in if not already logged out


        final CountDownLatch latch = new CountDownLatch(1);
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            Log.d("Testing", "User signed in for cleanup: " + userId);

            // First delete all moods
            db.collection("users").document(userId).collection("moods")
                    .get()
                    .addOnCompleteListener(moodsTask -> {
                        if (moodsTask.isSuccessful()) {
                            Log.d("Testing", "Found " + moodsTask.getResult().size() + " moods to delete");
                            for (QueryDocumentSnapshot document : moodsTask.getResult()) {
                                document.getReference().delete()
                                        .addOnSuccessListener(v -> Log.d("Testing", "Deleted mood: " + document.getId()))
                                        .addOnFailureListener(e -> Log.e("Testing", "Error deleting mood", e));
                            }

                            // Then delete user document
                            db.collection("users").document(userId).delete()
                                    .addOnSuccessListener(v -> {
                                        Log.d("Testing", "Deleted user document");
                                        // Finally delete auth user
                                        user.delete()
                                                .addOnSuccessListener(v2 -> {
                                                    Log.d("Testing", "Deleted auth user");
                                                    auth.signOut();
                                                    latch.countDown();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Testing", "Error deleting auth user", e);
                                                    latch.countDown();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Testing", "Error deleting user document", e);
                                        latch.countDown();
                                    });
                        } else {
                            Log.e("Testing", "Error getting moods", moodsTask.getException());
                            latch.countDown();
                        }
                    });
        } else {
            Log.e("Testing", "User was null after sign in");
            latch.countDown();
        }
        latch.await(30, TimeUnit.SECONDS); // Timeout after 30 seconds
        Log.d("user valid", "Cleanup complete");
    }
}