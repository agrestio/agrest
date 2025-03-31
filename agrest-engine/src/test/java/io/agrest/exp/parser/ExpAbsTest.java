package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpAbsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "abs(1)",
            "abs(  1 )",
            "abs(1.2)",
            "abs($a)",
            "abs(a)",
            "abs(abs(1))"})
    public void parse(String expString) {
        assertEquals(ExpAbs.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "abs(1)|abs(1)",
            "abs(  1 )|abs(1)",
            "abs(abs(1))|abs(abs(1))"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abs()", "ABS(a)", "abs(a and b)"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
