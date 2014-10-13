package com.nhl.link.rest.runtime.adapter.sencha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.cayenne.exp.Expression;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E4;

public class FilterProcessorTest extends TestWithCayenneMapping {

	private Entity<E4> e4Descriptor;
	private FilterProcessor processor;

	@Before
	public void setUp() {

		IJacksonService jsonParser = new JacksonService();

		PathCache pathCache = new PathCache();
		e4Descriptor = getClientEntity(E4.class);

		this.processor = new FilterProcessor(jsonParser, pathCache);
	}

	@Test
	public void testProcess_SingleFilter() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"xyz\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar likeIgnoreCase 'xyz%'"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_MultipleFilters() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor,
				"[{\"property\":\"cVarchar\",\"value\":\"xyz\"}, {\"property\":\"cVarchar\",\"value\":\"123\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar likeIgnoreCase 'xyz%' and cVarchar likeIgnoreCase '123%'"),
				e4Descriptor.getQualifier());
	}

	@Test(expected = LinkRestException.class)
	public void testProcess_InvalidProperty() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cDummp\",\"value\":\"xyz\"}]");
	}

	@Test
	public void testProcess_ValueEscape() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"x_%\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(E4.C_VARCHAR.likeInsensitive("x\\_\\%%"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_ValueNull() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":null}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(E4.C_VARCHAR.isNull(), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_ExactMatch() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"exactMatch\":true}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar = 'xyz'"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_Equal() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"=\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar = 'xyz'"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_NotEqual() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"!=\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar != 'xyz'"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_Like() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"like\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar likeIgnoreCase 'xyz%'"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_In() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":[\"xyz\",\"abc\"],\"operator\":\"in\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar in ('xyz', 'abc')"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Greater() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":6,\"operator\":\">\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar > 6"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_Greater_Null() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":null,\"operator\":\">\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("false"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_GreaterOrEqual() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":5,\"operator\":\">=\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar >= 5"), e4Descriptor.getQualifier());
	}

	@Test
	public void testProcess_Less() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":7,\"operator\":\"<\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar < 7"), e4Descriptor.getQualifier());
	}
	
	@Test
	public void testProcess_LessOrEqual() {
		assertNull(e4Descriptor.getQualifier());
		processor.process(e4Descriptor, "[{\"property\":\"cVarchar\",\"value\":\"xyz\",\"operator\":\"<=\"}]");

		assertNotNull(e4Descriptor.getQualifier());
		assertEquals(Expression.fromString("cVarchar <= 'xyz'"), e4Descriptor.getQualifier());
	}

}
