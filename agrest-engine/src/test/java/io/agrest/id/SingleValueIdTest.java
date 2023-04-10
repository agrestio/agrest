package io.agrest.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SingleValueIdTest {

    @Test
    public void testEquals() {
        SingleValueId id1 = new SingleValueId(1);
        assertEquals(id1, id1);
        assertEquals(id1, new SingleValueId(1));
        assertNotEquals(id1, new SingleValueId(-1));
        assertNotEquals(id1, new SingleValueId("1"));
    }

    @Test
    public void testHashCode() {
        SingleValueId id1 = new SingleValueId(1);
        assertEquals(id1.hashCode(), id1.hashCode());
        assertEquals(id1.hashCode(), new SingleValueId(1).hashCode());
        assertNotEquals(id1.hashCode(), new SingleValueId(-1).hashCode());
        assertNotEquals(id1.hashCode(), new SingleValueId("1").hashCode());
    }
}
