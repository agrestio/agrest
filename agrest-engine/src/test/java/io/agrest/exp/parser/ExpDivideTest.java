package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpDivideTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpDivide.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "1/2",
                "1 /  2",
                "1 / 1.3",
                "1 / $a",
                "1 / a",
                "1 / 0",
                "1 / abs(-3)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a /", AgException.class),
                Arguments.of("/", AgException.class),
                Arguments.of("1 / 'a'", AgException.class),
                Arguments.of("getDate() / 2", AgException.class)
        );
    }
}