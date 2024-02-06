package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpLocateTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "locate(a,b)",
            "locate ( a, b )",
            "locate('a', 'b')",
            "locate(a, b, 1)"
    })
    void parse(String expString) {
        assertEquals(ExpLocate.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "locate(a,b)|locate(a, b)",
            "locate ( a, b )|locate(a, b)",
            "locate (a, b, 1)|locate(a, b, (1))"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "locate()",
            "locate(a)",
            "locate(a, 1)",
            "locate(a, $b)",
            "locate(a, b, '1')",
            "LOCATE(a, b)"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
