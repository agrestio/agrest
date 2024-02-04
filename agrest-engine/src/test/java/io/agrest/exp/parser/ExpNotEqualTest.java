package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ExpNotEqualTest {

    @ParameterizedTest
    @ValueSource(strings = {"a!=b",
            "a != b",
            "a <> b",
            "a !=  b",
            "$a != $b",
            "1 != 2",
            "1 != 2.2",
            "1 != TRUE",
            "'1' != '2'",
            "null != c",
            "a != currentDate()"})
    public void parse(String expString) {
        assertEquals(ExpNotEqual.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a!=b|(a) != (b)",
            "a <> b|(a) != (b)",
            "a !=  b|(a) != (b)"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"!=", "a !=", "!= b"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }

    @Test
    public void deepCopy() {
        ExpNotEqual e = (ExpNotEqual) Exp.notEqual("a", 5);
        Exp copy = e.deepCopy();
        assertNotSame(e, copy);
        assertEquals(e, copy);
    }

    @Test
    public void shallowCopy_toString() {
        ExpNotEqual e = (ExpNotEqual) Exp.notEqual("a", 5);
        SimpleNode copy = e.shallowCopy();
        assertEquals("? != ?", copy.toString());
    }
}
