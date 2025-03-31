/**
 * FriendMoodEventsViewModelTest.java
 *
 * Unit tests for the FriendMoodEventsViewModel class in the UnemployedAvengers application.
 *
 * This test class verifies the behavior of the ViewModel responsible for managing
 * and exposing friend mood events data to the UI layer using LiveData.
 *
 * Key Test Cases:
 * - Verifies that mood events are properly set and exposed through LiveData
 * - Ensures observers are notified when mood events are updated
 * - Tests the integration between ViewModel and LiveData components
 *
 * Testing Approach:
 * - Uses Mockito to mock Observer for verifying LiveData updates
 * - Leverages InstantTaskExecutorRule for synchronous LiveData testing
 * - Follows standard Arrange-Act-Assert pattern
 *
 */
package com.example.unemployedavengers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.unemployedavengers.models.FriendMoodEventsViewModel;
import com.example.unemployedavengers.models.MoodEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FriendMoodEventsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule(); // Ensures LiveData runs synchronously

    private FriendMoodEventsViewModel viewModel;
    private Observer<List<MoodEvent>> observer;

    @Before
    public void setUp() {
        viewModel = new FriendMoodEventsViewModel();
        observer = mock(Observer.class);
        viewModel.getMoodEvents().observeForever(observer);
    }

    @Test
    public void testSetMoodEvents_updatesLiveData() {
        // Arrange
        MoodEvent mood1 = new MoodEvent("Happy", "Feeling great!","Social situation",System.currentTimeMillis(),"None","");
        MoodEvent mood2 = new MoodEvent("Sadness", "Feeling Bad","Social situation",System.currentTimeMillis(),"None","");
        List<MoodEvent> moodEvents = Arrays.asList(mood1, mood2);

        // Act
        viewModel.setMoodEvents(moodEvents);

        // Assert
        assertEquals(moodEvents, viewModel.getMoodEvents().getValue());
        verify(observer).onChanged(moodEvents); // Ensure observer was notified
    }
}
