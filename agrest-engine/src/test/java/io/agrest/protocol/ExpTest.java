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
    public void namedParams() {
        Exp exp = Exp.parse("a = $a and b = $b or c = $c and not d = $d");

        Map<String, Object> params = new HashMap<>();
        params.put("a", 2);
        params.put("b", 4);
        params.put("c", null);
        params.put("d", 16);

        assertEquals(
                "((a = 2) and (b = 4)) or ((c = null) and (not (d = 16)))",
                exp.namedParams(params).toString());
    }

    @Test
    public void namedParams_Pruning() {
        Exp exp = Exp.parse("a = $a and b = $b or c = $c and not d = $d");

        Map<String, Object> params1 = Map.of("a", 2, "b", 4, "c", 8);
        assertEquals("((a = 2) and (b = 4)) or (c = 8)", exp.namedParams(params1).toString());

        Map<String, Object> params2 = Map.of("c", 8);
        assertEquals("c = 8", exp.namedParams(params2).toString());

        Map<String, Object> params3 = new HashMap<>();
        params3.put("a", null);
        params3.put("c", 8);
        assertEquals("(a = null) or (c = 8)", exp.namedParams(params3).toString());
    }

    @Test
    public void namedParams_NoPruning() {
        Exp exp = Exp.parse("a = $a and b = $b or c = $c and not d = $d");

        Map<String, Object> params1 = Map.of("a", 2, "b", 4, "c", 8);
        assertEquals("((a = 2) and (b = 4)) or ((c = 8) and (not (d = $d)))",
                exp.namedParams(params1, false).toString());

        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", null);
        params2.put("c", 8);
        assertEquals("((a = null) and (b = $b)) or ((c = 8) and (not (d = $d)))",
                exp.namedParams(params2, false).toString());
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

        assertEquals("a = $1", raw.toString());
        assertEquals("a = 'b'", e1.toString());
        assertEquals("a = 'c'", e2.toString());
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


        assertEquals("1 > 2", e1.toString());
        assertEquals("(1 > 2) and (3 < 4)", e2.toString());
        assertEquals("(1 > 2) or (5 = 6)", e3.toString());
        assertEquals("((1 > 2) and (3 < 4)) or (7 > 8)", e4.toString());
        assertEquals("((1 > 2) or (5 = 6)) and (9 < 10)", e5.toString());
    }

    @Test
    public void positionalParamsThrowsOnTooFewParams() {
        assertThrows(AgExpressionException.class, () -> Exp.parse("a = $1").positionalParams());
    }

    @Test
    public void namedParams_PartialResolution() {

        Exp p1 = Exp.parse("a = $a").namedParams(Collections.emptyMap(), false);
        assertEquals("a = $a", p1.toString());

        Exp p2 = Exp.parse("a = $a and b = $b").namedParams(Map.of("b", 1), false);
        assertEquals("(a = $a) and (b = 1)", p2.toString());
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
    public void exists() {
        assertEquals("exists (details.value between 5 and 6)",
                Exp.exists("details.value between 5 and 6").toString());
    }

    @Test
    public void notExists() {
        assertEquals("not exists (details.value between 5 and 6)",
                Exp.notExists("details.value between 5 and 6").toString());
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
        assertEquals("a = 'b'", Exp.equal("a", "b").toString());
    }

    @Test
    public void notEqual() {
        assertEquals("a != 'b'", Exp.notEqual("a", "b").toString());
    }

    @Test
    public void greater() {
        assertEquals("a > 5", Exp.greater("a", 5).toString());
    }

    @Test
    public void greaterOrEqual() {
        assertEquals("a >= 5", Exp.greaterOrEqual("a", 5).toString());
    }

    @Test
    public void less() {
        assertEquals("a < 5", Exp.less("a", 5).toString());
    }

    @Test
    public void lessOrEqual() {
        assertEquals("a <= 5", Exp.lessOrEqual("a", 5).toString());
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
        assertEquals("not (a = 5)", Exp.not(Exp.equal("a", 5)).toString());
        assertEquals("a = 5", Exp.not(Exp.not(Exp.equal("a", 5))).toString());
    }
}
