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
