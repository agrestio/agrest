package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpBitwiseNotTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpBitwiseNot.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "~2",
                "~ 2",
                "~1.3",
                "~$a",
                "~a",
                "~abs(-3)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("~", AgException.class),
                Arguments.of("~'a'", AgException.class),
                Arguments.of("~getDate()", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("~2"), "~(2)"),
                Arguments.of(Exp.from("~ 2"), "~(2)")
        );
    }
}
