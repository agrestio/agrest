package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpSubstringTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpSubstring.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "substring(a, 0)",
                "substring ( a, 0 )",
                "substring('a', 0)",
                "substring(a, 0, 1)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("substring", AgException.class),
                Arguments.of("substring()", AgException.class),
                Arguments.of("substring(a)", AgException.class),
                Arguments.of("substring(1)", AgException.class),
                Arguments.of("SUBSTRING(a)", AgException.class)
        );
    }
}