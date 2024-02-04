package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpSqrtTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "sqrt(1)",
            "sqrt(  1 )",
            "sqrt(1.2)",
            "sqrt($a)",
            "sqrt(a)",
            "sqrt(abs(1))"
    })
    void parse(String expString) {
        assertEquals(ExpSqrt.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "sqrt(1)|sqrt(1)",
            "sqrt(  1 )|sqrt(1)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "sqrt",
            "sqrt()",
            "SQRT(a)",
            "sqrt(a and b)"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
