package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpLikeTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a like 'abc%'",
            "a like 'abc%' escape 'c'",
            "a like 'b'",
            "a like 1",
            "a like 1.2",
            "a like $b",
            "a like TRUE",
            "a like(b)"
    })
    void parse(String expString) {
        assertEquals(ExpLike.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a like b|a like b",
            "a like   b|a like b",
            "a like b escape 'c'|a like b escape 'c'",
            "a like (b)|a like b"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "like",
            "a like",
            "a like()",
            "a LIKE b",
            "a like b ESCAPE 'c'"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }
}
