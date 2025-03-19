package com.example.unemployedavengers.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class WithinFiveKmViewModel extends ViewModel {
    private final MutableLiveData<List<MoodEvent>> fiveKmEvents = new MutableLiveData<>();

    public LiveData<List<MoodEvent>> getMoodEvents() {
        return fiveKmEvents;
    }

    public void setMoodEvents(List<MoodEvent> events) {
        fiveKmEvents.setValue(events);
    }
}
