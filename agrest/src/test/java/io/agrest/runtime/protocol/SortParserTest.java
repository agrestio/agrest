package io.agrest.runtime.protocol;

import io.agrest.protocol.Sort;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SortParserTest extends TestWithCayenneMapping {

	private SortParser parser;

	@Before
	public void before() {
		JacksonService jacksonService = new JacksonService();
		this.parser = new SortParser(jacksonService);
	}

	@Test
	public void testProcess_Array() {

        Sort sort = parser.fromString("[{\"property\":\"name\"},{\"property\":\"address\"}]");

		assertNotNull(sort);
		assertEquals(2, sort.getSorts().size());
		assertNotNull(sort.getSorts().get(0));
		assertEquals("name", sort.getSorts().get(0).getProperty());
		assertNotNull(sort.getSorts().get(1));
		assertEquals("address", sort.getSorts().get(1).getProperty());
	}

	@Test
	public void testProcess_Object() {

        Sort sort = parser.fromString("{\"property\":\"name\"}");

		assertNotNull(sort);
		assertEquals("name", sort.getProperty());
	}

	@Test
	public void testProcess_Simple() {

        Sort sort = parser.fromString("name");

		assertNotNull(sort);
		assertEquals("name", sort.getProperty());
	}

}
