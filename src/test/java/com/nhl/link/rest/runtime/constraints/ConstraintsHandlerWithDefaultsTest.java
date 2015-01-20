package com.nhl.link.rest.runtime.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class ConstraintsHandlerWithDefaultsTest extends TestWithCayenneMapping {

	private ConstraintsHandler constraintHandler;
	private LrEntity<E1> lre1;
	private LrEntity<E2> lre2;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {

		EntityResolver resolver = runtime.getChannel().getEntityResolver();
		ICayennePersister cayenneService = mock(ICayennePersister.class);
		when(cayenneService.entityResolver()).thenReturn(resolver);
		IMetadataService metadataService = new MetadataService(Collections.<DataMap> emptyList(), cayenneService);

		List<EntityConstraint> r = new ArrayList<>();
		r.add(new DefaultEntityConstraint("E1", true, false, Collections.singleton(E1.AGE.getName()), Collections
				.<String> emptySet()));

		List<EntityConstraint> w = new ArrayList<>();
		w.add(new DefaultEntityConstraint("E2", false, false, Collections.singleton(E2.ADDRESS.getName()), Collections
				.<String> emptySet()));

		this.constraintHandler = new ConstraintsHandler(r, w, metadataService);

		ObjEntity e1 = runtime.getChannel().getEntityResolver().getObjEntity(E1.class);
		ObjEntity e2 = runtime.getChannel().getEntityResolver().getObjEntity(E2.class);
		
		lre1 = mock(LrEntity.class);
		when(lre1.getObjEntity()).thenReturn(e1);
		when(lre1.getType()).thenReturn(E1.class);
		when(lre1.getName()).thenReturn(e1.getName());

		lre2 = mock(LrEntity.class);
		when(lre2.getObjEntity()).thenReturn(e2);
		when(lre2.getType()).thenReturn(E2.class);
		when(lre2.getName()).thenReturn(e2.getName());
	}

	@Test
	public void testConstrainResponse_PerRequest() {

		TreeConstraints<E1> tc1 = TreeConstraints.excludeAll(E1.class).attributes(E1.DESCRIPTION);

		ResourceEntity<E1> te1 = new ResourceEntity<>(lre1);
		te1.getAttributes().add(E1.AGE.getName());
		te1.getAttributes().add(E1.DESCRIPTION.getName());

		DataResponse<E1> t1 = DataResponse.forType(E1.class).withClientEntity(te1);

		constraintHandler.constrainResponse(t1, null, tc1);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().contains(E1.DESCRIPTION.getName()));
		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_Default() {

		ResourceEntity<E1> te1 = new ResourceEntity<>(lre1);
		te1.getAttributes().add(E1.AGE.getName());
		te1.getAttributes().add(E1.DESCRIPTION.getName());

		DataResponse<E1> t1 = DataResponse.forType(E1.class).withClientEntity(te1);

		constraintHandler.constrainResponse(t1, null, null);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().contains(E1.AGE.getName()));
		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_None() {

		ResourceEntity<E2> te1 = new ResourceEntity<>(lre2);
		te1.getAttributes().add(E2.ADDRESS.getName());
		te1.getAttributes().add(E2.NAME.getName());

		DataResponse<E2> t1 = DataResponse.forType(E2.class).withClientEntity(te1);

		constraintHandler.constrainResponse(t1, null, null);
		assertEquals(2, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().contains(E2.ADDRESS.getName()));
		assertTrue(t1.getEntity().getAttributes().contains(E2.NAME.getName()));
		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

}
