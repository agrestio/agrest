package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpScalarIntTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "0",
            "1",

            // Integer.MAX_VALUE
            "2147483647",

            // Integer.MAX_VALUE + 1
            "2147483648L",
            "2147483648l",

            // Long.MAX_VALUE + 1
            "9223372036854775808H",
            "9223372036854775808h",

            "01234567",
            "0x12345678",
            "0x09abcdef",
            "0x09ABCDEF",
            "0X1",
            "+1"
    })
    void parse(String expString) {
        assertEquals(ExpScalar.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "1|1",
            " 1  |1",
            "2147483647|2147483647",
            "2147483648L|2147483648",
            "9223372036854775808H|9223372036854775808",
            "01234567|342391",
            "0x12345678|305419896",
            "0x09abcdef|162254319"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0x",
            "08",
            "0xG",
            "2147483648",
            "9223372036854775808l"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
