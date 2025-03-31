package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpAndTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a and b",
            "a and  b",
            "$a and $b",
            "1 and 2",
            "1 and 2.2",
            "1 and TRUE",
            "'1' and '2'",
            "null and b",
            "a and currentDate()"
    })
    public void parse(String expString) {
        assertEquals(ExpAnd.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a and b|a and b",
            "a and  b|a and b",
            "a and b and c|a and b and c",
            "(a or b) and (c or d)|(a or b) and (c or d)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a and", "and", "a AND b"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a and b|2",
            "a and b and c|3"
    })
    void countChildren(String exp, int expected) {
        assertEquals(((SimpleNode) Exp.parse(exp)).children.length, expected);
    }
}
