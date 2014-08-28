package com.nhl.link.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.map.ObjEntity;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E4;

public class TreeConstraintsTest extends TestWithCayenneMapping {

	private ObjEntity e4e;
	private DataResponse<E4> e4r;

	@Before
	public void before() {
		e4e = runtime.getChannel().getEntityResolver().getObjEntity(E4.class);
		Entity<E4> e4lre = new Entity<>(E4.class, e4e);
		e4r = new DataResponse<>(E4.class).withClientEntity(e4lre);
	}

	@Test
	public void testExcludeAll() {

		TreeConstraints<E4> tc = TreeConstraints.excludeAll(E4.class);
		ImmutableTreeConstraints itc = tc.build(e4r);
		assertFalse(itc.isIdIncluded());

		for (String a : e4e.getAttributeMap().keySet()) {
			assertFalse(itc.hasAttribute(a));
		}

		for (String r : e4e.getRelationshipMap().keySet()) {
			assertFalse(itc.hasChild(r));
		}
	}
	
	@Test
	public void testIdOnly() {

		TreeConstraints<E4> tc = TreeConstraints.idOnly(E4.class);
		ImmutableTreeConstraints itc = tc.build(e4r);
		assertTrue(itc.isIdIncluded());

		for (String a : e4e.getAttributeMap().keySet()) {
			assertFalse(itc.hasAttribute(a));
		}

		for (String r : e4e.getRelationshipMap().keySet()) {
			assertFalse(itc.hasChild(r));
		}
	}
	
	@Test
	public void testIdAndAttributes() {

		TreeConstraints<E4> tc = TreeConstraints.idAndAttributes(E4.class);
		ImmutableTreeConstraints itc = tc.build(e4r);
		assertTrue(itc.isIdIncluded());

		for (String a : e4e.getAttributeMap().keySet()) {
			assertTrue(itc.hasAttribute(a));
		}

		for (String r : e4e.getRelationshipMap().keySet()) {
			assertFalse(itc.hasChild(r));
		}
	}
}
