package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpPathTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpPath.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a",
                "A",
                "_",
                "a.b",
                "a.b.c",
                "a0",
                "a$",
                "a+",
                "a0$b+._c#d+"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("$a", AssertionFailedError.class),
                Arguments.of("0a", AgException.class),
                Arguments.of("a++", AgException.class),
                Arguments.of(".", AgException.class),
                Arguments.of(".b", AgException.class),
                Arguments.of("a..b", AgException.class),
                Arguments.of("a . b", AgException.class),
                Arguments.of("#a", AgException.class),
                Arguments.of("a#0", AgException.class),
                Arguments.of("a#a#a", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("a", "a"),
                Arguments.of(" a  ", "a")
        );
    }
}
