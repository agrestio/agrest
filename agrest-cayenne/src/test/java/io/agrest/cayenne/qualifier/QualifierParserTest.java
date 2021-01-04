package io.agrest.cayenne.qualifier;

import io.agrest.base.protocol.CayenneExp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QualifierParserTest {

    static final QualifierParser parser = new QualifierParser();

    @Test
    public void testParseSimple() {
        Expression e = parser.parse(CayenneExp.simple("a = 'b'"));
        assertEquals(ExpressionFactory.exp("a = 'b'"), e);
    }

    @Test
    public void testParseNamedParams() {
        Expression e = parser.parse(CayenneExp.withNamedParams("a = $a", Collections.singletonMap("a", "x")));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void testParsePositionalParams() {
        Expression e = parser.parse(CayenneExp.withPositionalParams("a = $a", "x"));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void testParseComposite() {

        CayenneExp e0 = CayenneExp.simple("a = 'b'");
        CayenneExp e1 = CayenneExp.withNamedParams("b = $a", Collections.singletonMap("a", "x"));
        CayenneExp e2 = CayenneExp.withPositionalParams("c = $a", "y");

        // multilevel composite with heterogeneous params
        CayenneExp e3 = CayenneExp.simple("d = 'z'")
                .and(e0)
                .or(e1)
                .and(e2);

        Expression e = parser.parse(e3);
        assertEquals(ExpressionFactory.exp("(((d = 'z') and (a = 'b')) or (b = 'x')) and (c = 'y')"), e);
    }
}
