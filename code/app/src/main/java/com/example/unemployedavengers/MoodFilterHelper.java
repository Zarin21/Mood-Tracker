/**
 * MoodFilterHelper - Utility class for filtering mood events based on multiple criteria.
 *
 * Design Pattern:
 * - Implements Strategy pattern for different filtering algorithms
 * - Follows Single Responsibility Principle for filter operations
 * - Uses Functional approach with static methods
 *
 * Key Responsibilities:
 * 1. Multi-criteria Filtering:
 *    - Combines mood type, reason text, and time-based filters
 *    - Applies filters sequentially in fixed order
 *    - Maintains immutability of original data
 *
 * 2. Filter Operations:
 *    - Mood type matching (case-sensitive contains)
 *    - Reason text word matching (case-insensitive)
 *    - Recent week time window filtering
 *
 * 3. Performance:
 *    - Optimized for linear time complexity O(n)
 *    - Minimal object creation during filtering
 *    - Early termination for empty filter conditions
 *
 * Technical Implementation:
 * - Static utility methods for stateless operation
 * - Defensive copying of input collections
 * - Null-safe comparisons
 * - Time calculations using system milliseconds
 *
 * Outstanding Issues/TODOs:
 * 1. No support for complex boolean filter combinations
 * 2. Limited to single-word reason filtering
 * 3. Hardcoded 7-day window for recent filter
 * 4. Could benefit from predicate composition
 * 5. No localization support for time calculations
 *
 * Dependencies:
 * - MoodEvent model class
 * - Java Collections Framework
 *
 * Usage Example:
 * List<MoodEvent> filtered = MoodFilterHelper.filterMoodEvents(
 *     events,
 *     true,
 *     false,
 *     true,
 *     "Happy",
 *     ""
 * );
 *
 * @see MoodEvent
 */
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