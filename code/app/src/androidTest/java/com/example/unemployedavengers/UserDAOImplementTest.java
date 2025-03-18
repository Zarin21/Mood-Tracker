package com.example.unemployedavengers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserDAOImplementTest {

    private UserDAOImplement userDAO;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @BeforeClass
    public static void setup(){
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void setUp() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        auth.signOut(); // Ensure no user is signed in before each test
        userDAO = new UserDAOImplement();
    }

    @After
    public void tearDown() {
        String projectId = "lab07-649c2";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        auth.signOut();
    }

    @Test
    public void testSignUpUser() throws Exception {
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String password = "password123";
        Tasks.await(userDAO.signUpUser(uniqueUsername, password), 30, TimeUnit.SECONDS);
        FirebaseUser currentUser = auth.getCurrentUser();
        assertNotNull("User should be signed in after signup", currentUser);
    }

    @Test
    public void testSignInUser() throws Exception {
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String password = "password123";
        Tasks.await(userDAO.signUpUser(uniqueUsername, password), 30, TimeUnit.SECONDS);
        auth.signOut();
        Tasks.await(userDAO.signInUser(uniqueUsername, password), 30, TimeUnit.SECONDS);
        FirebaseUser currentUser = auth.getCurrentUser();
        assertNotNull("User should be signed in after signin", currentUser);
    }

    @Test
    public void testCheckUserExists() throws Exception {
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String password = "password123";
        Tasks.await(userDAO.signUpUser(uniqueUsername, password), 30, TimeUnit.SECONDS);
        boolean exists = Tasks.await(userDAO.checkUserExists(uniqueUsername), 30, TimeUnit.SECONDS);
        assertTrue("User should exist in Firestore", exists);
    }

    @Test
    public void testChangeUsername() throws Exception {
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String password = "password123";
        Tasks.await(userDAO.signUpUser(uniqueUsername, password), 30, TimeUnit.SECONDS);
        String newUsername = uniqueUsername + "new";
        Tasks.await(userDAO.changeUsername(newUsername), 30, TimeUnit.SECONDS);
        User updatedUser = Tasks.await(userDAO.getUserByUsername(newUsername), 30, TimeUnit.SECONDS);
        assertNotNull("User should exist with new username", updatedUser);
        assertEquals("Username should be updated", newUsername, updatedUser.getUsername());
    }

    @Test
    public void testSearchUsers() throws Exception {
        String searcherUsername = "searcher" + System.currentTimeMillis();
        String password = "password123";
        Tasks.await(userDAO.signUpUser(searcherUsername, password), 30, TimeUnit.SECONDS);
        String targetUsername = "searchTarget" + System.currentTimeMillis();
        auth.signOut();
        Tasks.await(userDAO.signUpUser(targetUsername, password), 30, TimeUnit.SECONDS);
        auth.signOut();
        Tasks.await(userDAO.signInUser(searcherUsername, password), 30, TimeUnit.SECONDS);
        String searchPrefix = targetUsername.substring(0, 6);
        List<User> results = Tasks.await(userDAO.searchUsers(searchPrefix), 30, TimeUnit.SECONDS);
        for (User user : results) {
            assertNotEquals("Search results should not contain the searcher", searcherUsername, user.getUsername());
        }
    }
    @Test
    public void testUniqueUserNameSignUp() throws Exception {
        String username = "duplicateUser";
        String password = "password123";

        Tasks.await(userDAO.signUpUser(username, password), 30, TimeUnit.SECONDS);
        auth.signOut();

        Exception thrownException = null;
        try {
            Tasks.await(userDAO.signUpUser(username, password), 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            thrownException = e;
        }
        assertNotNull("Expected an exception when signing up with a duplicate username", thrownException);

        String message = thrownException.getMessage().toLowerCase();
        assertTrue("Exception message should indicate duplicate email or collision",
                message.contains("already in use") || message.contains("duplicate"));
    }
    @Test
    public void testChangePassword() throws Exception {
        String username = "changePassUser";
        String oldPassword = "oldPassword";
        Tasks.await(userDAO.signUpUser(username, oldPassword), 30, TimeUnit.SECONDS);
        FirebaseUser currentUser = auth.getCurrentUser();
        assertNotNull("User should be signed in after signup", currentUser);
        User user = new User(currentUser.getUid(), username, username.toLowerCase() + "@example.com", oldPassword);
        String newPassword = "newPassword";
        Tasks.await(userDAO.changePassword(user, newPassword), 30, TimeUnit.SECONDS);
        auth.signOut();
        Tasks.await(userDAO.signInUser(username, newPassword), 30, TimeUnit.SECONDS);
        currentUser = auth.getCurrentUser();
        assertNotNull("User should be signed in with new password", currentUser);
    }

    @Test
    public void testResetPassword() throws Exception {
        String username = "resetPassUser";
        String initialPassword = "initialPassword";
        Tasks.await(userDAO.signUpUser(username, initialPassword), 30, TimeUnit.SECONDS);
        String newPassword = "resetNewPassword";

        Tasks.await(userDAO.resetPassword(username, newPassword), 30, TimeUnit.SECONDS);
        auth.signOut();
        Tasks.await(userDAO.signInUser(username, newPassword), 30, TimeUnit.SECONDS);
        FirebaseUser currentUser = auth.getCurrentUser();
        assertNotNull("User should be signed in with reset password", currentUser);
    }
}
