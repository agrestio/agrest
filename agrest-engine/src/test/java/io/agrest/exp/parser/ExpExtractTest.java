package io.agrest.exp.parser;

import io.agrest.AgException;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class ExpExtractTest extends AbstractExpTest {

    @Override
    ExpTestVisitor provideVisitor() {
        return new ExpTestVisitor(ExpExtract.class);
    }

    @Override
    Stream<String> parseExp() {
        return Stream.of(
                "year(a)",
                "year ( a )",
                "month(a)",
                "week(a)",
                "day(a)",
                "dayOfYear(a)",
                "dayOfMonth(a)",
                "dayOfWeek(a)",
                "hour(a)",
                "minute(a)",
                "second(a)"
        );
    }

    @Override
    Stream<Arguments> parseExpThrows_AgException() {
        return Stream.of(
                Arguments.of("year", AgException.class),
                Arguments.of("year()", AgException.class),
                Arguments.of("YEAR(a)", AgException.class),
                Arguments.of("weekOfMonth(a)", AgException.class)
        );
    }
}