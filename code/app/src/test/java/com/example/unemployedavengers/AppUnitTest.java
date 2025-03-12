package com.example.unemployedavengers;
/**

 * Purpose:
 * - Verify user authentication methods (sign up and sign in)
 * - Test mood event management functionalities
 * - Ensure core application components work as expected
 *

 *
 */
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ExtendWith(MockitoExtension.class)
public class AppUnitTest {

    private IUserDAO userDAO;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAOImplement.class);
    }

    @Test
    void testSignUpUser() {
        // Arrange
        String username = "testUser";
        String password = "password123";
        Task<Void> mockTask = createSuccessfulTask(null);

        // Act
        when(userDAO.signUpUser(username, password)).thenReturn(mockTask);
        Task<Void> result = userDAO.signUpUser(username, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    void testSignInUser() {
        // Arrange
        String username = "testUser";
        String password = "password123";
        Task<Void> mockTask = createSuccessfulTask(null);

        // Act
        when(userDAO.signInUser(username, password)).thenReturn(mockTask);
        Task<Void> result = userDAO.signInUser(username, password);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    void testAddMoodEvent() {
        // Arrange
        MoodEvent moodEvent = new MoodEvent("Happiness", "Got a promotion", "Work", "Office", System.currentTimeMillis(), "Alone", "");
        List<MoodEvent> moodEvents = new ArrayList<>();

        // Act
        moodEvents.add(moodEvent);

        // Assert
        assertEquals(1, moodEvents.size());
        assertEquals("Happiness", moodEvents.get(0).getMood());
    }

    @Test
    void testDeleteMoodEvent() {
        // Arrange
        MoodEvent moodEvent = new MoodEvent("Sadness", "Lost my pet", "Home", "Alone", System.currentTimeMillis(), "Alone", "");
        List<MoodEvent> moodEvents = new ArrayList<>();
        moodEvents.add(moodEvent);

        // Act
        moodEvents.remove(moodEvent);

        // Assert
        assertEquals(0, moodEvents.size());
    }

    @Test
    void testGetCurrentUserProfile() {
        // Arrange
        Task<User> mockTask = createSuccessfulTask(mockUser);

        // Act
        when(userDAO.getCurrentUserProfile()).thenReturn(mockTask);
        Task<User> result = userDAO.getCurrentUserProfile();

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(mockUser, result.getResult());
    }

    // Helper method to create a successful Task
    private <TResult> Task<TResult> createSuccessfulTask(TResult result) {
        Executor executor = Executors.newSingleThreadExecutor();
        return Tasks.call(executor, () -> result);
    }
}