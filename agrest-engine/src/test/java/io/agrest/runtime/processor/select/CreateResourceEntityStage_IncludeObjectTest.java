package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.base.protocol.Include;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.runtime.entity.*;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateResourceEntityStage_IncludeObjectTest {

    private static CreateResourceEntityStage stage;
    private static IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeAll
    public static void beforeAll() {

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        AgDataMap dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        
        // prepare create entity stage
        IExpMerger expConstructor = new ExpMerger();
        ISortMerger sortConstructor = new SortMerger();
        IMapByMerger mapByConstructor = new MapByMerger(dataMap);
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(dataMap, expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        stage = new CreateResourceEntityStage(
                dataMap,
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
    public void testExecute_IncludeObject_Path() {

        SelectContext<Tr> context = new SelectContext<>(Tr.class);
        context.setMergedRequest(requestBuilderFactory.builder().addInclude(new Include("rtss")).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();
        assertNotNull(resourceEntity);
        assertTrue(resourceEntity.isIdIncluded());

        assertEquals(1, resourceEntity.getChildren().size());
        assertTrue(resourceEntity.getChildren().containsKey("rtss"));
    }

    @Test
    public void testExecute_IncludeObject_MapBy() {

        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"rtss\",\"mapBy\":\"rtt\"}"));

        SelectContext<Tr> context = new SelectContext<>(Tr.class);

        Include include = new Include("rtss", null, Collections.emptyList(), "rtt", null, null);
        context.setMergedRequest(requestBuilderFactory.builder().addInclude(include).build());

        stage.execute(context);

        ResourceEntity<Tr> resourceEntity = context.getEntity();
        assertNotNull(resourceEntity);

        ResourceEntity<?> reMapBy = resourceEntity.getChildren().get("rtss").getMapBy();
        assertNotNull(reMapBy);
        assertNotNull(reMapBy.getChildren().get("rtt"));
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
