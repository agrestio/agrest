package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpCurrentTimeTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpCurrentTime.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "currentTime()",
                "currentTime ( )"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("currentTime", AgException.class),
                Arguments.of("currentTime(0)", AgException.class),
                Arguments.of("CURRENTTIME()", AgException.class)
        );
    }
}