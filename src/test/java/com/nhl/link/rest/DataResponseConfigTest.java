package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E2;

public class DataResponseConfigTest extends TestWithCayenneMapping {

	@Test
	public void testDeepCopy() {

		DataResponseConfig c = new DataResponseConfig(getEntity(E2.class));
		c.fetchLimit(5);
		c.fetchOffset(6);

		c.getEntity().attributes(E2.ADDRESS, E2.NAME);

		DataResponseConfig cCopy = c.deepCopy();
		assertNotSame(c, cCopy);
		assertEquals(5, cCopy.getFetchLimit());
		assertEquals(6, cCopy.getFetchOffset());

		EntityConfig ecCopy = cCopy.getEntity();
		assertNotSame(c.getEntity(), ecCopy);
		assertSame(c.getEntity().entity, ecCopy.entity);
	}

}
