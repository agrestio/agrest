package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNotBetweenTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNotBetween.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a !between b and c",
                "a ! between b and c",
                "a not between b and c",
                "a !between (b + c) and d",
                "a !between b + c and d",
                "a !between TRUE and TRUE",
                // TODO: Should probably throw AgException.
                "a !between 1 and (c and d)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("a !between b", AgException.class),
                Arguments.of("a !between b and", AgException.class),
                Arguments.of("a !between and c", AgException.class),
                Arguments.of("a !between", AgException.class),
                Arguments.of("a !BETWEEN b and c", AgException.class),
                Arguments.of("a NOT between b and c", AgException.class),
                Arguments.of("a between b AND c", AgException.class)
        );
    }
}