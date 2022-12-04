package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpAddTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpAdd.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a + b",
                "a - b + c",
                "a + (b - c)",
                "a + (b and c)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("a +", AgException.class),
                Arguments.of("+", AgException.class)
        );
    }
}