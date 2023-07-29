package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ExpScalarListTest {

    private Exp parseList(String expString) {
        // Wrap scalar list in 'in' expression to meet grammar requirements.
        Exp parsed = Exp.parse("a in (" + expString + ")");
        assertNotNull(parsed);
        return ((SimpleNode) parsed).children[1];
    }

    @ParameterizedTest
    @ValueSource(strings = {"$a, $b",
            "$a,  $b",
            "$a",
            "$a, $b, $c",
            "1, 2",
            "1, 2.2",
            "1, TRUE",
            "'1', '2'"})
    public void parse(String expString) {
        Exp parsed = parseList(expString);
        assertNotNull(parsed);
        assertEquals(ExpScalarList.class, parsed.getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "'$a, $b'|'$a, $b'",
            "'$a,  $b'|'$a, $b'",
            "'$a, $b, $c'|'$a, $b, $c'"
    })
    void parsedToString(String expString, String expected) {
        Exp parsed = parseList(expString);
        assertEquals(expected, parsed.toString());
    }

    @ParameterizedTest
    // TODO: the last two variants should actually be valid
    @ValueSource(strings = {
            "$a,",
            "$a, $b,", ",",
            ",$b", "null, $b",
            "$a, currentDate()"
    })
    void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> parseList(expString));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "$a|1",
            "'$a, $b'|2",
            "'$a, $b, $c'|3"
    })
    public void countChildren(String expString, int expected) {
        Exp parsed = parseList(expString);
        assertEquals(expected, ((SimpleNode) parsed).jjtGetNumChildren());
    }
}
