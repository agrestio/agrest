package io.agrest.base.protocol;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CayenneExpTest {

    @Test
    public void testEquals() {

        CayenneExp e1 = new CayenneExp("a = $1", "b");
        CayenneExp e2 = new CayenneExp("a = $1", "b");
        CayenneExp e3 = new CayenneExp("a = $1", "c");
        CayenneExp e4 = new CayenneExp("b = $1", "b");

        // this is an invariant of e1, but we can't tell that until
        // the expression is parsed, so not equals
        CayenneExp e5 = new CayenneExp("a = $1", Collections.singletonMap("1", "b"));
        CayenneExp e6 = new CayenneExp("a = $1", Collections.singletonMap("1", "c"));

        assertEquals(e1, e1);
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertNotEquals(e1, e4);
        assertNotEquals(e1, e5);
        assertNotEquals(e5, e6);
    }

    @Test
    public void testEquals_PositionalInvariants() {

        CayenneExp e1 = new CayenneExp("a = $1");
        CayenneExp e2 = new CayenneExp("a = $1", new Object[0]);
        assertEquals(e1, e2);
    }
}
