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
                "(((a) = (2)) and ((b) = (4))) or (((c) = (8)) and (not ((d) = (16))))",
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

    @Test
    public void between() {
        assertEquals("a between 5 and 10", Exp.between("a", 5, 10).toString());
    }

    @Test
    public void notBetween() {
        assertEquals("a not between 5 and 10", Exp.notBetween("a", 5, 10).toString());
    }

    @Test
    public void equal() {
        assertEquals("(a) = ('b')", Exp.equal("a", "b").toString());
    }

    @Test
    public void lessOrEqual() {
        assertEquals("(a) <= (5)", Exp.lessOrEqual("a", 5).toString());
    }

    @Test
    public void like() {
        assertEquals("a like 'x%'", Exp.like("a", "x%").toString());
    }

    @Test
    public void notLike() {
        assertEquals("a not like 'x%'", Exp.notLike("a", "x%").toString());
    }

    @Test
    public void likeIgnoreCase() {
        assertEquals("a likeIgnoreCase 'x%'", Exp.likeIgnoreCase("a", "x%").toString());
    }

    @Test
    public void notLikeIgnoreCase() {
        assertEquals("a not likeIgnoreCase 'x%'", Exp.notLikeIgnoreCase("a", "x%").toString());
    }

    @Test
    public void in() {
        assertEquals("a in ('a', 4, 'b')", Exp.in("a", "a", 4, "b").toString());
    }

    @Test
    public void inCollection() {
        assertEquals("a in ('a', 4, 'b')", Exp.inCollection("a", List.of("a", 4, "b")).toString());
    }

    @Test
    public void notIn() {
        assertEquals("a not in ('a', 4, 'b')", Exp.notIn("a", "a", 4, "b").toString());
    }

    @Test
    public void notInCollection() {
        assertEquals("a not in ('a', 4, 'b')", Exp.notInCollection("a", List.of("a", 4, "b")).toString());
    }

    @Test
    public void not() {
        assertEquals("not ((a) = (5))", Exp.not(Exp.equal("a", 5)).toString());
        assertEquals("(a) = (5)", Exp.not(Exp.not(Exp.equal("a", 5))).toString());
    }
}
