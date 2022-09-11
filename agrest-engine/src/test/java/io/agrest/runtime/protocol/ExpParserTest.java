package io.agrest.runtime.protocol;

import io.agrest.exp.NamedParamsExp;
import io.agrest.exp.PositionalParamsExp;
import io.agrest.exp.SimpleExp;
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
	public void testProcess_Bare() {
        SimpleExp exp = (SimpleExp) parser.fromString("a = 12345 and b = 'John Smith' and c = true");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getTemplate());
	}

	@Test
	public void testProcess_Functions() {
		SimpleExp exp = (SimpleExp) parser.fromString("length(b) > 5");
		assertNotNull(exp);
		assertEquals("length(b) > 5", exp.getTemplate());
	}

	@Test
	public void testProcess_List() {
		SimpleExp exp = (SimpleExp) parser.fromString("[\"a = 12345 and b = 'John Smith' and c = true\"]");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getTemplate());
	}

	@Test
	public void testProcess_List_Params_String() {
		PositionalParamsExp exp = (PositionalParamsExp) parser.fromString("[\"b=$s\",\"x\"]");
		assertNotNull(exp);
		assertEquals("b=$s", exp.getTemplate());
		assertEquals(1, exp.getParams().length);
		assertEquals("\"x\"", exp.getParams()[0].toString());
	}

	@Test
	public void testProcess_List_Params_Multiple() {
		PositionalParamsExp exp = (PositionalParamsExp) parser.fromString( "[\"b=$s or b =$x or b =$s\",\"x\",\"y\"]");
		assertNotNull(exp);
		assertEquals("b=$s or b =$x or b =$s", exp.getTemplate());
		assertEquals(2, exp.getParams().length);
		assertEquals("\"x\"", exp.getParams()[0].toString());
		assertEquals("\"y\"", exp.getParams()[1].toString());
	}

	@Test
	public void testProcess_Map() {
		SimpleExp exp = (SimpleExp) parser.fromString("{\"exp\" : \"a = 12345 and b = 'John Smith' and c = true\"}");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getTemplate());
	}

	@Test
	public void testProcess_Map_Params_String() {
		NamedParamsExp exp = (NamedParamsExp) parser.fromString("{\"exp\" : \"b=$s\", \"params\":{\"s\":\"x\"}}");
		assertNotNull(exp);
		assertEquals("b=$s", exp.getTemplate());
		assertFalse(exp.getParams().isEmpty());
		assertEquals("\"x\"", exp.getParams().get("s").toString());
	}

	@Test
	public void testProcess_Map_Params_Null() {
		NamedParamsExp exp = (NamedParamsExp) parser.fromString( "{\"exp\" : \"c=$b\", \"params\":{\"b\": null}}");
		assertNotNull(exp);
		assertEquals("c=$b", exp.getTemplate());
		assertFalse(exp.getParams().isEmpty());
		assertNull(exp.getParams().get("b"));
	}
}
