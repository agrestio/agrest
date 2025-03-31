package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpConcatTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "concat('a','b')",
            "concat ('a',  'b')",
            "concat('a')",
            "concat(\"a\", \"b\")",
            "concat(a, b)",
            "concat(t.a, t.b)",
            "concat('a', t.b)"
    })
    void parse(String expString) {
        assertEquals(ExpConcat.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "concat(a,b)|concat(a, b)",
            "concat (a,  b)|concat(a, b)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"concat()", "concat(1, 2)", "concat($a, $b)", "CONCAT(a)", "concat(a, and)"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
