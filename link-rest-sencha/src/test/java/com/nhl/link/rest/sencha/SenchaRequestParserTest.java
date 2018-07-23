package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.path.IPathDescriptorManager;
import com.nhl.link.rest.runtime.path.PathDescriptorManager;
import com.nhl.link.rest.runtime.entity.CayenneExpMerger;
import com.nhl.link.rest.runtime.protocol.CayenneExpParser;
import com.nhl.link.rest.runtime.entity.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.entity.ICayenneExpMerger;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.entity.IMapByMerger;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.entity.MapByMerger;
import com.nhl.link.rest.runtime.protocol.MapByParser;
import com.nhl.link.rest.runtime.entity.ISizeMerger;
import com.nhl.link.rest.runtime.protocol.ISizeParser;
import com.nhl.link.rest.runtime.entity.SizeMerger;
import com.nhl.link.rest.runtime.protocol.SizeParser;
import com.nhl.link.rest.runtime.entity.ISortMerger;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.entity.SortMerger;
import com.nhl.link.rest.runtime.protocol.SortParser;
import com.nhl.link.rest.runtime.entity.ExcludeMerger;
import com.nhl.link.rest.runtime.protocol.ExcludeParser;
import com.nhl.link.rest.runtime.entity.IExcludeMerger;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.entity.IIncludeMerger;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.entity.IncludeMerger;
import com.nhl.link.rest.runtime.protocol.IncludeParser;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.parser.filter.ISenchaFilterParser;
import com.nhl.link.rest.sencha.parser.filter.SenchaFilterParser;
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

// TODO: separate tests for parse and construct entity stages into separate tests
public class SenchaRequestParserTest extends TestWithCayenneMapping {

    private SenchaParseRequestStage parseStage;
    private SenchaConstructResourceEntityStage constructEntityStage;

	@Before
	public void before() {

		IPathDescriptorManager pathCache = new PathDescriptorManager();
		IJacksonService jacksonService = new JacksonService();

		// prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
		ISortParser sortParser = new SortParser(jacksonService);
        IMapByParser mapByParser = new MapByParser();
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, mapByParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);
        ISenchaFilterParser filterParser = new SenchaFilterParser(jacksonService);

        this.parseStage = new SenchaParseRequestStage(expParser, sortParser, mapByParser, includeParser, excludeParser, filterParser);

        // prepare entity constructor stage
        ICayenneExpMerger expConstructor = new CayenneExpMerger(new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger();
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

		ISenchaFilterConstructor senchaFilterProcessor = new SenchaFilterConstructor(pathCache, new ExpressionPostProcessor(pathCache));

        this.constructEntityStage
                = new SenchaConstructResourceEntityStage(
                        createMetadataService(),
                        expConstructor ,
                        sortConstructor,
                        mapByConstructor,
                        sizeConstructor,
                        includeConstructor,
                        excludeConstructor,
                        senchaFilterProcessor);
	}

	protected SelectContext<E2> prepareContext(MultivaluedMap<String, String> params) {
        SelectContext<E2> context = new SelectContext<>(E2.class);

        // TODO: replace this with mock!!
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
		when(params.get(SenchaParseRequestStage.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

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
		when(params.get(SenchaParseRequestStage.FILTER)).thenReturn(Collections.singletonList("[{\"property\":\"name\",\"value\":\"xyz\"}]"));

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
