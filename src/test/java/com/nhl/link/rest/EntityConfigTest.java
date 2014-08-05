package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.cayenne.E5;

public class EntityConfigTest extends TestWithCayenneMapping {

	@Test
	public void testPath() {
		EntityConfig config0 = new EntityConfig(getEntity(E2.class));
		EntityConfig config0_ = config0.path(E2.ADDRESS);
		EntityConfig config02 = config0.path(E2.E3S.dot(E3.E5.dot(E5.DATE)));
		EntityConfig config01 = config0.path(E2.E3S);

		assertSame(config0, config0_);

		assertNotSame(config0, config02);
		assertNotSame(config0, config01);

		assertSame(config02, config01.path(E3.E5));
		assertTrue(config02.getAttributes().contains(E5.DATE.getName()));
	}

	@Test
	public void testDeepCopy() {

		EntityConfig e = new EntityConfig(getEntity(E2.class));

		e.attributes(E2.ADDRESS, E2.NAME);

		EntityConfig ecCopy = e.deepCopy();
		assertNotSame(e, ecCopy);
		assertSame(e.entity, ecCopy.entity);

		assertEquals(2, ecCopy.getAttributes().size());
		assertTrue(ecCopy.getAttributes().contains("address"));
		assertTrue(ecCopy.getAttributes().contains("name"));
	}
}
