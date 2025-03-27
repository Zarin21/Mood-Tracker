package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.models.Comment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for displaying comments in a ListView
 */
public class CommentAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private Map<String, List<Comment>> repliesMap; // Maps parent comment ID to replies
    private Map<String, String> userProfilePictures = new HashMap<>(); // Cache profile pics

    /**
     * Constructor for CommentAdapter
     *
     * @param context Application context
     * @param comments List of top-level comments
     */
    public CommentAdapter(Context context, List<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
        this.repliesMap = new HashMap<>();
    }

    /**
     * Add replies to a specific parent comment
     *
     * @param parentId ID of the parent comment
     * @param replies List of reply comments
     */
    public void setReplies(String parentId, List<Comment> replies) {
        repliesMap.put(parentId, replies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, parent, false);
        }

        Comment comment = getItem(position);
        if (comment == null) return view;

        TextView usernameText = view.findViewById(R.id.comment_username);
        TextView contentText = view.findViewById(R.id.comment_content);
        TextView timestampText = view.findViewById(R.id.comment_timestamp);
        ImageView profileImage = view.findViewById(R.id.comment_avatar); // Profile picture
        ImageView replyButton = view.findViewById(R.id.reply_button);
        LinearLayout repliesContainer = view.findViewById(R.id.replies_container);

        // Set comment data
        usernameText.setText(comment.getUsername());
        contentText.setText(comment.getContent());

        // Format timestamp (e.g., "3 hours ago")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(comment.getTimestamp()));
        timestampText.setText(formattedTime);

        // Load Profile Picture
        String userId = comment.getUserId();
        if (userProfilePictures.containsKey(userId)) {
            // Load cached image
            Glide.with(context).load(userProfilePictures.get(userId)).into(profileImage);
        } else {
            // Fetch from Firebase
            fetchUserProfilePicture(userId, profileImage);
        }

        // Clear previous replies
        repliesContainer.removeAllViews();

        // Add replies if available
        List<Comment> replies = repliesMap.get(comment.getId());
        if (replies != null && !replies.isEmpty()) {
            for (Comment reply : replies) {
                View replyView = LayoutInflater.from(getContext()).inflate(R.layout.reply_item, repliesContainer, false);

                TextView replyUsername = replyView.findViewById(R.id.reply_username);
                TextView replyContent = replyView.findViewById(R.id.reply_content);
                TextView replyTimestamp = replyView.findViewById(R.id.reply_timestamp);

                replyUsername.setText(reply.getUsername());
                replyContent.setText(reply.getContent());
                String replyFormattedTime = sdf.format(new Date(reply.getTimestamp()));
                replyTimestamp.setText(replyFormattedTime);

                repliesContainer.addView(replyView);
            }
        }

        return view;
    }

    private void fetchUserProfilePicture(String userId, ImageView profileImage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profilePicUrl = documentSnapshot.getString("avatar");
                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    userProfilePictures.put(userId, profilePicUrl); // Cache it
                    Glide.with(context).load(profilePicUrl).into(profileImage);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("CommentAdapter", "Failed to load profile picture", e);
        });
    }

}
