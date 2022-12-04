package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNotLikeTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNotLike.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a !like b",
                "a ! like b",
                "a not like b",
                "a !like TRUE",
                "a !like 1",
                "a !like(b)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("", AgException.class),
                Arguments.of("!like", AgException.class),
                Arguments.of("a !like() b", AgException.class),
                Arguments.of("a not(like(b))", AgException.class),
                Arguments.of("a !LIKE b", AgException.class),
                Arguments.of("a NOT like b", AgException.class)
        );
    }
}