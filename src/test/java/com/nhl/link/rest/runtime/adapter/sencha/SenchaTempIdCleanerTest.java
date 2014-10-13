package com.nhl.link.rest.runtime.adapter.sencha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.UpdateResponse;

public class SenchaTempIdCleanerTest {

	@Test
	public void testDashId() {

		EntityUpdate u1 = new EntityUpdate();
		u1.getOrCreateId().put("x", 1);
		EntityUpdate u2 = new EntityUpdate();
		u2.getOrCreateId().put("x", "My-123");
		
		EntityUpdate u3 = new EntityUpdate();
		u3.getOrCreateId().put("x", "My-My");

		UpdateResponse<Object> r = new UpdateResponse<>(Object.class);
		r.getUpdates().add(u1);
		r.getUpdates().add(u2);

		SenchaTempIdCleaner cleaner = SenchaTempIdCleaner.dashId();
		UpdateResponse<Object> ro = cleaner.afterParse(r);
		assertSame(r, ro);

		assertEquals(1, u1.getId().get("x"));
		assertNull(u2.getId().get("x"));
		assertEquals("My-My", u3.getId().get("x"));
	}
}
