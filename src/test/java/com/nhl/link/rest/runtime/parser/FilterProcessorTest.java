package com.nhl.link.rest.runtime.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.cayenne.exp.Expression;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.parser.FilterProcessor;
import com.nhl.link.rest.runtime.parser.PathCache;
import com.nhl.link.rest.runtime.parser.RequestJsonParser;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E4;

public class FilterProcessorTest extends TestWithCayenneMapping {

	private Entity<E4> e4Descriptor;
	private FilterProcessor processor;

	@Before
	public void setUp() {

		JsonFactory jsonFactory = new ObjectMapper().getJsonFactory();
		RequestJsonParser jsonParser = new RequestJsonParser(jsonFactory);

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

}
