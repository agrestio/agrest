package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpTrimTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpTrim.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "trim(a)",
                "trim ( a )",
                "trim('a')"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("trim", AgException.class),
                Arguments.of("trim()", AgException.class),
                Arguments.of("trim(1)", AgException.class),
                Arguments.of("trim($a)", AgException.class),
                Arguments.of("TRIM(a)", AgException.class)
        );
    }
}