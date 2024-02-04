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

            "1_2.",
            "1.2_3",
            "2e7_6",
            "1__2__3.4_5___6e7___8",

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
            "1_2.1|12.1",
            "2.2__34|2.234",
            ".3e7_6|3.0E75",

            // TODO: this is wrong, B suffix expected
            "1.7976931348623157e+309b|1.7976931348623157E+309"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1.7976931348623157e+308f",
            "1.7976931348623157e+309d",
            "_1.3",
            "1_.3",
            "1._3",
            "1.3_",
            "2.4_e2",
            "2.4e_2",
            "2.4e2_",
            ".",
            "0x1.F",
            "1e1.1"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
