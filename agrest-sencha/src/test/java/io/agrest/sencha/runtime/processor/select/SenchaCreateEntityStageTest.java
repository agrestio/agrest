package io.agrest.sencha.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.protocol.CayenneExp;
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
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.sencha.SenchaRequest;
import io.agrest.sencha.protocol.Filter;
import io.agrest.sencha.runtime.entity.ISenchaFilterExpressionCompiler;
import io.agrest.sencha.runtime.entity.SenchaFilterExpressionCompiler;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SenchaCreateEntityStageTest extends TestWithCayenneMapping {

    private SenchaCreateResourceEntityStage createEntityStage;
    private IAgRequestBuilderFactory requestBuilderFactory;

    @Before
    public void before() {

        IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare entity creation stage
        ICayenneExpMerger expConstructor = new CayenneExpMerger(new ExpressionParser(), new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger(mock(IMetadataService.class));
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(mock(IMetadataService.class), expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        ISenchaFilterExpressionCompiler senchaFilterProcessor = new SenchaFilterExpressionCompiler(pathCache, new ExpressionPostProcessor(pathCache));

        this.createEntityStage = new SenchaCreateResourceEntityStage(
                metadataService,
                expConstructor,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor,
                senchaFilterProcessor);

        this.requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(ICayenneExpParser.class),
                mock(ISortParser.class),
                mock(IIncludeParser.class),
                mock(IExcludeParser.class)
        );
    }

    @Test
    public void testSelectRequest_Filter() {
        SelectContext<E2> context = new SelectContext<>(E2.class);

        Filter filter = new Filter("name", "xyz", "like", false, false);
        SenchaRequest.set(context, SenchaRequest.builder().filters(Collections.singletonList(filter)).build());

        createEntityStage.doExecute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity.getQualifier());
        assertEquals(exp("name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
    }


    @Test
    public void testSelectRequest_Filter_CayenneExp() {
        SelectContext<E2> context = new SelectContext<>(E2.class);

        CayenneExp cayenneExp = new CayenneExp("address = '1 Main Street'");
        context.setMergedRequest(requestBuilderFactory.builder().cayenneExp(cayenneExp).build());

        Filter filter = new Filter("name", "xyz", "like", false, false);
        SenchaRequest.set(context, SenchaRequest.builder().filters(Collections.singletonList(filter)).build());

        createEntityStage.doExecute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

        assertNotNull(resourceEntity.getQualifier());
        assertEquals(exp("address = '1 Main Street' and name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
    }
}
