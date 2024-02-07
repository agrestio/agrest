package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpGreaterTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a>b",
            "a > b",
            "a >  b",
            "$a > $b",
            "1 > 2",
            "1 > 2.2",
            "1 > TRUE",
            "'1' > '2'",
            "null > c",
            "a > currentDate()"
    })
    void parse(String expString) {
        assertEquals(ExpGreater.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a>b|a > b",
            "a > b|a > b"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {">", "a >", "> b"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
