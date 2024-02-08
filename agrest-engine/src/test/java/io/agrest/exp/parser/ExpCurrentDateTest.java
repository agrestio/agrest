package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpCurrentDateTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "currentDate()",
            "currentDate ( )"
    })
    void parse(String expString) {
        assertEquals(ExpCurrentDate.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "currentDate()|currentDate()",
            "currentDate ( )|currentDate()"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"currentDate(0)", "CURRENTDATE()"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
