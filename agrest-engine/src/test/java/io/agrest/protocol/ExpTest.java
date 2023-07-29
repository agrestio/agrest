package io.agrest.protocol;

import io.agrest.exp.AgExpressionException;
import io.agrest.exp.parser.SimpleNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpTest {

    @Test
    public void testBind_equals() {

        Exp raw = Exp.parse("a = $1");
        Exp e1 = raw.positionalParams("b");
        Exp e2 = raw.namedParams(Map.of("1", "b"));

        assertEquals(e1, e2);
        assertNotEquals(raw, e1);
        assertNotEquals(raw, e2);

        assertEquals("(a) = ($1)", raw.toString());
        assertEquals("(a) = ('b')", e1.toString());
        assertEquals("(a) = ('b')", e2.toString());
    }

    @Test
    public void testBind_reusable() {
        Exp raw = Exp.parse("a = $1 and b = $2");

        Exp e1 = raw.positionalParams(5, 7);
        Exp e2 = raw.positionalParams(7, 5);
        Exp e3 = raw.namedParams(Map.of("1", 3, "2", 4));
        Exp e4 = raw.namedParams(Map.of("1", 4, "2", 3));
        Collection<Exp> all = List.of(e1, e2, e3, e4);

        // Make sure all of them are different.
        assertEquals(new ArrayList<>(all), new ArrayList<>(new LinkedHashSet<>(all)));

        assertEquals("((a) = (5)) and ((b) = (7))", e1.toString());
        assertEquals("((a) = (7)) and ((b) = (5))", e2.toString());
        assertEquals("((a) = (3)) and ((b) = (4))", e3.toString());
        assertEquals("((a) = (4)) and ((b) = (3))", e4.toString());
    }

    @Test
    public void testBind_pruning() {
        Exp raw = Exp.parse("a = $1 and b = $2 or c = $3 and not d = $4");

        Exp e1 = raw.namedParams(Map.of("1", 2, "2", 4, "3", 8, "4", 16));
        Exp e2 = raw.namedParams(Map.of("1", 2, "2", 4, "3", 8));
        Exp e3 = raw.namedParams(Map.of("3", 8));
        Exp e4 = raw.namedParams(new HashMap<>() {{
            put("1", null);
            put("3", 8);
        }});

        assertEquals("(((a) = (2)) and ((b) = (4))) or (((c) = (8)) and (!((d) = (16))))", e1.toString());
        assertEquals("(((a) = (2)) and ((b) = (4))) or ((c) = (8))", e2.toString());
        assertEquals("(c) = (8)", e3.toString());
        assertEquals("((a) = (null)) or ((c) = (8))", e4.toString());
    }

    @Test
    public void testBind_keepsImmutable() {
        Exp raw = Exp.parse("a = $1 and b = $2");
        Exp e1 = raw.positionalParams("test1", "test2");
        Exp e2 = raw.namedParams(Map.of("1", "one"));
        Exp e3 = raw.namedParams(Map.of("1", "two"), true);
        Exp e4 = raw.namedParams(Map.of("1", "three", "2", "four"));
        Exp e5 = raw.namedParams(Map.of("1", "five", "2", "six"), true);
        Exp e6 = raw.namedParams(Map.of("1", "seven", "2", "eight"), false);
        Collection<Exp> all = List.of(raw, e1, e2, e3, e4, e5, e6);

        // Make sure all of them are different.
        assertEquals(new ArrayList<>(all), new ArrayList<>(new LinkedHashSet<>(all)));

        assertEquals("((a) = ($1)) and ((b) = ($2))", raw.toString());
        assertEquals("((a) = ('test1')) and ((b) = ('test2'))", e1.toString());
        assertEquals("(a) = ('one')", e2.toString());
        assertEquals("(a) = ('two')", e3.toString());
        assertEquals("((a) = ('three')) and ((b) = ('four'))", e4.toString());
        assertEquals("((a) = ('five')) and ((b) = ('six'))", e5.toString());
        assertEquals("((a) = ('seven')) and ((b) = ('eight'))", e6.toString());
    }

    @Test
    public void testCompose_keepsImmutable() {
        Exp e1 = Exp.parse("1 > 2");
        Exp e2 = e1.and(Exp.parse("3 < 4"));
        Exp e3 = e1.or(Exp.parse("5 = 6"));
        Exp e4 = e2.or(Exp.parse("7 > 8"));
        Exp e5 = e3.and(Exp.parse("9 < 10"));
        Collection<Exp> all = List.of(e1, e2, e3, e4, e5);

        // Make sure all of them are different.
        assertEquals(new ArrayList<>(all), new ArrayList<>(new LinkedHashSet<>(all)));

        assertEquals("(1) > (2)", e1.toString());
        assertEquals("((1) > (2)) and ((3) < (4))", e2.toString());
        assertEquals("((1) > (2)) or ((5) = (6))", e3.toString());
        assertEquals("(((1) > (2)) and ((3) < (4))) or ((7) > (8))", e4.toString());
        assertEquals("(((1) > (2)) or ((5) = (6))) and ((9) < (10))", e5.toString());
    }

    @Test
    public void testCompose_Optimized() {
        Exp e1 = Exp.parse("1 > 2");
        Exp e2 = Exp.parse("3 < 4 and 5 = 6");
        Exp e3 = Exp.parse("7 > 8 or 9 != 10");
        List<Exp> exps = List.of(e1, e2, e3);

        Collection<Exp> all = new ArrayList<>();
        for (Exp exp : exps) {
            for (Exp otherExp : exps) {
                all.add(exp.and(otherExp));
                all.add(exp.or(otherExp));
            }
        }
        assertEquals(List.of(2, 2, 3, 2, 2, 3, 3, 2, 4, 2, 3, 3, 2, 3, 3, 3, 2, 4),
                     all.stream().map(exp -> ((SimpleNode) exp).jjtGetNumChildren()).collect(Collectors.toList()));
    }

    @Test
    public void testBind_throwsOn_tooFewParameters() {
        Exp raw = Exp.parse("a = $1");
        assertThrows(AgExpressionException.class, () -> raw.positionalParams());
        assertThrows(AgExpressionException.class, () -> raw.namedParams(Collections.emptyMap(), false));
    }

    @Test
    public void testBind_throwsOn_tooManyParameters() {
        Exp raw = Exp.parse("a = $1");
        assertThrows(AgExpressionException.class, () -> raw.positionalParams("a", "b"));

        // Unnecessary parameters are simply not used.
        raw.namedParams(Map.of("1", 2, "2", 4));
    }
}
