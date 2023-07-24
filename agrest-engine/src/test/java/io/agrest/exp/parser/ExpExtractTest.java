package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
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
                "year(t.a)",
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
    Stream<Arguments> parseExpThrows() {
        return Stream.of(
                Arguments.of("year", AgException.class),
                Arguments.of("year()", AgException.class),
                Arguments.of("year(0)", AgException.class),
                Arguments.of("year('now')", AgException.class),
                Arguments.of("year($a)", AgException.class),
                Arguments.of("year(null)", AgException.class),
                Arguments.of("YEAR(a)", AgException.class),
                Arguments.of("weekOfMonth(a)", AgException.class)
        );
    }

    @Override
    Stream<Arguments> stringifyRaw() {
        return Stream.of(
                Arguments.of("year(a)", "year(a)"),
                Arguments.of("year ( a )", "year(a)"),
                Arguments.of("month(a)", "month(a)"),
                Arguments.of("week(a)", "week(a)"),
                Arguments.of("day(a)", "day(a)"),
                Arguments.of("dayOfYear(a)", "dayOfYear(a)"),
                Arguments.of("dayOfMonth(a)", "dayOfMonth(a)"),
                Arguments.of("dayOfWeek(a)", "dayOfWeek(a)"),
                Arguments.of("hour(a)", "hour(a)"),
                Arguments.of("minute(a)", "minute(a)"),
                Arguments.of("second(a)", "second(a)")
        );
    }
}
