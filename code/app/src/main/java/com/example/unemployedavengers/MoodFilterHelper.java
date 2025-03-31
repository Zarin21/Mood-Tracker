package com.example.unemployedavengers;

import com.example.unemployedavengers.models.MoodEvent;
import java.util.ArrayList;
import java.util.List;

public class MoodFilterHelper {
    /**
     * Filters mood events based on specified criteria
     *
     * @param events List of original mood events
     * @param filterByMood Whether to filter by mood
     * @param filterByReason Whether to filter by reason
     * @param filterByWeek Whether to filter by recent week
     * @param moodType Mood type to filter (if filterByMood is true)
     * @param reasonText Reason text to filter (if filterByReason is true)
     * @return Filtered list of mood events
     */
    public static List<MoodEvent> filterMoodEvents(
            List<MoodEvent> events,
            boolean filterByMood,
            boolean filterByReason,
            boolean filterByWeek,
            String moodType,
            String reasonText) {

        // If no filters are applied, return the original list
        if (!filterByMood && !filterByReason && !filterByWeek) {
            return new ArrayList<>(events);
        }

        List<MoodEvent> filteredEvents = new ArrayList<>(events);

        // Filter by mood
        if (filterByMood) {
            filteredEvents = filterByMood(filteredEvents, moodType);
        }

        // Filter by reason
        if (filterByReason) {
            filteredEvents = filterByReason(filteredEvents, reasonText);
        }

        // Filter by recent week
        if (filterByWeek) {
            filteredEvents = filterByRecentWeek(filteredEvents);
        }

        return filteredEvents;
    }

    /**
     * Filter events by mood type
     */
    private static List<MoodEvent> filterByMood(List<MoodEvent> events, String moodType) {
        List<MoodEvent> filteredEvents = new ArrayList<>();
        for (MoodEvent event : events) {
            if (event.getMood() != null && event.getMood().contains(moodType)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    /**
     * Filter events by reason text
     */
    private static List<MoodEvent> filterByReason(List<MoodEvent> events, String reasonText) {
        List<MoodEvent> filteredEvents = new ArrayList<>();
        for (MoodEvent event : events) {
            if (event.getReason() != null) {
                String[] reasonWords = event.getReason().split("\\s+");
                for (String word : reasonWords) {
                    if (word.equalsIgnoreCase(reasonText)) {
                        filteredEvents.add(event);
                        break;
                    }
                }
            }
        }
        return filteredEvents;
    }

    /**
     * Filter events from the recent week
     */
    private static List<MoodEvent> filterByRecentWeek(List<MoodEvent> events) {
        long currentTime = System.currentTimeMillis();
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;

        List<MoodEvent> filteredEvents = new ArrayList<>();
        for (MoodEvent event : events) {
            if (event.getTime() >= (currentTime - sevenDaysMillis)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }
}