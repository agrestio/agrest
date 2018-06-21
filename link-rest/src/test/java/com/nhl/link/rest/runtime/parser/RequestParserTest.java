package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestParserTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, new ExpressionPostProcessor(pathCache));
		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, expProcessor);

		parser = new RequestParser(treeProcessor, sortProcessor, expProcessor);
	}

	@Test
	public void testSelectRequest_Default() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(3, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age"));

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), params);
		assertNotNull(resourceEntity);
		assertFalse(resourceEntity.isIdIncluded());

		assertEquals(2, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));

		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), params);
		assertNotNull(resourceEntity);
		assertFalse(resourceEntity.isIdIncluded());

		assertEquals(2, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "age"));

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.NAME.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.NAME.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age", "id"));
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "name"));

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));

		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(2, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.NAME.getName()));
		assertTrue(resourceEntity.getAttributes().containsKey(E2.ADDRESS.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ResourceEntity.isIdIncluded());
		assertEquals(2, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.NAME.getName()));
		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.PHONE_NUMBER.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeBothAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("name", "e3s.name"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertNotNull(resourceEntity);
		assertFalse(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.NAME.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertFalse(e3ResourceEntity.isIdIncluded());
		assertEquals(1, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.NAME.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeBothAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s.name"));
		when(params.get("exclude")).thenReturn(Arrays.asList("name"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.ADDRESS.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertFalse(e3ResourceEntity.isIdIncluded());
		assertEquals(1, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.NAME.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeBothAttrs2() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));
		when(params.get("exclude")).thenReturn(Arrays.asList("address", "e3s.name"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.NAME.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ResourceEntity.isIdIncluded());
		assertEquals(1, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.PHONE_NUMBER.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRelationshipIds() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("id", "e3s.id"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertTrue(resourceEntity.getAttributes().isEmpty());

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ResourceEntity.isIdIncluded());
		assertTrue(e3ResourceEntity.getAttributes().isEmpty());
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_SortSimple_NoDir() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList(E2.NAME.getName()));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_ASC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList(E2.NAME.getName()));
		when(params.get("dir")).thenReturn(Collections.singletonList("ASC"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_DESC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList(E2.NAME.getName()));
		when(params.get("dir")).thenReturn(Collections.singletonList("DESC"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);
		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_SortSimple_Garbage() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList("s1"));
		when(params.get("dir")).thenReturn(Collections.singletonList("XYZ"));

		parser.parseSelect(getLrEntity(E2.class), params);
	}

	@Test
	public void testSelectRequest_Sort() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);

		assertEquals(2, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
		Ordering o1 = it.next();
		Ordering o2 = it.next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals("name", o1.getSortSpecString());
		assertEquals(SortOrder.ASCENDING, o2.getSortOrder());
		assertEquals("address", o2.getSortSpecString());
	}

	@Test
	public void testSelectRequest_Sort_Dupes() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"name\",\"direction\":\"ASC\"}]"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);

		assertEquals(1, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
		Ordering o1 = it.next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_Sort_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"p1\",\"direction\":\"DESC\"},{\"property\":\"p2\",\"direction\":\"XXX\"}]"));

		parser.parseSelect(getLrEntity(E2.class), params);
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_CayenneExp_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp"))
				.thenReturn(Collections.singletonList("{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}"));

		parser.parseSelect(getLrEntity(E2.class), params);
	}

	@Test
	public void testSelectRequest_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"name = 'John Smith'\"}"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name = 'John Smith'"), resourceEntity.getQualifier());
	}
}
