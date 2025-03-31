package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpModTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "mod(a, b)",
            "mod( a, b )",
            "mod(1, 2)",
            "mod(1.2, 2.3)",
            "mod($a, $b)"
    })
    void parse(String expString) {
        assertEquals(ExpMod.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "mod(a, b)|mod(a, b)",
            "mod( a, b )|mod(a, b)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "mod()",
            "mod(a)",
            "mod(, b)",
            "mod(1, currentDate())",
            "mod('1', '2')"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
