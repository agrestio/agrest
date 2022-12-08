package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNotLikeIgnoreCaseTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNotLikeIgnoreCase.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a !likeIgnoreCase b",
                "a not likeIgnoreCase b",
                "a !likeIgnoreCase 'b'",
                "a !likeIgnoreCase 1",
                "a !likeIgnoreCase 1.2",
                "a !likeIgnoreCase $b",
                "a !likeIgnoreCase TRUE",
                "a !likeIgnoreCase(b)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("!likeIgnoreCase", AgException.class),
                Arguments.of("a !likeIgnoreCase", AgException.class),
                Arguments.of("a !likeIgnoreCase()", AgException.class),
                Arguments.of("a NOT likeIgnoreCase b", AgException.class),
                Arguments.of("a !LIKEIGNORECASE b", AgException.class)
        );
    }
}