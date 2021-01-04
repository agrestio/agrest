package io.agrest.base.protocol;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CayenneExpTest {

    @Test
    public void testEquals() {

        CayenneExp e1 = CayenneExp.withPositionalParams("a = $1", "b");
        CayenneExp e2 = CayenneExp.withPositionalParams("a = $1", "b");
        CayenneExp e3 = CayenneExp.withPositionalParams("a = $1", "c");
        CayenneExp e4 = CayenneExp.withPositionalParams("b = $1", "b");

        // this is an invariant of e1, but we can't tell that until
        // the expression is parsed, so not equals
        CayenneExp e5 = CayenneExp.withNamedParams("a = $1", Collections.singletonMap("1", "b"));
        CayenneExp e6 = CayenneExp.withNamedParams("a = $1", Collections.singletonMap("1", "c"));

        assertEquals(e1, e1);
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertNotEquals(e1, e4);
        assertNotEquals(e1, e5);
        assertNotEquals(e5, e6);
    }

    @Test
    public void testEquals_PositionalInvariants() {

        CayenneExp e0 = CayenneExp.simple("a = $1");
        CayenneExp e1 = CayenneExp.withPositionalParams("a = $1");
        CayenneExp e2 = CayenneExp.withPositionalParams("a = $1", new Object[0]);
        assertEquals(e0, e1);
        assertEquals(e1, e2);
    }
}
