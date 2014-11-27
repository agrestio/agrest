package com.nhl.link.rest.runtime.parser;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.filter.FilterProcessor;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E1;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.update.UpdateFilter;

public class RequestParserTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		ObjectContext sharedContext = runtime.newContext();
		ICayennePersister cayenneService = mock(ICayennePersister.class);
		when(cayenneService.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
		when(cayenneService.sharedContext()).thenReturn(sharedContext);
		when(cayenneService.newContext()).thenReturn(runtime.newContext());
		IMetadataService metadataService = new MetadataService(Collections.<DataMap> emptyList(), cayenneService);

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		IFilterProcessor filterProcessor = new FilterProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, filterProcessor, metadataService);

		parser = new RequestParser(Collections.<UpdateFilter> emptyList(), metadataService, jacksonService,
				new RelationshipMapper(), treeProcessor, sortProcessor, filterProcessor);
	}

	@Test
	public void testSelectRequest_Default() {

		UriInfo urlInfo = mock(UriInfo.class);
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E1> dataRequest = DataResponse.forType(E1.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E1> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());
		assertEquals(3, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E1> dataRequest = DataResponse.forType(E1.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E1> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertFalse(clientEntity.isIdIncluded());

		assertEquals(2, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E1.DESCRIPTION.getName()));
		assertTrue(clientEntity.getAttributes().contains(E1.AGE.getName()));

		assertTrue(clientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E1> dataRequest = DataResponse.forType(E1.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E1> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertFalse(clientEntity.isIdIncluded());

		assertEquals(2, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E1.DESCRIPTION.getName()));
		assertTrue(clientEntity.getAttributes().contains(E1.AGE.getName()));

		assertTrue(clientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "age"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E1> dataRequest = DataResponse.forType(E1.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E1> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());

		assertEquals(1, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E1.NAME.getName()));
		assertTrue(clientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E1> dataRequest = DataResponse.forType(E1.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E1> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());

		assertEquals(1, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E1.NAME.getName()));
		assertTrue(clientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age", "id"));
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "name"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E1> dataRequest = DataResponse.forType(E1.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E1> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());
		assertEquals(1, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E1.AGE.getName()));

		assertTrue(clientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());
		assertEquals(2, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E2.NAME.getName()));
		assertTrue(clientEntity.getAttributes().contains(E2.ADDRESS.getName()));

		assertEquals(1, clientEntity.getChildren().size());
		assertEquals(1, clientEntity.getChildren().entrySet().size());
		assertTrue(clientEntity.getChildren().keySet().contains(E2.E3S.getName()));

		Entity<?> e3ClientEntity = clientEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ClientEntity.isIdIncluded());
		assertEquals(2, e3ClientEntity.getAttributes().size());
		assertTrue(e3ClientEntity.getAttributes().contains(E3.NAME.getName()));
		assertTrue(e3ClientEntity.getAttributes().contains(E3.PHONE_NUMBER.getName()));
		assertTrue(e3ClientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeBothAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("name", "e3s.name"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertFalse(clientEntity.isIdIncluded());
		assertEquals(1, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E2.NAME.getName()));

		assertEquals(1, clientEntity.getChildren().size());
		assertEquals(1, clientEntity.getChildren().entrySet().size());
		assertTrue(clientEntity.getChildren().keySet().contains(E2.E3S.getName()));

		Entity<?> e3ClientEntity = clientEntity.getChildren().get(E2.E3S.getName());
		assertFalse(e3ClientEntity.isIdIncluded());
		assertEquals(1, e3ClientEntity.getAttributes().size());

		assertTrue(e3ClientEntity.getAttributes().contains(E3.NAME.getName()));
		assertTrue(e3ClientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeBothAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s.name"));
		when(params.get("exclude")).thenReturn(Arrays.asList("name"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());
		assertEquals(1, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E2.ADDRESS.getName()));

		assertEquals(1, clientEntity.getChildren().size());
		assertEquals(1, clientEntity.getChildren().entrySet().size());
		assertTrue(clientEntity.getChildren().keySet().contains(E2.E3S.getName()));

		Entity<?> e3ClientEntity = clientEntity.getChildren().get(E2.E3S.getName());
		assertFalse(e3ClientEntity.isIdIncluded());
		assertEquals(1, e3ClientEntity.getAttributes().size());

		assertTrue(e3ClientEntity.getAttributes().contains(E3.NAME.getName()));
		assertTrue(e3ClientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeBothAttrs2() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));
		when(params.get("exclude")).thenReturn(Arrays.asList("address", "e3s.name"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());
		assertEquals(1, clientEntity.getAttributes().size());
		assertTrue(clientEntity.getAttributes().contains(E2.NAME.getName()));

		assertEquals(1, clientEntity.getChildren().size());
		assertEquals(1, clientEntity.getChildren().entrySet().size());
		assertTrue(clientEntity.getChildren().keySet().contains(E2.E3S.getName()));

		Entity<?> e3ClientEntity = clientEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ClientEntity.isIdIncluded());
		assertEquals(1, e3ClientEntity.getAttributes().size());

		assertTrue(e3ClientEntity.getAttributes().contains(E3.PHONE_NUMBER.getName()));
		assertTrue(e3ClientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRelationshipIds() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("id", "e3s.id"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());
		assertTrue(clientEntity.getAttributes().isEmpty());

		assertEquals(1, clientEntity.getChildren().size());
		assertEquals(1, clientEntity.getChildren().entrySet().size());
		assertTrue(clientEntity.getChildren().keySet().contains(E2.E3S.getName()));

		Entity<?> e3ClientEntity = clientEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ClientEntity.isIdIncluded());
		assertTrue(e3ClientEntity.getAttributes().isEmpty());
		assertTrue(e3ClientEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_SortSimple_NoDir() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(E2.NAME.getName());

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);

		assertEquals(1, dataRequest.getEntity().getOrderings().size());
		Ordering o1 = dataRequest.getEntity().getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_ASC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(E2.NAME.getName());
		when(params.getFirst("dir")).thenReturn("ASC");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);

		assertEquals(1, dataRequest.getEntity().getOrderings().size());
		Ordering o1 = dataRequest.getEntity().getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_DESC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(E2.NAME.getName());
		when(params.getFirst("dir")).thenReturn("DESC");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);

		assertEquals(1, dataRequest.getEntity().getOrderings().size());
		Ordering o1 = dataRequest.getEntity().getOrderings().iterator().next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_SortSimple_Garbage() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn("s1");
		when(params.getFirst("dir")).thenReturn("XYZ");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);
	}

	@Test
	public void testSelectRequest_Sort() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(
				"[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);

		assertEquals(2, dataRequest.getEntity().getOrderings().size());
		Iterator<Ordering> it = dataRequest.getEntity().getOrderings().iterator();
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

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);

		assertEquals(1, dataRequest.getEntity().getOrderings().size());
		Iterator<Ordering> it = dataRequest.getEntity().getOrderings().iterator();
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

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_CayenneExp_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("cayenneExp")).thenReturn(
				"{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);
	}

	@Test
	public void testSelectRequest_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("cayenneExp")).thenReturn("{\"exp\" : \"name = 'John Smith'\"}");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest.getEntity().getQualifier());
		assertEquals(exp("name = 'John Smith'"), dataRequest.getEntity().getQualifier());
	}

	@Test
	public void testSelectRequest_Query() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("query")).thenReturn("Bla");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, E2.NAME.getName());
		assertNotNull(dataRequest.getEntity().getQualifier());
		assertEquals(exp("name likeIgnoreCase 'Bla%'"), dataRequest.getEntity().getQualifier());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_Query_Unsupported() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("query")).thenReturn("Bla");

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);
	}

}
