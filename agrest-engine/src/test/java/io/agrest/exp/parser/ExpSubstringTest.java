package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
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
                "substring(a, $b)",
                "substring(a, 0, 1)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("substring", AgException.class),
                Arguments.of("substring()", AgException.class),
                Arguments.of("substring(a)", AgException.class),
                Arguments.of("substring(a, '1')", AgException.class),
                Arguments.of("substring($a, 1)", AgException.class),
                Arguments.of("SUBSTRING(a, 0)", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("substring(a,0)", "substring(a, 0)"),
                Arguments.of("substring ( a, 0 )", "substring(a, 0)"),
                Arguments.of("substring(a, 0, 1)", "substring(a, 0, 1)")
        );
    }
}
