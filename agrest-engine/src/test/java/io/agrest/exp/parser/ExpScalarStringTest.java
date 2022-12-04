package io.agrest.exp.parser;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

@Disabled("To be discussed")
class ExpScalarStringTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalarString.class);
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
                // TODO: Should probably work properly.
                "\"\\\"example\\\"\""
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("example", AssertionFailedError.class)
        );
    }
}