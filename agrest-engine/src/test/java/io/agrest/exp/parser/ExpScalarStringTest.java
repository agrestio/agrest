package io.agrest.exp.parser;

import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ExpScalarStringTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "'example'",
            "\"example\"",
            "''",
            "'  '",
            "'123'",
            "'example\\'example\\''",
            "\"\\\"example\\\"\""
    })
    void parse(String expString) {
        assertEquals(ExpScalar.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = 'X', value = {
            "'example'|'example'",
            "  'example' |'example'",
            "\"example\"|'example'",
            "''|''",
            "'  '|'  '",
            "'example\\'example\\''|'example'example''",
            "\"\\\"example\\\"\"|'\"example\"'"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"example"})
    public void parseNotAString(String expString) {
        assertFalse(Exp.parse(expString) instanceof ExpScalar);
    }
}
