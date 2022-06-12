package io.agrest.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SingleValueIdTest {

    @Test
    public void testEquals() {

        SingleValueId objectId1 = new SingleValueId(1);

        assertEquals(objectId1, objectId1);

        SingleValueId objectId2 = new SingleValueId(1);

        assertEquals(objectId2, objectId1);

    }

    @Test
    public void testNotEquals() {
        SingleValueId objectId1 = new SingleValueId(1);

        assertNotEquals(1, objectId1);
        assertNotEquals(new SingleValueId(-1), objectId1);
    }

    @Test
    public void testHashcode() {

        SingleValueId id = new SingleValueId(100);

        assertEquals(id, id);
        assertEquals(new SingleValueId(0), new SingleValueId(0));
        assertEquals(new SingleValueId(""), new SingleValueId(""));
        assertNotEquals(new SingleValueId(1), new SingleValueId(-1));
    }
}
