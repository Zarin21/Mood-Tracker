package com.example.unemployedavengers.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class FriendMoodEventsViewModel extends ViewModel {
    private final MutableLiveData<List<MoodEvent>> friendMoodEvents = new MutableLiveData<>();

    public LiveData<List<MoodEvent>> getMoodEvents() {
        return friendMoodEvents;
    }

    public void setMoodEvents(List<MoodEvent> events) {
        friendMoodEvents.setValue(events);
    }
}
