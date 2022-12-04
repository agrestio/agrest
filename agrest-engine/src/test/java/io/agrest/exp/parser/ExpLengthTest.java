package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpLengthTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpLength.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "length(a)",
                "length ( a )",
                "length('a')"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("length", AgException.class),
                Arguments.of("length()", AgException.class),
                Arguments.of("length(1)", AgException.class),
                Arguments.of("LENGTH(a)", AgException.class)
        );
    }
}