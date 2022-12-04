package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpConcatTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpConcat.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "concat(a,b)",
                "concat ( a )",
                "concat(a, concat(b, c))",
                "concat(a, 'and')"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("concat()", AgException.class),
                Arguments.of("concat", AgException.class),
                Arguments.of("CONCAT(a)", AgException.class),
                Arguments.of("concat(a, and)", AgException.class)
        );
    }
}