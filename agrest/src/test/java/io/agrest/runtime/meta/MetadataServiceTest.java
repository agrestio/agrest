package io.agrest.runtime.meta;

import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.meta.AgEntity;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetadataServiceTest extends TestWithCayenneMapping {

	@Test
	public void testGetAgEntity_NoRelationships() {

		AgEntity<E4> e4 = metadataService.getAgEntity(E4.class);
		assertNotNull(e4);
		assertEquals("E4", e4.getName());
		assertSame(E4.class, e4.getType());

		assertEquals("E4", e4.getName());

		assertEquals(0, e4.getRelationships().size());

		assertNotNull(e4.getAttribute(E4.C_BOOLEAN.getName()));
		assertNotNull(e4.getAttribute(E4.C_DATE.getName()));
	}

	@Test
	public void testGetAgEntity_Relationships() {

		AgEntity<E5> e5 = metadataService.getAgEntity(E5.class);
		assertNotNull(e5);
		assertEquals("E5", e5.getName());
		assertSame(E5.class, e5.getType());

		assertEquals("E5", e5.getName());

		assertEquals(2, e5.getRelationships().size());

		assertNotNull(e5.getAttribute(E5.NAME.getName()));
		assertNotNull(e5.getAttribute(E5.DATE.getName()));
		assertNotNull(e5.getRelationship(E5.E2S.getName()));
	}
}
