package io.agrest.runtime.protocol;

import io.agrest.exp.parser.ExpRoot;
import io.agrest.runtime.jackson.JacksonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExpParserTest {

    private static ExpParser parser;

	@BeforeAll
	public static void beforeAll() {
		parser = new ExpParser(new JacksonService());
	}

	@Test
	public void testProcess_Bare() {
        ExpRoot exp = (ExpRoot) parser.fromString("a = 12345 and b = 'John Smith' and c = true");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getTemplate());
	}

	@Test
	public void testProcess_Functions() {
		ExpRoot exp = (ExpRoot) parser.fromString("length(b) > 5");
		assertNotNull(exp);
		assertEquals("length(b) > 5", exp.getTemplate());
	}

	@Test
	public void testProcess_List() {
		ExpRoot exp = (ExpRoot) parser.fromString("[\"a = 12345 and b = 'John Smith' and c = true\"]");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getTemplate());
	}

	@Test
	public void testProcess_List_Params_String() {
		ExpRoot exp = (ExpRoot) parser.fromString("[\"b=$s\",\"x\"]");
		assertNotNull(exp);
		assertEquals("b=$s", exp.getTemplate());
		assertEquals(1, exp.getPositionalParams().length);
		assertEquals("\"x\"", exp.getPositionalParams()[0].toString());
	}

	@Test
	public void testProcess_List_Params_Multiple() {
		ExpRoot exp = (ExpRoot) parser.fromString( "[\"b=$s or b =$x or b =$s\",\"x\",\"y\"]");
		assertNotNull(exp);
		assertEquals("b=$s or b =$x or b =$s", exp.getTemplate());
		assertEquals(2, exp.getPositionalParams().length);
		assertEquals("\"x\"", exp.getPositionalParams()[0].toString());
		assertEquals("\"y\"", exp.getPositionalParams()[1].toString());
	}

	@Test
	public void testProcess_Map() {
		ExpRoot exp = (ExpRoot) parser.fromString("{\"exp\" : \"a = 12345 and b = 'John Smith' and c = true\"}");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getTemplate());
	}

	@Test
	public void testProcess_Map_Params_String() {
		ExpRoot exp = (ExpRoot) parser.fromString("{\"exp\" : \"b=$s\", \"params\":{\"s\":\"x\"}}");
		assertNotNull(exp);
		assertEquals("b=$s", exp.getTemplate());
		assertFalse(exp.getNamedParams().isEmpty());
		assertEquals("\"x\"", exp.getNamedParams().get("s").toString());
	}

	@Test
	public void testProcess_Map_Params_Null() {
		ExpRoot exp = (ExpRoot) parser.fromString( "{\"exp\" : \"c=$b\", \"params\":{\"b\": null}}");
		assertNotNull(exp);
		assertEquals("c=$b", exp.getTemplate());
		assertFalse(exp.getNamedParams().isEmpty());
		assertNull(exp.getNamedParams().get("b"));
	}
}
