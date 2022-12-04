package io.agrest.exp.parser;

import io.agrest.AgException;
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
                "mod( a, b )"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("mod", AgException.class),
                Arguments.of("mod()", AgException.class),
                Arguments.of("mod(a)", AgException.class),
                Arguments.of("mod(, b)", AgException.class),
                Arguments.of("mod(a and b)", AgException.class)
        );
    }
}