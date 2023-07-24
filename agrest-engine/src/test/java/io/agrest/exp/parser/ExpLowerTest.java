package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpLowerTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpLower.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "lower(a)",
                "lower ( a )",
                "lower('a')"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("lower", AgException.class),
                Arguments.of("lower()", AgException.class),
                Arguments.of("lower(1)", AgException.class),
                Arguments.of("lower($a)", AgException.class),
                Arguments.of("LOWER(a)", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("lower(a)", "lower(a)"),
                Arguments.of("lower ( a )", "lower(a)")
        );
    }
}
