package com.example.unemployedavengers.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * ViewModel class for managing and providing mood events that are within a 5 km radius.
 * <p>
 * This ViewModel holds a list of mood events that are located within 5 km of the user's current location.
 * It uses LiveData to observe changes in the list of events and provides methods to retrieve and update the data.
 * </p>
 */
public class WithinFiveKmViewModel extends ViewModel {
    private final MutableLiveData<List<MoodEvent>> fiveKmEvents = new MutableLiveData<>();

    /**
     * Gets the LiveData for the list of mood events within 5 km.
     * <p>
     * This method allows observers to monitor changes in the list of mood events within 5 km of the user.
     * </p>
     *
     * @return A LiveData object containing the list of mood events within 5 km.
     */

    public LiveData<List<MoodEvent>> getMoodEvents() {
        return fiveKmEvents;
    }

    /**
     * Updates the list of mood events within 5 km.
     * <p>
     * This method allows the list of mood events to be updated. When the list is set, any active observers
     * of the LiveData will be notified of the change.
     * </p>
     *
     * @param events A list of mood events within 5 km to set in the LiveData.
     */
    public void setMoodEvents(List<MoodEvent> events) {
        fiveKmEvents.setValue(events);
    }
}
