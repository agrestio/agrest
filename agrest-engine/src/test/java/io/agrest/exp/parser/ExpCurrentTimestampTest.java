package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpCurrentTimestampTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpCurrentTimestamp.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "currentTimestamp()",
                "currentTimestamp ( )"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("currentTimestamp", AgException.class),
                Arguments.of("currentTimestamp(0)", AgException.class),
                Arguments.of("CURRENTTIMESTAMP()", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("currentTimestamp()"), "currentTimestamp()"),
                Arguments.of(Exp.from("currentTimestamp ( )"), "currentTimestamp()")
        );
    }
}
