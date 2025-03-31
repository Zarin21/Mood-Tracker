/**
 * MoodEventArrayAdapter - Custom ArrayAdapter for displaying MoodEvent objects.
 *
 * Purpose:
 * - Binds `MoodEvent` objects to a custom layout (`mood_event.xml`) for display in a `ListView`.
 * - Displays the mood and its timestamp for each event.
 * - Applies color styling to the mood text based on the mood type (e.g., red for anger, blue for fear).
 * - Utilizes `SimpleDateFormat` to format the timestamp for each `MoodEvent` for better readability.
 *
 * Design Pattern:
 * - Implements the `ArrayAdapter` design pattern to efficiently display a list of objects in a `ListView`.
 * - Uses a custom layout (`mood_event.xml`) to define how each `MoodEvent` is represented visually.
 * - Ensures that mood text is color-coded to represent different emotional states.
 *
 * Outstanding Issues:
 * - The mood color assignment is currently based on basic string matching, which may not be robust enough for more complex mood classifications (e.g., synonyms, different language support).
 * - The timestamp format is static (YYYY-MM-DD HH:mm), which may need localization or further flexibility based on user preferences or regional settings.
 * - If there are many `MoodEvent` objects, performance could be impacted, as each `getView()` method inflates the layout and calculates the color for each item. Optimization might be required if the list grows significantly.
 */

package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.core.content.ContextCompat;

import com.example.unemployedavengers.R;


import com.example.unemployedavengers.models.MoodEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 An array adapter that takes moodevent objects and format them according to mood_event.xml (showing only mood and date)
 */

public class MoodEventArrayAdapter extends ArrayAdapter<MoodEvent> {

    //Constructor
    public  MoodEventArrayAdapter(Context context, List<MoodEvent> moodEvents) {
        super(context, 0, moodEvents);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoodEvent moodEvent = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mood_event, parent, false);
        }

        TextView moodTextView = convertView.findViewById(R.id.mood_text);
        TextView timeTextView = convertView.findViewById(R.id.date_text);

        if (moodEvent != null) {
            moodTextView.setText(moodEvent.getMood());
            moodTextView.setTextColor(getMoodColor(getContext(), moodEvent.getMood()));

            // Format time using SimpleDateFormat
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String formattedTime = sdf.format(new Date(moodEvent.getTime()));
            timeTextView.setText(formattedTime);
        }

        return convertView;
    }

    // Method to return a color based on mood
    private int getMoodColor(Context context, String mood) {
        String lowerMood = mood.toLowerCase(); // Normalize case
        //select colour
        if (lowerMood.contains("anger")) return Color.RED;
        if (lowerMood.contains("confusion")) return ContextCompat.getColor(context, R.color.orange);
        if (lowerMood.contains("disgust")) return Color.GREEN;
        if (lowerMood.contains("fear")) return Color.BLUE;
        if (lowerMood.contains("happiness")) return ContextCompat.getColor(context, R.color.baby_blue);
        if (lowerMood.contains("sadness")) return Color.GRAY;
        if (lowerMood.contains("shame")) return ContextCompat.getColor(context, R.color.yellow);
        if (lowerMood.contains("surprise")) return ContextCompat.getColor(context, R.color.pink);

        return ContextCompat.getColor(context, R.color.black); // Default color
    }


}

