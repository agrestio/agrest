package io.agrest.runtime.processor.select;

import io.agrest.AgException;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExcludeMerger;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.entity.ICayenneExpMerger;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.entity.IMapByMerger;
import io.agrest.runtime.entity.ISizeMerger;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.entity.MapByMerger;
import io.agrest.runtime.entity.SizeMerger;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Iterator;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CreateEntityStageTest extends TestWithCayenneMapping {

    private CreateResourceEntityStage createEntityStage;
    private IAgRequestBuilderFactory requestBuilderFactory;

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
                expMerger,
                sortMerger,
                mapByMerger,
                sizeMerger,
                includeMerger,
                excludeMerger);

        this.requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(ICayenneExpParser.class),
                mock(ISortParser.class),
                mock(IIncludeParser.class),
                mock(IExcludeParser.class)
        );
    }

    @Test
    public void testSelectRequest_Default() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<E1> context = prepareContext(params, E1.class);

        context.setRawRequest(requestBuilderFactory.builder().build());

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
        context.setRawRequest(requestBuilderFactory.builder().addInclude(include1).addInclude(include2).build());

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

        context.setRawRequest(requestBuilderFactory.builder()
                .addInclude(new Include("description"))
                .addInclude(new Include("age")).build());

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
        context.setRawRequest(requestBuilderFactory.builder()
                .addExclude(exclude1)
                .addExclude(exclude2).build());

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
        
        context.setRawRequest(requestBuilderFactory.builder()
                .addExclude(new Exclude("description"))
                .addExclude(new Exclude("age")).build());

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

        context.setRawRequest(requestBuilderFactory.builder()
                .addInclude(include1)
                .addInclude(include2)
                .addInclude(include3)
                .addExclude(exclude1)
                .addExclude(exclude2)
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
        context.setRawRequest(requestBuilderFactory.builder().addInclude(new Include("e3s")).build());

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
        context.setRawRequest(requestBuilderFactory.builder()
                .addInclude(include1)
                .addInclude(include2).build());

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
        context.setRawRequest(requestBuilderFactory.builder()
                .addInclude(include)
                .addExclude(exclude)
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
        context.setRawRequest(requestBuilderFactory.builder()
                .addInclude(include)
                .addExclude(exclude1)
                .addExclude(exclude2)
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
        context.setRawRequest(requestBuilderFactory.builder()
                .addInclude(include1)
                .addInclude(include2).build());

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

        context.setRawRequest(requestBuilderFactory.builder().sort(sort).build());

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

        Sort sort = new Sort(E2.NAME.getName(), Dir.ASC);

        context.setRawRequest(requestBuilderFactory.builder()
                .sort(sort)
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

        Sort sort = new Sort(E2.NAME.getName(), Dir.DESC);

        context.setRawRequest(requestBuilderFactory.builder()
                .sort(sort)
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

        context.setRawRequest(requestBuilderFactory.builder().sort(sort).build());

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

        context.setRawRequest(requestBuilderFactory.builder().sort(sort).build());

        createEntityStage.execute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
        Ordering o1 = it.next();
        assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
        assertEquals(E2.NAME.getName(), o1.getSortSpecString());
    }

    @Test(expected = AgException.class)
    public void testSelectRequest_Sort_BadSpec() {

        SelectContext<E2> context = new SelectContext<>(E2.class);

        Sort sort = new Sort(Arrays.asList(
                new Sort("{\"property\":\"p1\",\"direction\":\"DESC\"}"),
                new Sort("{\"property\":\"p2\",\"direction\":\"XXX\"}")));

        context.setRawRequest(requestBuilderFactory.builder().sort(sort).build());

        createEntityStage.execute(context);
    }

    @Test(expected = AgException.class)
    public void testSelectRequest_CayenneExp_BadSpec() {

        SelectContext<E2> context = new SelectContext<>(E2.class);

        CayenneExp cayenneExp = new CayenneExp("numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true");

        context.setRawRequest(requestBuilderFactory.builder().cayenneExp(cayenneExp).build());

        createEntityStage.execute(context);
    }

    @Test
    public void testSelectRequest_CayenneExp() {

        SelectContext<E2> context = new SelectContext<>(E2.class);

        CayenneExp cayenneExp = new CayenneExp("name = 'John Smith'");

        context.setRawRequest(requestBuilderFactory.builder().cayenneExp(cayenneExp).build());

        createEntityStage.execute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity.getQualifier());
        assertEquals(exp("name = 'John Smith'"), resourceEntity.getQualifier());
    }
}
