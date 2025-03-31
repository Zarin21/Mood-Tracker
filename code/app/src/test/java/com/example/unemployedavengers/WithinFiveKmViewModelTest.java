package com.example.unemployedavengers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.WithinFiveKmViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class WithinFiveKmViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private WithinFiveKmViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new WithinFiveKmViewModel();
    }

    @Test
    public void testSetMoodEvents_updatesLiveData() {
        // Create mock data
        MoodEvent event1 = Mockito.mock(MoodEvent.class);
        MoodEvent event2 = Mockito.mock(MoodEvent.class);
        List<MoodEvent> moodEvents = Arrays.asList(event1, event2);

        // Observe LiveData
        viewModel.getMoodEvents().observeForever(events -> {});

        // Set data
        viewModel.setMoodEvents(moodEvents);

        // Verify LiveData is updated
        assertNotNull(viewModel.getMoodEvents().getValue());
        assertEquals(2, viewModel.getMoodEvents().getValue().size());
    }
}

