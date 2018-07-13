package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByProcessor;
import com.nhl.link.rest.runtime.parser.mapBy.MapByProcessor;
import com.nhl.link.rest.runtime.parser.size.ISizeProcessor;
import com.nhl.link.rest.runtime.parser.size.SizeProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeProcessor;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestParser_IncludeObjectTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, new ExpressionPostProcessor(pathCache));
		IMapByProcessor mapByProcessor = new MapByProcessor();
		ISizeProcessor sizeProcessor = new SizeProcessor();

		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		IIncludeProcessor includeProcessor = new IncludeProcessor(jacksonService, sortProcessor, expProcessor, mapByProcessor, sizeProcessor);
		IExcludeProcessor excludeProcessor = new ExcludeProcessor(jacksonService);

		parser = new RequestParser(includeProcessor, excludeProcessor, sortProcessor, expProcessor, mapByProcessor, sizeProcessor);
	}

	@Test
	public void testToDataRequest_IncludeObject_Path() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\"}"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);

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

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params);

		assertNotNull(resourceEntity);

		ResourceEntity<?> mapBy = resourceEntity.getChildren().get(E2.E3S.getName()).getMapBy();
		assertNotNull(mapBy);
		assertNotNull(mapBy.getChildren().get(E3.E5.getName()));
	}
}
