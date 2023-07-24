package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpNotInTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpNotIn.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "a !in('b','c')",
                "a !in ('b', 'c')",
                "a not in ('b', 'c')",
                "a !in ('b')",
                "a !in ('b',  'c')",
                "a !in ($b, $c)",
                "a !in (1, 2)",
                "a !in (1, 2.2)",
                "a !in (1, TRUE)",
                "a !in ('1', '2')",
                "a !in $b"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("a !in", AgException.class),
                Arguments.of("a !in ()", AgException.class),
                Arguments.of("a !in ('b',)", AgException.class),
                Arguments.of("a !in (, c)", AgException.class),
                Arguments.of("a !in (null, 'c')", AgException.class),
                Arguments.of("a NOT in (b, c)", AgException.class),
                Arguments.of("a !IN (b, c)", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("a !in('b','c')"), "a !in ('b', 'c')"),
                Arguments.of(Exp.from("a !in ('b',  'c')"), "a !in ('b', 'c')"),
                Arguments.of(Exp.from("a not in ('b', 'c')"), "a !in ('b', 'c')"),
                Arguments.of(Exp.from("a !in ('b', 'c', 'd')"), "a !in ('b', 'c', 'd')"),
                Arguments.of(Exp.from("a !in $b"), "a !in $b")
        );
    }
}
