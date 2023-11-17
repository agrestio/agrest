package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExpInTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a in('b','c')",
            "a in ('b', 'c')",
            "a in ('b')",
            "a in ('b',  'c')",
            "a in ($b, $c)",
            "a in (1, 2)",
            "a in (1, 2.2)",
            "a in (1, TRUE)",
            "a in ('1', '2')",
            "a in $b"})
    public void parse(String expString) {
        assertEquals(ExpIn.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a in('b','c')|a in ('b', 'c')",
            "a in ('b',  'c')|a in ('b', 'c')",
            "a in ('b', 'c', 'd')|a in ('b', 'c', 'd')",
            "a in $b|a in $b"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    // TODO: null should be a valid scalar in the list
    @ValueSource(strings = {
            "a in",
            "a in ()",
            "a in ('b',)",
            "a in (null, 'c')",
            "a in (, 'c')",
            "a IN ('b', 'c')"})
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }

    @Test
    public void parameterizedToString() {
        assertEquals("a in ('x', 'y')", Exp.parse("a in $l").positionalParams(List.of("'x'", "'y'")).toString());
    }

    @Test
    public void manualToString() {
        assertEquals("a in ('x', 'y')", Exp.in("a", "'x'", "'y'").toString());
    }

    @Test
    public void deepCopy() {
        Exp e = Exp.in("a", "x", "y");
        Exp eCopy = ((SimpleNode) e).deepCopy();
        assertNotSame(e, eCopy);
        assertEquals(e, eCopy);
    }
}
