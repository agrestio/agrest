package com.nhl.link.rest.sencha.runtime.processor.select;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.entity.CayenneExpMerger;
import com.nhl.link.rest.runtime.entity.ExcludeMerger;
import com.nhl.link.rest.runtime.entity.ExpressionPostProcessor;
import com.nhl.link.rest.runtime.entity.ICayenneExpMerger;
import com.nhl.link.rest.runtime.entity.IExcludeMerger;
import com.nhl.link.rest.runtime.entity.IIncludeMerger;
import com.nhl.link.rest.runtime.entity.IMapByMerger;
import com.nhl.link.rest.runtime.entity.ISizeMerger;
import com.nhl.link.rest.runtime.entity.ISortMerger;
import com.nhl.link.rest.runtime.entity.IncludeMerger;
import com.nhl.link.rest.runtime.entity.MapByMerger;
import com.nhl.link.rest.runtime.entity.SizeMerger;
import com.nhl.link.rest.runtime.entity.SortMerger;
import com.nhl.link.rest.runtime.path.IPathDescriptorManager;
import com.nhl.link.rest.runtime.path.PathDescriptorManager;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.SenchaRequest;
import com.nhl.link.rest.sencha.protocol.Filter;
import com.nhl.link.rest.sencha.runtime.entity.ISenchaFilterExpressionCompiler;
import com.nhl.link.rest.sencha.runtime.entity.SenchaFilterExpressionCompiler;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
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
		context.setRawRequest(LrRequest.builder().cayenneExp(cayenneExp).build());

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
		context.setRawRequest(LrRequest.builder().sort(sort).build());

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
