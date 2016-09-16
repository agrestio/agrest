package com.nhl.link.rest.runtime.adapter.sencha;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.apache.cayenne.exp.Expression;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

import java.util.Date;
import java.util.GregorianCalendar;

public class SenchaFilterProcessorTest extends TestWithCayenneMapping {

	private LrEntity<E4> e4Entity;
	private SenchaFilterProcessor processor;

	@Before
	public void setUp() {

		IJacksonService jsonParser = new JacksonService();
		PathCache pathCache = new PathCache();

		this.processor = new SenchaFilterProcessor(jsonParser, pathCache);
		this.e4Entity = getLrEntity(E4.class);
	}

	@Test
	public void testProcess_SingleFilter() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":\"xyz\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar likeIgnoreCase 'xyz%'"), e);
	}

	@Test
	public void testProcess_SingleFilter_Disabled() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"disabled\":\"true\"}]");

		assertNull(e);
	}

	@Test
	public void testProcess_MultipleFilters() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\"}, {\"property\":\"cVarchar\",\"value\":\"123\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar likeIgnoreCase 'xyz%' and cVarchar likeIgnoreCase '123%'"), e);
	}

	@Test
	public void testProcess_MultipleFilters_Disabled() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\", \"disabled\":\"true\"}, {\"property\":\"cVarchar\",\"value\":\"123\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar likeIgnoreCase '123%'"), e);
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_InvalidProperty() {
		processor.process(e4Entity, "[{\"property\":\"cDummp\",\"value\":\"xyz\"}]");
	}

	@Test
	public void testProcess_ValueEscape() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":\"x_%\"}]");

		assertNotNull(e);
		assertEquals(E4.C_VARCHAR.likeIgnoreCase("x\\_\\%%"), e);
	}

	@Test
	public void testProcess_ValueNull() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":null}]");

		assertNotNull(e);
		assertEquals(E4.C_VARCHAR.isNull(), e);
	}

	@Test
	public void testProcess_ExactMatch() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"exactMatch\":true}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar = 'xyz'"), e);
	}

	@Test
	public void testProcess_Equal() {
		Expression e = processor
				.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"=\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar = 'xyz'"), e);
	}

	@Test
	public void testProcess_NotEqual() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"!=\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar != 'xyz'"), e);
	}

	@Test
	public void testProcess_Like() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"like\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar likeIgnoreCase 'xyz%'"), e);
	}

	@Test
	public void testProcess_In() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":[\"xyz\",\"abc\"],\"operator\":\"in\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar in ('xyz', 'abc')"), e);
	}

	@Test
	public void testProcess_Greater() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":6,\"operator\":\">\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar > 6"), e);
	}

	@Test
	public void testProcess_Greater_Null() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":null,\"operator\":\">\"}]");

		assertNotNull(e);
		assertEquals(exp("false"), e);
	}

	@Test
	public void testProcess_GreaterOrEqual() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":5,\"operator\":\">=\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar >= 5"), e);
	}

	@Test
	public void testProcess_Less() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cVarchar\",\"value\":7,\"operator\":\"<\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar < 7"), e);
	}

	@Test
	public void testProcess_LessOrEqual() {
		Expression e = processor.process(e4Entity,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"<=\"}]");

		assertNotNull(e);
		assertEquals(exp("cVarchar <= 'xyz'"), e);
	}

	@Test
	public void testProcess_Date() {
		Expression e = processor.process(e4Entity, "[{\"property\":\"cDate\",\"value\":\"2016-03-26\",\"operator\":\">\"}]");

		assertNotNull(e);
		assertThat(e.getOperand(1), new IsInstanceOf(Date.class));
		assertEquals(e.getOperand(1), new GregorianCalendar(2016, 2, 26).getTime());
	}
}
