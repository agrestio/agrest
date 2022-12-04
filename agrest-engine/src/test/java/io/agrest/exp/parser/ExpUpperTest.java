package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpUpperTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpUpper.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "upper(a)",
                "upper ( a )",
                "upper('a')"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("upper", AgException.class),
                Arguments.of("upper()", AgException.class),
                Arguments.of("upper(1)", AgException.class),
                Arguments.of("UPPER(a)", AgException.class)
        );
    }
}