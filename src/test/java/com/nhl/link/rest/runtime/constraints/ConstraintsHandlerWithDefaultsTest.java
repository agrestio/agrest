package com.nhl.link.rest.runtime.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class ConstraintsHandlerWithDefaultsTest extends TestWithCayenneMapping {

	private ConstraintsHandler constraintHandler;
	private LrEntity<E1> lre1;
	private LrEntity<E2> lre2;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {

		List<EntityConstraint> r = new ArrayList<>();
		r.add(new DefaultEntityConstraint("E1", true, false, Collections.singleton(E1.AGE.getName()), Collections
				.<String> emptySet()));

		List<EntityConstraint> w = new ArrayList<>();
		w.add(new DefaultEntityConstraint("E2", false, false, Collections.singleton(E2.ADDRESS.getName()), Collections
				.<String> emptySet()));

		this.constraintHandler = new ConstraintsHandler(r, w);

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
		appendAttribute(te1, E1.AGE, Integer.class);
		appendAttribute(te1, E1.DESCRIPTION, String.class);

		DataResponse<E1> t1 = DataResponse.forType(E1.class).resourceEntity(te1);

		constraintHandler.constrainResponse(t1, null, tc1);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_Default() {

		ResourceEntity<E1> te1 = new ResourceEntity<>(lre1);
		appendAttribute(te1, E1.AGE, Integer.class);
		appendAttribute(te1, E1.DESCRIPTION, String.class);

		DataResponse<E1> t1 = DataResponse.forType(E1.class).resourceEntity(te1);

		constraintHandler.constrainResponse(t1, null, null);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().containsKey(E1.AGE.getName()));
		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_None() {

		ResourceEntity<E2> te1 = new ResourceEntity<>(lre2);
		appendAttribute(te1, E2.ADDRESS, String.class);
		appendAttribute(te1, E2.NAME, String.class);

		DataResponse<E2> t1 = DataResponse.forType(E2.class).resourceEntity(te1);

		constraintHandler.constrainResponse(t1, null, null);
		assertEquals(2, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().containsKey(E2.ADDRESS.getName()));
		assertTrue(t1.getEntity().getAttributes().containsKey(E2.NAME.getName()));

		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

}
