package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;
import org.opentest4j.AssertionFailedError;

import java.util.stream.Stream;

class ExpObjPathTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpObjPath.class);
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
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("$a", AssertionFailedError.class),

                Arguments.of("0a", AgException.class),
                Arguments.of("a++", AgException.class),

                Arguments.of(".", TokenMgrException.class),
                Arguments.of(".b", TokenMgrException.class),
                Arguments.of("a..b", TokenMgrException.class),
                Arguments.of("a . b", TokenMgrException.class),
                Arguments.of("#a", TokenMgrException.class),
                Arguments.of("a#0", TokenMgrException.class),
                Arguments.of("a#a#a", TokenMgrException.class)
        );
    }
}