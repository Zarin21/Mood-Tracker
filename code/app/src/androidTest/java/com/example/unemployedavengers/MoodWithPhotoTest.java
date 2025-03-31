package com.example.unemployedavengers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ScrollView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.unemployedavengers.CustomMatchers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for US 02.02.01
 * US 02.02.01: As a participant, I want to express the reason why for a mood event using a photograph.
 */
@RunWith(AndroidJUnit4.class)
public class MoodWithPhotoTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private static final String TEST_USERNAME = "phototest"; // Match email prefix
    private static final String TEST_PASSWORD = "123456";
    private static final String TEST_EMAIL = "phototest@example.com";

    private Context context;
    private String userId;
    private Uri testImageUri;

    @BeforeClass
    public static void setup() {
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        int authPort = 9099;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);
    }

    @Before
    public void setUp() throws InterruptedException, IOException {
        Log.d("MoodWithPhotoTest", "Starting test setup");

        // Get context
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Initialize Firebase and clear any existing session
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }

        // Create test image
        Log.d("MoodWithPhotoTest", "Creating test image");
        testImageUri = createTestImage(300, 300, Bitmap.CompressFormat.JPEG, 70, "test_image.jpg");

        // Log the file size
        File testFile = new File(context.getCacheDir(), "test_image.jpg");
        Log.d("MoodWithPhotoTest", "Test image size: " + testFile.length() + " bytes");

        // Set up test user
        Log.d("MoodWithPhotoTest", "Setting up test user");
        setupTestUser();
        Log.d("MoodWithPhotoTest", "Test setup complete");
    }

    private Uri createTestImage(int width, int height, Bitmap.CompressFormat format,
                                int quality, String filename) throws IOException {
        // Create a bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw some content on the bitmap
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("Mood Test Image", width/4, height/2, paint);

        // Add some visual elements
        for (int i = 0; i < 5; i++) {
            paint.setColor(Color.rgb(
                    i * 50 % 255,
                    (i * 30 + 100) % 255,
                    (i * 70 + 50) % 255
            ));
            canvas.drawCircle(
                    width / 2f,
                    height / 2f,
                    100 - (i * 20),
                    paint
            );
        }

        // Save bitmap to file
        File file = new File(context.getCacheDir(), filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(format, quality, out);
        }

        Log.d("MoodWithPhotoTest", "Created image " + filename + " with size " + file.length() + " bytes");
        return Uri.fromFile(file);
    }

    private void setupTestUser() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false}; // Track success

        // First check if user already exists and remove it
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Try to sign in with the test account to see if it exists
        auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    // User exists, let's delete and recreate
                    String uid = authResult.getUser().getUid();

                    // Delete from Firestore first
                    db.collection("users").document(uid)
                            .delete()
                            .addOnCompleteListener(deleteTask -> {
                                // Now delete the auth profile
                                authResult.getUser().delete()
                                        .addOnCompleteListener(userDeleteTask -> {
                                            // Now create a new user
                                            createNewTestUser(latch, success);
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    // User doesn't exist, create it
                    createNewTestUser(latch, success);
                });

        // Wait for the operations to complete
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        if (!completed) {
            Log.e("MoodWithPhotoTest", "Timed out waiting for test user setup");
            throw new RuntimeException("Timed out waiting for test user setup");
        }

        // Verify user creation was successful
        if (!success[0]) {
            Log.e("MoodWithPhotoTest", "Failed to create test user");
            throw new RuntimeException("Failed to create test user");
        }
    }

    private void createNewTestUser(CountDownLatch latch, boolean[] success) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userId = task.getResult().getUser().getUid();
                        Log.d("MoodWithPhotoTest", "Created auth user with ID: " + userId);

                        // Add user to Firestore
                        Map<String, Object> testUser = new HashMap<>();
                        testUser.put("userId", userId);
                        testUser.put("username", TEST_USERNAME);
                        testUser.put("dummyEmail", TEST_EMAIL); // Make sure this matches your User model
                        testUser.put("email", TEST_EMAIL); // Some implementations use email directly
                        testUser.put("password", TEST_PASSWORD);

                        db.collection("users")
                                .document(userId)
                                .set(testUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("MoodWithPhotoTest", "Test user created successfully in Firestore");
                                    success[0] = true;
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MoodWithPhotoTest", "Error creating test user in Firestore", e);
                                    latch.countDown();
                                });
                    } else {
                        Log.e("MoodWithPhotoTest", "Error creating authentication", task.getException());
                        latch.countDown();
                    }
                });
    }

    private void login() {
        Log.d("MoodWithPhotoTest", "Starting login process");

        // Make sure we're on the start screen
        try {
            onView(withId(R.id.tvStartLogin)).check(matches(isDisplayed()));
            onView(withId(R.id.tvStartLogin)).perform(click());
        } catch (Exception e) {
            Log.e("MoodWithPhotoTest", "Error accessing start screen", e);
            throw e;
        }

        Log.d("MoodWithPhotoTest", "Entering login credentials");
        try {
            // Enter credentials
            onView(withId(R.id.etUsername)).perform(typeText(TEST_USERNAME), closeSoftKeyboard());
            onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

            // Click login
            onView(withId(R.id.btnLogin)).perform(click());
        } catch (Exception e) {
            Log.e("MoodWithPhotoTest", "Error during login process", e);
            throw e;
        }

        Log.d("MoodWithPhotoTest", "Handling location permissions");
        try {
            CustomMatchers.handleLocationPermissionPopup();
        } catch (Exception e) {
            Log.e("MoodWithPhotoTest", "Error handling location permissions", e);
        }

        // Wait for login to complete and navigate to dashboard
        Log.d("MoodWithPhotoTest", "Waiting for login to complete");
        SystemClock.sleep(5000);

        // Verify login was successful by checking for dashboard elements
        Log.d("MoodWithPhotoTest", "Verifying successful login");
        try {
            onView(withId(R.id.add_mood_button)).check(matches(isDisplayed()));
            Log.d("MoodWithPhotoTest", "Login successful - dashboard visible");
        } catch (Exception e) {
            Log.e("MoodWithPhotoTest", "Login failed - dashboard not visible", e);

            // Try to get more diagnostic information
            Log.e("MoodWithPhotoTest", "Current visible elements:");
            try {
                onView(withId(R.id.tvLoginTitle)).check(matches(isDisplayed()));
                Log.e("MoodWithPhotoTest", "- Login screen is still visible");
            } catch (Exception ex) {
                // Not on login screen
            }

            throw new RuntimeException("Login failed - dashboard not visible", e);
        }
    }

    @Test
    public void testAddMoodWithPhoto() throws InterruptedException {
        try {
            // Login with test user
            Log.d("MoodWithPhotoTest", "Starting testAddMoodWithPhoto");
            login();

            // Navigate to add mood screen
            Log.d("MoodWithPhotoTest", "Navigating to add mood screen");
            onView(withId(R.id.add_mood_button)).perform(click());

            // Select a mood
            Log.d("MoodWithPhotoTest", "Selecting mood");
            onView(withId(R.id.spinnerEmotion)).perform(click());
            onView(withText("😄Happiness")).perform(click());

            // Add reason text
            Log.d("MoodWithPhotoTest", "Adding reason text");
            onView(withId(R.id.editReason)).perform(typeText("Testing with photo"), closeSoftKeyboard());

            // Inject test image using ActivityScenario
            Log.d("MoodWithPhotoTest", "Injecting test image");
            activityScenarioRule.getScenario().onActivity(activity -> {
                // Find the current fragment (InputDialog)
                try {
                    androidx.fragment.app.Fragment currentFragment = activity
                            .getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment)
                            .getChildFragmentManager()
                            .getFragments().get(0);

                    Log.d("MoodWithPhotoTest", "Current fragment type: " + currentFragment.getClass().getName());

                    if (currentFragment instanceof InputDialog) {
                        InputDialog inputDialog = (InputDialog) currentFragment;

                        // Use reflection to set the imageUri field and process it
                        try {
                            java.lang.reflect.Field imageUriField = InputDialog.class.getDeclaredField("imageUri");
                            imageUriField.setAccessible(true);
                            imageUriField.set(inputDialog, testImageUri);
                            Log.d("MoodWithPhotoTest", "Set imageUri field via reflection");

                            // Manually refresh image preview
                            android.widget.ImageView imagePreview = activity.findViewById(R.id.imagePreview);
                            if (imagePreview != null) {
                                activity.runOnUiThread(() -> {
                                    imagePreview.setImageURI(testImageUri);
                                    Log.d("MoodWithPhotoTest", "Updated image preview");
                                });
                            } else {
                                Log.e("MoodWithPhotoTest", "imagePreview view not found");
                            }

                            // If there's a processSelectedImage method, call it
                            try {
                                java.lang.reflect.Method processMethod = InputDialog.class.getDeclaredMethod("processSelectedImage", Uri.class);
                                processMethod.setAccessible(true);
                                processMethod.invoke(inputDialog, testImageUri);
                                Log.d("MoodWithPhotoTest", "Called processSelectedImage method");
                            } catch (NoSuchMethodException e) {
                                // Method doesn't exist, that's ok
                                Log.d("MoodWithPhotoTest", "processSelectedImage method not found");
                            }
                        } catch (Exception e) {
                            Log.e("MoodWithPhotoTest", "Error setting imageUri via reflection", e);
                        }
                    } else {
                        Log.e("MoodWithPhotoTest", "Current fragment is not InputDialog");
                    }
                } catch (Exception e) {
                    Log.e("MoodWithPhotoTest", "Error finding current fragment", e);
                }
            });

            // Wait for the image to be processed
            Log.d("MoodWithPhotoTest", "Waiting for image processing");
            SystemClock.sleep(2000);

            // Scroll the parent ScrollView to show the confirm button
            Log.d("MoodWithPhotoTest", "Scrolling to show confirm button");
            onView(withClassName(Matchers.equalTo(ScrollView.class.getName()))).perform(swipeUp());
            SystemClock.sleep(1000); // Wait for the scroll to complete
            // Try again with another swipe if needed
            try {
                onView(withClassName(Matchers.equalTo(ScrollView.class.getName()))).perform(swipeUp());
                SystemClock.sleep(1000);
            } catch (Exception e) {
                Log.d("MoodWithPhotoTest", "Second swipe attempt failed, continuing anyway", e);
            }

            // Submit the mood
            Log.d("MoodWithPhotoTest", "Submitting mood");
            try {
                onView(withId(R.id.buttonConfirm)).perform(click());
            } catch (Exception e) {
                Log.e("MoodWithPhotoTest", "Error clicking confirm button", e);
                throw e;
            }

            // Wait for the mood to be saved
            Log.d("MoodWithPhotoTest", "Waiting for mood to be saved");
            SystemClock.sleep(3000);

            // Verify the mood was created
            Log.d("MoodWithPhotoTest", "Verifying mood was created");
            onView(withText("😄Happiness")).check(matches(isDisplayed()));

            // Click on the mood to verify the image was saved
            Log.d("MoodWithPhotoTest", "Clicking on mood to verify image");
            onView(withText("😄Happiness")).perform(click());

            // Verify the image is displayed
            Log.d("MoodWithPhotoTest", "Checking if image is displayed");
            onView(withId(R.id.imagePreview)).check(matches(isDisplayed()));

            // Return to dashboard
            Log.d("MoodWithPhotoTest", "Returning to dashboard");

            // Scroll the ScrollView again to find the cancel button
            onView(withClassName(Matchers.equalTo(ScrollView.class.getName()))).perform(swipeUp());
            SystemClock.sleep(1000);

            try {
                onView(withId(R.id.buttonCancel)).perform(click());
            } catch (Exception e) {
                Log.e("MoodWithPhotoTest", "Error clicking cancel button", e);
                throw e;
            }

            Log.d("MoodWithPhotoTest", "testAddMoodWithPhoto completed successfully");
        } catch (Exception e) {
            Log.e("MoodWithPhotoTest", "Test failed with exception", e);
            throw e;
        }
    }
    @Test
    public void testAddMoodWithLargePhoto() throws IOException, InterruptedException {
        // Create a large test image (>64KB)
        Uri largeImageUri = createLargeTestImage(800, 800, Bitmap.CompressFormat.JPEG, 90, "large_test_image.jpg");

        // Verify the image is indeed >64KB
        File largeFile = new File(largeImageUri.getPath());
        assertTrue("Test image should be >64KB", largeFile.length() > 64 * 1024);
        Log.d("MoodWithPhotoTest", "Large test image size: " + largeFile.length() + " bytes");

        // Login with test user
        login();

        // Navigate to add mood screen
        onView(withId(R.id.add_mood_button)).perform(click());

        // Select a mood
        onView(withId(R.id.spinnerEmotion)).perform(click());
        onView(withText("😄Happiness")).perform(click());

        // Add reason text
        onView(withId(R.id.editReason)).perform(typeText("Testing with large photo"), closeSoftKeyboard());

        // Inject the large image using ActivityScenario
        activityScenarioRule.getScenario().onActivity(activity -> {
            try {
                // Find the InputDialog fragment
                androidx.fragment.app.Fragment currentFragment = activity
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment)
                        .getChildFragmentManager()
                        .getFragments().get(0);

                if (currentFragment instanceof InputDialog) {
                    InputDialog inputDialog = (InputDialog) currentFragment;

                    // Use reflection to set the imageUri and fileSize
                    java.lang.reflect.Field imageUriField = InputDialog.class.getDeclaredField("imageUri");
                    imageUriField.setAccessible(true);
                    imageUriField.set(inputDialog, largeImageUri);

                    java.lang.reflect.Field fileSizeField = InputDialog.class.getDeclaredField("fileSize");
                    fileSizeField.setAccessible(true);
                    fileSizeField.set(inputDialog, (int)largeFile.length());

                    // Update the preview
                    android.widget.ImageView imagePreview = activity.findViewById(R.id.imagePreview);
                    if (imagePreview != null) {
                        activity.runOnUiThread(() -> {
                            imagePreview.setImageURI(largeImageUri);
                        });
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for processing
        SystemClock.sleep(2000);

        // Scroll to show confirm button
        onView(withClassName(Matchers.equalTo(ScrollView.class.getName()))).perform(swipeUp());
        SystemClock.sleep(1000);

        // Attempt to submit the mood
        onView(withId(R.id.buttonConfirm)).perform(click());

        // Wait for error message
        SystemClock.sleep(1000);


        // Verify we're still in the input dialog (not navigated away)
        onView(withId(R.id.spinnerEmotion)).check(matches(isDisplayed()));
        onView(withId(R.id.editReason)).check(matches(isDisplayed()));

        // Clean up by canceling
        onView(withId(R.id.buttonCancel)).perform(click());
    }

    private Uri createLargeTestImage(int width, int height, Bitmap.CompressFormat format,
                                     int quality, String filename) throws IOException {
        // Create a detailed bitmap that will be >64KB when saved
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Fill with a complex pattern that will create a large file
        Paint paint = new Paint();
        for (int i = 0; i < 1000; i++) {
            paint.setColor(Color.rgb(
                    (int)(Math.random() * 255),
                    (int)(Math.random() * 255),
                    (int)(Math.random() * 255)
            ));
            canvas.drawCircle(
                    (float)(Math.random() * width),
                    (float)(Math.random() * height),
                    (float)(Math.random() * 50 + 10),
                    paint
            );
        }

        // Add text
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);
        canvas.drawText("Large Test Image", width/4, height/2, paint);

        // Save to file
        File file = new File(context.getCacheDir(), filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(format, quality, out);
        }

        return Uri.fromFile(file);
    }


    @After
    public void tearDown() {
        Log.d("MoodWithPhotoTest", "Starting test cleanup");

        // Clean up test files
        try {
            new File(context.getCacheDir(), "test_image.jpg").delete();
            Log.d("MoodWithPhotoTest", "Test image files deleted");
        } catch (Exception e) {
            Log.e("MoodWithPhotoTest", "Error deleting test image files", e);
        }

        // Clean up test user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            Log.d("MoodWithPhotoTest", "Cleaning up test user: " + uid);

            // Delete user data and auth profile
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // First delete any moods
            db.collection("users").document(uid).collection("moods")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("MoodWithPhotoTest", "Found " + queryDocumentSnapshots.size() + " mood documents to delete");
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }

                        // Then delete the user document
                        db.collection("users").document(uid)
                                .delete()
                                .addOnCompleteListener(task -> {
                                    Log.d("MoodWithPhotoTest", "User document deleted");

                                    // Finally delete the auth profile
                                    user.delete().addOnCompleteListener(deleteTask -> {
                                        Log.d("MoodWithPhotoTest", "User auth profile deleted");
                                    });
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MoodWithPhotoTest", "Error cleaning up user mood data", e);
                    });
        } else {
            Log.d("MoodWithPhotoTest", "No authenticated user to clean up");
        }

        Log.d("MoodWithPhotoTest", "Test cleanup complete");
    }
}