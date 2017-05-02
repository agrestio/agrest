package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class MetadataServiceTest extends TestWithCayenneMapping {

	@Test
	public void testGetLrEntity_NoRelationships() {

		LrPersistentEntity<E4> e4 = (LrPersistentEntity<E4>) metadataService.getLrEntity(E4.class);
		assertNotNull(e4);
		assertEquals("E4", e4.getName());
		assertSame(E4.class, e4.getType());

		assertEquals("E4", e4.getName());

		assertEquals(7, e4.getPersistentAttributes().size());
		assertEquals(0, e4.getRelationships().size());

		assertNotNull(e4.getPersistentAttribute(E4.C_BOOLEAN.getName()));
		assertNotNull(e4.getPersistentAttribute(E4.C_DATE.getName()));
	}

	@Test
	public void testGetLrEntity_Relationships() {

		LrPersistentEntity<E5> e5 = (LrPersistentEntity<E5>) metadataService.getLrEntity(E5.class);
		assertNotNull(e5);
		assertEquals("E5", e5.getName());
		assertSame(E5.class, e5.getType());

		assertEquals("E5", e5.getName());

		assertEquals(2, e5.getPersistentAttributes().size());
		assertEquals(2, e5.getRelationships().size());

		assertNotNull(e5.getPersistentAttribute(E5.NAME.getName()));
		assertNotNull(e5.getPersistentAttribute(E5.DATE.getName()));
		assertNotNull(e5.getRelationship(E5.E2S.getName()));
	}
}
