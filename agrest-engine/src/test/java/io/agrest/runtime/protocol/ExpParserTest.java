package io.agrest.runtime.protocol;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import io.agrest.runtime.jackson.JacksonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExpParserTest {

    private static ExpParser parser;

    @BeforeAll
    public static void beforeAll() {
        parser = new ExpParser(JacksonService.create());
    }

    @Test
    public void fromString_Numbers() {
        assertEquals("1", parser.fromString("1").toString());
        assertEquals("2.1", parser.fromString("2.1").toString());
        assertEquals("-(55)", parser.fromString("-55").toString());

        // TODO: this is wrong, L suffix expected
        assertEquals("3147483647", parser.fromString("3147483647L").toString());

        // TODO: this is wrong, B suffix expected
        assertEquals("2.1000001", parser.fromString("2.1000001B").toString());
    }

    @Test
    public void fromString_Strings() {
        assertEquals("'a'", parser.fromString("'a'").toString());
        assertEquals("'a'", parser.fromString("\"a\"").toString());

        // TODO: this is wrong, single quote must be escaped or double quotes used
        assertEquals("'a'b'", parser.fromString("\"a'b\"").toString());
        assertEquals("'a\"b'", parser.fromString("'a\"b'").toString());
        assertEquals("'a'b'", parser.fromString("'a\\'b'").toString());
        assertEquals("'a\"b'", parser.fromString("\"a\\\"b\"").toString());
    }

    @Test
    public void fromString_Strings_BadEscaping() {
        assertThrows(AgException.class, () -> parser.fromString("'a'b'").toString());
    }

    @Test
    public void fromString_Bare() {
        Exp exp = parser.fromString("a = 12345 and b = 'John Smith' and c = true");
        assertNotNull(exp);
        assertEquals("((a) = (12345)) and ((b) = ('John Smith')) and ((c) = (true))", exp.toString());
    }

    @Test
    public void fromString_Functions() {
        Exp exp = parser.fromString("length(b) > 5");
        assertNotNull(exp);
        assertEquals("(length(b)) > (5)", exp.toString());
    }

    @Test
    public void fromString_List() {
        Exp exp = parser.fromString("[\"a = 12345 and b = 'John Smith' and c = true\"]");
        assertNotNull(exp);
        assertEquals("((a) = (12345)) and ((b) = ('John Smith')) and ((c) = (true))", exp.toString());
    }

    @Test
    public void fromString_List_Params_String() {
        Exp exp = parser.fromString("[\"b=$s\",\"x\"]");
        assertNotNull(exp);
        assertEquals("(b) = ('x')", exp.toString());
    }

    @Test
    public void fromString_List_Params_Multiple() {
        Exp exp = parser.fromString("[\"b=$s or b =$x or b =$s\",\"x\",\"y\"]");
        assertNotNull(exp);
        assertEquals("((b) = ('x')) or ((b) = ('y')) or ((b) = ('x'))", exp.toString());
    }

    @Test
    public void fromString_Map() {
        Exp exp = parser.fromString("{\"exp\" : \"a = 12345 and b = 'John Smith' and c = true\"}");
        assertNotNull(exp);
        assertEquals("((a) = (12345)) and ((b) = ('John Smith')) and ((c) = (true))", exp.toString());
    }

    @Test
    public void fromString_Map_Params_String() {
        Exp exp = parser.fromString("{\"exp\" : \"b=$s\", \"params\":{\"s\":\"x\"}}");
        assertNotNull(exp);
        assertEquals("(b) = ('x')", exp.toString());
    }

    @Test
    public void fromString_Map_Params_Null() {
        Exp exp = parser.fromString("{\"exp\" : \"c=$b\", \"params\":{\"b\": null}}");
        assertNotNull(exp);
        assertEquals("(c) = (null)", exp.toString());
    }
}
