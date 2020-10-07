package io.agrest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleObjectIdTest {

    @Test
    public void testEquals() {

        SimpleObjectId objectId1 = new SimpleObjectId(1);

        assertEquals(objectId1, objectId1);

        SimpleObjectId objectId2 = new SimpleObjectId(1);

        assertEquals(objectId2, objectId1);

    }

    @Test
    public void testNotEquals() {
        SimpleObjectId objectId1 = new SimpleObjectId(1);

        assertNotEquals(1, objectId1);
        assertNotEquals(new SimpleObjectId(-1), objectId1);
    }

    @Test
    public void testHashcode() {

        SimpleObjectId id = new SimpleObjectId(100);

        assertEquals(id, id);
        assertEquals(new SimpleObjectId(0), new SimpleObjectId(0));
        assertEquals(new SimpleObjectId(""), new SimpleObjectId(""));
        assertNotEquals(new SimpleObjectId(1), new SimpleObjectId(-1));
    }
}
