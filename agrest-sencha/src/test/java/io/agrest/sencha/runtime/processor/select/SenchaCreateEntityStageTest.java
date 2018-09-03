package io.agrest.sencha.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Dir;
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
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.sencha.SenchaRequest;
import io.agrest.sencha.protocol.Filter;
import io.agrest.sencha.runtime.entity.ISenchaFilterExpressionCompiler;
import io.agrest.sencha.runtime.entity.SenchaFilterExpressionCompiler;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SenchaCreateEntityStageTest extends TestWithCayenneMapping {

    private SenchaCreateResourceEntityStage createEntityStage;

	@Before
	public void before() {

		IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare entity creation stage
        ICayenneExpMerger expConstructor = new CayenneExpMerger(new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger();
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

		ISenchaFilterExpressionCompiler senchaFilterProcessor = new SenchaFilterExpressionCompiler(pathCache, new ExpressionPostProcessor(pathCache));

        this.createEntityStage
                = new SenchaCreateResourceEntityStage(
                        createMetadataService(),
                        expConstructor ,
                        sortConstructor,
                        mapByConstructor,
                        sizeConstructor,
                        includeConstructor,
                        excludeConstructor,
                        senchaFilterProcessor);
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
		context.setRawRequest(AgRequest.builder().cayenneExp(cayenneExp).build());

		Filter filter = new Filter("name", "xyz", "like", false, false);
		SenchaRequest.set(context, SenchaRequest.builder().filters(Collections.singletonList(filter)).build());

		createEntityStage.doExecute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity.getQualifier());
		assertEquals(exp("address = '1 Main Street' and name likeIgnoreCase 'xyz%'"), resourceEntity.getQualifier());
	}

	@Test
	public void testSelectRequest_Sort_Group() {
		SelectContext<E2> context = new SelectContext<>(E2.class);

		Sort sort = new Sort(Arrays.asList(
				new Sort("name", Dir.DESC),
				new Sort("address", Dir.ASC)));
		context.setRawRequest(AgRequest.builder().sort(sort).build());

		Sort group = new Sort(Arrays.asList(
				new Sort("id", Dir.DESC),
				new Sort("address", Dir.ASC)));
		SenchaRequest.set(context, SenchaRequest.builder().group(group).build());


		createEntityStage.doExecute(context);

        ResourceEntity<E2> resourceEntity = context.getEntity();

		assertEquals(3, resourceEntity.getOrderings().size());
		Iterator<Ordering> it = resourceEntity.getOrderings().iterator();
		Ordering o1 = it.next();
		Ordering o2 = it.next();
		Ordering o3 = it.next();

		assertEquals(SortOrder.DESCENDING, o1.getSortOrder());
		assertEquals("db:id", o1.getSortSpecString());
		assertEquals(SortOrder.ASCENDING, o2.getSortOrder());
		assertEquals("address", o2.getSortSpecString());
		assertEquals(SortOrder.DESCENDING, o3.getSortOrder());
		assertEquals("name", o3.getSortSpecString());
	}
}
