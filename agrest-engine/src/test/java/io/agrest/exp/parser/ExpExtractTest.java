package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpExtractTest {

    @ParameterizedTest
    @ValueSource(strings = {
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
    })
    void parse(String expString) {
        assertEquals(ExpExtract.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "year(a)|year(a)",
            "year ( a )|year(a)",
            "month(a)|month(a)",
            "week(a)|week(a)",
            "day(a)|day(a)",
            "dayOfYear(a)|dayOfYear(a)",
            "dayOfMonth(a)|dayOfMonth(a)",
            "dayOfWeek(a)|dayOfWeek(a)",
            "hour(a)|hour(a)",
            "minute(a)|minute(a)",
            "second(a)|second(a)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"year()", "year(0)", "year('now')", "year($a)", "year(null)", "YEAR(a)", "weekOfMonth(a)"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
