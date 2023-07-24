package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpOrTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpOr.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a or b",
                "a or  b",
                "$a or $b",
                "1 or 2",
                "1 or 2.2",
                "1 or TRUE",
                "'1' or '2'",
                "null or b",
                "a or currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a or", AgException.class),
                Arguments.of("or", AgException.class),
                Arguments.of("a OR b", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("a or b", "(a) or (b)"),
                Arguments.of("a or  b", "(a) or (b)"),
                Arguments.of("a or b or c", "(a) or (b) or (c)"),
                Arguments.of("a and b or c and d", "((a) and (b)) or ((c) and (d))")
        );
    }

    Stream<Arguments> countChildren() {
        return Stream.of(
                Arguments.of("a or b", 2),
                Arguments.of("a or b or c", 3)
        );
    }

    @ParameterizedTest
    @MethodSource
    void countChildren(Exp exp, int expected) {
        assertEquals(((SimpleNode) exp).children.length, expected);
    }
}
