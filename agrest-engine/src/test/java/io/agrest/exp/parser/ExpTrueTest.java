package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpTrueTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpTrue.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "true",
                "TRUE"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("True", AssertionFailedError.class),

                Arguments.of("true()", AgException.class)
        );
    }
}