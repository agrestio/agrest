package io.agrest.cayenne.qualifier;

import io.agrest.base.protocol.Exp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QualifierParserTest {

    static final QualifierParser parser = new QualifierParser();

    @Test
    public void testParseSimple() {
        Expression e = parser.parse(Exp.simple("a = 'b'"));
        assertEquals(ExpressionFactory.exp("a = 'b'"), e);
    }

    @Test
    public void testParseNamedParams() {
        Expression e = parser.parse(Exp.withNamedParams("a = $a", Collections.singletonMap("a", "x")));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void testParsePositionalParams() {
        Expression e = parser.parse(Exp.withPositionalParams("a = $a", "x"));
        assertEquals(ExpressionFactory.exp("a = 'x'"), e);
    }

    @Test
    public void testParseComposite() {

        Exp e0 = Exp.simple("a = 'b'");
        Exp e1 = Exp.withNamedParams("b = $a", Collections.singletonMap("a", "x"));
        Exp e2 = Exp.withPositionalParams("c = $a", "y");

        // multilevel composite with heterogeneous params
        Exp e3 = Exp.simple("d = 'z'")
                .and(e0)
                .or(e1)
                .and(e2);

        Expression e = parser.parse(e3);
        assertEquals(ExpressionFactory.exp("(((d = 'z') and (a = 'b')) or (b = 'x')) and (c = 'y')"), e);
    }
}
