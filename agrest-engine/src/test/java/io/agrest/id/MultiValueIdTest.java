package io.agrest.id;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MultiValueIdTest {

    @Test
    public void equals() {

        assertTrue(new MultiValueId(new HashMap<>()).equals(new MultiValueId(new HashMap<>())));

        Map<String, Object> ids1 = new HashMap<>();
        ids1.put("id1", new Integer(1));
        ids1.put("id2", "2");
        MultiValueId objectId1 = new MultiValueId(ids1);

        assertTrue(objectId1.equals(objectId1));

        MultiValueId objectId2 = new MultiValueId(ids1);

        assertTrue(objectId1.equals(objectId2));

        Map<String, Object> ids3 = new HashMap<>();
        ids3.put("id1", new Integer(1));
        ids3.put("id2", "2");
        MultiValueId objectId3 = new MultiValueId(ids3);

        assertTrue(objectId1.equals(objectId3));

    }

    @Test
    public void notEquals() {

        Map<String, Object> ids1 = new HashMap<>();
        ids1.put("id1", new Integer(1));
        ids1.put("id2", "2");
        MultiValueId objectId1 = new MultiValueId(ids1);

        assertFalse(objectId1.equals(new String("2")));

        assertFalse(objectId1.equals(new MultiValueId(new HashMap<>())));

        Map<String, Object> ids2 = new HashMap<>();
        ids2.put("id1", new Integer(-1));
        ids2.put("id2", "2");
        MultiValueId objectId2 = new MultiValueId(ids2);

        assertFalse(objectId1.equals(objectId2));
    }

    @Test
    public void _hashCode() {

        assertEquals(new MultiValueId(new HashMap<>()), new MultiValueId(new HashMap<>()));

        Map<String, Object> ids1 = new HashMap<>();
        ids1.put("id1", new Integer(1));
        ids1.put("id2", "2");
        MultiValueId objectId1 = new MultiValueId(ids1);

        MultiValueId objectId2 = new MultiValueId(ids1);

        assertEquals(objectId1.hashCode(), objectId2.hashCode());

        Map<String, Object> ids3 = new HashMap<>();
        ids3.put("id1", new Integer(1));
        ids3.put("id2", "2");
        MultiValueId objectId3 = new MultiValueId(ids3);

        assertEquals(objectId1.hashCode(), objectId3.hashCode());

        assertNotEquals(objectId1.hashCode(), new MultiValueId(new HashMap<>()));

        Map<String, Object> ids4 = new HashMap<>();
        ids4.put("id1", new Integer(1));
        ids4.put("id2", "22");
        MultiValueId objectId4 = new MultiValueId(ids4);

        assertNotEquals(objectId1.hashCode(), objectId4.hashCode());
    }
}
