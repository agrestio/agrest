package io.agrest.protocol;

import io.agrest.exp.AgExpressionException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpTest {

    @Test
    public void testBind_equals() {

        Exp raw = Exp.from("a = $1");
        Exp e1 = raw.positionalParams("b");
        Exp e2 = raw.namedParams(Map.of("1", "b"));

        assertEquals(e1, e2);
        assertNotEquals(raw, e1);
        assertNotEquals(raw, e2);

        assertEquals("a = $1", raw.toString());
        assertEquals("a = 'b'", e1.toString());
        assertEquals("a = 'b'", e2.toString());
    }

    @Test
    public void testBind_reusable() {
        Exp raw = Exp.from("a = $1 and b = $2");

        Exp e1 = raw.positionalParams(5, 7);
        Exp e2 = raw.positionalParams(7, 5);
        Exp e3 = raw.namedParams(Map.of("1", 3, "2", 4));
        Exp e4 = raw.namedParams(Map.of("1", 4, "2", 3));
        Collection<Exp> all = List.of(e1, e2, e3, e4);

        // Make sure all of them are different.
        assertEquals(new ArrayList<>(all), new ArrayList<>(new LinkedHashSet<>(all)));

        assertEquals("a = 5 and b = 7", e1.toString());
        assertEquals("a = 7 and b = 5", e2.toString());
        assertEquals("a = 3 and b = 4", e3.toString());
        assertEquals("a = 4 and b = 3", e4.toString());
    }

    @Test
    public void testBind_pruning() {
        Exp raw = Exp.from("a = $1 and b = $2 or c = $3 and not d = $4");

        Exp e1 = raw.namedParams(Map.of("1", 2, "2", 4, "3", 8, "4", 16));
        Exp e2 = raw.namedParams(Map.of("1", 2, "2", 4, "3", 8));
        Exp e3 = raw.namedParams(Map.of("3", 8));
        Exp e4 = raw.namedParams(new HashMap<>() {{
            put("1", null);
            put("3", 8);
        }});

        assertEquals("a = 2 and b = 4 or c = 8 and !(d = 16)", e1.toString());
        assertEquals("a = 2 and b = 4 or c = 8", e2.toString());
        assertEquals("c = 8", e3.toString());
        assertEquals("a = null or c = 8", e4.toString());
    }

    @Test
    public void testBind_throwsOn_tooFewParameters() {
        Exp raw = Exp.from("a = $1");
        assertThrows(AgExpressionException.class, () -> raw.positionalParams());
        assertThrows(AgExpressionException.class, () -> raw.namedParams(Collections.emptyMap(), false));
    }

    @Test
    public void testBind_throwsOn_tooManyParameters() {
        Exp raw = Exp.from("a = $1");
        assertThrows(AgExpressionException.class, () -> raw.positionalParams("a", "b"));

        // Unnecessary parameters are simply not used.
        raw.namedParams(Map.of("1", 2, "2", 4));
    }
}
