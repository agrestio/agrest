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

import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class ConstraintsHandlerWithDefaultsTest extends TestWithCayenneMapping {

	private ConstraintsHandler constraintHandler;
	private LrPersistentEntity<E1> lre1;
	private LrPersistentEntity<E2> lre2;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {

		List<EntityConstraint> r = new ArrayList<>();
		r.add(new DefaultEntityConstraint("E1", true, false, Collections.singleton(E1.AGE.getName()),
				Collections.<String> emptySet()));

		List<EntityConstraint> w = new ArrayList<>();
		w.add(new DefaultEntityConstraint("E2", false, false, Collections.singleton(E2.ADDRESS.getName()),
				Collections.<String> emptySet()));

		ObjEntity e1 = runtime.getChannel().getEntityResolver().getObjEntity(E1.class);
		ObjEntity e2 = runtime.getChannel().getEntityResolver().getObjEntity(E2.class);

		lre1 = mock(LrPersistentEntity.class);
		when(lre1.getObjEntity()).thenReturn(e1);
		when(lre1.getType()).thenReturn(E1.class);
		when(lre1.getName()).thenReturn(e1.getName());

		lre2 = mock(LrPersistentEntity.class);
		when(lre2.getObjEntity()).thenReturn(e2);
		when(lre2.getType()).thenReturn(E2.class);
		when(lre2.getName()).thenReturn(e2.getName());

		this.constraintHandler = new ConstraintsHandler(r, w);
	}

	@Test
	public void testConstrainResponse_PerRequest() {

		ConstraintsBuilder<E1> tc1 = ConstraintsBuilder.excludeAll(E1.class).attributes(E1.DESCRIPTION);

		ResourceEntity<E1> te1 = new ResourceEntity<>(lre1);
		appendAttribute(te1, E1.AGE, Integer.class);
		appendAttribute(te1, E1.DESCRIPTION, String.class);

		constraintHandler.constrainResponse(te1, null, tc1);
		assertEquals(1, te1.getAttributes().size());
		assertTrue(te1.getAttributes().containsKey(E1.DESCRIPTION.getName()));
		assertTrue(te1.getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_Default() {

		ResourceEntity<E1> te1 = new ResourceEntity<>(lre1);
		appendAttribute(te1, E1.AGE, Integer.class);
		appendAttribute(te1, E1.DESCRIPTION, String.class);

		constraintHandler.constrainResponse(te1, null, null);
		assertEquals(1, te1.getAttributes().size());
		assertTrue(te1.getAttributes().containsKey(E1.AGE.getName()));
		assertTrue(te1.getChildren().isEmpty());
	}

	@Test
	public void testConstrainResponse_None() {

		ResourceEntity<E2> te1 = new ResourceEntity<>(lre2);
		appendAttribute(te1, E2.ADDRESS, String.class);
		appendAttribute(te1, E2.NAME, String.class);

		constraintHandler.constrainResponse(te1, null, null);
		assertEquals(2, te1.getAttributes().size());
		assertTrue(te1.getAttributes().containsKey(E2.ADDRESS.getName()));
		assertTrue(te1.getAttributes().containsKey(E2.NAME.getName()));

		assertTrue(te1.getChildren().isEmpty());
	}

}
