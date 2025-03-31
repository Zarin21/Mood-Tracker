package com.example.unemployedavengers;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.unemployedavengers.models.MoodEvent;

import java.util.Arrays;
import java.util.List;

public class MoodFilterHelperTest {

    @Test
    public void filterMoodEvents_NoFilters_ReturnsAllEvents() {
        // Arrange
        long currentTime = System.currentTimeMillis();
        MoodEvent mood1 = new MoodEvent("Happy", "Feeling great!", "Social", currentTime, "None", "");
        MoodEvent mood2 = new MoodEvent("Sadness", "Feeling bad", "Alone", currentTime, "None", "");
        List<MoodEvent> moodEvents = Arrays.asList(mood1, mood2);

        // Act
        List<MoodEvent> filteredEvents = MoodFilterHelper.filterMoodEvents(
                moodEvents,
                false,
                false,
                false,
                "",
                ""
        );

        // Assert
        assertEquals(2, filteredEvents.size());
        assertTrue(filteredEvents.contains(mood1));
        assertTrue(filteredEvents.contains(mood2));
    }

    @Test
    public void filterMoodEvents_ByMood_ReturnsCorrectEvents() {
        // Arrange
        long currentTime = System.currentTimeMillis();
        MoodEvent mood1 = new MoodEvent("Happy", "Feeling great!", "Social", currentTime, "None", "");
        MoodEvent mood2 = new MoodEvent("Sadness", "Feeling bad", "Alone", currentTime, "None", "");
        List<MoodEvent> moodEvents = Arrays.asList(mood1, mood2);

        // Act
        List<MoodEvent> filteredEvents = MoodFilterHelper.filterMoodEvents(
                moodEvents,
                true,
                false,
                false,
                "Happy",
                ""
        );

        // Assert
        assertEquals(1, filteredEvents.size());
        assertTrue(filteredEvents.contains(mood1));
    }


    @Test
    public void filterMoodEvents_ByRecentWeek_ReturnsRecentEvents() {
        // Arrange
        long currentTime = System.currentTimeMillis();
        MoodEvent recentMood = new MoodEvent("Happy", "Feeling great!", "Social", currentTime, "None", "");
        MoodEvent oldMood = new MoodEvent("Sadness", "Feeling bad", "Alone",
                currentTime - (8L * 24 * 60 * 60 * 1000), // 8 days ago
                "None", ""
        );
        List<MoodEvent> moodEvents = Arrays.asList(recentMood, oldMood);

        // Act
        List<MoodEvent> filteredEvents = MoodFilterHelper.filterMoodEvents(
                moodEvents,
                false,
                false,
                true,
                "",
                ""
        );

        // Assert
        assertEquals(1, filteredEvents.size());
        assertTrue(filteredEvents.contains(recentMood));
    }
}