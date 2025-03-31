package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ExpExistsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "exists a",
            "exists a.b",
    })
    public void parse(String expString) {
        assertEquals(ExpExists.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "exists a|exists a",
            "exists a.b|exists a.b",
            "exists a.b or a.c in (1, 2, 3)|(exists a.b) or (a.c in (1, 2, 3))",
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "exists",
            "exists()",
            "exists (name == 'test')",
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
