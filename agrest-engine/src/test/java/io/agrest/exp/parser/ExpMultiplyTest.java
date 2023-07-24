package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpMultiplyTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpMultiply.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "1*2",
                "1 *  2",
                "1 * 1.3",
                "1 * $a",
                "1 * a",
                "1 * 0",
                "1 * abs(-3)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a *", AgException.class),
                Arguments.of("*", AgException.class),
                Arguments.of("1 * 'a'", AgException.class),
                Arguments.of("getDate() * 2", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("1*2"), "(1) * (2)"),
                Arguments.of(Exp.from("1 *  2"), "(1) * (2)")
        );
    }
}
