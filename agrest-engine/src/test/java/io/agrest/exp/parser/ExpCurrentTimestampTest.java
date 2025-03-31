package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpCurrentTimestampTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "currentTimestamp()",
            "currentTimestamp ( )"
    })
    void parse(String expString) {
        assertEquals(ExpCurrentTimestamp.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "currentTimestamp()|currentTimestamp()",
            "currentTimestamp ( )|currentTimestamp()"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"currentTimestamp(0)", "CURRENTTIMESTAMP()"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
