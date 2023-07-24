package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNamedParameterTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNamedParameter.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "$a",
                "$1",
                "$ a",
                "$a.b"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("$", AgException.class),
                Arguments.of("$$a", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("$a"), "$a"),
                Arguments.of(Exp.from("$ a"), "$a"),
                Arguments.of(Exp.from("$a.b"), "$a.b")
        );
    }
}
