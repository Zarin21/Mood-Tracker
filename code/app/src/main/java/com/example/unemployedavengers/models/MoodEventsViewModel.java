package com.example.unemployedavengers.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * ViewModel class for managing and exposing a list of MoodEvent objects.
 * <p>
 * This ViewModel is responsible for storing and managing the list of MoodEvent objects.
 * It provides methods for retrieving the list of events (as LiveData) and updating the list.
 * </p>
 */
public class MoodEventsViewModel extends ViewModel {
    private final MutableLiveData<List<MoodEvent>> moodEvents = new MutableLiveData<>();

    /**
     * Retrieves the LiveData list of MoodEvent objects.
     * <p>
     * The list is observed for changes, and any updates will be reflected in the UI.
     * </p>
     *
     * @return the LiveData object containing the list of MoodEvent objects.
     */
    public LiveData<List<MoodEvent>> getMoodEvents() {
        return moodEvents;
    }

    /**
     * Updates the list of MoodEvent objects stored in the ViewModel.
     * <p>
     * This method sets the new list of MoodEvent objects, which will be observed by any observers
     * of the `moodEvents` LiveData. Any updates to this list will trigger UI updates.
     * </p>
     *
     * @param events the new list of MoodEvent objects to set.
     */
    public void setMoodEvents(List<MoodEvent> events) {
        moodEvents.setValue(events);
    }
}

