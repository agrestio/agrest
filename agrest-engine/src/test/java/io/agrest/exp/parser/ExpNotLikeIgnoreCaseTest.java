package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpNotLikeIgnoreCaseTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a !likeIgnoreCase b",
            "a not likeIgnoreCase b",
            "a !likeIgnoreCase b escape 'c'",
            "a !likeIgnoreCase 'b'",
            "a !likeIgnoreCase 1",
            "a !likeIgnoreCase 1.2",
            "a !likeIgnoreCase $b",
            "a !likeIgnoreCase TRUE",
            "a !likeIgnoreCase(b)"
    })
    void parse(String expString) {
        assertEquals(ExpNotLikeIgnoreCase.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a !likeIgnoreCase b|a not likeIgnoreCase b",
            "a !likeIgnoreCase   b|a not likeIgnoreCase b",
            "a not likeIgnoreCase b|a not likeIgnoreCase b",
            "a !likeIgnoreCase b escape 'c'|a not likeIgnoreCase b escape 'c'",
            "a !likeIgnoreCase (b)|a not likeIgnoreCase b"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "!likeIgnoreCase",
            "a !likeIgnoreCase",
            "a !likeIgnoreCase()",
            "a NOT likeIgnoreCase b",
            "a !LIKEIGNORECASE b",
            "a !likeIgnoreCase b ESCAPE 'c'"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
