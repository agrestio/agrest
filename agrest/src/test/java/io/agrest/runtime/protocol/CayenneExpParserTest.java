package io.agrest.runtime.protocol;

import io.agrest.protocol.CayenneExp;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CayenneExpParserTest extends TestWithCayenneMapping {

    private CayenneExpParser parser;

	@Before
	public void setUp() {

		IJacksonService jsonParser = new JacksonService();

		this.parser = new CayenneExpParser(jsonParser);
	}

	@Test
	public void testProcess_Bare() {

        CayenneExp exp = parser.fromString("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true");

		assertNotNull(exp);
		assertEquals("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true", exp.getExp());
	}

	@Test
	public void testProcess_Functions() {

        CayenneExp exp = parser.fromString("length(cVarchar) > 5");

		assertNotNull(exp);
		assertEquals("length(cVarchar) > 5", exp.getExp());
	}

	@Test
	public void testProcess_List() {

        CayenneExp exp = parser.fromString("[\"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"]");

		assertNotNull(exp);
		assertEquals("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true", exp.getExp());
	}

	@Test
	public void testProcess_List_Params_String() {

        CayenneExp exp = parser.fromString("[\"cVarchar=$s\",\"x\"]");

		assertNotNull(exp);
		assertEquals("cVarchar=$s", exp.getExp());
		assertFalse(exp.getInPositionParams().isEmpty());
		assertEquals("\"x\"", exp.getInPositionParams().get(0).toString());
	}

	@Test
	public void testProcess_List_Params_Multiple() {

        CayenneExp exp = parser.fromString( "[\"cVarchar=$s or cVarchar =$x or cVarchar =$s\",\"x\",\"y\"]");

		assertNotNull(exp);
		assertEquals("cVarchar=$s or cVarchar =$x or cVarchar =$s", exp.getExp());
		assertEquals(2, exp.getInPositionParams().size());
		assertEquals("\"x\"", exp.getInPositionParams().get(0).toString());
		assertEquals("\"y\"", exp.getInPositionParams().get(1).toString());
	}

	@Test
	public void testProcess_Map() {

        CayenneExp exp = parser.fromString("{\"exp\" : \"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"}");

		assertNotNull(exp);
		assertEquals("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true", exp.getExp());
	}

	@Test
	public void testProcess_Map_Params_String() {

        CayenneExp exp = parser.fromString("{\"exp\" : \"cVarchar=$s\", \"params\":{\"s\":\"x\"}}");

		assertNotNull(exp);
		assertEquals("cVarchar=$s", exp.getExp());
		assertFalse(exp.getParams().isEmpty());
		assertEquals("\"x\"", exp.getParams().get("s").toString());
	}

	@Test
	public void testProcess_Map_Params_Null() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": null}}");

		assertNotNull(exp);
		assertEquals("cBoolean=$b", exp.getExp());
		assertFalse(exp.getParams().isEmpty());
		assertNull(exp.getParams().get("b"));
	}
}
