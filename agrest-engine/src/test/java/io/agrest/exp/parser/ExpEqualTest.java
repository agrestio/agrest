package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.exp.AgExpression;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ExpEqualTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpEqual.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a=b",
                "a = b",
                "a =  b",
                "a == b",
                "$a = $b",
                "1 = 2",
                "1 = 2.2",
                "1 = TRUE",
                "'1' = '2'",
                "null = c",
                "a = currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("=", AgException.class),
                Arguments.of("a =", AgException.class),
                Arguments.of("= b", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("a=b", "(a) = (b)"),
                Arguments.of("a = b", "(a) = (b)")
        );
    }

    @Test
    public void deepCopy() {
        Exp e = Exp.equal("a", 5);
        Exp eCopy = ((AgExpression) e).deepCopy();
        assertNotSame(e, eCopy);
        assertEquals(e, eCopy);
    }

}
