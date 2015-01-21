package com.nhl.link.rest.runtime.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DataMap;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.unit.TestWithCayenneMapping;

public class MetadataServiceTest extends TestWithCayenneMapping {

	private MetadataService metadataService;

	@Before
	public void before() {
		ObjectContext sharedContext = runtime.newContext();

		ICayennePersister cayenneService = mock(ICayennePersister.class);
		when(cayenneService.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
		when(cayenneService.sharedContext()).thenReturn(sharedContext);
		when(cayenneService.newContext()).thenReturn(runtime.newContext());

		this.metadataService = new MetadataService(Collections.<DataMap> emptyList(),
				Collections.<String, LrEntityOverlay<?>> emptyMap(), cayenneService);
	}

	@Test
	public void testGetLrEntity_NoRelationships() {

		LrEntity<E4> e4 = metadataService.getLrEntity(E4.class);
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
	public void testGetLrEntity_Relationships() {

		LrEntity<E5> e5 = metadataService.getLrEntity(E5.class);
		assertNotNull(e5);
		assertEquals("E5", e5.getName());
		assertSame(E5.class, e5.getType());

		assertNotNull(e5.getObjEntity());
		assertEquals("E5", e5.getObjEntity().getName());

		assertEquals(2, e5.getPersistentAttributes().size());
		assertEquals(1, e5.getRelationships().size());

		assertNotNull(e5.getPersistentAttribute(E5.NAME.getName()));
		assertNotNull(e5.getPersistentAttribute(E5.DATE.getName()));
		assertNotNull(e5.getRelationship(E5.E2S.getName()));
	}
}
