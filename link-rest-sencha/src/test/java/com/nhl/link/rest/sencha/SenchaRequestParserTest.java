package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.KeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SenchaRequestParserTest extends TestWithCayenneMapping {

	private SenchaRequestParser parser;

	@Before
	public void before() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ISortProcessor sortProcessor = new SenchaSortProcessor(jacksonService, pathCache);

		ICayenneExpProcessor expProcessor = new CayenneExpProcessor(jacksonService, new ExpressionPostProcessor(pathCache));
		IKeyValueExpProcessor kvExpProcessor = new KeyValueExpProcessor();
		ISenchaFilterProcessor senchaFilterProcessor = new SenchaFilterProcessor(jacksonService, pathCache,
				new ExpressionPostProcessor(pathCache));
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, expProcessor);

		parser = new SenchaRequestParser(treeProcessor, sortProcessor, expProcessor, kvExpProcessor,
				senchaFilterProcessor);
	}

	@Test
	public void testSelectRequest_Filter() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get(SenchaRequestParser.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params, null);

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Query_Filter() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("query")).thenReturn(Collections.singletonList("Bla"));
		when(params.get(SenchaRequestParser.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params, E2.NAME.getName());

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name likeIgnoreCase 'Bla%' and name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Filter_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"address = '1 Main Street'\"}"));
		when(params.get(SenchaRequestParser.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params, null);

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("address = '1 Main Street' and name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Sort_Group() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
				Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));
		when(params.get(SenchaSortProcessor.GROUP)).thenReturn(
				Collections.singletonList("[{\"property\":\"id\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));


		ResourceEntity<E2> resourceEntity = parser.parseSelect(getLrEntity(E2.class), params, null);

		assertEquals(3, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
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
