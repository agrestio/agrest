package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpAbsTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpAbs.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "abs(1)",
                "abs(  1 )",
                "abs(1.2)",
                "abs($a)",
                "abs(a)",
                "abs(abs(1))"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("abs", AgException.class),
                Arguments.of("abs()", AgException.class),
                Arguments.of("ABS(a)", AgException.class),
                Arguments.of("abs(a and b)", AgException.class)
        );
    }
}