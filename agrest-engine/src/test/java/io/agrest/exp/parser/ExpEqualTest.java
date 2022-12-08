package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpEqualTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpEqual.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a=b",
                "a = b",
                "a =  b",
                "a == b",
                "$a = $b",
                "1 = 2",
                "1 = 2.2",
                "1 = TRUE",
                "'1' = '2'",
                "null = c",
                "a = currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("=", AgException.class),
                Arguments.of("a =", AgException.class),
                Arguments.of("= b", AgException.class)
        );
    }
}