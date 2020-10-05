package io.agrest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleObjectIdTest {

    @Test
    public void testEquals() {

        SimpleObjectId objectId1 = new SimpleObjectId(1);

        assertTrue(objectId1.equals(objectId1));

        SimpleObjectId objectId2 = new SimpleObjectId(1);

        assertTrue(objectId1.equals(objectId2));

    }

    @Test
    public void testNotEquals() {
        SimpleObjectId objectId1 = new SimpleObjectId(1);

        assertFalse(objectId1.equals(new Integer(1)));

        assertFalse(objectId1.equals(new SimpleObjectId(-1)));
    }

    @Test
    public void testHashcode() {

        SimpleObjectId id = new SimpleObjectId(new Integer(100));

        assertEquals(id, id);

        assertEquals(new SimpleObjectId(0), new SimpleObjectId(0));

        assertEquals(new SimpleObjectId(""), new SimpleObjectId(""));

        assertNotEquals(new SimpleObjectId(1), new SimpleObjectId(-1));
    }
}
