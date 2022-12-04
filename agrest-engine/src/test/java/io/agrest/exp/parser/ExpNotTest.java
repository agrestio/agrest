package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNotTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNot.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "!a",
                "! a",
                "not a",
                "not(a)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("!", AgException.class),
                Arguments.of("NOT a", AgException.class)
        );
    }
}