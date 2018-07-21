package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpParser;
import com.nhl.link.rest.runtime.parser.filter.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.mapBy.MapByConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.MapByParser;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeParser;
import com.nhl.link.rest.runtime.parser.size.SizeConstructor;
import com.nhl.link.rest.runtime.parser.size.SizeParser;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.sort.SortConstructor;
import com.nhl.link.rest.runtime.parser.sort.SortParser;
import com.nhl.link.rest.runtime.parser.tree.ExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.ExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.runtime.parser.tree.IncludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IncludeParser;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SenchaRequestParserTest extends TestWithCayenneMapping {

    private SenchaParseRequestStage parseStage;
    private SenchaConstructResourceEntityStage constructEntityStage;

	@Before
	public void before() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();

		// prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
		ISortParser sortParser = new SortParser(jacksonService);
        IMapByParser mapByParser = new MapByParser();
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, mapByParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        this.parseStage = new SenchaParseRequestStage(expParser, sortParser, mapByParser, includeParser, excludeParser);

        // prepare entity constructor stage
        ICayenneExpConstructor expConstructor = new CayenneExpConstructor(new ExpressionPostProcessor(pathCache));
        ISortConstructor sortConstructor = new SortConstructor(pathCache);
        IMapByConstructor mapByConstructor = new MapByConstructor();
        ISizeConstructor sizeConstructor = new SizeConstructor();
        IIncludeConstructor includeConstructor = new IncludeConstructor(expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeConstructor excludeConstructor = new ExcludeConstructor();

		ISenchaFilterProcessor senchaFilterProcessor = new SenchaFilterProcessor(jacksonService, pathCache,
				new ExpressionPostProcessor(pathCache));

        this.constructEntityStage
                = new SenchaConstructResourceEntityStage(
                        createMetadataService(),
                        expConstructor ,
                        sortConstructor,
                        mapByConstructor,
                        sizeConstructor,
                        includeConstructor,
                        excludeConstructor,
                        sortParser,
                        senchaFilterProcessor);
	}

	protected SelectContext<E2> prepareContext(MultivaluedMap<String, String> params) {
        SelectContext<E2> context = new SelectContext<>(E2.class);
        context.setUriInfo(new UriInfo() {
            @Override public String getPath() { return null; }
            @Override public String getPath(boolean decode) { return null; }
            @Override public List<PathSegment> getPathSegments() { return null; }
            @Override public List<PathSegment> getPathSegments(boolean decode) { return null; }
            @Override public URI getRequestUri() { return null; }
            @Override public UriBuilder getRequestUriBuilder() { return null; }
            @Override public URI getAbsolutePath() { return null; }
            @Override public UriBuilder getAbsolutePathBuilder() { return null; }
            @Override public URI getBaseUri() { return null; }
            @Override public UriBuilder getBaseUriBuilder() { return null; }
            @Override public MultivaluedMap<String, String> getPathParameters() { return null; }
            @Override public MultivaluedMap<String, String> getPathParameters(boolean decode) { return null; }
            @Override public MultivaluedMap<String, String> getQueryParameters() { return params; }
            @Override public MultivaluedMap<String, String> getQueryParameters(boolean decode) { return null; }
            @Override public List<String> getMatchedURIs() { return null; }
            @Override public List<String> getMatchedURIs(boolean decode) { return null; }
            @Override public List<Object> getMatchedResources() { return null; }
            @Override public URI resolve(URI uri) { return null; }
            @Override public URI relativize(URI uri) { return null; }});
        return context;
    }

    @Test
	public void testSelectRequest_Filter() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get(SenchaConstructResourceEntityStage.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

        SelectContext<E2> context = prepareContext(params);

        parseStage.doExecute(context);
        constructEntityStage.doExecute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
	}


	@Test
	public void testSelectRequest_Filter_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"address = '1 Main Street'\"}"));
		when(params.get(SenchaConstructResourceEntityStage.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

        SelectContext<E2> context = prepareContext(params);

        parseStage.doExecute(context);
        constructEntityStage.doExecute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("address = '1 Main Street' and name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Sort_Group() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
				Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));
		when(params.get(SenchaParseRequestStage.GROUP)).thenReturn(
				Collections.singletonList("[{\"property\":\"id\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));


        SelectContext<E2> context = prepareContext(params);

        parseStage.doExecute(context);
        constructEntityStage.doExecute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

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
