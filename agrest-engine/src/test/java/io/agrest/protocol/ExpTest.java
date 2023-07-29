package io.agrest.protocol;

import io.agrest.exp.AgExpressionException;
import io.agrest.exp.parser.SimpleNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExpTest {

    @Test
    public void namedParamsPruning() {
        Exp exp = Exp.parse("a = $1 and b = $2 or c = $3 and not d = $4");

        assertEquals(
                "(((a) = (2)) and ((b) = (4))) or (((c) = (8)) and (!((d) = (16))))",
                exp.namedParams(Map.of("1", 2, "2", 4, "3", 8, "4", 16)).toString());

        assertEquals("(((a) = (2)) and ((b) = (4))) or ((c) = (8))",
                exp.namedParams(Map.of("1", 2, "2", 4, "3", 8)).toString());

        assertEquals("(c) = (8)", exp.namedParams(Map.of("3", 8)).toString());

        Map<String, Object> withNulls = new HashMap<>();
        withNulls.put("1", null);
        withNulls.put("3", 8);
        assertEquals("((a) = (null)) or ((c) = (8))", exp.namedParams(withNulls).toString());
    }

    @Test
    public void parentImmutable() {
        Exp c1 = Exp.equal("a", 1);
        Exp c2 = Exp.equal("a", 2);

        Exp and = Exp.and(c1, c2);
        assertNull(((SimpleNode) c1).jjtGetParent(), "parent was reset");
        assertNull(((SimpleNode) c2).jjtGetParent(), "parent was reset");

        assertNotSame(c1, ((SimpleNode) and).jjtGetChild(0));
        assertEquals(c1, ((SimpleNode) and).jjtGetChild(0));
        assertSame(and, ((SimpleNode) and).jjtGetChild(0).jjtGetParent());

        assertNotSame(c2, ((SimpleNode) and).jjtGetChild(1));
        assertEquals(c2, ((SimpleNode) and).jjtGetChild(1));
        assertSame(and, ((SimpleNode) and).jjtGetChild(1).jjtGetParent());
    }

    @Test
    public void paramsImmutable() {
        Exp raw = Exp.parse("a = $1");
        Exp e1 = raw.positionalParams("b");
        Exp e2 = raw.namedParams(Map.of("1", "c"));

        assertEquals("(a) = ($1)", raw.toString());
        assertEquals("(a) = ('b')", e1.toString());
        assertEquals("(a) = ('c')", e2.toString());
    }

    @Test
    public void andOrImmutable() {
        Exp e1 = Exp.parse("1 > 2");
        Exp e2 = e1.and(Exp.parse("3 < 4"));
        Exp e3 = e1.or(Exp.parse("5 = 6"));
        Exp e4 = e2.or(Exp.parse("7 > 8"));
        Exp e5 = e3.and(Exp.parse("9 < 10"));
        List<Exp> all = List.of(e1, e2, e3, e4, e5);

        assertEquals(5, new ArrayList<>(new HashSet<>(all)).size(), "Some expressions were reused");


        assertEquals("(1) > (2)", e1.toString());
        assertEquals("((1) > (2)) and ((3) < (4))", e2.toString());
        assertEquals("((1) > (2)) or ((5) = (6))", e3.toString());
        assertEquals("(((1) > (2)) and ((3) < (4))) or ((7) > (8))", e4.toString());
        assertEquals("(((1) > (2)) or ((5) = (6))) and ((9) < (10))", e5.toString());
    }

    @Test
    public void andCompact() {
        assertEquals(2, ((SimpleNode) Exp.parse("3 < 4 and 5 = 6")).jjtGetNumChildren());
        assertEquals(3, ((SimpleNode) Exp.parse("3 < 4 and 5 = 6 and 7 > 8")).jjtGetNumChildren());
        assertEquals(4, ((SimpleNode) Exp.parse("3 < 4 and 5 = 6 and 7 > 8 and 9 != 10")).jjtGetNumChildren());
    }

    @Test
    public void orCompact() {
        assertEquals(2, ((SimpleNode) Exp.parse("3 < 4 or 5 = 6")).jjtGetNumChildren());
        assertEquals(3, ((SimpleNode) Exp.parse("3 < 4 or 5 = 6 or 7 > 8")).jjtGetNumChildren());
        assertEquals(4, ((SimpleNode) Exp.parse("3 < 4 or 5 = 6 or 7 > 8 or 9 != 10")).jjtGetNumChildren());
    }

    @Test
    public void positionalParamsThrowsOnTooFewParams() {
        assertThrows(AgExpressionException.class, () -> Exp.parse("a = $1").positionalParams());
    }

    @Test
    public void namedParamsThrowsOnTooFewParams() {
        assertThrows(AgExpressionException.class, () -> Exp.parse("a = $1").namedParams(Collections.emptyMap(), false));
    }

    @Test
    public void positionalParamsThrowsOnTooManyParams() {
        assertThrows(AgExpressionException.class, () -> Exp.parse("a = $1").positionalParams("a", "b"));
    }

    @Test
    public void namedParamsIgnoresExtraParams() {
        Exp.parse("a = $1").namedParams(Map.of("1", 2, "2", 4));
    }
}
