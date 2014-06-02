package com.nhl.link.rest;

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
}
