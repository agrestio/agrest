package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpBitwiseNotTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpBitwiseNot.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "~1",
                "~ 1",
                "~-1"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("~", AgException.class)
        );
    }
}