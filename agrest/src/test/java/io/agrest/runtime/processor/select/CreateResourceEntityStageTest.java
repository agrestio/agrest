package io.agrest.runtime.processor.select;

import io.agrest.AgException;
import io.agrest.ResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExcludeMerger;
import io.agrest.runtime.entity.ExpressionParser;
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
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateResourceEntityStageTest {

    private static CreateResourceEntityStage stage;
    private static IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeClass
    public static void beforeAll() {

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        MetadataService metadataService = new MetadataService(Collections.singletonList(compiler));

        IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare create entity stage
        ICayenneExpMerger expMerger = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathCache));
        ISortMerger sortMerger = new SortMerger(pathCache);
        IMapByMerger mapByMerger = new MapByMerger(mock(IMetadataService.class));
        ISizeMerger sizeMerger = new SizeMerger();
        IIncludeMerger includeMerger = new IncludeMerger(mock(IMetadataService.class), expMerger, sortMerger, mapByMerger, sizeMerger);
        IExcludeMerger excludeMerger = new ExcludeMerger();

        stage = new CreateResourceEntityStage(
                metadataService,
                expMerger,
                sortMerger,
                mapByMerger,
                sizeMerger,
                includeMerger,
                excludeMerger);

        requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(ICayenneExpParser.class),
                mock(ISortParser.class),
                mock(IIncludeParser.class),
                mock(IExcludeParser.class)
        );
    }

    @Test
    public void testExecute_Default() {

        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        SelectContext<Tr> context = new SelectContext<>(Tr.class);
        context.setUriInfo(uriInfo);
        context.setMergedRequest(requestBuilderFactory.builder().build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(3, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_Include() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("a"))
                .addInclude(new Include("b")).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertFalse(resourceEntity.isIdIncluded());

        assertEquals(2, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("a"));
        assertTrue(resourceEntity.getAttributes().containsKey("b"));

        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_Exclude() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addExclude(new Exclude("a"))
                .addExclude(new Exclude("b")).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());

        assertEquals(1, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("c"));
        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeExcludeAttrs() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class);

        Include include1 = new Include("a");
        Include include2 = new Include("b");
        Include include3 = new Include("id");
        Exclude exclude1 = new Exclude("a");
        Exclude exclude2 = new Exclude("c");

        context.setMergedRequest(requestBuilderFactory.builder()
                .addInclude(include1)
                .addInclude(include2)
                .addInclude(include3)
                .addExclude(exclude1)
                .addExclude(exclude2)
                .build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("b"));

        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeRels() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory.builder().addInclude(new Include("rtt")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(2, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("m"));
        assertTrue(resourceEntity.getAttributes().containsKey("n"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<Tt> ttEntity = (ResourceEntity<Tt>) resourceEntity.getChildren().get("rtt");
        assertTrue(ttEntity.isIdIncluded());
        assertEquals(2, ttEntity.getAttributes().size());

        assertTrue(ttEntity.getAttributes().containsKey("o"));
        assertTrue(ttEntity.getAttributes().containsKey("p"));
        assertTrue(ttEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeBothAttrs() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory.builder()
                .addInclude(new Include("m"))
                .addInclude(new Include("rtt.o")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertFalse(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("m"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertFalse(e3ResourceEntity.isIdIncluded());
        assertEquals(1, e3ResourceEntity.getAttributes().size());

        assertTrue(e3ResourceEntity.getAttributes().containsKey("o"));
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeExcludeBothAttrs() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);

        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("rtt.o"))
                .addExclude(new Exclude("m"))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("n"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertFalse(e3ResourceEntity.isIdIncluded());
        assertEquals(1, e3ResourceEntity.getAttributes().size());

        assertTrue(e3ResourceEntity.getAttributes().containsKey("o"));
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeExcludeBothAttrs2() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("rtt"))
                .addExclude(new Exclude("n"))
                .addExclude(new Exclude("rtt.o"))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getAttributes().size());
        assertTrue(resourceEntity.getAttributes().containsKey("m"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertTrue(e3ResourceEntity.isIdIncluded());
        assertEquals(1, e3ResourceEntity.getAttributes().size());

        assertTrue(e3ResourceEntity.getAttributes().containsKey("p"));
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeRelationshipIds() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("id"))
                .addInclude(new Include("rtt.id")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertTrue(resourceEntity.getAttributes().isEmpty());

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertTrue(e3ResourceEntity.isIdIncluded());
        assertTrue(e3ResourceEntity.getAttributes().isEmpty());
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_SortSimple_NoDir() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addOrdering(new Sort("n")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Ordering o1 = resourceEntity.getOrderings().iterator().next();
        assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
        assertEquals("n", o1.getSortSpecString());
    }

    @Test
    public void testExecute_SortSimple_ASC() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addOrdering(new Sort("n", Dir.ASC))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Ordering o1 = resourceEntity.getOrderings().iterator().next();
        assertEquals(SortOrder.ASCENDING, o1.getSortOrder());
        assertEquals("n", o1.getSortSpecString());
    }

    @Test
    public void testExecute_SortSimple_DESC() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addOrdering(new Sort("n", Dir.DESC))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Ordering o1 = resourceEntity.getOrderings().iterator().next();
        assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
        assertEquals("n", o1.getSortSpecString());
    }

    @Test
    public void testExecute_Sort() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addOrdering(new Sort("m", Dir.DESC))
                .addOrdering(new Sort("n", Dir.ASC)).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(2, resourceEntity.getOrderings().size());
        Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
        Ordering o1 = it.next();
        Ordering o2 = it.next();
        assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
        assertEquals("m", o1.getSortSpecString());
        assertEquals(SortOrder.ASCENDING, o2.getSortOrder());
        assertEquals("n", o2.getSortSpecString());
    }

    @Test
    public void testExecute_Sort_Dupes() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);

        context.setMergedRequest(requestBuilderFactory
                .builder()
                .addOrdering(new Sort("n", Dir.DESC))
                .addOrdering(new Sort("n", Dir.ASC)).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
        Ordering o1 = it.next();
        assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
        assertEquals("n", o1.getSortSpecString());
    }

    @Test(expected = AgException.class)
    public void testExecute_CayenneExp_BadSpec() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .cayenneExp(new CayenneExp("x = 12345 and y = 'John Smith' and z = true")).build());

        stage.execute(context);
    }

    @Test
    public void testExecute_CayenneExp() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class);
        context.setMergedRequest(requestBuilderFactory
                .builder()
                .cayenneExp(new CayenneExp("m = 'John Smith'")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();
        assertNotNull(resourceEntity.getQualifier());
        assertEquals(exp("m = 'John Smith'"), resourceEntity.getQualifier());
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public int getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getB() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getC() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public List<Ts> getRtss() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Ts {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getN() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getM() {
            throw new UnsupportedOperationException();
        }


        @AgRelationship
        public Tt getRtt() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Tt {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }


        @AgAttribute
        public String getO() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getP() {
            throw new UnsupportedOperationException();
        }
    }
}
