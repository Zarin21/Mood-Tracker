/**
 * ProfileTest.java
 *
 * Instrumented UI tests for user profile functionality in the Unemployed Avengers app.
 *
 * Tests user stories related to:
 * - US 3.1.1: Unique username enforcement during signup and profile updates
 * - US 3.2.1: User search functionality
 * - US 3.3.1: Profile viewing and following capabilities
 *
 * Testing Approach:
 * - Uses Espresso for UI testing
 * - Sets up multiple test users with moods in Firebase emulators
 * - Verifies profile uniqueness constraints
 * - Tests search and profile viewing flows
 * - Includes comprehensive test data cleanup
 *
 * Key Test Scenarios:
 * 1. Attempting to create duplicate usernames
 * 2. Searching for other users
 * 3. Viewing other user profiles
 * 4. Following/unfollowing functionality
 */
package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileTest {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        int authPort = 9099;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Map<String, String>> users = Arrays.asList(
                Map.of("email", "user1@example.com", "password", "password1", "username", "User1"),
                Map.of("email", "user2@example.com", "password", "password2", "username", "User2"),
                Map.of("email", "user3@example.com", "password", "password3", "username", "User3")
        );

        AtomicInteger usersProcessed = new AtomicInteger(0);

        for (Map<String, String> user : users) {
            auth.createUserWithEmailAndPassword(user.get("email"), user.get("password"))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = Objects.requireNonNull(task.getResult().getUser()).getUid();
                            Log.d("Testing", "User created with UID: " + userId);

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userId", userId);
                            userData.put("username", user.get("username"));
                            userData.put("email", user.get("email"));

                            db.collection("users").document(userId).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Testing", "User added to Firestore: " + userId);
                                        seedMoods(db, userId, usersProcessed, latch, users.size());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firebase", "Error adding user to Firestore", e);
                                        checkCompletion(usersProcessed, users.size(), latch);
                                    });
                        } else {
                            Log.e("Testing", "Failed to create user", task.getException());
                            checkCompletion(usersProcessed, users.size(), latch);
                        }
                    });
        }
        latch.await();

    }
    private void seedMoods(FirebaseFirestore db, String userId, AtomicInteger usersProcessed, CountDownLatch latch, int totalUsers) {
        List<Map<String, Object>> moods = Arrays.asList(
                Map.of("mood", "\uD83D\uDE04Happiness", "reason", "Feeling great!", "time", System.currentTimeMillis(),"publicStatus",true),
                Map.of("mood", "\uD83D\uDE14Sadness", "reason", "A bit down today", "time", System.currentTimeMillis(),"publicStatus",false),
                Map.of("mood", "\uD83D\uDE20Anger", "reason", "Frustrated with work", "time", System.currentTimeMillis(),"publicStatus",true)
        );

        AtomicInteger moodsProcessed = new AtomicInteger(0);

        for (Map<String, Object> mood : moods) {
            Map<String, Object> moodData = new HashMap<>(mood);
            moodData.put("userId", userId);
            moodData.put("radioSituation", "Alone");

            db.collection("users").document(userId).collection("moods")
                    .add(moodData)
                    .addOnSuccessListener(documentReference -> {
                        documentReference.update("id", documentReference.getId())
                                .addOnSuccessListener(aVoid -> Log.d("Testing", "Mood added: " + documentReference.getId()))
                                .addOnFailureListener(e -> Log.w("Firebase", "Error updating mood ID", e));
                        checkMoodCompletion(moodsProcessed, moods.size(), usersProcessed, totalUsers, latch);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firebase", "Error adding mood", e);
                        checkMoodCompletion(moodsProcessed, moods.size(), usersProcessed, totalUsers, latch);
                    });
        }
    }

    public void login(String username, String password){
        onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));

        onView(withId(R.id.tvStartLogin)).perform(click());
        onView(withId(R.id.etUsername)).perform(ViewActions.typeText(username),closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText(password),closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        CustomMatchers.handleLocationPermissionPopup();
    }
    private void checkMoodCompletion(AtomicInteger moodsProcessed, int totalMoods, AtomicInteger usersProcessed, int totalUsers, CountDownLatch latch) {
        if (moodsProcessed.incrementAndGet() == totalMoods) {
            checkCompletion(usersProcessed, totalUsers, latch);
        }
    }

    private void checkCompletion(AtomicInteger usersProcessed, int totalUsers, CountDownLatch latch) {
        if (usersProcessed.incrementAndGet() == totalUsers) {
            latch.countDown();
        }
    }
    //US 3.1.1
    @Test
    public void testingUniqueProfile() throws InterruptedException{
        onView(withId(R.id.btnStart)).perform(click());
        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("User1"),closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText("Password"),closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(ViewActions.typeText("Password"),closeSoftKeyboard());
        onView(withId(R.id.btnSignup)).perform(click());
        Thread.sleep(3000);
        onView(withText("Signup")).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).perform(click());
        login("User1","password1");
        Espresso.onIdle();
        onView(withId(R.id.profileFragment)).perform(click());
        onView(withText("User1")).check(matches(isDisplayed()));
        onView(withId(R.id.btnChangeUsername)).perform(click());
        onView(withId(R.id.etNewUsername)).perform(ViewActions.typeText("User2"),closeSoftKeyboard());
        onView(withId(R.id.btnSubmitUsername)).perform(click());

        onView(withText("User1")).check(matches(isDisplayed()));

    }
    //US 3.2.1, 3.3.1
    @Test
    public void testingSearchUserAndProfile(){
        login("User1","password1");
        Espresso.onIdle();
        onView(withId(R.id.friendsButton)).perform(click());
        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("User2"),closeSoftKeyboard());
        onView(withId(R.id.search_button)).perform(click());
        onView(withId(R.id.etUsername)).perform(ViewActions.clearText());
        Espresso.onIdle();
        onView(withText("User2")).check(matches(isDisplayed()));
        onView(withText("User2")).perform(click());
        Espresso.onIdle();
        onView(withText("User2")).check(matches(isDisplayed()));
        onView(withText("Follow")).check(matches(isDisplayed()));

    }


    @AfterClass
    public static void tearDown() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3); // Wait for all 3 deletions

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> usersToDelete = Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com");
        List<String> passwords = Arrays.asList("password1", "password2", "password3");

        AtomicInteger usersProcessed = new AtomicInteger(0);

        for (int i = 0; i < usersToDelete.size(); i++) {
            String email = usersToDelete.get(i);
            String password = passwords.get(i);
            Log.d("FilterTest", "Attempting to delete user: " + email);

            // Sign in the user before deletion
            auth.signOut();
            Thread.sleep(1000);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null) {
                            String userId = user.getUid();
                            Log.d("FilterTest", "User signed in: " + userId);
                            // Delete the user's moods
                            db.collection("users").document(userId).collection("moods")
                                    .get()
                                    .addOnSuccessListener(moodsSnapshot -> {
                                        Log.d("FilterTest", "Deleting moods for user: " + userId);
                                        for (QueryDocumentSnapshot moodDoc : moodsSnapshot) {
                                            moodDoc.getReference().delete()
                                                    .addOnFailureListener(e -> Log.e("Testing", "Error deleting mood document for user: " + userId, e));
                                        }

                                        // After moods are deleted, delete the user document
                                        db.collection("users").document(userId).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("FilterTest", "User document deleted: " + userId);
                                                    // Finally, delete the user from Firebase Authentication
                                                    user.delete()
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                Log.d("FilterTest", "User deleted from Firebase Auth: " + userId);
                                                                if (usersProcessed.incrementAndGet() == usersToDelete.size()) {
                                                                    latch.countDown(); // Decrement latch when all users are deleted
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("FilterTest", "Error deleting user from Firebase Auth: " + userId, e);
                                                                latch.countDown(); // Ensure latch is counted down even on failure
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("FilterTest", "Error deleting user from Firestore: " + userId, e);
                                                    latch.countDown(); // Ensure latch is counted down even on failure
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FilterTest", "Error retrieving moods for user: " + userId, e);
                                        latch.countDown(); // Ensure latch is counted down even on failure
                                    });
                        } else {
                            Log.e("FilterTest", "No user is currently signed in for email: " + email);
                            latch.countDown(); // Ensure latch is counted down even if user is not signed in
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FilterTest", "Login failed for user: " + email, e);
                        latch.countDown(); // Ensure latch is counted down even on failure
                    });
        }

        latch.await();  // Wait for all deletions to complete before finishing teardown
        Log.d("ProfileTest", "Test completed");
    }


}
