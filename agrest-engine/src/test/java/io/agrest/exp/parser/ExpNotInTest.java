package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpNotInTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a !in('b','c')",
            "a !in ('b', 'c')",
            "a not in ('b', 'c')",
            "a !in ('b')",
            "a !in ('b',  'c')",
            "a !in ($b, $c)",
            "a !in (1, 2)",
            "a !in (1, 2.2)",
            "a !in (1, TRUE)",
            "a !in ('1', '2')",
            "a !in $b"
    })
    void parse(String expString) {
        assertEquals(ExpNotIn.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a !in('b','c')|a not in ('b', 'c')",
            "a !in ('b',  'c')|a not in ('b', 'c')",
            "a not in ('b', 'c')|a not in ('b', 'c')",
            "a !in ('b', 'c', 'd')|a not in ('b', 'c', 'd')",
            "a !in $b|a not in $b"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a !in",
            "a !in ()",
            "a !in ('b',)",
            "a !in (, c)",
            "a !in (null, 'c')",
            "a NOT in (b, c)",
            "a !IN (b, c)"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
