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
                "a and  b",
                "$a and $b",
                "1 and 2",
                "1 and 2.2",
                "1 and TRUE",
                "'1' and '2'",
                "null and b",
                "a and currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a and", AgException.class),
                Arguments.of("and", AgException.class),
                Arguments.of("a AND b", AgException.class)
        );
    }
}