package io.agrest;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CompoundObjectIdTest {

    @Test
    public void testEquals() {

        assertTrue(new CompoundObjectId(new HashMap<>()).equals(new CompoundObjectId(new HashMap<>())));

        Map<String, Object> ids1 = new HashMap<>();
        ids1.put("id1", new Integer(1));
        ids1.put("id2", "2");
        CompoundObjectId objectId1 = new CompoundObjectId(ids1);

        assertTrue(objectId1.equals(objectId1));

        CompoundObjectId objectId2 = new CompoundObjectId(ids1);

        assertTrue(objectId1.equals(objectId2));

        Map<String, Object> ids3 = new HashMap<>();
        ids3.put("id1", new Integer(1));
        ids3.put("id2", "2");
        CompoundObjectId objectId3 = new CompoundObjectId(ids3);

        assertTrue(objectId1.equals(objectId3));

    }

    @Test
    public void testNotEquals() {

        Map<String, Object> ids1 = new HashMap<>();
        ids1.put("id1", new Integer(1));
        ids1.put("id2", "2");
        CompoundObjectId objectId1 = new CompoundObjectId(ids1);

        assertFalse(objectId1.equals(new String("2")));

        assertFalse(objectId1.equals(new CompoundObjectId(new HashMap<>())));

        Map<String, Object> ids2 = new HashMap<>();
        ids2.put("id1", new Integer(-1));
        ids2.put("id2", "2");
        CompoundObjectId objectId2 = new CompoundObjectId(ids2);

        assertFalse(objectId1.equals(objectId2));
    }

    @Test
    public void testHashcode() {

        assertEquals(new CompoundObjectId(new HashMap<>()), new CompoundObjectId(new HashMap<>()));

        Map<String, Object> ids1 = new HashMap<>();
        ids1.put("id1", new Integer(1));
        ids1.put("id2", "2");
        CompoundObjectId objectId1 = new CompoundObjectId(ids1);

        CompoundObjectId objectId2 = new CompoundObjectId(ids1);

        assertEquals(objectId1.hashCode(), objectId2.hashCode());

        Map<String, Object> ids3 = new HashMap<>();
        ids3.put("id1", new Integer(1));
        ids3.put("id2", "2");
        CompoundObjectId objectId3 = new CompoundObjectId(ids3);

        assertEquals(objectId1.hashCode(), objectId3.hashCode());

        assertNotEquals(objectId1.hashCode(), new CompoundObjectId(new HashMap<>()));

        Map<String, Object> ids4 = new HashMap<>();
        ids4.put("id1", new Integer(1));
        ids4.put("id2", "22");
        CompoundObjectId objectId4 = new CompoundObjectId(ids4);

        assertNotEquals(objectId1.hashCode(), objectId4.hashCode());
    }
}
