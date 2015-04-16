package com.nhl.link.rest.runtime.adapter.sencha;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.KeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.update.UpdateFilter;

public class SenchaRequestParserTest extends TestWithCayenneMapping {

	private SenchaRequestParser parser;

	@Before
	public void before() {

		IJsonValueConverterFactory converterFactory = new DefaultJsonValueConverterFactory();

		IPathCache pathCache = new PathCache(metadataService);
		IJacksonService jacksonService = new JacksonService();
		ISortProcessor sortProcessor = new SenchaSortProcessor(jacksonService, pathCache);

		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, pathCache);
		IKeyValueExpProcessor kvExpProcessor = new KeyValueExpProcessor();
		ISenchaFilterProcessor senchaFilterProcessor = new SenchaFilterProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, expProcessor,
				metadataService);

		parser = new SenchaRequestParser(Collections.<UpdateFilter> emptyList(), metadataService, jacksonService,
				new RelationshipMapper(), treeProcessor, sortProcessor, converterFactory, expProcessor, kvExpProcessor,
				senchaFilterProcessor);
	}

	@Test
	public void testSelectRequest_Filter() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst(SenchaRequestParser.FILTER)).thenReturn("[{\"property\":\"name\",\"value\":\"xyz\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		SelectContext<E2> context = new SelectContext<>(E2.class);
		context.setResponse(DataResponse.forType(E2.class));
		context.setUriInfo(uriInfo);
		parser.parseSelect(context);

		assertNotNull(context.getResponse().getEntity().getQualifier());
		assertEquals(exp("name likeIgnoreCase 'xyz%'"), context.getResponse().getEntity().getQualifier());
	}

	@Test
	public void testSelectRequest_Query_Filter() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("query")).thenReturn("Bla");
		when(params.getFirst(SenchaRequestParser.FILTER)).thenReturn("[{\"property\":\"name\",\"value\":\"xyz\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		SelectContext<E2> context = new SelectContext<>(E2.class);
		context.setResponse(DataResponse.forType(E2.class));
		context.setUriInfo(uriInfo);
		context.setAutocompleteProperty(E2.NAME.getName());
		parser.parseSelect(context);

		assertNotNull(context.getResponse().getEntity().getQualifier());
		assertEquals(exp("name likeIgnoreCase 'Bla%' and name likeIgnoreCase 'xyz%'"), context.getResponse()
				.getEntity().getQualifier());
	}

	@Test
	public void testSelectRequest_Filter_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("cayenneExp")).thenReturn("{\"exp\" : \"address = '1 Main Street'\"}");
		when(params.getFirst(SenchaRequestParser.FILTER)).thenReturn("[{\"property\":\"name\",\"value\":\"xyz\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		SelectContext<E2> context = new SelectContext<>(E2.class);
		context.setResponse(DataResponse.forType(E2.class));
		context.setUriInfo(uriInfo);
		parser.parseSelect(context);

		assertNotNull(context.getResponse().getEntity().getQualifier());
		assertEquals(exp("address = '1 Main Street' and name likeIgnoreCase 'xyz%'"), context.getResponse().getEntity()
				.getQualifier());
	}

	@Test
	public void testSelectRequest_Sort_Group() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.getFirst("sort")).thenReturn(
				"[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]");
		when(params.getFirst(SenchaSortProcessor.GROUP)).thenReturn(
				"[{\"property\":\"id\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]");

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(params);

		SelectContext<E2> context = new SelectContext<>(E2.class);
		context.setResponse(DataResponse.forType(E2.class));
		context.setUriInfo(uriInfo);
		parser.parseSelect(context);

		assertEquals(3, context.getResponse().getEntity().getOrderings().size());
		Iterator<Ordering> it = context.getResponse().getEntity().getOrderings().iterator();
		Ordering o1 = it.next();
		Ordering o2 = it.next();
		Ordering o3 = it.next();

		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals("db:id", o1.getSortSpecString());
		assertEquals(SortOrder.ASCENDING, o2.getSortOrder());
		assertEquals("address", o2.getSortSpecString());
		assertEquals(SortOrder.DESCENDING, o3.getSortOrder());
		assertEquals("name", o3.getSortSpecString());
	}
}
