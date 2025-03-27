
package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matchers;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;

import com.example.unemployedavengers.auth.Login;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class InputTest {
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
/*
    @Before
    public void seedDatabase() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);  // Initialize latch with 1


        Map<String, Object> testUser = new HashMap<>();
        testUser.put("username", "testUser");
        testUser.put("password","123456");
        testUser.put("email", "testuser@example.com");

        db.collection("users")
                .add(testUser)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Testing test", "DocumentSnapshot added with ID: " + documentReference.getId());
                    latch.countDown();  // Decrement latch when operation is complete
                })
                .addOnFailureListener(e -> {
                    Log.w("Firebase", "Error adding document", e);
                    latch.countDown();  // Ensure latch is decremented even on failure
                });

        latch.await();  // Wait for the operation to finish before proceeding
    }


 */


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
                                    testMood.put("existed",true);
                                    testMood.put("hasLocation",false);
                                    testMood.put("imageUri","");
                                    //testMood.put("latitude",null);
                                    //testMood.put("longitude",null);
                                    testMood.put("mood", "\uD83D\uDE04Happiness");
                                    testMood.put("publicStatus",true);
                                    testMood.put("radioSituation","Alone");
                                    testMood.put("reason","code pls work");
                                    testMood.put("situation","code pls work");
                                    testMood.put("time", 1742402187646L);
                                    testMood.put("userId",userId);
                                    testUser.put("userName","testUser");

                                    // Add mood to Firestore under the user's mood collection
                                    db.collection("users")
                                            .document(userId)
                                            .collection("moods")
                                            .add(testMood)
                                            .addOnSuccessListener(documentReference-> {
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

    public void login(){
        onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));

        onView(withId(R.id.tvStartLogin)).perform(click());
        onView(withId(R.id.etUsername)).perform(ViewActions.typeText("testUser"),closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(ViewActions.typeText("123456"),closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        CustomMatchers.handleLocationPermissionPopup();
    }


    @Test
    /*
    Tests US 1.1.1, 1.2.1, 1.3.1, 1.4.1, 1.7.1, 2.1.1, 2.4.1
     */
    public void testAddMood() throws InterruptedException{
        login();
        Espresso.onIdle();

        onView(withId(R.id.add_mood_button)).perform(click());

        onView(withId(R.id.spinnerEmotion)).perform(click());

        //Check all spinner + colour + emoticon
        onView(withText("\uD83D\uDE20Anger")).check(matches((CustomMatchers.hasTextColor(Color.RED))));
        onView(withText("\uD83D\uDE15Confusion")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.orange)))));
        onView(withText("\uD83E\uDD22Disgust")).check(matches((CustomMatchers.hasTextColor(Color.GREEN))));
        onView(withText("\uD83D\uDE28Fear")).check(matches((CustomMatchers.hasTextColor(Color.BLUE))));
        onView(withText("\uD83D\uDE04Happiness")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.baby_blue)))));
        onView(withText("\uD83D\uDE14Sadness")).check(matches((CustomMatchers.hasTextColor(Color.GRAY))));
        onView(withText("\uD83D\uDE33Shame")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.yellow)))));
        onView(withText("\uD83D\uDE2FSurprise")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.pink)))));

        onView(withText("\uD83D\uDE20Anger")).perform(click());
        onView(withText("\uD83D\uDE20Anger")).check(matches((CustomMatchers.hasTextColor(Color.RED))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE15Confusion")).perform(click());
        onView(withText("\uD83D\uDE15Confusion")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.orange)))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83E\uDD22Disgust")).perform(click());
        onView(withText("\uD83E\uDD22Disgust")).check(matches((CustomMatchers.hasTextColor(Color.GREEN))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE28Fear")).perform(click());
        onView(withText("\uD83D\uDE28Fear")).check(matches((CustomMatchers.hasTextColor(Color.BLUE))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE04Happiness")).perform(click());
        onView(withText("\uD83D\uDE04Happiness")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.baby_blue)))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE14Sadness")).perform(click());
        onView(withText("\uD83D\uDE14Sadness")).check(matches((CustomMatchers.hasTextColor(Color.GRAY))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE33Shame")).perform(click());
        onView(withText("\uD83D\uDE33Shame")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.yellow)))));

        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE2FSurprise")).perform(click());
        onView(withText("\uD83D\uDE2FSurprise")).check(matches((CustomMatchers.hasTextColor(ContextCompat.getColor(getInstrumentation().getTargetContext(), R.color.pink)))));

        //Check reason more than 200 char
        String longString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{},.<>?/|`~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{},.<>?/|`~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@######";
        onView(withId(R.id.editReason)).perform(ViewActions.typeText(longString), closeSoftKeyboard());

        //Check social sitaution radio
        onView(withId(R.id.radioAlone)).perform(click());
        //Check social situation text
        onView(withId(R.id.editSocialSituation)).perform(ViewActions.typeText("testing"), closeSoftKeyboard());

        //Check public status
        onView(withId(R.id.radioPublicStatus)).perform(click());

        //Confirm
        onView(withId(R.id.scrollView)).perform(swipeUp());
        Espresso.onIdle();
        onView(withId(R.id.buttonConfirm))
                .check(matches(isDisplayed()))  // Check if button is visible
                .check(matches(isClickable())) // Check if button is clickable
                .perform(click()); // Click the button

        Thread.sleep(5000);

        //Check all info is stored
        onView(withText("\uD83D\uDE2FSurprise")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE2FSurprise")).perform(click());
        Thread.sleep(1000);
        onView(withText("\uD83D\uDE2FSurprise")).check(matches(isDisplayed()));
        onView(withId(R.id.editReason)).check(matches(withText("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{},.<>?/|`~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{},.<>?/|`~ABCDEFGHIJKLMNOPQRSTUV")));
        onView(withId(R.id.radioAlone)).check(matches(isChecked()));
        onView(withId(R.id.radioCrowd)).check(matches(not(isChecked())));
        onView(withId(R.id.radioNone)).check(matches(not(isChecked())));
        onView(withId(R.id.radioTwoSeveral)).check(matches(not(isChecked())));
        onView(withId(R.id.editSocialSituation)).check(matches(withText("testing")));
        onView(withId(R.id.radioPublicStatus)).check(matches(isChecked()));
        onView(withId(R.id.radioPrivateStatus)).check(matches(not(isChecked())));

        //Check cancel does not change any info
        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE04Happiness")).perform(click());
        onView(withId(R.id.editReason)).perform(ViewActions.clearText());
        onView(withId(R.id.radioNone)).perform(click());
        onView(withId(R.id.editSocialSituation)).perform(ViewActions.clearText());
        onView(withId(R.id.radioPrivateStatus)).perform(click());

        onView(withId(R.id.scrollView)).perform(swipeUp());
        Espresso.onIdle();

        onView(withId(R.id.buttonCancel))
                .check(matches(isDisplayed()))  // Check if button is visible
                .check(matches(isClickable())) // Check if button is clickable
                .perform(click()); // Click the button


        Thread.sleep(5000);

        onView(withText("\uD83D\uDE2FSurprise")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE2FSurprise")).perform(click());
        Thread.sleep(1000);
        onView(withText("\uD83D\uDE2FSurprise")).check(matches(isDisplayed()));
        onView(withId(R.id.editReason)).check(matches(withText("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{},.<>?/|`~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{},.<>?/|`~ABCDEFGHIJKLMNOPQRSTUV")));
        onView(withId(R.id.radioAlone)).check(matches(isChecked()));
        onView(withId(R.id.radioCrowd)).check(matches(not(isChecked())));
        onView(withId(R.id.radioNone)).check(matches(not(isChecked())));
        onView(withId(R.id.radioTwoSeveral)).check(matches(not(isChecked())));
        onView(withId(R.id.editSocialSituation)).check(matches(withText("testing")));
        onView(withId(R.id.radioPublicStatus)).check(matches(isChecked()));
        onView(withId(R.id.radioPrivateStatus)).check(matches(not(isChecked())));
    }


    @Test
    /*
    Test US 1.5.1
     */
    public void testEditMood() throws InterruptedException{
        login();

        Thread.sleep(3000);
        //Check original info is correct
        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE04Happiness")).perform(click());
        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withId(R.id.editReason)).check(matches(withText("code pls work")));
        onView(withId(R.id.radioAlone)).check(matches(isChecked()));
        onView(withId(R.id.radioCrowd)).check(matches(not(isChecked())));
        onView(withId(R.id.radioNone)).check(matches(not(isChecked())));
        onView(withId(R.id.radioTwoSeveral)).check(matches(not(isChecked())));
        onView(withId(R.id.editSocialSituation)).check(matches(withText("code pls work")));
        onView(withId(R.id.radioPublicStatus)).check(matches(isChecked()));
        onView(withId(R.id.radioPrivateStatus)).check(matches(not(isChecked())));

        //edit
        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("\uD83D\uDE2FSurprise")).perform(click());
        onView(withId(R.id.editReason)).perform(ViewActions.clearText());
        onView(withId(R.id.editReason)).perform(ViewActions.typeText("code"), closeSoftKeyboard());
        onView(withId(R.id.radioNone)).perform(click());
        onView(withId(R.id.editSocialSituation)).perform(ViewActions.clearText());
        onView(withId(R.id.editSocialSituation)).perform(ViewActions.typeText("code"), closeSoftKeyboard());
        onView(withId(R.id.radioPrivateStatus)).perform(click());
        //add image later
        onView(withId(R.id.scrollView)).perform(swipeUp());
        Espresso.onIdle();
        onView(withId(R.id.buttonConfirm))
                .check(matches(isDisplayed()))  // Check if button is visible
                .check(matches(isClickable())) // Check if button is clickable
                .perform(click()); // Click the button

        Thread.sleep(3000);
        onView(withText("\uD83D\uDE2FSurprise")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE2FSurprise")).perform(click());
        onView(withText("\uD83D\uDE2FSurprise")).check(matches(isDisplayed()));
        onView(withId(R.id.editReason)).check(matches(withText("code")));
        onView(withId(R.id.radioAlone)).check(matches(not(isChecked())));
        onView(withId(R.id.radioCrowd)).check(matches(not(isChecked())));
        onView(withId(R.id.radioNone)).check(matches(isChecked()));
        onView(withId(R.id.radioTwoSeveral)).check(matches(not(isChecked())));
        onView(withId(R.id.editSocialSituation)).check(matches(withText("code")));
        onView(withId(R.id.radioPublicStatus)).check(matches(not(isChecked())));
        onView(withId(R.id.radioPrivateStatus)).check(matches(isChecked()));

    }

    @Test
    /*
    Test US 1.6.1
     */
    public void testDeleteMood() throws InterruptedException{
        login();
        Thread.sleep(3000);
        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE04Happiness")).perform(longClick());
        onView(withText("Cancel")).perform(click());

        onView(withText("\uD83D\uDE04Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83D\uDE04Happiness")).perform(longClick());
        onView(withText("Delete")).perform(click());
        onView(withText("\uD83D\uDE04Happiness")).check(doesNotExist());


    }


    @After
    public void tearDown() {
        // Ensure the user is signed in if not already logged out
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // First, delete the user's mood data
            db.collection("users").document(userId).collection("moods")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Delete all mood documents for the user
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().delete()
                                        .addOnFailureListener(e -> Log.e("Testing", "Error deleting mood document", e));
                            }

                            // After deleting moods, delete the user's data from Firestore
                            db.collection("users").document(userId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // After Firestore delete, delete the user from Firebase Auth
                                        user.delete()
                                                .addOnSuccessListener(aVoid1 -> Log.d("Testing", "User deleted from Firebase Auth"))
                                                .addOnFailureListener(e -> Log.e("Testing", "Error deleting user from Firebase Auth", e));
                                    })
                                    .addOnFailureListener(e -> Log.e("Testing", "Error deleting user from Firestore", e));

                        } else {
                            Log.e("Testing", "Error retrieving moods", task.getException());
                        }
                    });

            // Optionally, sign out the user to ensure no user remains signed in after the test
            auth.signOut();
        } else {
            Log.e("Testing", "No user is currently signed in.");
        }
    }



}
