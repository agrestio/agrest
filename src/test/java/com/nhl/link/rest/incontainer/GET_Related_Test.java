package com.nhl.link.rest.incontainer;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.resource.E2Resource;
import com.nhl.link.rest.unit.resource.E3Resource;

public class GET_Related_Test extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
	}

	@Test
	public void testGet_ToMany_Constrained() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e2/constraints/1/e3s").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_ValidRel_ToMany() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e2/1/e3s").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_ValidRel_ToOne() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/e3/7/e2").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_InvalidRel() {
		Response r1 = target("/e2/1/dummyrel").request().get();

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyrel'\"}",
				r1.readEntity(String.class));
	}

	// TODO: it would be nice if we catch invalid root ids.. the way the query
	// is built now, there's no easy way to tell an empty relationship from an
	// invalid ID ... Should we do an unconditional select by ID before the
	// relationship query?

	// @Test
	// public void testGet_NoSuchId() {
	// Response r1 = target("/lr/related/e2/1/e3s").queryParam("include",
	// "id").request().get();
	// assertEquals(Status.NOT_FOUND.getStatusCode(), r1.getStatus());
	// assertEquals("{\"success\":false,\"message\":\"No object for ID '1' and entity 'E2'\"}",
	// r1.readEntity(String.class));
	// }
}
