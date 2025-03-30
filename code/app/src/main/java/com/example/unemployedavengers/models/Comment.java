package com.example.unemployedavengers.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a comment made on a mood event.
 * Users can comment on mood events and reply to other comments.
 * The comment can either be a top-level comment or a reply to another comment.
 */
public class Comment implements Serializable {
    private String id;
    private String moodEventId;
    private String userId;
    private String username;
    private String content;
    private long timestamp;
    private String parentId;  // null for top-level comments, otherwise contains parent comment ID
    private List<String> replyIds; // IDs of replies to this comment

    /**
     * Empty constructor required for Firestore serialization.
     */
    public Comment() {
        replyIds = new ArrayList<>();
    }

    /**
     * Constructor for a new comment
     *
     * @param moodEventId The ID of the mood event this comment is attached to
     * @param userId The ID of the user making the comment
     * @param username The username of the user making the comment
     * @param content The comment content
     * @param parentId The ID of the parent comment (null if top-level comment)
     */
    public Comment(String moodEventId, String userId, String username, String content, String parentId) {
        this.moodEventId = moodEventId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.parentId = parentId;
        this.replyIds = new ArrayList<>();
    }

    /**
     * Returns the unique identifier for the comment.
     *
     * @return The comment ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the comment.
     *
     * @param id The comment ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the ID of the mood event this comment is associated with.
     *
     * @return The mood event ID.
     */
    public String getMoodEventId() {
        return moodEventId;
    }

    /**
     * Sets the ID of the mood event this comment is associated with.
     *
     * @param moodEventId The mood event ID to set.
     */
    public void setMoodEventId(String moodEventId) {
        this.moodEventId = moodEventId;
    }

    /**
     * Returns the ID of the user who made the comment.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who made the comment.
     *
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the username of the user who made the comment.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user who made the comment.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the content of the comment.
     *
     * @return The comment content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the comment.
     *
     * @param content The content to set for the comment.
     */

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the timestamp of when the comment was created.
     *
     * @return The timestamp in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the ID of the parent comment, or null if this is a top-level comment.
     *
     * @return The parent comment ID, or null if top-level.
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the ID of the parent comment.
     *
     * @param parentId The parent comment ID to set, or null if this is a top-level comment.
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns a list of reply IDs associated with this comment.
     *
     * @return The list of reply IDs.
     */
    public List<String> getReplyIds() {
        return replyIds;
    }

    /**
     * Sets the list of reply IDs associated with this comment.
     *
     * @param replyIds The list of reply IDs to set.
     */
    public void setReplyIds(List<String> replyIds) {
        this.replyIds = replyIds;
    }

    /**
     * Adds a reply ID to the list of replies for this comment.
     *
     * @param replyId The reply ID to add to the list.
     */
    public void addReplyId(String replyId) {
        if (this.replyIds == null) {
            this.replyIds = new ArrayList<>();
        }
        this.replyIds.add(replyId);
    }
}