package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpLessOrEqualTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpLessOrEqual.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a<=b",
                "a <= b",
                "a <= b + c",
                "a + b <= c",
                // TODO: Should probably throw AgException
                "a <= (b and c)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("a <=", AgException.class),
                Arguments.of("<= b", AgException.class),
                Arguments.of("<=", AgException.class),
                Arguments.of("a <= b <= c", AgException.class)
        );
    }
}