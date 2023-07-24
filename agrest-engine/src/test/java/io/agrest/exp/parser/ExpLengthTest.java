package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
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
                "length('a')",
                "length(\"a\")"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("length", AgException.class),
                Arguments.of("length()", AgException.class),
                Arguments.of("length(1)", AgException.class),
                Arguments.of("length($a)", AgException.class),
                Arguments.of("LENGTH(a)", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("length(a)", "length(a)"),
                Arguments.of("length ( a )", "length(a)")
        );
    }
}
