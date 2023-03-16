package io.agrest.exp.parser;

import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpScalarNullTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpScalar.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "null",
                "NULL"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("nil", AssertionFailedError.class),
                Arguments.of("Null", AssertionFailedError.class),
                Arguments.of("None", AssertionFailedError.class)
        );
    }
}