package com.nhl.link.rest.runtime.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.map.ObjEntity;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.runtime.constraints.ConstraintsHandler;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E1;
import com.nhl.link.rest.unit.cayenne.E2;

public class ConstraintsHandlerWithDefaultsTest extends TestWithCayenneMapping {

	private ConstraintsHandler constraintHandler;
	private ObjEntity e1;
	private ObjEntity e2;

	@Before
	public void before() {
		Map<String, TreeConstraints<?>> r = new HashMap<>();
		r.put(E1.class.getName(), TreeConstraints.idOnly(E1.class).attribute(E1.AGE));

		Map<String, TreeConstraints<?>> w = new HashMap<>();
		w.put(E2.class.getName(), TreeConstraints.idOnly(E2.class).attribute(E2.ADDRESS));

		this.constraintHandler = new ConstraintsHandler(r, w);

		e1 = runtime.getChannel().getEntityResolver().getObjEntity(E1.class);
		e2 = runtime.getChannel().getEntityResolver().getObjEntity(E2.class);
	}

	@Test
	public void testConstrainResponse_PerRequest() {

		TreeConstraints<E1> tc1 = TreeConstraints.excludeAll(E1.class).attributes(E1.DESCRIPTION);

		Entity<E1> te1 = new Entity<>(E1.class, e1);
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

		Entity<E1> te1 = new Entity<>(E1.class, e1);
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

		Entity<E2> te1 = new Entity<>(E2.class, e2);
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
