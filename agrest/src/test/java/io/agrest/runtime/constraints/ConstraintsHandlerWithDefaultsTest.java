package io.agrest.runtime.constraints;

import io.agrest.EntityConstraint;
import io.agrest.ResourceEntity;
import io.agrest.constraints.Constraint;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.meta.AgEntity;
import io.agrest.unit.TestWithCayenneMapping;
import org.apache.cayenne.map.ObjEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConstraintsHandlerWithDefaultsTest extends TestWithCayenneMapping {

	private ConstraintsHandler constraintHandler;
	private AgEntity<E1> age1;
	private AgEntity<E2> age2;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {

		List<EntityConstraint> r = new ArrayList<>();
		r.add(new DefaultEntityConstraint("E1", true, false, Collections.singleton(E1.AGE.getName()),
				Collections.emptySet()));

		List<EntityConstraint> w = new ArrayList<>();
		w.add(new DefaultEntityConstraint("E2", false, false, Collections.singleton(E2.ADDRESS.getName()),
				Collections.emptySet()));

		ObjEntity e1 = runtime.getChannel().getEntityResolver().getObjEntity(E1.class);
		ObjEntity e2 = runtime.getChannel().getEntityResolver().getObjEntity(E2.class);

		age1 = mock(AgEntity.class);
		when(age1.getType()).thenReturn(E1.class);
		when(age1.getName()).thenReturn(e1.getName());

		age2 = mock(AgEntity.class);
		when(age2.getType()).thenReturn(E2.class);
		when(age2.getName()).thenReturn(e2.getName());

		this.constraintHandler = new ConstraintsHandler(r, w);
	}

	@Test
	public void testConstrainResponse_PerRequest() {

		Constraint<E1> tc1 = Constraint.excludeAll(E1.class).attributes(E1.DESCRIPTION);

		ResourceEntity<E1> te1 = new ResourceEntity<>(age1, null);
		appendAttribute(te1, E1.AGE, Integer.class);
		appendAttribute(te1, E1.DESCRIPTION, String.class);

		constraintHandler.constrainResponse(te1, null, tc1);
		assertEquals(1, te1.getAttributes().size());
		assertTrue(te1.getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(te1.getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_Default() {

		ResourceEntity<E1> te1 = new ResourceEntity<>(age1, null);
		appendAttribute(te1, E1.AGE, Integer.class);
		appendAttribute(te1, E1.DESCRIPTION, String.class);

		constraintHandler.constrainResponse(te1, null, null);
		assertEquals(1, te1.getAttributes().size());
		assertTrue(te1.getAttributes().containsKey(E1.AGE.getName()));
		assertTrue(te1.getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_None() {

		ResourceEntity<E2> te1 = new ResourceEntity<>(age2, null);
		appendAttribute(te1, E2.ADDRESS, String.class);
		appendAttribute(te1, E2.NAME, String.class);

		constraintHandler.constrainResponse(te1, null, null);
		assertEquals(2, te1.getAttributes().size());
		assertTrue(te1.getAttributes().containsKey(E2.ADDRESS.getName()));
		assertTrue(te1.getAttributes().containsKey(E2.NAME.getName()));

		assertTrue(te1.getChildren().isEmpty());
	}

}
