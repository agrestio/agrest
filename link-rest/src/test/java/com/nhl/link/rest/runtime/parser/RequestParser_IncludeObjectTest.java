package com.nhl.link.rest.runtime.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.ResourceEntity;
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

public class RequestParser_IncludeObjectTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, new ExpressionPostProcessor(pathCache));
		IKeyValueExpProcessor kvExpProcessor = new KeyValueExpProcessor();

		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, expProcessor);

		parser = new RequestParser(treeProcessor, sortProcessor, expProcessor, kvExpProcessor);
	}

	@Test
	public void testToDataRequest_IncludeObject_Path() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\"}"));

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getChildren().size());
		assertTrue(resourceEntity.getChildren().containsKey(E2.E3S.getName()));
	}

	@Test
	public void testToDataRequest_IncludeObject_MapBy() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\",\"mapBy\":\"e5\"}"));

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), uriInfo, null);

		assertNotNull(resourceEntity);

		ResourceEntity<?> mapBy = resourceEntity.getChildren().get(E2.E3S.getName()).getMapBy();
		assertNotNull(mapBy);
		assertNotNull(mapBy.getChildren().get(E3.E5.getName()));
	}
}
