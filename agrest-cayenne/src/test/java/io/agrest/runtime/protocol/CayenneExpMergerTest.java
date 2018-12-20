package io.agrest.runtime.protocol;

import io.agrest.AgException;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.protocol.CayenneExp;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ExpressionParserTreeConstants;
import org.apache.cayenne.exp.parser.SimpleNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CayenneExpMergerTest extends TestWithCayenneMapping {

	private ResourceEntity<E4, Expression> e4Entity;
	private CayenneExpMerger merger;

	@Before
	public void setUp() {

		PathDescriptorManager pathDescriptorManager = new PathDescriptorManager();

		this.merger = new CayenneExpMerger(new ExpressionPostProcessor(pathDescriptorManager));
		this.e4Entity = getResourceEntity(E4.class);
	}

	@Test
	public void testProcess_Bare() {

        CayenneExp exp = new CayenneExp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true");
	    merger.merge(e4Entity, exp);

		Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cInt = 12345 and cVarchar = 'John Smith' and cBoolean = true"), e);
	}

	@Test
	public void testProcess_Functions() {

        CayenneExp exp = new CayenneExp("length(cVarchar) > 5");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("length(cVarchar) > 5"), e);
	}


	@Test
	public void testProcess_List_Params_String() {

        CayenneExp exp = new CayenneExp("cVarchar=$s","x");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cVarchar='x'"), e);
	}

	@Test
	public void testProcess_List_Params_Multiple() {

        CayenneExp exp = new CayenneExp( "cVarchar=$s or cVarchar =$x or cVarchar =$s","x", "y");
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cVarchar='x' or cVarchar='y' or cVarchar='x'"), e);
	}


	@Test
	public void testProcess_Map_Params_String() {

        CayenneExp exp = new CayenneExp("cVarchar=$s", Collections.singletonMap("s", "x"));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cVarchar='x'"), e);
	}

	@Test
	public void testProcess_Map_Params_Int() {

        CayenneExp exp = new CayenneExp("cInt=$n", Collections.singletonMap("n", 453));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cInt=453"), e);
	}

	@Test
	public void testProcess_Map_Params_Float() {

        CayenneExp exp = new CayenneExp("cDecimal=$n", Collections.singletonMap("n", 4.4009));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cDecimal=4.4009"), e);
	}

	@Test
	public void testProcess_Map_Params_Float_Negative() {

        CayenneExp exp = new CayenneExp("cDecimal=$n", Collections.singletonMap("n", -4.4009));
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

        CayenneExp exp = new CayenneExp("cBoolean=$b", Collections.singletonMap("b", true));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cBoolean=true"), e);
	}

	@Test
	public void testProcess_Map_Params_Boolean_False() {

        CayenneExp exp = new CayenneExp("cBoolean=$b", Collections.singletonMap("b", false));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cBoolean=false"), e);
	}

	@Test(expected = AgException.class)
	public void testProcess_Params_InvalidPath() {
        CayenneExp exp = new CayenneExp("invalid/path=$b", Collections.singletonMap("b", false));
        merger.merge(e4Entity, exp);
	}

	@Test
	public void testProcess_Map_Params_Null() {

        CayenneExp exp = new CayenneExp("cBoolean=$b", Collections.singletonMap("b", null));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);
		assertEquals(exp("cBoolean=null"), e);
	}

	@Test(expected = AgException.class)
	public void testProcess_Map_Params_Date_NonISO() {
        CayenneExp exp = new CayenneExp("cTimestamp=$d", Collections.singletonMap("d", "2014:02:03"));
        merger.merge(e4Entity, exp);
	}

	@Test
	public void testProcess_Map_Params_Date_Local_TZ() {

        CayenneExp exp = new CayenneExp("cTimestamp=$d", Collections.singletonMap("d", "2014-02-03T14:06:35"));
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

        CayenneExp exp = new CayenneExp("cTimestamp=$d", Collections.singletonMap("d", "2014-02-03T22:06:35Z"));
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

        CayenneExp exp = new CayenneExp("cTimestamp=$d", Collections.singletonMap("d", "2013-06-03"));
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

        CayenneExp exp = new CayenneExp("cTimestamp=$d", Collections.singletonMap("d", "2013-06-03T22:06:35Z"));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		assertNotNull(e);

		GregorianCalendar cal = new GregorianCalendar(2013, 5, 3, 15, 6, 35);
		cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		Date date = cal.getTime();

		Expression expected = exp("cTimestamp=$d", date);
		assertEquals(expected, e);
	}

	@Test(expected = AgException.class)
	public void testProcess_DisallowDBPath() {
        CayenneExp exp = new CayenneExp("db:id=$i", Collections.singletonMap("i", 5));
        merger.merge(e4Entity, exp);
	}

	@Test
	public void testProcess_MatchByRootId() {

        CayenneExp exp = new CayenneExp("id=$i", Collections.singletonMap("i", 5));
        merger.merge(e4Entity, exp);

        Expression e = e4Entity.getQualifier();

		Expression expected = exp("db:id=$i", 5);
		assertEquals(expected, e);
	}
}
