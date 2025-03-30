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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.arrayadapters.CommentAdapter;
import com.example.unemployedavengers.databinding.MoodDetailBinding;
import com.example.unemployedavengers.implementationDAO.CommentManager;
import com.example.unemployedavengers.models.Comment;
import com.example.unemployedavengers.models.MoodEvent;

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
        commentAdapter = new CommentAdapter(requireContext(), comments);
        commentsList.setAdapter(commentAdapter);

        // Get mood event from arguments
        if (getArguments() != null) {
            moodEvent = (MoodEvent) getArguments().getSerializable("selected_mood_event");
            if (moodEvent != null) {
                displayMoodEvent();
                loadComments();
            }
        }

        // Set up back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_moodDetailFragment_to_followedUserMoodEventsFragment)
        );

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

        // Set up Reply Listener
        commentsList.setOnItemClickListener(((adapterView, view1, i, l) -> {
            enterReplyMode(((Comment) commentsList.getItemAtPosition(i)).getId(), currentUsername);
        }));

    }

    private void displayMoodEvent() {
        // Display mood information
        binding.tvMoodType.setText(moodEvent.getMood());

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

    private void loadComments() {
        if (moodEvent == null || moodEvent.getId() == null) {
            return;
        }

        // To test
        Log.d("MOOD EVENT", moodEvent.getId());

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