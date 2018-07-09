package com.nhl.link.rest.runtime.parser.filter;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.apache.cayenne.exp.Expression;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CayenneExpProcessorTest extends TestWithCayenneMapping {

	private ResourceEntity<E4> e4Entity;
	private CayenneExpProcessor processor;

	@Before
	public void setUp() {

		IJacksonService jsonParser = new JacksonService();
		PathCache pathCache = new PathCache();
		this.processor = new CayenneExpProcessor(jsonParser, new ExpressionPostProcessor(pathCache));
		this.e4Entity = getResourceEntity(E4.class);
	}

	@Test
	public void testProcess_Bare() {

		processor.process(e4Entity, "cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_Functions() {

		processor.process(e4Entity, "length(cVarchar) > 5");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("length(cVarchar) > 5"), e);
	}

	@Test
	public void testProcess_List() {

		processor.process(e4Entity, "[\"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"]");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_List_Params_String() {

		processor.process(e4Entity, "[\"cVarchar=$s\",\"x\"]");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cVarchar='x'"), e);
	}

	@Test
	public void testProcess_List_Params_Multiple() {

		processor.process(e4Entity, "[\"cVarchar=$s or cVarchar =$x or cVarchar =$s\",\"x\",\"y\"]");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cVarchar='x' or cVarchar='y' or cVarchar='x'"), e);
	}

	@Test
	public void testProcess_Map() {

		processor.process(e4Entity,
				"{\"exp\" : \"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_Map_Params_String() {

		processor.process(e4Entity, "{\"exp\" : \"cVarchar=$s\", \"params\":{\"s\":\"x\"}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cVarchar='x'"), e);
	}

	@Test
	public void testProcess_Map_Params_Int() {

		processor.process(e4Entity, "{\"exp\" : \"cInt=$n\", \"params\":{\"n\":453}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cInt=453"), e);
	}

	@Test
	public void testProcess_Map_Params_Float() {

		processor.process(e4Entity, "{\"exp\" : \"cDecimal=$n\", \"params\":{\"n\":4.4009}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cDecimal=4.4009"), e);
	}

	@Test
	public void testProcess_Map_Params_Float_Negative() {

		processor.process(e4Entity, "{\"exp\" : \"cDecimal=$n\", \"params\":{\"n\":-4.4009}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);

		// Cayenne parses 'fromString' as ASTNegate(ASTScalar), so to compare
		// apples to apples, let's convert it back to String.. not an ideal
		// comparison, but a good approximation
		assertEquals("cDecimal = -4.4009", e.toString());
	}

	@Test
	public void testProcess_Map_Params_Boolean_True() {

		processor.process(e4Entity, "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": true}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cBoolean=true"), e);
	}

	@Test
	public void testProcess_Map_Params_Boolean_False() {

		processor.process(e4Entity, "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": false}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cBoolean=false"), e);
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_Params_InvalidPath() {

		processor.process(e4Entity, "{\"exp\" : \"invalid/path=$b\", \"params\":{\"b\": false}}");
	}

	@Test
	public void testProcess_Map_Params_Null() {

		processor.process(e4Entity, "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": null}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);
		assertEquals(exp("cBoolean=null"), e);
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_Map_Params_Date_NonISO() {

		processor.process(e4Entity, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014:02:03\"}}");
	}

	@Test
	public void testProcess_Map_Params_Date_Local_TZ() {

		processor.process(e4Entity,
				"{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014-02-03T14:06:35\"}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);

		GregorianCalendar cal = new GregorianCalendar(2014, 1, 3, 14, 6, 35);
		cal.setTimeZone(TimeZone.getDefault());
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e);
	}

	@Test
	public void testProcess_Map_Params_Date_TZ_Zulu() {

		processor.process(e4Entity,
				"{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014-02-03T22:06:35Z\"}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);

		GregorianCalendar cal = new GregorianCalendar(2014, 1, 3, 14, 6, 35);
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e);
	}

	@Test
	public void testProcess_Map_Params_Date_TZ_Zulu_DST() {

		processor.process(e4Entity, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2013-06-03\"}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);

		GregorianCalendar cal = new GregorianCalendar(2013, 5, 3);
		cal.setTimeZone(TimeZone.getDefault());
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e);
	}

	@Test
	public void testProcess_Map_Params_Date_NoTime() {

		processor.process(e4Entity,
				"{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2013-06-03T22:06:35Z\"}}");

		Expression e = e4Entity.getQualifier();
		assertNotNull(e);

		GregorianCalendar cal = new GregorianCalendar(2013, 5, 3, 15, 6, 35);
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e);
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_DisallowDBPath() {

		processor.process(e4Entity, "{\"exp\" : \"db:id=$i\", \"params\":{\"i\": 5}}");
	}

	@Test
	public void testProcess_MatchByRootId() {

		processor.process(e4Entity, "{\"exp\" : \"id=$i\", \"params\":{\"i\": 5}}");

		Expression e = e4Entity.getQualifier();
		Expression expected = exp("db:id=$i", 5);
		assertEquals(expected, e);
	}
}
