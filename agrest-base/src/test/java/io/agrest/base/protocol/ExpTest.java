package io.agrest.base.protocol;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ExpTest {

    @Test
    public void testEquals() {

        Exp e1 = new Exp("a = $1", "b");
        Exp e2 = new Exp("a = $1", "b");
        Exp e3 = new Exp("a = $1", "c");
        Exp e4 = new Exp("b = $1", "b");

        // this is an invariant of e1, but we can't tell that until
        // the expression is parsed, so not equals
        Exp e5 = new Exp("a = $1", Collections.singletonMap("1", "b"));
        Exp e6 = new Exp("a = $1", Collections.singletonMap("1", "c"));

        assertEquals(e1, e1);
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertNotEquals(e1, e4);
        assertNotEquals(e1, e5);
        assertNotEquals(e5, e6);
    }

    @Test
    public void testEquals_PositionalInvariants() {

        Exp e1 = new Exp("a = $1");
        Exp e2 = new Exp("a = $1", new Object[0]);
        assertEquals(e1, e2);
    }
}
