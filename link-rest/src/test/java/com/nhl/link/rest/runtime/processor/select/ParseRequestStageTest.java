package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.protocol.CayenneExpParser;
import com.nhl.link.rest.runtime.protocol.ExcludeParser;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.protocol.ISizeParser;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.protocol.IncludeParser;
import com.nhl.link.rest.runtime.protocol.MapByParser;
import com.nhl.link.rest.runtime.protocol.SizeParser;
import com.nhl.link.rest.runtime.protocol.SortParser;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParseRequestStageTest extends TestWithCayenneMapping {

    private ParseRequestStage parseStage;

	@Before
	public void setUp() {

		IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        IMapByParser mapByParser = new MapByParser();
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, mapByParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        this.parseStage = new ParseRequestStage(expParser, sortParser, mapByParser, includeParser, excludeParser);
	}

	@Test
	public void testSelectRequest_Default() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNull(context.getRawRequest().getCayenneExp());
		assertNull(context.getRawRequest().getSortDirection());
		assertNull(context.getRawRequest().getSort());
		assertNull(context.getRawRequest().getMapBy());
		assertNull(context.getRawRequest().getLimit());
		assertNull(context.getRawRequest().getStart());
		assertTrue(context.getRawRequest().getIncludes().isEmpty());
		assertTrue(context.getRawRequest().getExcludes().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

		assertNotNull(context.getRawRequest());

		assertEquals(2, context.getRawRequest().getIncludes().size());
		assertEquals("description", context.getRawRequest().getIncludes().get(0).getValue());
		assertEquals("age", context.getRawRequest().getIncludes().get(1).getValue());
	}

	@Test
	public void testSelectRequest_IncludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

		assertNotNull(context.getRawRequest());

		assertEquals(1, context.getRawRequest().getIncludes().size());
		assertEquals(2, context.getRawRequest().getIncludes().get(0).getIncludes().size());
		assertEquals("description", context.getRawRequest().getIncludes().get(0).getIncludes().get(0).getValue());
		assertEquals("age", context.getRawRequest().getIncludes().get(0).getIncludes().get(1).getValue());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "age"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

		assertNotNull(context.getRawRequest());

		assertEquals(2, context.getRawRequest().getExcludes().size());
		assertEquals("description", context.getRawRequest().getExcludes().get(0).getPath());
		assertEquals("age", context.getRawRequest().getExcludes().get(1).getPath());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

		assertNotNull(context.getRawRequest());

		assertEquals(1, context.getRawRequest().getExcludes().size());
		assertEquals(2, context.getRawRequest().getExcludes().get(0).getExcludes().size());
		assertEquals("description", context.getRawRequest().getExcludes().get(0).getExcludes().get(0).getPath());
		assertEquals("age", context.getRawRequest().getExcludes().get(0).getExcludes().get(1).getPath());
	}

	@Test
	public void testSelectRequest_IncludeExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age", "id"));
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "name"));

		SelectContext<E1> context = prepareContext(params, E1.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());

		assertEquals(3, context.getRawRequest().getIncludes().size());
		assertEquals("description", context.getRawRequest().getIncludes().get(0).getValue());
		assertEquals("age", context.getRawRequest().getIncludes().get(1).getValue());
		assertEquals("id", context.getRawRequest().getIncludes().get(2).getValue());

		assertEquals(2, context.getRawRequest().getExcludes().size());
		assertEquals("description", context.getRawRequest().getExcludes().get(0).getPath());
		assertEquals("name", context.getRawRequest().getExcludes().get(1).getPath());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());

		assertEquals(1, context.getRawRequest().getIncludes().size());
		assertEquals("e3s", context.getRawRequest().getIncludes().get(0).getValue());
	}


	@Test
	public void testSelectRequest_SortSimple_NoDir() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList("e2"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNotNull(context.getRawRequest().getSort());

		assertEquals("e2", context.getRawRequest().getSort().getProperty());
	}

	@Test
	public void testSelectRequest_SortSimple_ASC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList("e2"));
		when(params.get("dir")).thenReturn(Collections.singletonList("ASC"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNotNull(context.getRawRequest().getSort());
		assertNotNull(context.getRawRequest().getSortDirection());

		assertEquals("e2", context.getRawRequest().getSort().getProperty());
		assertEquals(Dir.ASC, context.getRawRequest().getSortDirection());
	}

	@Test
	public void testSelectRequest_SortSimple_DESC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList("e2"));
		when(params.get("dir")).thenReturn(Collections.singletonList("DESC"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNotNull(context.getRawRequest().getSort());
		assertNotNull(context.getRawRequest().getSortDirection());

		assertEquals("e2", context.getRawRequest().getSort().getProperty());
		assertEquals(Dir.DESC, context.getRawRequest().getSortDirection());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_SortSimple_Garbage() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList("s1"));
		when(params.get("dir")).thenReturn(Collections.singletonList("XYZ"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
	}

	@Test
	public void testSelectRequest_Sort() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNotNull(context.getRawRequest().getSort());
		assertEquals(2, context.getRawRequest().getSort().getSorts().size());

		assertEquals("name", context.getRawRequest().getSort().getSorts().get(0).getProperty());
		assertEquals(Dir.DESC, context.getRawRequest().getSort().getSorts().get(0).getDirection());
		assertEquals("address", context.getRawRequest().getSort().getSorts().get(1).getProperty());
		assertEquals(Dir.ASC, context.getRawRequest().getSort().getSorts().get(1).getDirection());
	}

	@Test
	public void testSelectRequest_Sort_Dupes() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"name\",\"direction\":\"ASC\"}]"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNotNull(context.getRawRequest().getSort());
		assertEquals(2, context.getRawRequest().getSort().getSorts().size());

		assertEquals("name", context.getRawRequest().getSort().getSorts().get(0).getProperty());
		assertEquals(Dir.DESC, context.getRawRequest().getSort().getSorts().get(0).getDirection());
		assertEquals("name", context.getRawRequest().getSort().getSorts().get(1).getProperty());
		assertEquals(Dir.ASC, context.getRawRequest().getSort().getSorts().get(1).getDirection());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_Sort_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"p1\",\"direction\":\"DESC\"},{\"property\":\"p2\",\"direction\":\"XXX\"}]"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_CayenneExp_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp"))
				.thenReturn(Collections.singletonList("{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
	}

	@Test
	public void testSelectRequest_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"name = 'John Smith'\"}"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);

		assertNotNull(context.getRawRequest());
		assertNotNull(context.getRawRequest().getCayenneExp());

		assertEquals("name = 'John Smith'", context.getRawRequest().getCayenneExp().getExp());
	}
}
