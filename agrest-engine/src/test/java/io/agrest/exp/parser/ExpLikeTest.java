package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpLikeTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpLike.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a like b",
                "a like 'b'",
                "a like 1",
                "a like 1.2",
                "a like $b",
                "a like TRUE",
                "a like(b)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("like", AgException.class),
                Arguments.of("a like", AgException.class),
                Arguments.of("a like()", AgException.class),
                Arguments.of("a LIKE b", AgException.class)
        );
    }
}