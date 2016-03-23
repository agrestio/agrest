package com.nhl.link.rest.runtime.parser;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

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
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.KeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class RequestParserTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		IPathCache pathCache = new PathCache(metadataService);
		IJacksonService jacksonService = new JacksonService();
		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, pathCache);
		IKeyValueExpProcessor kvExpProcessor = new KeyValueExpProcessor();
		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, expProcessor,
				metadataService);

		parser = new RequestParser(treeProcessor, sortProcessor, expProcessor, kvExpProcessor);
	}

	@Test
	public void testSelectRequest_Default() {

		UriInfo uriInfo = mock(UriInfo.class);
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E1> resourceEntity = parser.parseSelect(getLrEntity(E1.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
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

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
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
		when(params.getFirst("sort")).thenReturn(E2.NAME.getName());

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_ASC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(E2.NAME.getName());
		when(params.getFirst("dir")).thenReturn("ASC");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_DESC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(E2.NAME.getName());
		when(params.getFirst("dir")).thenReturn("DESC");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_SortSimple_Garbage() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn("s1");
		when(params.getFirst("dir")).thenReturn("XYZ");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
	}

	@Test
	public void testSelectRequest_Sort() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(
				"[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

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
		when(params.getFirst("sort")).thenReturn(
				"[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"name\",\"direction\":\"ASC\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

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
		when(params.getFirst("sort")).thenReturn(
				"[{\"property\":\"p1\",\"direction\":\"DESC\"},{\"property\":\"p2\",\"direction\":\"XXX\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_CayenneExp_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("cayenneExp"))
				.thenReturn("{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		parser.parseSelect(getLrEntity(E2.class), uriInfo, null);
	}

	@Test
	public void testSelectRequest_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("cayenneExp")).thenReturn("{\"exp\" : \"name = 'John Smith'\"}");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name = 'John Smith'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Query() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("query")).thenReturn("Bla");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, E2.NAME.getName());
 
		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name likeIgnoreCase 'Bla%'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Query_Ignored() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("query")).thenReturn("Bla");

		// if "query" parameter exists, but no property to match against is
		// passed, it should be ignored per #60
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

		assertNull(resourceEntity.getQualifier());
	}

}
