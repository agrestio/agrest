package io.agrest.exp.parser;

import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpScalarStringTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalar.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "'example'",
                "\"example\"",
                "''",
                "'  '",
                "'123'",
                "'example\\'example\\''",
                "\"\\\"example\\\"\""
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("example", AssertionFailedError.class)
        );
    }
}