package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpPathTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "A",
            "_",
            "a.b",
            "a.b.c",
            "a0",
            "a$",
            "a+",
            "a0$b+._c#d+",

            "year",
            "month",
            "week",
            "day_of_year",
            "day",
            "day_of_month",
            "day_of_week",
            "hour",
            "minute",
            "second",

            "current_date",
            "current_time",
            "current_timestamp",

            "concat",
            "substring",
            "trim",
            "lower",
            "upper",

            "length",
            "locate",
            "abs",
            "sqrt",
            "mod"
    })
    void parse(String expString) {
        assertEquals(ExpPath.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a|a",
            " a  |a"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0a",
            "a++",
            ".",
            ".b",
            "a..b",
            "a . b",
            "#a",
            "a#0",
            "a#a#a"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
