/**
 * CommentTest.java
 *
 * Unit tests for the Comment model class in the UnemployedAvengers application.
 *
 * This test class verifies:
 * 1. Proper construction of Comment objects with all required fields
 * 2. Correct functioning of getter and setter methods
 * 3. Behavior of reply management functionality
 * 4. Default values and null safety
 *
 * The tests follow standard JUnit practices with:
 * - @Before setup method to initialize test objects
 * - Individual test methods for each logical unit of functionality
 * - Clear assertions for expected behavior
 *
 * Key Test Cases:
 * - Constructor initializes all fields correctly
 * - Setters properly update model state
 * - Reply ID management works as expected
 * - Timestamp is automatically generated on creation
 *

 */
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
