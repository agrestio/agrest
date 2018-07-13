package com.nhl.link.rest.runtime.parser;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P2;
import com.nhl.link.rest.meta.cayenne.CayenneEntityCompiler;
import com.nhl.link.rest.meta.compiler.LrEntityCompiler;
import com.nhl.link.rest.meta.compiler.PojoEntityCompiler;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestParser_WithPojoTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, new ExpressionPostProcessor(pathCache));
		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		IMapByProcessor mapByProcessor = new MapByProcessor();
		ISizeProcessor sizeProcessor = new SizeProcessor();
		IIncludeProcessor includeProcessor = new IncludeProcessor(jacksonService, sortProcessor, expProcessor, mapByProcessor, sizeProcessor);
		IExcludeProcessor excludeProcessor = new ExcludeProcessor(jacksonService);


		parser = new RequestParser(includeProcessor, excludeProcessor, sortProcessor, expProcessor, mapByProcessor, sizeProcessor);
	}

	@Override
	protected IMetadataService createMetadataService() {

		List<LrEntityCompiler> compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));
		compilers.add(new CayenneEntityCompiler(mockCayennePersister, Collections.emptyMap(), converterFactory));

		return new MetadataService(compilers, mockCayennePersister);
	}

	@Test
	public void testSelectRequest_Default() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

		ResourceEntity<P1> ce1 = parser.parseSelect(getLrEntity(P1.class), params);
		assertNotNull(ce1);
		assertTrue(ce1.isIdIncluded());
		assertEquals(1, ce1.getAttributes().size());
		assertTrue(ce1.getChildren().isEmpty());

		ResourceEntity<P2> ce2 = parser.parseSelect(getLrEntity(P2.class), params);

		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(0, ce2.getChildren().size());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("p1"));

		ResourceEntity<P2> ce2 = parser.parseSelect(getLrEntity(P2.class), params);

		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(1, ce2.getChildren().size());

		assertTrue(ce2.getChildren().keySet().contains("p1"));

	}

}
