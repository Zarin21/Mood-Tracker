/*
 * FollowedUserMoodEventAdapter - A custom adapter for displaying mood events from followed users.
 *
 * Purpose:
 * - Extends RecyclerView.Adapter to display MoodEvent objects with emoji representations
 * - Shows the username of the followed user alongside their mood information
 * - Applies appropriate color styling based on the mood type
 */
package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.models.MoodEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FollowedUserMoodEventAdapter extends ArrayAdapter<MoodEvent> {

    private Context context;

    public FollowedUserMoodEventAdapter(Context context, List<MoodEvent> moodEvents) {
        super(context, 0, moodEvents);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.followed_user_mood_items, parent, false);
        }

        MoodEvent moodEvent = getItem(position);
        if (moodEvent == null) return view;

        // Get references to views
        TextView moodEmoji = view.findViewById(R.id.moodEmoji);
        TextView moodText = view.findViewById(R.id.mood_text);
        TextView usernameText = view.findViewById(R.id.usernameText);
        TextView dateText = view.findViewById(R.id.date_text);
        ImageView profileCircle = view.findViewById(R.id.profileCircle);

        // Set the mood emoji and text
        String mood = moodEvent.getMood();
        setMoodEmojiAndText(moodEmoji, moodText, mood);

        // Extract username from trigger field
        // The format is "username: trigger text"
        String triggerText = moodEvent.getTrigger();
        String username = "Friend";

        if (triggerText != null && triggerText.contains(":")) {
            String[] parts = triggerText.split(":", 2);
            username = parts[0].trim();
        }

        // Set the username
        usernameText.setText(username);

        // Format and set the date (MM/dd/yy)
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        String formattedTime = sdf.format(new Date(moodEvent.getTime()));
        dateText.setText(formattedTime);

        // Assign a color to the profile circle based on username
        int colorHash = Math.abs(username.hashCode()) % 5;
        int[] colors = {
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#673AB7"), // Deep Purple
                Color.parseColor("#3F51B5"), // Indigo
                Color.parseColor("#2196F3")  // Blue
        };
        profileCircle.getBackground().setTint(colors[colorHash]);

        return view;
    }

    /**
     * Sets the appropriate emoji and text color based on the mood
     */
    private void setMoodEmojiAndText(TextView emojiView, TextView textView, String mood) {
        String lowerMood = mood.toLowerCase();
        String emoji = "😊"; // Default emoji (happy)
        int textColor = Color.parseColor("#00BCD4"); // Default color (cyan)

        if (lowerMood.contains("anger") || lowerMood.contains("angry")) {
            emoji = "😠";
            textView.setText("Angry");
            textColor = Color.RED;
        } else if (lowerMood.contains("confusion")) {
            emoji = "😕";
            textView.setText("Confused");
            textColor = ContextCompat.getColor(context, R.color.orange);
        } else if (lowerMood.contains("disgust")) {
            emoji = "🤢";
            textView.setText("Disgusted");
            textColor = Color.GREEN;
        } else if (lowerMood.contains("fear") || lowerMood.contains("scared")) {
            emoji = "😨";
            textView.setText("Scared");
            textColor = Color.BLUE;
        } else if (lowerMood.contains("happiness") || lowerMood.contains("happy")) {
            emoji = "😄";
            textView.setText("Happy");
            textColor = ContextCompat.getColor(context, R.color.baby_blue);
        } else if (lowerMood.contains("sadness") || lowerMood.contains("sad")) {
            emoji = "😔";
            textView.setText("Sad");
            textColor = Color.GRAY;
        } else if (lowerMood.contains("shame")) {
            emoji = "😳";
            textView.setText("Shame");
            textColor = ContextCompat.getColor(context, R.color.yellow);
        } else if (lowerMood.contains("surprise") || lowerMood.contains("surprised")) {
            emoji = "😯";
            textView.setText("Surprised");
            textColor = ContextCompat.getColor(context, R.color.pink);
        } else {
            textView.setText(mood);
        }

        emojiView.setText(emoji);
        textView.setTextColor(textColor);
    }
}