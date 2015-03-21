package com.nhl.link.rest.runtime.parser.filter;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.cayenne.exp.Expression;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class CayenneExpProcessorTest extends TestWithCayenneMapping {

	private ResourceEntity<E4> e4Descriptor;
	private CayenneExpProcessor processor;

	@Before
	public void setUp() {

		IJacksonService jsonParser = new JacksonService();

		PathCache pathCache = new PathCache(metadataService);
		this.e4Descriptor = getResourceEntity(E4.class);
		this.processor = new CayenneExpProcessor(jsonParser, pathCache);
	}

	@Test
	public void testProcess_Bare() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_List() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[\"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_List_Params_String() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[\"cVarchar=$s\",\"x\"]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cVarchar='x'"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_List_Params_Multiple() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[\"cVarchar=$s or cVarchar =$x or cVarchar =$s\",\"x\",\"y\"]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cVarchar='x' or cVarchar='y' or cVarchar='x'"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_String() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cVarchar=$s\", \"params\":{\"s\":\"x\"}}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cVarchar='x'"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Int() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cInt=$n\", \"params\":{\"n\":453}}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cInt=453"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Float() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cDecimal=$n\", \"params\":{\"n\":4.4009}}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cDecimal=4.4009"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Float_Negative() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cDecimal=$n\", \"params\":{\"n\":-4.4009}}");

		assertNotNull(e4Descriptor.getQualifier());

		// Cayenne parses 'fromString' as ASTNegate(ASTScalar), so to compare
		// apples to apples, let's convert it back to String.. not an ideal
		// comparison, but a good approximation
		assertEquals("cDecimal = -4.4009", e4Descriptor.getQualifier().toString());
	}

	@Test
	public void testProcess_Map_Params_Boolean_True() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": true}}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cBoolean=true"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Boolean_False() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": false}}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cBoolean=false"), e4Descriptor.getQualifier());
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_Params_InvalidPath() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"invalid/path=$b\", \"params\":{\"b\": false}}");
	}

	@Test
	public void testProcess_Map_Params_Null() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": null}}");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(exp("cBoolean=null"), e4Descriptor.getQualifier());
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_Map_Params_Date_NonISO() {
		processor.process(e4Descriptor, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014:02:03\"}}");
	}

	@Test
	public void testProcess_Map_Params_Date_Local_TZ() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014-02-03T14:06:35\"}}");

		assertNotNull(e4Descriptor.getQualifier());

		GregorianCalendar cal = new GregorianCalendar(2014, 1, 3, 14, 6, 35);
		cal.setTimeZone(TimeZone.getDefault());
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Date_TZ_Zulu() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014-02-03T22:06:35Z\"}}");

		assertNotNull(e4Descriptor.getQualifier());

		GregorianCalendar cal = new GregorianCalendar(2014, 1, 3, 14, 6, 35);
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Date_TZ_Zulu_DST() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2013-06-03\"}}");

		assertNotNull(e4Descriptor.getQualifier());

		GregorianCalendar cal = new GregorianCalendar(2013, 5, 3);
		cal.setTimeZone(TimeZone.getDefault());
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Map_Params_Date_NoTime() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2013-06-03T22:06:35Z\"}}");

		assertNotNull(e4Descriptor.getQualifier());

		GregorianCalendar cal = new GregorianCalendar(2013, 5, 3, 15, 6, 35);
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e4Descriptor.getQualifier());
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_DisallowDBPath() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"db:id=$i\", \"params\":{\"i\": 5}}");
	}

	@Test
	public void testProcess_MatchByRootId() {

		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "{\"exp\" : \"id=$i\", \"params\":{\"i\": 5}}");
		Expression expected = exp("db:id=$i", 5);
		assertEquals(expected, e4Descriptor.getQualifier());
	}
}
