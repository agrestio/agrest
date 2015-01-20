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
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class ConstraintsHandlerWithDefaultsTest extends TestWithCayenneMapping {

	private ConstraintsHandler constraintHandler;
	private ObjEntity e1;
	private ObjEntity e2;

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

		e1 = runtime.getChannel().getEntityResolver().getObjEntity(E1.class);
		e2 = runtime.getChannel().getEntityResolver().getObjEntity(E2.class);
	}

	@Test
	public void testConstrainResponse_PerRequest() {

		TreeConstraints<E1> tc1 = TreeConstraints.excludeAll(E1.class).attributes(E1.DESCRIPTION);

		ResourceEntity<E1> te1 = new ResourceEntity<>(E1.class, e1);
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

		ResourceEntity<E1> te1 = new ResourceEntity<>(E1.class, e1);
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

		ResourceEntity<E2> te1 = new ResourceEntity<>(E2.class, e2);
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
