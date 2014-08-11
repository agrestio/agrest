package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

public class LinkRestService_InContainer_GET_Related_Test extends JerseyTestOnDerby {

	@Before
	public void before() {
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E4"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E3"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E2"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E5"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E6"));
	}

	@Test
	public void testGet_ValidRel_ToMany() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/lr/related/e2/1/e3s").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
	}
	
	@Test
	public void testGet_ValidRel_ToOne() {

		// make sure we have e3s for more than one e2 - this will help us
		// confirm that relationship queries are properly filtered.

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (2, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (7, 2, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response r1 = target("/lr/related/e3/7/e2").queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void testGet_InvalidRel() {
		Response r1 = target("/lr/related/e2/1/dummyRel").request().get();

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyRel'\"}",
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
