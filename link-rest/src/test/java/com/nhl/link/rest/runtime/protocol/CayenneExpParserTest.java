package com.nhl.link.rest.runtime.protocol;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.runtime.entity.CayenneExpMerger;
import com.nhl.link.rest.runtime.entity.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.path.PathDescriptorManager;
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

// TODO: split merger test
public class CayenneExpParserTest extends TestWithCayenneMapping {

	private ResourceEntity<E4> e4Entity;
    private CayenneExpParser parser;
	private CayenneExpMerger merger;

	@Before
	public void setUp() {

		IJacksonService jsonParser = new JacksonService();
		PathDescriptorManager pathDescriptorManager = new PathDescriptorManager();

		this.parser = new CayenneExpParser(jsonParser);
		this.merger = new CayenneExpMerger(new ExpressionPostProcessor(pathDescriptorManager));
		this.e4Entity = getResourceEntity(E4.class);
	}

	@Test
	public void testProcess_Bare() {

        CayenneExp exp = parser.fromString("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true");
	    merger.merge(e4Entity, exp);

		Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_Functions() {

        CayenneExp exp = parser.fromString("length(cVarchar) > 5");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("length(cVarchar) > 5"), e);
	}

	@Test
	public void testProcess_List() {

        CayenneExp exp = parser.fromString("[\"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"]");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_List_Params_String() {

        CayenneExp exp = parser.fromString("[\"cVarchar=$s\",\"x\"]");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cVarchar='x'"), e);
	}

	@Test
	public void testProcess_List_Params_Multiple() {

        CayenneExp exp = parser.fromString( "[\"cVarchar=$s or cVarchar =$x or cVarchar =$s\",\"x\",\"y\"]");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cVarchar='x' or cVarchar='y' or cVarchar='x'"), e);
	}

	@Test
	public void testProcess_Map() {

        CayenneExp exp = parser.fromString("{\"exp\" : \"cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true\"}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_Map_Params_String() {

        CayenneExp exp = parser.fromString("{\"exp\" : \"cVarchar=$s\", \"params\":{\"s\":\"x\"}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cVarchar='x'"), e);
	}

	@Test
	public void testProcess_Map_Params_Int() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cInt=$n\", \"params\":{\"n\":453}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cInt=453"), e);
	}

	@Test
	public void testProcess_Map_Params_Float() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cDecimal=$n\", \"params\":{\"n\":4.4009}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cDecimal=4.4009"), e);
	}

	@Test
	public void testProcess_Map_Params_Float_Negative() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cDecimal=$n\", \"params\":{\"n\":-4.4009}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);

		// Cayenne parses 'fromString' as ASTNegate(ASTScalar), so to compare
		// apples to apples, let's convert it back to String.. not an ideal
		// comparison, but a good approximation
		assertEquals("cDecimal = -4.4009", e.toString());
	}

	@Test
	public void testProcess_Map_Params_Boolean_True() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": true}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cBoolean=true"), e);
	}

	@Test
	public void testProcess_Map_Params_Boolean_False() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": false}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cBoolean=false"), e);
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_Params_InvalidPath() {
        CayenneExp exp = parser.fromString(  "{\"exp\" : \"invalid/path=$b\", \"params\":{\"b\": false}}");
        merger.merge(e4Entity, exp);
	}

	@Test
	public void testProcess_Map_Params_Null() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cBoolean=$b\", \"params\":{\"b\": null}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cBoolean=null"), e);
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_Map_Params_Date_NonISO() {
        CayenneExp exp = parser.fromString( "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014:02:03\"}}");
        merger.merge(e4Entity, exp);
	}

	@Test
	public void testProcess_Map_Params_Date_Local_TZ() {

        CayenneExp exp = parser.fromString("{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014-02-03T14:06:35\"}}");
        merger.merge(e4Entity, exp);

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

        CayenneExp exp = parser.fromString("{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2014-02-03T22:06:35Z\"}}");
        merger.merge(e4Entity, exp);

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

        CayenneExp exp = parser.fromString( "{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2013-06-03\"}}");
        merger.merge(e4Entity, exp);

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

        CayenneExp exp = parser.fromString("{\"exp\" : \"cTimestamp=$d\", \"params\":{\"d\": \"2013-06-03T22:06:35Z\"}}");
        merger.merge(e4Entity, exp);

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
        CayenneExp exp = parser.fromString( "{\"exp\" : \"db:id=$i\", \"params\":{\"i\": 5}}");
        merger.merge(e4Entity, exp);
	}

	@Test
	public void testProcess_MatchByRootId() {

        CayenneExp exp = parser.fromString( "{\"exp\" : \"id=$i\", \"params\":{\"i\": 5}}");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		Expression expected = exp("db:id=$i", 5);
		assertEquals(expected, e);
	}
}
