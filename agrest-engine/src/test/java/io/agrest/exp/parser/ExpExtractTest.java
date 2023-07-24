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
    Stream<Arguments> stringify() {
        return Stream.of(
                Arguments.of(Exp.from("year(a)"), "year(a)"),
                Arguments.of(Exp.from("year ( a )"), "year(a)"),
                Arguments.of(Exp.from("month(a)"), "month(a)"),
                Arguments.of(Exp.from("week(a)"), "week(a)"),
                Arguments.of(Exp.from("day(a)"), "day(a)"),
                Arguments.of(Exp.from("dayOfYear(a)"), "dayOfYear(a)"),
                Arguments.of(Exp.from("dayOfMonth(a)"), "dayOfMonth(a)"),
                Arguments.of(Exp.from("dayOfWeek(a)"), "dayOfWeek(a)"),
                Arguments.of(Exp.from("hour(a)"), "hour(a)"),
                Arguments.of(Exp.from("minute(a)"), "minute(a)"),
                Arguments.of(Exp.from("second(a)"), "second(a)")
        );
    }
}
