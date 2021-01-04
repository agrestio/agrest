package io.agrest.sencha.runtime.processor.select;

import io.agrest.base.protocol.Exp;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.CayenneNoDbTest;
import io.agrest.runtime.entity.*;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IExpParser;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class SenchaCreateEntityStageTest extends CayenneNoDbTest {

    private SenchaCreateResourceEntityStage createEntityStage;
    private IAgRequestBuilderFactory requestBuilderFactory;

    @BeforeEach
    public void before() {

        // prepare entity creation stage
        IExpMerger expConstructor = new ExpMerger();
        ISortMerger sortConstructor = new SortMerger();
        IMapByMerger mapByConstructor = new MapByMerger(dataMap);
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(dataMap, expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        ISenchaFilterExpressionCompiler senchaFilterProcessor = new SenchaFilterExpressionCompiler();

        this.createEntityStage = new SenchaCreateResourceEntityStage(
                dataMap,
                expConstructor,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor,
                senchaFilterProcessor);

        this.requestBuilderFactory = new DefaultRequestBuilderFactory(
                mock(IExpParser.class),
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
        assertEquals(Exp.simple("name likeIgnoreCase 'xyz%'"), context.getEntity().getQualifier());
    }

    @Test
    public void testSelectRequest_Filter_Exp() {
        SelectContext<E2> context = new SelectContext<>(E2.class);

        Exp exp = Exp.simple("address = '1 Main Street'");
        context.setMergedRequest(requestBuilderFactory.builder().exp(exp).build());

        Filter filter = new Filter("name", "xyz", "like", false, false);
        SenchaRequest.set(context, SenchaRequest.builder().filters(Collections.singletonList(filter)).build());

        createEntityStage.doExecute(context);

        assertEquals(exp.and(Exp.simple("name likeIgnoreCase 'xyz%'")), context.getEntity().getQualifier());
    }
}
