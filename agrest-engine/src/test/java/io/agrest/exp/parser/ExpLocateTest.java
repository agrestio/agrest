package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpLocateTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpLocate.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "locate(a,b)",
                "locate ( a, b )",
                "locate('a', 'b')",
                "locate(a, b, 1)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("locate", AgException.class),
                Arguments.of("locate()", AgException.class),
                Arguments.of("locate(a)", AgException.class),
                Arguments.of("locate(a, 1)", AgException.class),
                Arguments.of("locate(a, $b)", AgException.class),
                Arguments.of("locate(a, b, '1')", AgException.class),
                Arguments.of("LOCATE(a, b)", AgException.class)
        );
    }
}