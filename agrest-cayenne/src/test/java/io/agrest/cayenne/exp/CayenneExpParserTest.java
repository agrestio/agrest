package io.agrest.cayenne.exp;

import io.agrest.protocol.Exp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CayenneExpParserTest {

    static final CayenneExpParser parser = new CayenneExpParser();

    @Test
    public void testParseSimple() {
        Expression e = parser.parse(Exp.from("a = 'b'"));
        assertEquals(ExpressionFactory.exp("a = 'b'"), e);
    }

    @Test
    public void testParseNamedParams() {
        Expression e = parser.parse(Exp.from("a = $a").withNamedParams(Map.of("a", "x")));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void testParsePositionalParams() {
        Expression e = parser.parse(Exp.from("a = $a").withPositionalParams("x"));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void testParseKeyValue_Eq() {
        Expression e = parser.parse(Exp.keyValue("a", "=", 5));
        assertEquals(ExpressionFactory.exp("a = 5"), e);
    }

    @Test
    public void testParseKeyValue_Eq_Date() {
        LocalDate d = LocalDate.of(1999, 8, 7);
        Expression e = parser.parse(Exp.keyValue("a", "=", d));
        assertEquals(ExpressionFactory.exp("a = $a").paramsArray(d), e);
    }

    @Test
    public void testParseKeyValue_Eq_Object() {
        Object o = new Object();
        Expression e = parser.parse(Exp.keyValue("a", "=", o));
        assertEquals(ExpressionFactory.exp("a = $a").paramsArray(o), e);
    }

    @Test
    public void testParseKeyValue_In() {
        Expression e1 = parser.parse(Exp.keyValue("a", "in", asList(5, 6, 7)));
        assertEquals(ExpressionFactory.exp("a in (5, 6, 7)"), e1);

        Expression e2 = parser.parse(Exp.keyValue("a", "in", new String[]{"x", "y", "z"}));
        assertEquals(ExpressionFactory.exp("a in ('x','y','z')"), e2);

        Expression e3 = parser.parse(Exp.keyValue("a", "in", new Integer[]{5, 6, 7}));
        assertEquals(ExpressionFactory.exp("a in (5, 6, 7)"), e3);
    }

    @Test
    public void testParseKeyValue_DB_Path_Eq() {
        Expression e = parser.parse(Exp.keyValue("db:a", "=", 5));
        assertEquals(ExpressionFactory.exp("db:a = 5"), e);
    }

    @Test
    public void testParseComposite() {

        Exp e0 = Exp.from("a = 'b'");
        Exp e1 = Exp.from("b = $a").withNamedParams(Map.of("a", "x"));
        Exp e2 = Exp.from("c = $a").withPositionalParams("y");

        // multilevel composite with heterogeneous params
        Exp e3 = Exp.from("d = 'z'")
                .and(e0)
                .or(e1)
                .and(e2);

        Expression e = parser.parse(e3);
        assertEquals(ExpressionFactory.exp("(((d = 'z') and (a = 'b')) or (b = 'x')) and (c = 'y')"), e);
    }
}
