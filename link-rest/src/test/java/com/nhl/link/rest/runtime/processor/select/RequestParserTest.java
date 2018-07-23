package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.runtime.entity.CayenneExpMerger;
import com.nhl.link.rest.runtime.entity.ExcludeMerger;
import com.nhl.link.rest.runtime.entity.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.entity.ICayenneExpMerger;
import com.nhl.link.rest.runtime.entity.IExcludeMerger;
import com.nhl.link.rest.runtime.entity.IIncludeMerger;
import com.nhl.link.rest.runtime.entity.IMapByMerger;
import com.nhl.link.rest.runtime.entity.ISizeMerger;
import com.nhl.link.rest.runtime.entity.ISortMerger;
import com.nhl.link.rest.runtime.entity.IncludeMerger;
import com.nhl.link.rest.runtime.entity.MapByMerger;
import com.nhl.link.rest.runtime.entity.SizeMerger;
import com.nhl.link.rest.runtime.entity.SortMerger;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.path.IPathDescriptorManager;
import com.nhl.link.rest.runtime.path.PathDescriptorManager;
import com.nhl.link.rest.runtime.protocol.CayenneExpParser;
import com.nhl.link.rest.runtime.protocol.ExcludeParser;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.protocol.ISizeParser;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.protocol.IncludeParser;
import com.nhl.link.rest.runtime.protocol.MapByParser;
import com.nhl.link.rest.runtime.protocol.SizeParser;
import com.nhl.link.rest.runtime.protocol.SortParser;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// TODO: split this test by stage. Currently testing combined PARSED_REQUEST and CREATE_ENTITY
public class RequestParserTest extends TestWithCayenneMapping {

    private ParseRequestStage parseStage;
    private CreateResourceEntityStage constructEntityStage;

	@Before
	public void setUp() {

		IPathDescriptorManager pathCache = new PathDescriptorManager();
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
        ICayenneExpMerger expConstructor = new CayenneExpMerger(new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger();
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        this.constructEntityStage
                = new CreateResourceEntityStage(
                createMetadataService(),
                expConstructor ,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor);
	}

	@Test
	public void testSelectRequest_Default() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);
        constructEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(3, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);
        constructEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertFalse(resourceEntity.isIdIncluded());

		assertEquals(2, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));

		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);
        constructEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertFalse(resourceEntity.isIdIncluded());

		assertEquals(2, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "age"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);
        constructEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.NAME.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs_AsArray() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("exclude")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);
        constructEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.NAME.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("description", "age", "id"));
		when(params.get("exclude")).thenReturn(Arrays.asList("description", "name"));

		SelectContext<E1> context = prepareContext(params, E1.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));

		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(2, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.NAME.getName()));
		assertTrue(resourceEntity.getAttributes().containsKey(E2.ADDRESS.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ResourceEntity.isIdIncluded());
		assertEquals(2, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.NAME.getName()));
		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.PHONE_NUMBER.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeBothAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("name", "e3s.name"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertFalse(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.NAME.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertFalse(e3ResourceEntity.isIdIncluded());
		assertEquals(1, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.NAME.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeBothAttrs() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s.name"));
		when(params.get("exclude")).thenReturn(Arrays.asList("name"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.ADDRESS.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertFalse(e3ResourceEntity.isIdIncluded());
		assertEquals(1, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.NAME.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeBothAttrs2() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("e3s"));
		when(params.get("exclude")).thenReturn(Arrays.asList("address", "e3s.name"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E2.NAME.getName()));

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ResourceEntity.isIdIncluded());
		assertEquals(1, e3ResourceEntity.getAttributes().size());

		assertTrue(e3ResourceEntity.getAttributes().containsKey(E3.PHONE_NUMBER.getName()));
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRelationshipIds() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("id", "e3s.id"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertTrue(resourceEntity.getAttributes().isEmpty());

		assertEquals(1, resourceEntity.getChildren().size());
		assertEquals(1, resourceEntity.getChildren().entrySet().size());
		assertTrue(resourceEntity.getChildren().keySet().contains(E2.E3S.getName()));

		ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get(E2.E3S.getName());
		assertTrue(e3ResourceEntity.isIdIncluded());
		assertTrue(e3ResourceEntity.getAttributes().isEmpty());
		assertTrue(e3ResourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_SortSimple_NoDir() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList(E2.NAME.getName()));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_ASC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList(E2.NAME.getName()));
		when(params.get("dir")).thenReturn(Collections.singletonList("ASC"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_DESC() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList(E2.NAME.getName()));
		when(params.get("dir")).thenReturn(Collections.singletonList("DESC"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_SortSimple_Garbage() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(Collections.singletonList("s1"));
		when(params.get("dir")).thenReturn(Collections.singletonList("XYZ"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);
	}

	@Test
	public void testSelectRequest_Sort() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(2, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
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
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"name\",\"direction\":\"ASC\"}]"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
		Ordering o1 = it.next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_Sort_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"p1\",\"direction\":\"DESC\"},{\"property\":\"p2\",\"direction\":\"XXX\"}]"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_CayenneExp_BadSpec() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp"))
				.thenReturn(Collections.singletonList("{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);
	}

	@Test
	public void testSelectRequest_CayenneExp() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"name = 'John Smith'\"}"));

		SelectContext<E2> context = prepareContext(params, E2.class);

		parseStage.execute(context);
		constructEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name = 'John Smith'"), resourceEntity.getQualifier());
	}
}
