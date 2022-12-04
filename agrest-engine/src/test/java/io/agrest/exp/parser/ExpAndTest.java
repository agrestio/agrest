package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpAndTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpAnd.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a and b",
                "a and b and c",
                "a and (b or c)",
                // TODO: Should probably throw AgException.
                "a and (b + c)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("a and", AgException.class),
                Arguments.of("and", AgException.class),
                Arguments.of("a AND b", AgException.class)
        );
    }
}