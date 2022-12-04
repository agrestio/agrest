package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpCurrentDateTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpCurrentDate.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "currentDate()",
                "currentDate ( )"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("currentDate", AgException.class),
                Arguments.of("CURRENTDATE()", AgException.class)
        );
    }
}