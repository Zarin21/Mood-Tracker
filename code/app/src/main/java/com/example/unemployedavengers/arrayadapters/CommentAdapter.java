package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.models.Comment;

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
        ImageView replyButton = view.findViewById(R.id.reply_button);
        LinearLayout repliesContainer = view.findViewById(R.id.replies_container);

        // Set comment data
        usernameText.setText(comment.getUsername());
        contentText.setText(comment.getContent());

        // Format timestamp (e.g., "3 hours ago")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(comment.getTimestamp()));
        timestampText.setText(formattedTime);

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
}