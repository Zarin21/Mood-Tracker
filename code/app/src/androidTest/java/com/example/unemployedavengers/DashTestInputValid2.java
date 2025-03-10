package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
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
public class DashTestInputValid2{
    private static final String TAG = "DashTestInputValid2";

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


    @Test
    public void testTriggerField() {
        try {
            Log.d(TAG, "Beginning testTriggerField");
            loginAndNavigateToDashboard();

            // Click add mood button
            Log.d(TAG, "Clicking add mood button");
            onView(withId(R.id.add_mood_button)).perform(click());

            // Test just the trigger field
            Log.d(TAG, "Entering text in trigger field");
            onView(withId(R.id.editTrigger)).perform(typeText("Test trigger"), closeSoftKeyboard());

            // Check that text was entered correctly
            Log.d(TAG, "Verifying trigger text was entered correctly");
            onView(withId(R.id.editTrigger)).check(matches(withText("Test trigger")));
            Log.d(TAG, "Trigger text verified, test successful");

            // Go back to dashboard
            Espresso.pressBack();
        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReasonWordLimitValidation() {
        try {
            Log.d(TAG, "Beginning testReasonWordLimitValidation");
            loginAndNavigateToDashboard();

            // Click add mood button
            Log.d(TAG, "Clicking add mood button");
            onView(withId(R.id.add_mood_button)).perform(click());

            // Enter more than 3 words
            Log.d(TAG, "Entering text with more than 3 words in reason field");
            onView(withId(R.id.editReason)).perform(typeText("This is way too many words"), closeSoftKeyboard());

            // Select a radio button (required for form validation)
            Log.d(TAG, "Selecting 'Alone' radio button");
            onView(withId(R.id.radioAlone)).perform(click());

            // Try to confirm
            Log.d(TAG, "Clicking confirm button");
            onView(withId(R.id.buttonConfirm)).perform(click());

            // Check for error message
            Log.d(TAG, "Checking for error message");
            onView(withId(R.id.editReason)).check(matches(hasErrorText("Reason must be 3 words or less!")));
            Log.d(TAG, "Error message verified, test successful");

            // Go back to dashboard
            Espresso.pressBack();
        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception", e);
            throw new RuntimeException(e);
        }

    }
    @Test
    public void testReasonFieldMaxLength() {
        try {
            Log.d(TAG, "Beginning testReasonFieldMaxLength");
            loginAndNavigateToDashboard();

            // First, wait to ensure dashboard is fully loaded
            Thread.sleep(2000);

            // Verify we're on the dashboard by checking for the add_mood_button
            Log.d(TAG, "Verifying we're on dashboard by checking for add_mood_button");
            onView(withId(R.id.add_mood_button)).check(matches(isDisplayed()));

            // Click add mood button to navigate to input dialog
            Log.d(TAG, "Clicking add_mood_button to navigate to input dialog");
            onView(withId(R.id.add_mood_button)).perform(click());

            // Wait for input dialog to appear
            Log.d(TAG, "Waiting for input dialog to appear");
            Thread.sleep(2000);

            // Verify we're on the input dialog by checking for the buttonConfirm
            Log.d(TAG, "Verifying we're on input dialog by checking for buttonConfirm");
            onView(withId(R.id.buttonConfirm)).check(matches(isDisplayed()));

            // Try to enter more than 20 characters (but the EditText should limit it to 20)
            String longText = "This is a very long text that exceeds twenty characters";
            Log.d(TAG, "Entering text with more than 20 characters in reason field");
            onView(withId(R.id.editReason)).perform(typeText(longText), closeSoftKeyboard());

            // Verify that only 20 characters were actually entered due to maxLength attribute
            Log.d(TAG, "Verifying text was truncated to 20 characters");
            onView(withId(R.id.editReason)).check(matches(withText(longText.substring(0, 20))));
            Log.d(TAG, "Text length verification successful");

            // Go back to dashboard
            Espresso.pressBack();
        } catch (Exception e) {
            Log.e(TAG, "Test failed with exception", e);
            throw new RuntimeException(e);
        }
    }


    @After
    public void clearDatabase() {
        Log.d(TAG, "Cleaning up database after tests");
        FirebaseTestHelper.clearDatabase();
    }

}
