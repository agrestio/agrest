package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.ResourceEntity;
import io.agrest.it.fixture.pojo.model.P1;
import io.agrest.it.fixture.pojo.model.P2;
import io.agrest.meta.compiler.CayenneEntityCompiler;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.protocol.Include;
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
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateEntityStage_WithPojoTest extends TestWithCayenneMapping {

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

	@Override
	protected IMetadataService createMetadataService() {

		List<AgEntityCompiler> compilers = new ArrayList<>();
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));
		compilers.add(new CayenneEntityCompiler(mockCayennePersister, Collections.emptyMap(), converterFactory));

		return new MetadataService(compilers);
	}

	@Test
	public void testSelectRequest_Default() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<P1, ?> context = prepareContext(params, P1.class);

		context.setRawRequest(AgRequest.builder().build());
		createEntityStage.execute(context);

        ResourceEntity<P1, ?> ce1 = context.getEntity();


		assertNotNull(ce1);
		assertTrue(ce1.isIdIncluded());
		assertEquals(1, ce1.getAttributes().size());
		assertTrue(ce1.getChildren().isEmpty());


        SelectContext<P2, ?> context2 = prepareContext(params, P2.class);

		context2.setRawRequest(AgRequest.builder().build());
		createEntityStage.execute(context2);

        ResourceEntity<P2, ?> ce2 = context2.getEntity();

		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(0, ce2.getChildren().size());
	}

	@Test
	public void testSelectRequest_IncludeRels() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("p1"));


		SelectContext<P2, ?> context2 = new SelectContext<>(P2.class);

		Include include = new Include("p1");
		context2.setRawRequest(AgRequest.builder().includes(Collections.singletonList(include)).build());


		createEntityStage.execute(context2);

        ResourceEntity<P2, ?> ce2 = context2.getEntity();

		assertNotNull(ce2);
		assertTrue(ce2.isIdIncluded());
		assertEquals(1, ce2.getAttributes().size());
		assertEquals(1, ce2.getChildren().size());

		assertTrue(ce2.getChildren().keySet().contains("p1"));

	}

}
