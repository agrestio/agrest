package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpFalseTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpFalse.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "false",
                "FALSE"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("False", AssertionError.class),
                Arguments.of("false()", AgException.class)
        );
    }
}