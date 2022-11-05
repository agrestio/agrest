package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;
import io.agrest.runtime.entity.ExcludeMerger;
import io.agrest.runtime.entity.ExpMerger;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IExpMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.entity.IMapByMerger;
import io.agrest.runtime.entity.ISizeMerger;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.entity.IncludeMerger;
import io.agrest.runtime.entity.MapByMerger;
import io.agrest.runtime.entity.SizeMerger;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.processor.select.stage.SelectCreateResourceEntityStage;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.di.Injector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CreateResourceEntityStageTest {

    private static SelectCreateResourceEntityStage stage;
    private static IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeAll
    public static void beforeAll() {

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Map.of());
        AgSchema schema = new LazySchema(List.of(compiler));

        // prepare create entity stage
        IExpMerger expMerger = new ExpMerger();
        ISortMerger sortMerger = new SortMerger();
        IMapByMerger mapByMerger = new MapByMerger(schema);
        ISizeMerger sizeMerger = new SizeMerger();
        IIncludeMerger includeMerger = new IncludeMerger(schema, expMerger, sortMerger, mapByMerger, sizeMerger);
        IExcludeMerger excludeMerger = new ExcludeMerger();

        stage = new SelectCreateResourceEntityStage(
                schema,
                expMerger,
                sortMerger,
                mapByMerger,
                sizeMerger,
                includeMerger,
                excludeMerger);

        requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(IExpParser.class),
                mock(ISortParser.class),
                mock(IIncludeParser.class),
                mock(IExcludeParser.class)
        );
    }

    @Test
    public void testExecute_Default() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory.builder().build());
        context.mergeClientParameters(new HashMap<>());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(3, resourceEntity.getBaseProjection().getAttributes().size());
        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_Include() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("a"))
                .addInclude(new Include("b")).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertFalse(resourceEntity.isIdIncluded());

        assertEquals(2, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("a"));
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("b"));

        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_Exclude() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addExclude(new Exclude("a"))
                .addExclude(new Exclude("b")).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());

        assertEquals(1, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("c"));
        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeExcludeAttrs() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));

        Include include1 = new Include("a");
        Include include2 = new Include("b");
        Include include3 = new Include("id");
        Exclude exclude1 = new Exclude("a");
        Exclude exclude2 = new Exclude("c");

        context.setRequest(requestBuilderFactory.builder()
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
        assertEquals(1, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("b"));

        assertTrue(resourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeRels() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory.builder().addInclude(new Include("rtt")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(2, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("m"));
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("n"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<Tt> ttEntity = (ResourceEntity<Tt>) resourceEntity.getChildren().get("rtt");
        assertTrue(ttEntity.isIdIncluded());
        assertEquals(2, ttEntity.getBaseProjection().getAttributes().size());

        assertNotNull(ttEntity.getBaseProjection().getAttribute("o"));
        assertNotNull(ttEntity.getBaseProjection().getAttribute("p"));
        assertTrue(ttEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeBothAttrs() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory.builder()
                .addInclude(new Include("m"))
                .addInclude(new Include("rtt.o")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertFalse(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("m"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertFalse(e3ResourceEntity.isIdIncluded());
        assertEquals(1, e3ResourceEntity.getBaseProjection().getAttributes().size());

        assertNotNull(e3ResourceEntity.getBaseProjection().getAttribute("o"));
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeExcludeBothAttrs() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));

        context.setRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("rtt.o"))
                .addExclude(new Exclude("m"))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("n"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertFalse(e3ResourceEntity.isIdIncluded());
        assertEquals(1, e3ResourceEntity.getBaseProjection().getAttributes().size());

        assertNotNull(e3ResourceEntity.getBaseProjection().getAttribute("o"));
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeExcludeBothAttrs2() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("rtt"))
                .addExclude(new Exclude("n"))
                .addExclude(new Exclude("rtt.o"))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertEquals(1, resourceEntity.getBaseProjection().getAttributes().size());
        assertNotNull(resourceEntity.getBaseProjection().getAttribute("m"));

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertTrue(e3ResourceEntity.isIdIncluded());
        assertEquals(1, e3ResourceEntity.getBaseProjection().getAttributes().size());

        assertNotNull(e3ResourceEntity.getBaseProjection().getAttribute("p"));
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_IncludeRelationshipIds() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addInclude(new Include("id"))
                .addInclude(new Include("rtt.id")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());
        assertTrue(resourceEntity.getBaseProjection().getAttributes().isEmpty());

        assertEquals(1, resourceEntity.getChildren().size());
        assertEquals(1, resourceEntity.getChildren().entrySet().size());
        assertTrue(resourceEntity.getChildren().keySet().contains("rtt"));

        ResourceEntity<?> e3ResourceEntity = resourceEntity.getChildren().get("rtt");
        assertTrue(e3ResourceEntity.isIdIncluded());
        assertTrue(e3ResourceEntity.getBaseProjection().getAttributes().isEmpty());
        assertTrue(e3ResourceEntity.getChildren().isEmpty());
    }

    @Test
    public void testExecute_SortSimple_NoDir() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addSort(new Sort("n")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Sort o1 = resourceEntity.getOrderings().iterator().next();
        assertEquals(new Sort("n", Direction.asc), o1);
    }

    @Test
    public void testExecute_SortSimple_ASC() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addSort(new Sort("n", Direction.asc))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Sort o1 = resourceEntity.getOrderings().iterator().next();
        assertEquals(new Sort("n", Direction.asc), o1);
    }

    @Test
    public void testExecute_SortSimple_DESC() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addSort(new Sort("n", Direction.desc))
                .build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(1, resourceEntity.getOrderings().size());
        Sort o1 = resourceEntity.getOrderings().iterator().next();
        assertEquals(new Sort("n", Direction.desc), o1);
    }

    @Test
    public void testExecute_Sort() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .addSort(new Sort("m", Direction.desc))
                .addSort(new Sort("n", Direction.asc)).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();

        assertEquals(2, resourceEntity.getOrderings().size());
        assertEquals(new Sort("m", Direction.desc), resourceEntity.getOrderings().get(0));
        assertEquals(new Sort("n", Direction.asc), resourceEntity.getOrderings().get(1));
    }

    @Test
    public void testExecute_Exp_BadSpec() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .andExp(Exp.simple("x = 12345 and y = 'John Smith' and z = true")).build());

        assertDoesNotThrow(() -> stage.execute(context), "Even though the passed spec is invalid, no parsing should occur at this stage");
    }

    @Test
    public void testExecute_Exp() {

        SelectContext<Ts> context = new SelectContext<>(Ts.class,
                requestBuilderFactory.builder(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory
                .builder()
                .andExp(Exp.simple("m = 'John Smith'")).build());

        stage.execute(context);

        ResourceEntity<Ts> resourceEntity = context.getEntity();
        assertEquals(Exp.simple("m = 'John Smith'"), resourceEntity.getExp());
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
