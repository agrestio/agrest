package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpModTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpMod.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "mod(a, b)",
                "mod( a, b )",
                "mod(1, 2)",
                "mod(1.2, 2.3)",
                "mod($a, $b)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("mod", AgException.class),
                Arguments.of("mod()", AgException.class),
                Arguments.of("mod(a)", AgException.class),
                Arguments.of("mod(, b)", AgException.class),
                Arguments.of("mod(1, currentDate())", AgException.class),
                Arguments.of("mod('1', '2')", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("mod(a, b)", "mod(a, b)"),
                Arguments.of("mod( a, b )", "mod(a, b)")
        );
    }
}
