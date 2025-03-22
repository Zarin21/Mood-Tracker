package com.example.unemployedavengers.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MoodEventsViewModel extends ViewModel {
    private final MutableLiveData<List<MoodEvent>> moodEvents = new MutableLiveData<>();

    public LiveData<List<MoodEvent>> getMoodEvents() {
        return moodEvents;
    }

    public void setMoodEvents(List<MoodEvent> events) {
        moodEvents.setValue(events);
    }
}

