package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpSubstringTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "substring(a, 0)",
            "substring ( a, 0 )",
            "substring('a', 0)",
            "substring(a, $b)",
            "substring(a, 0, 1)"
    })
    void parse(String expString) {
        assertEquals(ExpSubstring.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "substring(a,0)|substring(a, 0)",
            "substring ( a, 0 )|substring(a, 0)",
            "substring(a, 0, 1)|substring(a, 0, 1)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "substring()",
            "substring(a)",
            "substring(a, '1')",
            "substring($a, 1)",
            "SUBSTRING(a, 0)"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
