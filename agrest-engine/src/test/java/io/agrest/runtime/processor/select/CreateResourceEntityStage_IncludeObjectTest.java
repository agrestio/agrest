package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import io.agrest.protocol.Include;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CreateResourceEntityStage_IncludeObjectTest {

    private static SelectCreateResourceEntityStage stage;
    private static IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeAll
    public static void beforeAll() {

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Map.of());
        AgSchema schema = new LazySchema(List.of(compiler));

        // prepare create entity stage
        IExpMerger expConstructor = new ExpMerger();
        ISortMerger sortConstructor = new SortMerger();
        IMapByMerger mapByConstructor = new MapByMerger(schema);
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(schema, expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        stage = new SelectCreateResourceEntityStage(
                schema,
                expConstructor,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor);

        requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(IExpParser.class),
                mock(ISortParser.class),
                mock(IIncludeParser.class),
                mock(IExcludeParser.class)
        );
    }

    @Test
    public void execute_IncludeObject_Path() {

        SelectContext<Tr> context = new SelectContext<>(
                Tr.class,
                requestBuilderFactory.builder(),
                PathChecker.ofDefault(),
                mock(Injector.class));
        context.setRequest(requestBuilderFactory.builder().addInclude(new Include("rtss")).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();
        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());

        assertEquals(1, resourceEntity.getChildren().size());
        assertNotNull(resourceEntity.getChild("rtss"));
    }

    @Test
    public void execute_IncludeObject_MapBy() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class,
                requestBuilderFactory.builder(),
                PathChecker.ofDefault(),
                mock(Injector.class));

        Include include = new Include("rtss", null, Collections.emptyList(), "rtt", null, null);
        context.setRequest(requestBuilderFactory.builder().addInclude(include).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();
        assertNotNull(resourceEntity);

        ResourceEntity<?> reMapBy = resourceEntity.getChild("rtss").getMapBy();
        assertNotNull(reMapBy);
        assertNotNull(reMapBy.getChild("rtt"));
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
    }
}
