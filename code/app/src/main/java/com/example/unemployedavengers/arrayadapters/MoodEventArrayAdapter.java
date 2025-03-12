/*
 * MoodEventArrayAdapter - An ArrayAdapter that binds MoodEvent objects to list items.
 *
 * Purpose:
 * - Formats and displays mood events using a custom layout (mood_event.xml).
 * - Sets the mood text and formats the time, applying color styling based on the mood.
 */
package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
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

