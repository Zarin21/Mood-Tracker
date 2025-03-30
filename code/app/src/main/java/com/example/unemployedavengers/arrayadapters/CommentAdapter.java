package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.implementationDAO.CommentManager;
import com.example.unemployedavengers.models.Comment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adapter for displaying comments in a ListView
 */
public class CommentAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private Map<String, List<Comment>> repliesMap; // Maps parent comment ID to replies
    private Map<String, String> userProfilePictures = new HashMap<>(); // Cache profile pics
    private String user;
    // In CommentAdapter.java
    /**
     * Constructor for CommentAdapter
     *
     * @param context Application context
     * @param comments List of top-level comments
     * @param currentUser ID of the current user
     */
    public CommentAdapter(Context context, List<Comment> comments, String currentUser) {
        super(context, 0, comments);
        this.context = context;
        this.repliesMap = new HashMap<>();
        this.user = currentUser;
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
        ImageView profileImage = view.findViewById(R.id.comment_avatar);
        ImageView replyButton = view.findViewById(R.id.reply_button);
        LinearLayout repliesContainer = view.findViewById(R.id.replies_container);
        Button likeButton = view.findViewById(R.id.btnLike);
        TextView likesView = view.findViewById(R.id.tvLikeCount);

        // Set comment data
        usernameText.setText(comment.getUsername());
        contentText.setText(comment.getContent());

        // Format timestamp
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

        // Check if the current user has liked this comment
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference commentRef = db.collection("comments").document(comment.getId());

        AtomicInteger likes = new AtomicInteger();
        commentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.getLong("likeCount") != null) {
                likes.set(Math.toIntExact(documentSnapshot.getLong("likeCount")));
            }  else {
                likes.set(0);
                commentRef.update("likeCount", 0);
            }
        });

        likesView.setText(String.valueOf(comment.getLikeCount()));

        commentRef.collection("likes").document(user).get().addOnSuccessListener(documentSnapshot -> {
            final boolean[] isLiked = {documentSnapshot.exists()};

            if (isLiked[0]) {
                likeButton.setText("Unlike");
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_filled, 0, 0, 0);
            } else {
                likeButton.setText("Like");
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up, 0, 0, 0);
            }

            likeButton.setOnClickListener(v -> {
                    updateLikes(comment.getId(), isLiked[0], likeButton, likesView, likes, commentRef);
                    if (isLiked[0]) likes.set(likes.get() - 1);
                    else likes.set(likes.get() + 1);
                    isLiked[0] = !isLiked[0];
            });
        });

        return view;
    }

    private void updateLikes(String commentId, boolean isLiked, Button likeButton, TextView likesView, AtomicInteger likes, DocumentReference commentRef) {
        if (isLiked) {
            likeButton.setText("Like");
            likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up, 0, 0, 0);
            likesView.setText(String.valueOf(likes.get() - 1));
            commentRef.collection("likes").document(user).delete().addOnSuccessListener(aVoid -> {
                commentRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1));
                Log.d("CommentAdapter", "Like removed successfully");
            }).addOnFailureListener(e -> {
                Log.e("CommentAdapter", "Failed to remove like", e);
            });
        } else {
            likeButton.setText("Unlike");
            likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_filled, 0, 0, 0);
            likesView.setText(String.valueOf(likes.get() + 1));
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("userId", user);
            likeData.put("timestamp", System.currentTimeMillis());

            commentRef.collection("likes").document(user).set(likeData).addOnSuccessListener(aVoid -> {
                commentRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(1));
                Log.d("CommentAdapter", "Like added successfully");
            }).addOnFailureListener(e -> {
                Log.e("CommentAdapter", "Failed to add like", e);
            });
        }
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
