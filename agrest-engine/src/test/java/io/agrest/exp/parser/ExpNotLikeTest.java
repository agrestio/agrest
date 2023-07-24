package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
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
                "a not like b",
                "a !like b escape 'c'",
                "a !like 'b'",
                "a !like 1",
                "a !like 1.2",
                "a !like $b",
                "a !like TRUE",
                "a !like(b)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("!like", AgException.class),
                Arguments.of("a !like", AgException.class),
                Arguments.of("a !like()", AgException.class),
                Arguments.of("a NOT like b", AgException.class),
                Arguments.of("a !LIKE b", AgException.class),
                Arguments.of("a !like b ESCAPE 'c'", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("a !like b"), "a !like b"),
                Arguments.of(Exp.from("a !like   b"), "a !like b"),
                Arguments.of(Exp.from("a !like b escape 'c'"), "a !like b escape 'c'"),
                Arguments.of(Exp.from("a not like b"), "a !like b"),
                Arguments.of(Exp.from("a !like (b)"), "a !like b")
        );
    }
}
