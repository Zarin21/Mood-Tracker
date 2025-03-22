package com.example.unemployedavengers.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment model for mood events.
 * Allows users to comment on mood events and reply to other comments.
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

    // Empty constructor for Firestore
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

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMoodEventId() {
        return moodEventId;
    }

    public void setMoodEventId(String moodEventId) {
        this.moodEventId = moodEventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getReplyIds() {
        return replyIds;
    }

    public void setReplyIds(List<String> replyIds) {
        this.replyIds = replyIds;
    }

    public void addReplyId(String replyId) {
        if (this.replyIds == null) {
            this.replyIds = new ArrayList<>();
        }
        this.replyIds.add(replyId);
    }
}