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
import com.nhl.link.rest.runtime.processor.select.ConstructResourceEntityStage;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
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

    private ParseRequestStage parseStage;
    private ConstructResourceEntityStage constructEntityStage;

	@Before
	public void setUp() {

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        IMapByParser mapByParser = new MapByParser();
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, mapByParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        this.parseStage = new ParseRequestStage(expParser, sortParser, mapByParser, includeParser, excludeParser);

        // prepare entity constructor stage
        ICayenneExpConstructor expConstructor = new CayenneExpConstructor(new ExpressionPostProcessor(pathCache));
        ISortConstructor sortConstructor = new SortConstructor(pathCache);
        IMapByConstructor mapByConstructor = new MapByConstructor();
        ISizeConstructor sizeConstructor = new SizeConstructor();
        IIncludeConstructor includeConstructor = new IncludeConstructor(expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeConstructor excludeConstructor = new ExcludeConstructor();

        this.constructEntityStage
                = new ConstructResourceEntityStage(
                createMetadataService(),
                expConstructor ,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor);
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

        SelectContext<P1> context = prepareContext(params, P1.class);

        parseStage.execute(context);
        constructEntityStage.execute(context);

        ResourceEntity<P1> ce1 = context.getEntity();


		assertNotNull(ce1);
		assertTrue(ce1.isIdIncluded());
		assertEquals(1, ce1.getAttributes().size());
		assertTrue(ce1.getChildren().isEmpty());


        SelectContext<P2> context2 = prepareContext(params, P2.class);

        parseStage.execute(context2);
        constructEntityStage.execute(context2);

        ResourceEntity<P2> ce2 = context2.getEntity();

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

        SelectContext<P2> context2 = prepareContext(params, P2.class);

        parseStage.execute(context2);
        constructEntityStage.execute(context2);

        ResourceEntity<P2> ce2 = context2.getEntity();

		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(1, ce2.getChildren().size());

		assertTrue(ce2.getChildren().keySet().contains("p1"));

	}

}
