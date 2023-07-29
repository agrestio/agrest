package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.exp.AgExpression;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class ExpInTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpIn.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a in('b','c')",
                "a in ('b', 'c')",
                "a in ('b')",
                "a in ('b',  'c')",
                "a in ($b, $c)",
                "a in (1, 2)",
                "a in (1, 2.2)",
                "a in (1, TRUE)",
                "a in ('1', '2')",
                "a in $b"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a in", AgException.class),
                Arguments.of("a in ()", AgException.class),
                Arguments.of("a in ('b',)", AgException.class),
                Arguments.of("a in (null, 'c')", AgException.class),
                Arguments.of("a in (, 'c')", AgException.class),
                Arguments.of("a IN ('b', 'c')", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("a in('b','c')", "a in ('b', 'c')"),
                Arguments.of("a in ('b',  'c')", "a in ('b', 'c')"),
                Arguments.of("a in ('b', 'c', 'd')", "a in ('b', 'c', 'd')"),
                Arguments.of("a in $b", "a in $b")
        );
    }

    @Test
    public void parameterizedToString() {
        assertEquals("a in ('x', 'y')", Exp.parse("a in $l").positionalParams(List.of("x", "y")).toString());
    }

    @Test
    public void manualToString() {
        assertEquals("a in ('x', 'y')", Exp.in("a", "x", "y").toString());
    }

    @Test
    public void deepCopy() {
        Exp e = Exp.in("a", "x", "y");
        Exp eCopy = ((AgExpression) e).deepCopy();
        assertNotSame(e, eCopy);
        assertEquals(e, eCopy);
    }
}
