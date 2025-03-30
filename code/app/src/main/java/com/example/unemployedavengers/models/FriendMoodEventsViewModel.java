package com.example.unemployedavengers.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * ViewModel that holds the list of mood events related to a user's friends.
 * The ViewModel allows observation of the list of friend mood events and provides methods
 * to update and retrieve the data.
 */
public class FriendMoodEventsViewModel extends ViewModel {

    private final MutableLiveData<List<MoodEvent>> friendMoodEvents = new MutableLiveData<>();

    /**
     * Returns the LiveData object containing the list of friend mood events.
     * The LiveData object is observed by other components to get updates when the data changes.
     *
     * @return A LiveData object containing the list of friend mood events.
     */
    public LiveData<List<MoodEvent>> getMoodEvents() {
        return friendMoodEvents;
    }

    /**
     * Updates the list of mood events related to friends.
     * This method is used to set the value of the list of friend mood events.
     *
     * @param events A list of mood events to set.
     */
    public void setMoodEvents(List<MoodEvent> events) {
        friendMoodEvents.setValue(events);
    }
}
