package io.agrest.exp.parser;

import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ExpScalarNullTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "NULL"
    })
    void parse(String expString) {
        assertEquals(ExpScalar.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "null|null",
            " null  |null",
            "NULL|null"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nil",
            "Null",
            "None"
    })
    public void parseNotANull(String expString) {
        assertFalse(Exp.parse(expString) instanceof ExpScalar);
    }
}
