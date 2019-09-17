package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.it.fixture.pojo.model.P1;
import io.agrest.it.fixture.pojo.model.P2;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.protocol.Include;
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
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CreateEntityStage_WithPojoTest extends TestWithCayenneMapping {

    private CreateResourceEntityStage createEntityStage;
    private IAgRequestBuilderFactory requestBuilderFactory;

    @Before
    public void setUp() {

        IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare create entity stage
        ICayenneExpMerger expConstructor = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger(mock(IMetadataService.class));
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(mock(IMetadataService.class), expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        this.createEntityStage = new CreateResourceEntityStage(
                metadataService,
                expConstructor,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor);

        this.requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(ICayenneExpParser.class),
                mock(ISortParser.class),
                mock(IIncludeParser.class),
                mock(IExcludeParser.class)
        );
    }

    @Override
    protected List<AgEntityCompiler> createEntityCompilers() {
        List<AgEntityCompiler> compilers = super.createEntityCompilers();

        // reorder compilers for POJO one to go first
        Collections.swap(compilers, 0, 1);
        return compilers;
    }

    @Test
    public void testSelectRequest_Default() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<P1> context = prepareContext(params, P1.class);

        context.setMergedRequest(requestBuilderFactory.builder().build());
        createEntityStage.execute(context);

        ResourceEntity<P1> ce1 = context.getEntity();


        assertNotNull(ce1);
        assertTrue(ce1.isIdIncluded());
        assertEquals(1, ce1.getAttributes().size());
        assertTrue(ce1.getChildren().isEmpty());


        SelectContext<P2> context2 = prepareContext(params, P2.class);

        context2.setMergedRequest(requestBuilderFactory.builder().build());
        createEntityStage.execute(context2);

        ResourceEntity<P2> ce2 = context2.getEntity();

        assertNotNull(ce2);
        assertTrue(ce2.isIdIncluded());
        assertEquals(1, ce2.getAttributes().size());
        assertEquals(0, ce2.getChildren().size());
    }

    @Test
    public void testSelectRequest_IncludeRels() {

        SelectContext<P2> context2 = new SelectContext<>(P2.class);
        context2.setMergedRequest(requestBuilderFactory.builder().addInclude(new Include("p1")).build());

        createEntityStage.execute(context2);

        ResourceEntity<P2> ce2 = context2.getEntity();

        assertNotNull(ce2);
        assertTrue(ce2.isIdIncluded());
        assertEquals(1, ce2.getAttributes().size());
        assertEquals(1, ce2.getChildren().size());

        assertTrue(ce2.getChildren().keySet().contains("p1"));
    }
}
