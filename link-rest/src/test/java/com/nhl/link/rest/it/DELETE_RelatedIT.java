package com.nhl.link.rest.it;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E8Resource;

public class DELETE_RelatedIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
		context.register(E8Resource.class);
	}

	@Test
	public void testDelete_All_ToMany() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		insert("e8", "id, name", "1, 'xxx'");
		insert("e8", "id, name", "2, 'yyy'");
		insert("e7", "id, e8_id, name", "7, 2, 'zzz'");
		insert("e7", "id, e8_id, name", "8, 1, 'yyy'");
		insert("e7", "id, e8_id, name", "9, 1, 'zzz'");

		assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e7 WHERE e8_id = 1"));
		Response r1 = target("/e8/1/e7s").request().delete();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true}", r1.readEntity(String.class));

		assertEquals(0, intForQuery("SELECT COUNT(1) FROM utest.e7 WHERE e8_id = 1"));
	}

	@Test
	public void testDelete_ValidRel_ToMany() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "2, 'yyy'");
		insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
		insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
		insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

		assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

		Response r1 = target("/e2/1/e3s/9").request().delete();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true}", r1.readEntity(String.class));

		assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
	}

	@Test
	public void testDelete_ValidRel_ToOne() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "2, 'yyy'");
		insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
		insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
		insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

		assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

		Response r1 = target("/e3/9/e2/1").request().delete();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true}", r1.readEntity(String.class));

		assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
	}

	@Test
	public void testDelete_ValidRel_ToOne_All() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		insert("e2", "id, name", "1, 'xxx'");
		insert("e2", "id, name", "2, 'yyy'");
		insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
		insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
		insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

		assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

		Response r1 = target("/e3/9/e2").request().delete();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true}", r1.readEntity(String.class));

		assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
	}

	@Test
	public void testDelete_InvalidRel() {
		Response r1 = target("/e2/1/dummyRel/9").request().delete();

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyRel'\"}",
				r1.readEntity(String.class));
	}

	@Test
	public void testDelete_NoSuchId_Source() {
		Response r1 = target("/e2/22/e3s/9").request().delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), r1.getStatus());

		String responseEntity = r1.readEntity(String.class).replaceFirst("\\'[\\d]+\\'", "''");
		assertEquals("{\"success\":false,\"message\":\"No object for ID '' and entity 'E2'\"}", responseEntity);
	}
}
