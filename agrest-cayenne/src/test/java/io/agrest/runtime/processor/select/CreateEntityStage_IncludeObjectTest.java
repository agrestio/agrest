package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.protocol.Include;
import io.agrest.protocol.MapBy;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.entity.ExcludeMerger;
import io.agrest.runtime.entity.ExpressionPostProcessor;
import io.agrest.runtime.entity.IAgExpMerger;
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
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateEntityStage_IncludeObjectTest extends TestWithCayenneMapping {

    private CreateResourceEntityStage createEntityStage;

	@Before
	public void setUp() {

		IPathDescriptorManager pathCache = new PathDescriptorManager();

        // prepare create entity stage
        IAgExpMerger expConstructor = new CayenneExpMerger(new ExpressionPostProcessor(pathCache));
        ISortMerger sortConstructor = new SortMerger(pathCache);
        IMapByMerger mapByConstructor = new MapByMerger();
        ISizeMerger sizeConstructor = new SizeMerger();
        IIncludeMerger includeConstructor = new IncludeMerger(expConstructor, sortConstructor, mapByConstructor, sizeConstructor);
        IExcludeMerger excludeConstructor = new ExcludeMerger();

        this.createEntityStage
                = new CreateResourceEntityStage(
                createMetadataService(),
                expConstructor ,
                sortConstructor,
                mapByConstructor,
                sizeConstructor,
                includeConstructor,
                excludeConstructor);
    }

	@Test
	public void testToDataRequest_IncludeObject_Path() {

        SelectContext<E2, ?> context = new SelectContext<>(E2.class);

        Include include = new Include("e3s");
        context.setRawRequest(AgRequest.builder().includes(Collections.singletonList(include)).build());

        createEntityStage.execute(context);

        ResourceEntity<E2, ?> resourceEntity = context.getEntity();

		assertNotNull(resourceEntity);
		assertTrue(resourceEntity.isIdIncluded());

		assertEquals(1, resourceEntity.getChildren().size());
		assertTrue(resourceEntity.getChildren().containsKey(E2.E3S.getName()));
	}

	@Test
	public void testToDataRequest_IncludeObject_MapBy() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\",\"mapBy\":\"e5\"}"));

        SelectContext<E2, ?> context = new SelectContext<>(E2.class);

        Include include = new Include(null, null, new MapBy("e5"), "e3s", null, null, null);
        context.setRawRequest(AgRequest.builder().includes(Collections.singletonList(include)).build());

        createEntityStage.execute(context);

        ResourceEntity<E2, ?> resourceEntity = context.getEntity();


		assertNotNull(resourceEntity);

		ResourceEntity<?, ?> reMapBy = resourceEntity.getChildren().get(E2.E3S.getName()).getMapBy();
		assertNotNull(reMapBy);
		assertNotNull(reMapBy.getChildren().get(E3.E5.getName()));
	}
}
