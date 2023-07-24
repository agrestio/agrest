package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpOrTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpOr.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a or b",
                "a or  b",
                "$a or $b",
                "1 or 2",
                "1 or 2.2",
                "1 or TRUE",
                "'1' or '2'",
                "null or b",
                "a or currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a or", AgException.class),
                Arguments.of("or", AgException.class),
                Arguments.of("a OR b", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("a or b"), "(a) or (b)"),
                Arguments.of(Exp.from("a or  b"), "(a) or (b)"),
                Arguments.of(Exp.from("a or b or c"), "((a) or (b)) or (c)")
        );
    }
}
