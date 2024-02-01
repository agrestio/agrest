package io.agrest.cayenne.exp;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CayenneExpParserTest {

    static final CayenneExpParser parser = new CayenneExpParser();

    @Test
    public void parseNamedParams() {
        Expression e = parser.parse(Exp.parse("a = $a").namedParams(Map.of("a", "x")));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void parsePositionalParams() {
        Expression e = parser.parse(Exp.parse("a = $a").positionalParams("x"));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void parsePositionalParams_NullAndParam() {
        Exp agExp = Exp.parse("a = null or a.b = $b").positionalParams("B");
        Expression e = parser.parse(agExp);
        assertEquals(ExpressionFactory.exp("a = null or a.b = 'B'"), e);
    }

    @Test
    public void parseEqual() {
        Expression e = parser.parse(Exp.equal("a", 5));
        assertEquals(ExpressionFactory.exp("a = 5"), e);
    }

    @Test
    public void parseNotEqual() {
        Expression e = parser.parse(Exp.notEqual("a", 5));
        assertEquals(ExpressionFactory.exp("a != 5"), e);
    }

    @Test
    public void parseEqualDate() {
        LocalDate d = LocalDate.of(1999, 8, 7);
        Expression e = parser.parse(Exp.equal("a", d));
        assertEquals(ExpressionFactory.exp("a = $a").paramsArray(d), e);
    }

    @Test
    public void parseEqual_Object() {
        Object o = new Object();
        Expression e = parser.parse(Exp.equal("a", o));
        assertEquals(ExpressionFactory.exp("a = $a").paramsArray(o), e);
    }

    @Test
    public void parseIn() {
        Expression e1 = parser.parse(Exp.in("a", 5, 6, 7));
        assertEquals(ExpressionFactory.exp("a in (5, 6, 7)"), e1);

        Expression e2 = parser.parse(Exp.in("a", "x", "y", "z"));
        assertEquals(ExpressionFactory.exp("a in ('x','y','z')"), e2);

        Expression e3 = parser.parse(Exp.in("a", 5, 6, 7));
        assertEquals(ExpressionFactory.exp("a in (5, 6, 7)"), e3);
    }

    @Test
    public void parseEqual_DbPath() {
        Expression e = parser.parse(Exp.equal("db:a", 5));
        assertEquals(ExpressionFactory.exp("db:a = 5"), e);
    }

    @Test
    public void parseCompositeCondition() {

        Exp e0 = Exp.parse("a = 'b'");
        Exp e1 = Exp.parse("b = $a").namedParams(Map.of("a", "x"));
        Exp e2 = Exp.parse("c = $a").positionalParams("y");

        // multilevel composite with heterogeneous params
        Exp e3 = Exp.parse("d = 'z'")
                .and(e0)
                .or(e1)
                .and(e2);

        Expression e = parser.parse(e3);
        assertEquals(ExpressionFactory.exp("(((d = 'z') and (a = 'b')) or (b = 'x')) and (c = 'y')"), e);
    }

    @Test
    public void parseComposite_DifferentOrder() {
        Exp e1 = Exp.parse("id = $id").positionalParams(1);
        Exp e2 = Exp.in("otherId", 1, 2, 3);
        Exp composite1 = e1.and(e2);
        Exp composite2 = e2.and(e1);

        Expression cayenneComposite1 = parser.parse(composite1);
        Expression cayenneComposite2 = parser.parse(composite2);
        assertEquals(ExpressionFactory.exp("(id = 1) and (otherId in (1, 2, 3))"), cayenneComposite1);
        assertEquals(ExpressionFactory.exp("(otherId in (1, 2, 3)) and (id = 1)"), cayenneComposite2);
    }
}
