
package com.example.unemployedavengers.models;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a mood event with associated details like mood, reason, situation, time, and location.
 * <p>
 * This model encapsulates information regarding the user's mood at a given time, reasons behind the mood,
 * the situation and context in which the mood was experienced, and optional location details. The class also
 * handles the public status of the event (whether it's publicly viewable) and contains a unique identifier.
 * </p>
 */
public class MoodEvent implements Serializable {

    // Attributes
    private String userName;
    private String mood;
    private String imageUri;
    private String reason;
    private String situation;
    private String radioSituation;
    private long time;
    private boolean existed;
    private String id;
    private String userId;
    private boolean publicStatus = true;
    private double latitude;
    private double longitude;
    private boolean hasLocation = false;

    /**
     * Gets the username of the user who created the mood event.
     *
     * @return the username as a string.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username of the user who created the mood event.
     *
     * @param userName the username to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the mood of the user.
     *
     * @return the mood as a string.
     */
    public String getMood() {
        return mood;
    }

    /**
     * Sets the mood of the user.
     *
     * @param mood the mood to set.
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * Gets the reason behind the user's mood.
     *
     * @return the reason as a string.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason behind the user's mood.
     *
     * @param reason the reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the situation in which the mood was experienced.
     *
     * @return the situation as a string.
     */
    public String getSituation() {
        return situation;
    }

    /**
     * Sets the situation in which the mood was experienced.
     *
     * @param situation the situation to set.
     */
    public void setSituation(String situation) {
        this.situation = situation;
    }

    /**
     * Gets the time when the mood event was created.
     *
     * @return the time as a long (timestamp).
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the time when the mood event was created.
     * This value is set by the system and cannot be altered by the user.
     *
     * @param time the timestamp to set.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Gets the public status of the mood event.
     *
     * @return true if the mood event is public, false otherwise.
     */
    public boolean getPublicStatus() {
        return publicStatus;
    }

    /**
     * Sets the public status of the mood event.
     *
     * @param publicStatus the public status to set.
     */
    public void setPublicStatus(boolean publicStatus) {
        this.publicStatus = publicStatus;
    }

    /**
     * Gets whether the mood event has been marked as "existed".
     *
     * @return true if the event is marked as existed, false otherwise.
     */
    public boolean getExisted() {
        return existed;
    }

    /**
     * Sets whether the mood event is marked as "existed".
     *
     * @param existed the existed status to set.
     */
    public void setExisted(boolean existed) {
        this.existed = existed;
    }

    /**
     * Gets the unique identifier of the mood event.
     *
     * @return the ID as a string.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the mood event.
     *
     * @param id the ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user ID of the person who created the mood event.
     *
     * @return the user ID as a string.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the person who created the mood event.
     *
     * @param userId the user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the radio situation associated with the mood event.
     *
     * @return the radio situation as a string.
     */
    public String getRadioSituation() {
        return radioSituation;
    }

    /**
     * Sets the radio situation associated with the mood event.
     *
     * @param radioSituation the radio situation to set.
     */
    public void setRadioSituation(String radioSituation) {
        this.radioSituation = radioSituation;
    }

    /**
     * Gets the URI of the image associated with the mood event.
     *
     * @return the image URI as a string.
     */
    public String getImageUri() {
        return imageUri;
    }

    /**
     * Sets the URI of the image associated with the mood event.
     *
     * @param imageUri the image URI to set.
     */
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * Gets the latitude of the location where the mood event took place.
     *
     * @return the latitude as a double.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of the location where the mood event took place.
     *
     * @param latitude the latitude to set.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude of the location where the mood event took place.
     *
     * @return the longitude as a double.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of the location where the mood event took place.
     *
     * @param longitude the longitude to set.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets whether the mood event has a location associated with it.
     *
     * @return true if the event has location data, false otherwise.
     */
    public boolean getHasLocation() {
        return hasLocation;
    }

    /**
     * Sets whether the mood event has location data.
     *
     * @param hasLocation true if the event has location data, false otherwise.
     */
    public void setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
    }

    /**
     * Default constructor for Firebase and empty initialization.
     */
    public MoodEvent() {
        // Empty constructor for Firebase deserialization
    }
    /**
     * Constructor to create a MoodEvent object without specifying publicStatus.
     * This constructor defaults the publicStatus to true.
     *
     * @param mood         the mood for the event.
     * @param reason       the reason behind the mood.
     * @param situation    the situation in which the mood occurred.
     * @param time         the time when the mood event was created.
     * @param radioSituation the radio situation related to the event.
     * @param imageUri     the URI of the image associated with the event.
     */
    public MoodEvent(String mood, String reason, String situation, long time, String radioSituation, String imageUri) {
        this.mood = mood;
        this.reason = reason;
        this.situation = situation;
        this.time = time;
        this.radioSituation = radioSituation;
        this.imageUri = imageUri;
        this.publicStatus = true; // Default to public
        this.id = String.valueOf(UUID.randomUUID());
    }

    /**
     * Constructor to create a MoodEvent object with all parameters, including publicStatus.
     *
     * @param mood         the mood for the event.
     * @param reason       the reason behind the mood.
     * @param situation    the situation in which the mood occurred.
     * @param time         the time when the mood event was created.
     * @param radioSituation the radio situation related to the event.
     * @param imageUri     the URI of the image associated with the event.
     * @param publicStatus whether the event is public or not.
     */
    public MoodEvent(String mood, String reason, String situation, long time, String radioSituation, String imageUri, boolean publicStatus) {
        this.mood = mood;
        this.reason = reason;
        this.situation = situation;
        this.time = time;
        this.radioSituation = radioSituation;
        this.imageUri = imageUri;
        this.publicStatus = publicStatus;
        this.id = String.valueOf(UUID.randomUUID());
    }
}
