package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpSqrtTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpSqrt.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "sqrt(a)",
                "sqrt ( a )"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("sqrt", AgException.class),
                Arguments.of("sqrt()", AgException.class),
                Arguments.of("SQRT(a)", AgException.class)
        );
    }
}