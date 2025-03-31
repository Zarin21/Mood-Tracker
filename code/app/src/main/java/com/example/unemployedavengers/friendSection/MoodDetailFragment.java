/**
 * MoodDetailFragment displays detailed information about a selected mood event.
 *
 * This fragment includes functionality for:
 * - Displaying mood event details (mood type, time, reason, situation, and image).
 * - Loading and displaying top-level comments and their replies.
 * - Enabling users to add comments and replies.
 * - Navigating back to the previous screen.
 *
 * The fragment retrieves the selected mood event from the arguments, fetches related comments and replies from Firestore,
 * and allows users to interact by submitting comments or replies. The comment section dynamically updates based on user interactions.
 */

package com.example.unemployedavengers.friendSection;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.arrayadapters.CommentAdapter;
import com.example.unemployedavengers.databinding.MoodDetailBinding;
import com.example.unemployedavengers.implementationDAO.CommentManager;
import com.example.unemployedavengers.models.Comment;
import com.example.unemployedavengers.models.MoodEvent;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment for displaying a mood event's details and comments
 */
public class MoodDetailFragment extends Fragment {
    private MoodDetailBinding binding;
    private MoodEvent moodEvent;
    private CommentAdapter commentAdapter;
    private CommentManager commentManager;
    private List<Comment> comments;
    private Map<String, List<Comment>> repliesMap;
    private String currentUserId;
    private String currentUsername;
    private EditText commentInput;
    private Button submitCommentButton;
    private String replyingToCommentId;
    private View replyView;
    private String source;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MoodDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize CommentManager
        commentManager = new CommentManager();
        comments = new ArrayList<>();
        repliesMap = new HashMap<>();

        // Get current user info
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userID", null);
        currentUsername = sharedPreferences.getString("username", null);

        // Get references to comment views
        commentInput = view.findViewById(R.id.comment_input);
        submitCommentButton = view.findViewById(R.id.submit_comment);
        ListView commentsList = view.findViewById(R.id.comments_list);
        TextView commentCount = view.findViewById(R.id.comment_count);

        // Set up comment adapter
        commentAdapter = new CommentAdapter(requireContext(), comments, currentUserId);
        commentsList.setAdapter(commentAdapter);

        // Get mood event from arguments
        if (getArguments() != null) {
            moodEvent = (MoodEvent) getArguments().getSerializable("selected_mood_event");
            source = getArguments().getString("source");
            if (moodEvent != null) {
                displayMoodEvent();
                loadComments();
            }
        }

        // Set up back button
        binding.btnBack.setOnClickListener(v -> {
            if ("FollowedUserMoodEvents".equals(source)) {
                Navigation.findNavController(v)
                        .navigate(R.id.action_moodDetailFragment_to_followedUserMoodEventsFragment);
            } else {
                Navigation.findNavController(v)
                        .navigate(R.id.action_moodDetailFragment_to_historyFragment);
            }
        });


