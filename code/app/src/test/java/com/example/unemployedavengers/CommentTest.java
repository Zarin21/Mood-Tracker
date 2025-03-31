package com.example.unemployedavengers;

import static org.junit.Assert.*;

import com.example.unemployedavengers.models.Comment;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CommentTest {

    private Comment comment;

    @Before
    public void setUp() {
        comment = new Comment("mood123", "user456", "testUser", "This is a test comment.", null);
    }

    @Test
    public void testConstructor() {
        assertEquals("mood123", comment.getMoodEventId());
        assertEquals("user456", comment.getUserId());
        assertEquals("testUser", comment.getUsername());
        assertEquals("This is a test comment.", comment.getContent());
        assertNull(comment.getParentId());
        assertNotNull(comment.getReplyIds());
        assertTrue(comment.getReplyIds().isEmpty());
        assertTrue(comment.getTimestamp() > 0);
    }

    @Test
    public void testSettersAndGetters() {
        comment.setId("comment789");
        assertEquals("comment789", comment.getId());

        comment.setMoodEventId("mood999");
        assertEquals("mood999", comment.getMoodEventId());

        comment.setUserId("user999");
        assertEquals("user999", comment.getUserId());

        comment.setUsername("newUser");
        assertEquals("newUser", comment.getUsername());

        comment.setContent("Updated content");
        assertEquals("Updated content", comment.getContent());

        comment.setParentId("parent456");
        assertEquals("parent456", comment.getParentId());
    }

    @Test
    public void testAddReplyId() {
        comment.addReplyId("reply123");
        List<String> replyIds = comment.getReplyIds();
        assertEquals(1, replyIds.size());
        assertEquals("reply123", replyIds.get(0));
    }
}
