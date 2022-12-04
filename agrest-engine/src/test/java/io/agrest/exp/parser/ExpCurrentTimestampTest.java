package io.agrest.exp.parser;

import io.agrest.AgException;
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
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("currentTimestamp", AgException.class),
                Arguments.of("CURRENTTIMESTAMP()", AgException.class)
        );
    }
}