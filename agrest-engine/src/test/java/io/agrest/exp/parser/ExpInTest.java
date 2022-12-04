package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpInTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpIn.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a in($b)",
                "a in(1,2,3)",
                "a in ( 1, 2, 3 )"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("in", AgException.class),
                Arguments.of("a in", AgException.class),
                Arguments.of("a in()", AgException.class),
                Arguments.of("a IN(1, 2, 3)", AgException.class)
        );
    }
}