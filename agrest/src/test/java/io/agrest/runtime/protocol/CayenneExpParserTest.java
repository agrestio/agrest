package io.agrest.runtime.protocol;

import io.agrest.protocol.CayenneExp;
import io.agrest.runtime.jackson.JacksonService;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class CayenneExpParserTest {

    private static CayenneExpParser parser;

	@BeforeClass
	public static void beforeAll() {
		parser = new CayenneExpParser(new JacksonService());
	}

	@Test
	public void testProcess_Bare() {
        CayenneExp exp = parser.fromString("a = 12345 and b = 'John Smith' and c = true");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getExp());
	}

	@Test
	public void testProcess_Functions() {
        CayenneExp exp = parser.fromString("length(b) > 5");
		assertNotNull(exp);
		assertEquals("length(b) > 5", exp.getExp());
	}

	@Test
	public void testProcess_List() {
        CayenneExp exp = parser.fromString("[\"a = 12345 and b = 'John Smith' and c = true\"]");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getExp());
	}

	@Test
	public void testProcess_List_Params_String() {
        CayenneExp exp = parser.fromString("[\"b=$s\",\"x\"]");
		assertNotNull(exp);
		assertEquals("b=$s", exp.getExp());
		assertFalse(exp.getInPositionParams().isEmpty());
		assertEquals("\"x\"", exp.getInPositionParams().get(0).toString());
	}

	@Test
	public void testProcess_List_Params_Multiple() {
        CayenneExp exp = parser.fromString( "[\"b=$s or b =$x or b =$s\",\"x\",\"y\"]");
		assertNotNull(exp);
		assertEquals("b=$s or b =$x or b =$s", exp.getExp());
		assertEquals(2, exp.getInPositionParams().size());
		assertEquals("\"x\"", exp.getInPositionParams().get(0).toString());
		assertEquals("\"y\"", exp.getInPositionParams().get(1).toString());
	}

	@Test
	public void testProcess_Map() {
        CayenneExp exp = parser.fromString("{\"exp\" : \"a = 12345 and b = 'John Smith' and c = true\"}");
		assertNotNull(exp);
		assertEquals("a = 12345 and b = 'John Smith' and c = true", exp.getExp());
	}

	@Test
	public void testProcess_Map_Params_String() {
        CayenneExp exp = parser.fromString("{\"exp\" : \"b=$s\", \"params\":{\"s\":\"x\"}}");
		assertNotNull(exp);
		assertEquals("b=$s", exp.getExp());
		assertFalse(exp.getParams().isEmpty());
		assertEquals("\"x\"", exp.getParams().get("s").toString());
	}

	@Test
	public void testProcess_Map_Params_Null() {
        CayenneExp exp = parser.fromString( "{\"exp\" : \"c=$b\", \"params\":{\"b\": null}}");
		assertNotNull(exp);
		assertEquals("c=$b", exp.getExp());
		assertFalse(exp.getParams().isEmpty());
		assertNull(exp.getParams().get("b"));
	}
}
