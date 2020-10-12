package io.agrest.sencha.runtime.processor.select;

import io.agrest.base.protocol.CayenneExp;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.runtime.entity.*;
import io.agrest.runtime.meta.IMetadataService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class SenchaCreateEntityStageTest extends CayenneNoDbTest {

    private SenchaCreateResourceEntityStage createEntityStage;
    private IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeEach
    public void before() {

        // prepare entity creation stage
        ICayenneExpMerger expConstructor = new CayenneExpMerger();
        ISortMerger sortConstructor = new SortMerger();
        IMapByMerger mapByConstructor = new MapByMerger(mock(IMetadataService.class));
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(mock(IMetadataService.class), expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        ISenchaFilterExpressionCompiler senchaFilterProcessor = new SenchaFilterExpressionCompiler();

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

        List<CayenneExp> qualifiers = context.getEntity().getQualifiers();
        assertEquals(1, qualifiers.size());
        assertEquals(new CayenneExp("name likeIgnoreCase 'xyz%'"), qualifiers.get(0));
    }

    @Test
    public void testSelectRequest_Filter_CayenneExp() {
        SelectContext<E2> context = new SelectContext<>(E2.class);

        CayenneExp cayenneExp = new CayenneExp("address = '1 Main Street'");
        context.setMergedRequest(requestBuilderFactory.builder().cayenneExp(cayenneExp).build());

        Filter filter = new Filter("name", "xyz", "like", false, false);
        SenchaRequest.set(context, SenchaRequest.builder().filters(Collections.singletonList(filter)).build());

        createEntityStage.doExecute(context);

        List<CayenneExp> qualifiers = context.getEntity().getQualifiers();
        assertEquals(2, qualifiers.size());
        assertEquals(cayenneExp, qualifiers.get(0));
        assertEquals(new CayenneExp("name likeIgnoreCase 'xyz%'"), qualifiers.get(1));
    }
}
