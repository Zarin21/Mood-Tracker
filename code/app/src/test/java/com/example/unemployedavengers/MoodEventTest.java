/**
 * MoodEventTest.java
 *
 * Unit tests for the MoodEvent model class which represents a user's mood entry
 * in the UnemployedAvengers application. Tests cover:
 * - Object construction (both parameterized and default constructors)
 * - All getter and setter methods
 * - Location data handling (latitude/longitude)
 * - Edge cases (null values, empty strings)
 * - Automatic ID generation
 * - Default value initialization
 *
 * Testing Approach:
 * - Uses JUnit 4 test framework
 * - Follows Arrange-Act-Assert pattern
 * - Tests both normal operation and edge cases
 * - Verifies proper null handling
 * - Tests field validation where applicable
 */
package com.example.unemployedavengers;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.unemployedavengers.models.MoodEvent;

public class MoodEventTest {

    private MoodEvent moodEvent;
    private final String TEST_MOOD = "😊Happiness";
    private final String TEST_REASON = "Had a great day!";
    private final String TEST_SITUATION = "At the park";
    private final String TEST_RADIO_SITUATION = "Alone";
    private final long TEST_TIME = System.currentTimeMillis();
    private final String TEST_IMAGE_URI = "content://image.jpg";
    private final String TEST_USER_ID = "user123";

    @Before
    public void setUp() {
        moodEvent = new MoodEvent(
                TEST_MOOD,
                TEST_REASON,
                TEST_SITUATION,
                TEST_TIME,
                TEST_RADIO_SITUATION,
                TEST_IMAGE_URI,
                true
        );
        moodEvent.setUserId(TEST_USER_ID);
    }

    //----- Constructor Tests -----//
    @Test
    public void constructor_ShouldSetAllFieldsCorrectly() {
        assertEquals(TEST_MOOD, moodEvent.getMood());
        assertEquals(TEST_REASON, moodEvent.getReason());
        assertEquals(TEST_SITUATION, moodEvent.getSituation());
        assertEquals(TEST_TIME, moodEvent.getTime());
        assertEquals(TEST_RADIO_SITUATION, moodEvent.getRadioSituation());
        assertEquals(TEST_IMAGE_URI, moodEvent.getImageUri());
        assertTrue(moodEvent.getPublicStatus());
        assertNotNull(moodEvent.getId());
    }

    @Test
    public void emptyConstructor_ShouldInitializeWithDefaults() {
        MoodEvent emptyMood = new MoodEvent();
        assertNull(emptyMood.getMood());
        assertNull(emptyMood.getReason());
        assertEquals(0, emptyMood.getTime());
        assertFalse(emptyMood.getExisted());
        assertNull(emptyMood.getId()); // Expect null for empty constructor
    }

    //----- Getter/Setter Tests -----//
    @Test
    public void settersAndGetters_ShouldWorkForAllFields() {
        // Test mutable fields
        String newMood = "😢Sadness";
        moodEvent.setMood(newMood);
        assertEquals(newMood, moodEvent.getMood());

        String newReason = "Missed my bus";
        moodEvent.setReason(newReason);
        assertEquals(newReason, moodEvent.getReason());

        moodEvent.setPublicStatus(false);
        assertFalse(moodEvent.getPublicStatus());

        moodEvent.setExisted(true);
        assertTrue(moodEvent.getExisted());

        String newId = "new-id-123";
        moodEvent.setId(newId);
        assertEquals(newId, moodEvent.getId());

        String newUserId = "user456";
        moodEvent.setUserId(newUserId);
        assertEquals(newUserId, moodEvent.getUserId());
    }

    //----- Location Tests -----//
    @Test
    public void locationFields_ShouldStoreCoordinatesCorrectly() {
        moodEvent.setLatitude(40.7128);
        moodEvent.setLongitude(-74.0060);
        moodEvent.setHasLocation(true);

        assertEquals(40.7128, moodEvent.getLatitude(), 0.001);
        assertEquals(-74.0060, moodEvent.getLongitude(), 0.001);
        assertTrue(moodEvent.getHasLocation());
    }

    @Test
    public void hasLocation_ShouldDefaultToFalse() {
        assertFalse(moodEvent.getHasLocation());
    }

    //----- Edge Cases -----//
    @Test
    public void setImageUri_ShouldHandleNullValues() {
        moodEvent.setImageUri(null);
        assertNull(moodEvent.getImageUri());
    }

    @Test
    public void setEmptyStrings_ShouldWork() {
        moodEvent.setMood("");
        moodEvent.setReason("");
        moodEvent.setSituation("");

        assertEquals("", moodEvent.getMood());
        assertEquals("", moodEvent.getReason());
        assertEquals("", moodEvent.getSituation());
    }

    //----- ID Generation Tests -----//
    @Test
    public void id_ShouldBeGeneratedAutomatically() {
        MoodEvent newMood = new MoodEvent("😢Sadness","unit testing","unit testing situation", System.currentTimeMillis(),"None","");
        assertNotNull(newMood.getId());
        assertFalse(newMood.getId().isEmpty());
    }

    @Test
    public void userName_ShouldBeSettable() {
        String testName = "TestUser";
        moodEvent.setUserName(testName);
        assertEquals(testName, moodEvent.getUserName());
    }
}