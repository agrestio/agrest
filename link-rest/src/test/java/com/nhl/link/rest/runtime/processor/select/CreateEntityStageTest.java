package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Sort;
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
import com.nhl.link.rest.runtime.path.IPathDescriptorManager;
import com.nhl.link.rest.runtime.path.PathDescriptorManager;
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

public class CreateEntityStageTest extends TestWithCayenneMapping {

    private CreateResourceEntityStage createEntityStage;

	@Before
	public void setUp() {

		IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare create entity stage
        ICayenneExpMerger expMerger = new CayenneExpMerger(new ExpressionPostProcessor(pathCache));
        ISortMerger sortMerger = new SortMerger(pathCache);
        IMapByMerger mapByMerger = new MapByMerger();
        ISizeMerger sizeMerger = new SizeMerger();
        IIncludeMerger includeMerger = new IncludeMerger(expMerger, sortMerger, mapByMerger, sizeMerger);
        IExcludeMerger excludeMerger = new ExcludeMerger();

        this.createEntityStage
                = new CreateResourceEntityStage(
                createMetadataService(),
                expMerger ,
                sortMerger,
                mapByMerger,
                sizeMerger,
                includeMerger,
                excludeMerger);
	}

	@Test
	public void testSelectRequest_Default() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<E1> context = prepareContext(params, E1.class);

        context.setRawRequest(LrRequest.builder().build());

		createEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(3, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeAttrs() {

		SelectContext<E1> context = new SelectContext<>(E1.class);

		Include include1 = new Include("description");
		Include include2 = new Include("age");
		context.setRawRequest(LrRequest.builder().includes(Arrays.asList(include1, include2)).build());

		createEntityStage.execute(context);

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

        SelectContext<E1> context = new SelectContext<>(E1.class);

		Include include = new Include(Arrays.asList(
				new Include("description"),
				new Include("age")));
		context.setRawRequest(LrRequest.builder().includes(Collections.singletonList(include)).build());

		createEntityStage.execute(context);

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

		SelectContext<E1> context = new SelectContext<>(E1.class);

		Exclude exclude1 = new Exclude("description");
		Exclude exclude2 = new Exclude("age");
		context.setRawRequest(LrRequest.builder().excludes(Arrays.asList(exclude1, exclude2)).build());

		createEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.NAME.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_ExcludeAttrs_AsArray() {

		SelectContext<E1> context = new SelectContext<>(E1.class);

		Exclude exclude = new Exclude(Arrays.asList(
				new Exclude("description"),
				new Exclude("age")));
		context.setRawRequest(LrRequest.builder().excludes(Collections.singletonList(exclude)).build());

		createEntityStage.execute(context);

        ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.NAME.getName()));
		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeExcludeAttrs() {

		SelectContext<E1> context = new SelectContext<>(E1.class);

		Include include1 = new Include("description");
		Include include2 = new Include("age");
		Include include3 = new Include("id");
		Exclude exclude1 = new Exclude("description");
		Exclude exclude2 = new Exclude("name");


		context.setRawRequest(LrRequest.builder()
				.includes(Arrays.asList(include1, include2, include3))
				.excludes(Arrays.asList(exclude1, exclude2))
				.build());

		createEntityStage.execute(context);

		ResourceEntity<E1> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());
		assertEquals(1, resourceEntity.getAttributes().size());
		assertTrue(resourceEntity.getAttributes().containsKey(E1.AGE.getName()));

		assertTrue(resourceEntity.getChildren().isEmpty());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Include include = new Include("e3s");

		context.setRawRequest(LrRequest.builder().includes(Collections.singletonList(include)).build());

		createEntityStage.execute(context);

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

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Include include1 = new Include("name");
		Include include2 = new Include("e3s.name");
		context.setRawRequest(LrRequest.builder().includes(Arrays.asList(include1, include2)).build());

		createEntityStage.execute(context);

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

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Include include = new Include("e3s.name");
		Exclude exclude = new Exclude("name");
		context.setRawRequest(LrRequest.builder()
				.includes(Collections.singletonList(include))
				.excludes(Collections.singletonList(exclude))
				.build());

		createEntityStage.execute(context);

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

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Include include = new Include("e3s");
		Exclude exclude1 = new Exclude("address");
		Exclude exclude2 = new Exclude("e3s.name");
		context.setRawRequest(LrRequest.builder()
				.includes(Collections.singletonList(include))
				.excludes(Arrays.asList(exclude1, exclude2))
				.build());

		createEntityStage.execute(context);

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

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Include include1 = new Include("id");
		Include include2 = new Include("e3s.id");
		context.setRawRequest(LrRequest.builder().includes(Arrays.asList(include1, include2)).build());

		createEntityStage.execute(context);

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

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(E2.NAME.getName());

		context.setRawRequest(LrRequest.builder().sort(sort).build());

		createEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_ASC() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(E2.NAME.getName());

		context.setRawRequest(LrRequest.builder()
				.sort(sort)
				.sortDirection(Dir.ASC)
				.build());

		createEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_SortSimple_DESC() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(E2.NAME.getName());

		context.setRawRequest(LrRequest.builder()
				.sort(sort)
				.sortDirection(Dir.DESC)
				.build());

		createEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Ordering o1 = resourceEntity.getOrderings().iterator().next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test
	public void testSelectRequest_Sort() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(Arrays.asList(
				new Sort("name", Dir.DESC),
				new Sort("address", Dir.ASC)));

		context.setRawRequest(LrRequest.builder().sort(sort).build());

		createEntityStage.execute(context);

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

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(Arrays.asList(
				new Sort("name", Dir.DESC),
				new Sort("name", Dir.ASC)));

		context.setRawRequest(LrRequest.builder().sort(sort).build());

		createEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(1, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
		Ordering o1 = it.next();
		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals(E2.NAME.getName(), o1.getSortSpecString());
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_Sort_BadSpec() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(Arrays.asList(
				new Sort("{\"property\":\"p1\",\"direction\":\"DESC\"}"),
				new Sort("{\"property\":\"p2\",\"direction\":\"XXX\"}")));

		context.setRawRequest(LrRequest.builder().sort(sort).build());

		createEntityStage.execute(context);
	}

	@Test(expected = LinkRestException.class)
	public void testSelectRequest_CayenneExp_BadSpec() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		CayenneExp cayenneExp  = new CayenneExp("numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true");

		context.setRawRequest(LrRequest.builder().cayenneExp(cayenneExp).build());

		createEntityStage.execute(context);
	}

	@Test
	public void testSelectRequest_CayenneExp() {

		SelectContext<E2> context = new SelectContext<>(E2.class);

		CayenneExp cayenneExp  = new CayenneExp("name = 'John Smith'");

		context.setRawRequest(LrRequest.builder().cayenneExp(cayenneExp).build());

		createEntityStage.execute(context);

		ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("name = 'John Smith'"), resourceEntity.getQualifier());
	}
}