        // Set up comment submission
        submitCommentButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                if (replyingToCommentId != null) {
                    // Add reply to a comment
                    addCommentReply(content, replyingToCommentId);
                } else {
                    // Add a new top-level comment
                    addComment(content);
                }
            }
        });

        commentsList.setOnItemClickListener(((adapterView, view1, i, l) -> {
            enterReplyMode(((Comment) commentsList.getItemAtPosition(i)).getId(), ((Comment) commentsList.getItemAtPosition(i)).getUsername());
        }));

        commentsList.setOnItemLongClickListener((adapterView, view1, i, l) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Comment");
                builder.setMessage("Are you sure you want to delete this comment?");
                builder.setPositiveButton("Delete", (dialog, which) -> {
                    CommentManager commentManager = new CommentManager();
                    commentManager.deleteComment(comments.get(i).getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Comment deleted", Toast.LENGTH_SHORT).show();
                                loadComments();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                            });
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.thememain));
                negativeButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.thememain));
                return true;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(moodEvent.getUserId());
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profilePicUrl = documentSnapshot.getString("avatar");
                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    Glide.with(getActivity()).load(profilePicUrl).into((ImageView) view.findViewById(R.id.event_author_picture));
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("CommentAdapter", "Failed to load profile picture", e);
        });
    }

    private void displayMoodEvent() {
        // Display mood information
        binding.tvMoodType.setText(moodEvent.getMood());

        // Set appropriate mood color based on mood type
        binding.tvMoodType.setTextColor(getMoodColor(requireContext(), moodEvent.getMood()));

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(moodEvent.getTime()));
        binding.tvMoodTime.setText(formattedTime);

        // Set username
        binding.tvMoodUsername.setText(moodEvent.getUserName());

        // Set reason if available
        if (moodEvent.getReason() != null && !moodEvent.getReason().isEmpty()) {
            binding.tvMoodReason.setText(moodEvent.getReason());
            binding.tvMoodReason.setVisibility(VISIBLE);
            binding.tvReasonLabel.setVisibility(VISIBLE);
        } else {
            binding.tvMoodReason.setVisibility(GONE);
            binding.tvReasonLabel.setVisibility(GONE);
        }

        // Set situation if available
        if (moodEvent.getSituation() != null && !moodEvent.getSituation().isEmpty()) {
            binding.tvMoodSituation.setText(moodEvent.getSituation());
            binding.tvMoodSituation.setVisibility(VISIBLE);
            binding.tvSituationLabel.setVisibility(VISIBLE);
        } else {
            binding.tvMoodSituation.setVisibility(GONE);
            binding.tvSituationLabel.setVisibility(GONE);
        }

        // Load image if available
        if (moodEvent.getImageUri() != null && !moodEvent.getImageUri().isEmpty()) {
            binding.ivMoodImage.setVisibility(VISIBLE);
            Glide.with(requireContext())
                    .load(moodEvent.getImageUri())
                    .into(binding.ivMoodImage);
        } else {
            binding.ivMoodImage.setVisibility(GONE);
        }
    }

    /**
     * Helper method to set consistent mood colors based on mood type
     * @param context Application context
     * @param mood The mood string to determine color for
     * @return The appropriate color for the mood
     */
    private int getMoodColor(Context context, String mood) {
        String lowerMood = mood.toLowerCase(); // Normalize case

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

    private void loadComments() {
        if (moodEvent == null || moodEvent.getId() == null) {
            return;
        }

        commentManager.getCommentsForMoodEvent(moodEvent.getId(), false)
                .addOnSuccessListener(topLevelComments -> {
                    comments.clear();
                    comments.addAll(topLevelComments);

                    // Update comment count
                    View view = getView();
                    if (view != null) {
                        TextView commentCount = view.findViewById(R.id.comment_count);
                        commentCount.setText(String.valueOf(comments.size()));
                    }

                    // Load replies for each comment
                    for (Comment comment : comments) {
                        loadRepliesForComment(comment.getId());
                    }

                    commentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRepliesForComment(String commentId) {
        commentManager.getRepliesForComment(commentId)
                .addOnSuccessListener(replyList -> {
                    repliesMap.put(commentId, replyList);
                    commentAdapter.setReplies(commentId, replyList);
                })
                .addOnFailureListener(e -> {
                    Log.d("Comment Replies not found", commentId);
                });
    }

    private void addComment(String content) {
        if (moodEvent == null || moodEvent.getId() == null || currentUserId == null) {
            Toast.makeText(requireContext(), "Unable to add comment", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment newComment = new Comment(moodEvent.getId(), currentUserId, currentUsername, content, null);

        commentManager.addComment(newComment)
                .addOnSuccessListener(aVoid -> {
                    // Clear input and refresh comments
                    commentInput.setText("");
                    loadComments();
                    Toast.makeText(requireContext(), "Comment added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error adding comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addCommentReply(String content, String parentCommentId) {
        if (moodEvent == null || moodEvent.getId() == null || currentUserId == null) {
            Toast.makeText(requireContext(), "Unable to add reply", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment newReply = new Comment(moodEvent.getId(), currentUserId, currentUsername, content, parentCommentId);

        commentManager.addComment(newReply)
                .addOnSuccessListener(aVoid -> {
                    // Clear input, exit reply mode, and refresh comments
                    commentInput.setText("");
                    exitReplyMode();
                    loadRepliesForComment(parentCommentId);
                    Toast.makeText(requireContext(), "Reply added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error adding reply: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void enterReplyMode(String commentId, String username) {
        getActivity().findViewById(R.id.reply_section).setVisibility(VISIBLE);

        replyingToCommentId = commentId;
        // Find views in the new layout
        TextView replyUsername = getActivity().findViewById(R.id.reply_username);
        TextView cancelReply = getActivity().findViewById(R.id.cancel_reply);

        replyUsername.setText("Replying to " + username);
        cancelReply.setOnClickListener(v -> exitReplyMode());
    }

    private void exitReplyMode() {
        replyingToCommentId = null;
        getActivity().findViewById(R.id.reply_section).setVisibility(GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}