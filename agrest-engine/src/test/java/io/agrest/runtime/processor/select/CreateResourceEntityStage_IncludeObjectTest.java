package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.base.protocol.Include;
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
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateResourceEntityStage_IncludeObjectTest {

    private static CreateResourceEntityStage stage;
    private static IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeClass
    public static void beforeAll() {

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        MetadataService metadataService = new MetadataService(Collections.singletonList(compiler));

        IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare create entity stage
        ICayenneExpMerger expConstructor = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger(mock(IMetadataService.class));
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(mock(IMetadataService.class), expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        stage = new CreateResourceEntityStage(
                metadataService,
                expConstructor,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor);

        requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(ICayenneExpParser.class),
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
