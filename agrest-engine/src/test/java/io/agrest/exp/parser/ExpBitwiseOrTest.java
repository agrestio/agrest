package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpBitwiseOrTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "1|2",
            "1 |  2",
            "1 | 1.3",
            "1 | $a",
            "1 | a",
            "1 | abs(-3)"
    })
    void parse(String expString) {
        assertEquals(ExpBitwiseOr.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ':', value = {
            "1|2:(1) | (2)",
            "1 |  2:(1) | (2)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 |", "| 2", "|", "1 || 2", "1 | 'a'", "1 | getDate()"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
