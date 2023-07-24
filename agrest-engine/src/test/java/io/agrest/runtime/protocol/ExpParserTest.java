package io.agrest.runtime.protocol;

import io.agrest.protocol.Exp;
import io.agrest.runtime.jackson.JacksonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExpParserTest {

    private static ExpParser parser;

    @BeforeAll
    public static void beforeAll() {
        parser = new ExpParser(JacksonService.create());
    }

    @Test
    public void testProcess_Bare() {
        Exp exp = parser.fromString("a = 12345 and b = 'John Smith' and c = true");
        assertNotNull(exp);
        assertEquals("((a) = (12345)) and ((b) = ('John Smith')) and ((c) = (true))", exp.toString());
    }

    @Test
    public void testProcess_Functions() {
        Exp exp = parser.fromString("length(b) > 5");
        assertNotNull(exp);
        assertEquals("(length(b)) > (5)", exp.toString());
    }

    @Test
    public void testProcess_List() {
        Exp exp = parser.fromString("[\"a = 12345 and b = 'John Smith' and c = true\"]");
        assertNotNull(exp);
        assertEquals("((a) = (12345)) and ((b) = ('John Smith')) and ((c) = (true))", exp.toString());
    }

    @Test
    public void testProcess_List_Params_String() {
        Exp exp = parser.fromString("[\"b=$s\",\"x\"]");
        assertNotNull(exp);
        assertEquals("(b) = ('x')", exp.toString());
    }

    @Test
    public void testProcess_List_Params_Multiple() {
        Exp exp = parser.fromString("[\"b=$s or b =$x or b =$s\",\"x\",\"y\"]");
        assertNotNull(exp);
        assertEquals("((b) = ('x')) or ((b) = ('y')) or ((b) = ('x'))", exp.toString());
    }

    @Test
    public void testProcess_Map() {
        Exp exp = parser.fromString("{\"exp\" : \"a = 12345 and b = 'John Smith' and c = true\"}");
        assertNotNull(exp);
        assertEquals("((a) = (12345)) and ((b) = ('John Smith')) and ((c) = (true))", exp.toString());
    }

    @Test
    public void testProcess_Map_Params_String() {
        Exp exp = parser.fromString("{\"exp\" : \"b=$s\", \"params\":{\"s\":\"x\"}}");
        assertNotNull(exp);
        assertEquals("(b) = ('x')", exp.toString());
    }

    @Test
    public void testProcess_Map_Params_Null() {
        Exp exp = parser.fromString("{\"exp\" : \"c=$b\", \"params\":{\"b\": null}}");
        assertNotNull(exp);
        assertEquals("(c) = (null)", exp.toString());
    }
}
