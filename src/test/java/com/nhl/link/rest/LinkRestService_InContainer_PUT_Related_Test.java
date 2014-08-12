package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

public class LinkRestService_InContainer_PUT_Related_Test extends JerseyTestOnDerby {

	private ObjectContext context;

	@Before
	public void before() {

		context = runtime.newContext();

		context.performGenericQuery(new EJBQLQuery("delete from E4"));
		context.performGenericQuery(new EJBQLQuery("delete from E3"));
		context.performGenericQuery(new EJBQLQuery("delete from E2"));
		context.performGenericQuery(new EJBQLQuery("delete from E5"));
		context.performGenericQuery(new EJBQLQuery("delete from E6"));
	}

	@Test
	public void testRelate_ValidRel_ToOne_Existing() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		// POST with empty body ... how bad is that?
		Response r1 = target("/lr/related/e3/8/e2/24").request().put(Entity.entity("", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":24,\"address\":null,\"name\":\"xxx\"}],\"total\":1}",
				r1.readEntity(String.class));

		assertEquals("yyy", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e3 WHERE e2_id = 24")
				.selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_Existing_WithUpdate() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		// POST with empty body ... how bad is that?
		Response r1 = target("/lr/related/e3/8/e2/24").request().put(
				Entity.entity("{\"name\":\"123\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":24,\"address\":null,\"name\":\"123\"}],\"total\":1}",
				r1.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e2").selectOne(context));

		assertEquals("yyy", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e3 WHERE e2_id = 24")
				.selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_New_AutogenId() {

		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		// POST with empty body ... how bad is that?
		Response r1 = target("/lr/related/e3/8/e2/24").request().put(
				Entity.entity("{\"name\":\"123\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Can't create 'E2' with fixed id\"}", r1.readEntity(String.class));

		assertEquals(0, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e2").selectOne(context));
	}
}
