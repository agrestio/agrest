package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpAndTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpAnd.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a and b",
                "a and  b",
                "$a and $b",
                "1 and 2",
                "1 and 2.2",
                "1 and TRUE",
                "'1' and '2'",
                "null and b",
                "a and currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a and", AgException.class),
                Arguments.of("and", AgException.class),
                Arguments.of("a AND b", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("a and b", "(a) and (b)"),
                Arguments.of("a and  b", "(a) and (b)"),
                Arguments.of("a and b and c", "(a) and (b) and (c)"),
                Arguments.of("(a or b) and (c or d)", "((a) or (b)) and ((c) or (d))")
        );
    }

    Stream<Arguments> countChildren() {
        return Stream.of(
                Arguments.of("a and b", 2),
                Arguments.of("a and b and c", 3)
        );
    }

    @ParameterizedTest
    @MethodSource
    void countChildren(Exp exp, int expected) {
        assertEquals(((SimpleNode) exp).children.length, expected);
    }
}
