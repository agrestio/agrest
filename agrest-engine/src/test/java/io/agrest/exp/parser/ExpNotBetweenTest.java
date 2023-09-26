package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpNotBetweenTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a !between b and c",
            "a not between b and c",
            "a !between  b and  c",
            "a !between $b and $c",
            "a !between 1 and 2",
            "a !between 1 and 2.2",
            "a !between 1 and TRUE",
            "a !between '1' and '2'",
            "a !between null and c",
            "a !between a and currentDate()"
    })
    void parse(String expString) {
        assertEquals(ExpNotBetween.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a !between b and c|a not between b and c",
            "a not between b and c|a not between b and c",
            "a !between  b and  c|a not between b and c"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a !between b",
            "a !between b and",
            "a !between and c",
            "a !between",
            "a NOT between b and c",
            "a !BETWEEN b and c",
            "a !between b AND c"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
