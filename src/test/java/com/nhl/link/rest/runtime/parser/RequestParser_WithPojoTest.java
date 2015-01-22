package com.nhl.link.rest.runtime.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P2;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.filter.FilterProcessor;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.update.UpdateFilter;

public class RequestParser_WithPojoTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		IJsonValueConverterFactory converterFactory = new DefaultJsonValueConverterFactory();

		IPathCache pathCache = new PathCache(metadataService);
		IJacksonService jacksonService = new JacksonService();
		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		IFilterProcessor filterProcessor = new FilterProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, filterProcessor,
				metadataService);

		parser = new RequestParser(Collections.<UpdateFilter> emptyList(), metadataService, jacksonService,
				new RelationshipMapper(), treeProcessor, sortProcessor, filterProcessor, converterFactory);
	}

	@Override
	protected IMetadataService createMetadataService() {
		List<LrEntity<?>> pojos = Arrays.asList(LrEntityBuilder.build(P1.class), LrEntityBuilder.build(P2.class));
		return new MetadataService(pojos, Collections.<String, LrEntityOverlay<?>> emptyMap(), mockCayennePersister);
	}

	@Test
	public void testSelectRequest_Default() {

		UriInfo urlInfo = mock(UriInfo.class);
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<P1> req1 = DataResponse.forType(P1.class);
		parser.parseSelect(req1, urlInfo, null);

		assertNotNull(req1);
		ResourceEntity<P1> ce1 = req1.getEntity();
		assertNotNull(ce1);
		assertTrue(ce1.isIdIncluded());
		assertEquals(1, ce1.getAttributes().size());
		assertTrue(ce1.getChildren().isEmpty());

		DataResponse<P2> req2 = DataResponse.forType(P2.class);
		parser.parseSelect(req2, urlInfo, null);

		assertNotNull(req2);
		ResourceEntity<P2> ce2 = req2.getEntity();
		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(0, ce2.getChildren().size());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		UriInfo urlInfo = mock(UriInfo.class);
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("p1"));

		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<P2> req2 = DataResponse.forType(P2.class);
		parser.parseSelect(req2, urlInfo, null);

		assertNotNull(req2);
		ResourceEntity<P2> ce2 = req2.getEntity();
		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(1, ce2.getChildren().size());

		assertTrue(ce2.getChildren().keySet().contains("p1"));

	}

}
