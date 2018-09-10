package io.agrest.runtime.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Test;

public class MetadataServiceTest extends TestWithCayenneMapping {

	@Test
	public void testGetAgEntity_NoRelationships() {

		AgPersistentEntity<E4> e4 = (AgPersistentEntity<E4>) metadataService.getAgEntity(E4.class);
		assertNotNull(e4);
		assertEquals("E4", e4.getName());
		assertSame(E4.class, e4.getType());

		assertNotNull(e4.getObjEntity());
		assertEquals("E4", e4.getObjEntity().getName());

		assertEquals(7, e4.getPersistentAttributes().size());
		assertEquals(0, e4.getRelationships().size());

		assertNotNull(e4.getPersistentAttribute(E4.C_BOOLEAN.getName()));
		assertNotNull(e4.getPersistentAttribute(E4.C_DATE.getName()));
	}

	@Test
	public void testGetAgEntity_Relationships() {

		AgPersistentEntity<E5> e5 = (AgPersistentEntity<E5>) metadataService.getAgEntity(E5.class);
		assertNotNull(e5);
		assertEquals("E5", e5.getName());
		assertSame(E5.class, e5.getType());

		assertNotNull(e5.getObjEntity());
		assertEquals("E5", e5.getObjEntity().getName());

		assertEquals(2, e5.getPersistentAttributes().size());
		assertEquals(2, e5.getRelationships().size());

		assertNotNull(e5.getPersistentAttribute(E5.NAME.getName()));
		assertNotNull(e5.getPersistentAttribute(E5.DATE.getName()));
		assertNotNull(e5.getRelationship(E5.E2S.getName()));
	}
}
