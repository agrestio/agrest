package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpOrTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a or b",
            "a or  b",
            "$a or $b",
            "1 or 2",
            "1 or 2.2",
            "1 or TRUE",
            "'1' or '2'",
            "null or b",
            "a or currentDate()"
    })
    void parse(String expString) {
        assertEquals(ExpOr.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a !in('b','c')|a not in ('b', 'c')",
            "a !in ('b',  'c')|a not in ('b', 'c')",
            "a not in ('b', 'c')|a not in ('b', 'c')",
            "a !in ('b', 'c', 'd')|a not in ('b', 'c', 'd')",
            "a !in $b|a not in $b"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a or",
            "or",
            "a OR b"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a or b|2",
            "a or b or c|3"
    })
    void countChildren(String exp, int expected) {
        assertEquals(((SimpleNode) Exp.parse(exp)).children.length, expected);
    }
}
