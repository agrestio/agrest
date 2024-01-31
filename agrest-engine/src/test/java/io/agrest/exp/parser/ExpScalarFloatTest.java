package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpScalarFloatTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "1.0",
            ".1",
            "1e1",
            "1E1",
            "1.1e1",

            // Float.MAX_VALUE
            "3.4028235e+38f",
            "3.4028235e+38F",

            // Double.MAX_VALUE
            "1.7976931348623157e+308",
            "1.7976931348623157e+308d",
            "1.7976931348623157e+308D",

            // 10 * Double.MAX_VALUE
            "1.7976931348623157e+309b",
            "1.7976931348623157e+309B"
    })
    void parse(String expString) {
        assertEquals(ExpScalar.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "1.0|1.0",
            "  1.0 |1.0",
            ".1|0.1",
            "1e1|10.0",
            "1E1|10.0",
            "1.1e1|11.0",
            "3.4028235e+38f|3.4028235E38",
            "1.7976931348623157e+308|1.7976931348623157E308",
            "1.7976931348623157e+308d|1.7976931348623157E308",
            "1.7976931348623157e+309b|1.7976931348623157E+309B"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1.7976931348623157e+308f",
            "1.7976931348623157e+309d",
            ".",
            "0x1.F",
            "1e1.1"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
