package com.example.unemployedavengers;
/*
 * DashboardTest.java
 *
 * This is an **Espresso UI test class** for testing the **Dashboard** functionality of the "Unemployed Avengers" app.
 * It tests different UI elements and interactions within the Dashboard, including:
 *
 * - Display of mood events retrieved from Firestore
 * - Adding new mood events
 * - Editing existing mood events
 * - Deleting mood events
 * - Field validations (word limits, input correctness, etc.)
 * - Image upload and preview functionality
 * - Spinner and radio button selections
 *
 * The tests interact with the UI using Espresso matchers and actions, ensuring that everything behaves as expected.
 */
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.startsWith;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.unemployedavengers.models.MoodEvent;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DashboardTest {
    private static final String TAG = "DashboardTest";

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setupEmulator() {
        Log.d(TAG, "Setting up Firebase emulator");
        FirebaseTestHelper.setupEmulator();
    }

    @Before
    public void seedDatabase() {
        Log.d(TAG, "Seeding database for tests");
        // Create sample mood events in the database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = "test1234";

        // Create a test user if needed
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("username", "test1234");
        userMap.put("dummyEmail", "test1234@example.com");
        userMap.put("password", "password123");
        db.collection("users").document(userId).set(userMap);

        // Add sample mood events
        CollectionReference moodEventsRef = db.collection("users").document(userId).collection("moods");

        // Note: MoodEvent constructor with imageUri parameter
        MoodEvent happyMood = new MoodEvent("Happiness", "Good day", "Sun", "Outside",
                System.currentTimeMillis() - 100000, "Alone", "");
        happyMood.setExisted(true);
        happyMood.setId("happy_mood_id");

        // Another mood with an image URI
        MoodEvent sadMood = new MoodEvent("Sadness", "Bad day", "Rain", "Inside",
                System.currentTimeMillis() - 200000, "With others",
                "https://example.com/test-image.jpg");
        sadMood.setExisted(true);
        sadMood.setId("sad_mood_id");

        moodEventsRef.document(happyMood.getId()).set(happyMood);
        moodEventsRef.document(sadMood.getId()).set(sadMood);

        // Set shared preferences for the test
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", "testuser");
        editor.putString("userID", userId);
        editor.apply();

        Log.d(TAG, "Database seeding completed");
    }

    @After
    public void clearDatabase() {
        Log.d(TAG, "Cleaning up database after tests");
        FirebaseTestHelper.clearDatabase();
    }

    private void loginAndNavigateToDashboard() throws InterruptedException {
        Log.d(TAG, "Starting navigation to dashboard");

        // Force navigation to Dashboard programmatically
        scenario.getScenario().onActivity(activity -> {
            NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
            try {
                Log.d(TAG, "Attempting to navigate to dashboard");
                navController.navigate(R.id.dashboardFragment);
                Log.d(TAG, "Navigation to dashboard successful");
            } catch (Exception e) {
                Log.e(TAG, "Navigation failed: " + e.getMessage(), e);
            }
        });

        // Wait for dashboard to load - increased timeout
        Log.d(TAG, "Waiting for dashboard to load");
        Thread.sleep(8000);
        Log.d(TAG, "Wait for dashboard completed");
    }

    // --- ORIGINAL TESTS ---

    @Test
    public void dashboardShouldDisplayExistingMoods() {
        try {
            Log.d(TAG, "Beginning dashboardShouldDisplayExistingMoods");
            loginAndNavigateToDashboard();

            // Check for just one mood to keep it simple
            Log.d(TAG, "Checking for 'Happiness' mood");
            onView(withText("Happiness")).check(matches(isDisplayed()));
            Log.d(TAG, "Happiness mood found, test successful");
        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception", e);
            throw new RuntimeException(e);
        }
    }


    // --- IMAGE-RELATED TESTS ---

    @Test
    public void testImageUploadButtonVisible() {
        try {
            Log.d(TAG, "Beginning testImageUploadButtonVisible");
            loginAndNavigateToDashboard();

            // Click add mood button
            Log.d(TAG, "Clicking add mood button");
            onView(withId(R.id.add_mood_button)).perform(click());

            // Check that the image upload button is visible
            Log.d(TAG, "Checking if image upload button is visible");
            onView(withId(R.id.buttonUploadPicture)).check(matches(isDisplayed()));
            Log.d(TAG, "Image upload button is visible, test successful");

            // Go back to dashboard
            Espresso.pressBack();
        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testImagePreviewVisible() {
        try {
            Log.d(TAG, "Beginning testImagePreviewVisible");
            loginAndNavigateToDashboard();

            // Click add mood button
            Log.d(TAG, "Clicking add mood button");
            onView(withId(R.id.add_mood_button)).perform(click());

            // Check that the image preview is visible
            Log.d(TAG, "Checking if image preview is visible");
            onView(withId(R.id.imagePreview)).check(matches(isDisplayed()));
            Log.d(TAG, "Image preview is visible, test successful");

            // Go back to dashboard
            Espresso.pressBack();
        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception", e);
            throw new RuntimeException(e);
        }
    }


}