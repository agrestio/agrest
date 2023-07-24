package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpBetweenTest extends AbstractExpTest {

    @Override
    protected ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpBetween.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a between b and c",
                "a between  b and  c",
                "a between $b and $c",
                "a between 1 and 2",
                "a between 1 and 2.2",
                "a between 1 and TRUE",
                "a between '1' and '2'",
                "a between null and c",
                "a between a and currentDate()"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a between b", AgException.class),
                Arguments.of("a between b and", AgException.class),
                Arguments.of("a between and c", AgException.class),
                Arguments.of("a between", AgException.class),
                Arguments.of("a BETWEEN b and c", AgException.class),
                Arguments.of("a between b AND c", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("a between b and c", "a between b and c"),
                Arguments.of("a between  b and  c", "a between b and c")
        );
    }
}
