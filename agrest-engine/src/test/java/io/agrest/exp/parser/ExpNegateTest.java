package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNegateTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNegate.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "-a",
                "- a",
                "-1",
                "-1.1",

                // TODO: Should throw AgException.
                "--a",
                "---a"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("-", AgException.class),
                Arguments.of("-'1'", AgException.class),
                Arguments.of("-currentDate()", AgException.class)
        );
    }
}