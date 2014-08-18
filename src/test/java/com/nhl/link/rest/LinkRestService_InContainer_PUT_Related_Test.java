package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
		context.performGenericQuery(new EJBQLQuery("delete from E7"));
		context.performGenericQuery(new EJBQLQuery("delete from E9"));
		context.performGenericQuery(new EJBQLQuery("delete from E8"));
	}

	@Test
	public void testRelate_EmptyPutWithID() {

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
	public void testRelate_ValidRel_ToOne_Existing() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		Response r1 = target("/lr/related/e3/8/e2/24").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

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
	public void testRelate_ToMany_MixedCollection() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (15, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (16, 'xxx')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (7, 'zzz', 16)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (8, 'yyy', 15)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (9, 'aaa', 15)"));

		Response r1 = target("/lr/related/e8/15/e7s").request().put(
				Entity.entity("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]",
						MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":1,\"name\":\"newname\"},"
				+ "{\"id\":8,\"name\":\"123\"}],\"total\":2}", r1.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));

		// testing idempotency

		Response r2 = target("/lr/related/e8/15/e7s").request().put(
				Entity.entity("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]",
						MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":1,\"name\":\"newname\"},"
				+ "{\"id\":8,\"name\":\"123\"}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_New_AutogenId() {

		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		Response r1 = target("/lr/related/e3/8/e2/24").request().put(
				Entity.entity("{\"name\":\"123\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Can't create 'E2' with fixed id\"}", r1.readEntity(String.class));

		assertEquals(0, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e2").selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_New_DefaultId() {

		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id) values (7)"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id) values (8)"));

		Response r1 = target("/lr/related/e7/8/e8/24").request().put(
				Entity.entity("{\"name\":\"aaa\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":24,\"name\":\"aaa\"}],\"total\":1}",
				r1.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e8").selectOne(context));
		assertEquals("aaa", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e8").selectOne(context));
		assertEquals(24, SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e8").selectOne(context).intValue());
		assertEquals(24,
				SQLSelect.scalarQuery(Integer.class, "SELECT e8_id FROM utest.e7 WHERE id = 8").selectOne(context)
						.intValue());
		assertNull(SQLSelect.scalarQuery(String.class, "SELECT e8_id FROM utest.e7 WHERE id = 7").selectOne(context));

		// PUT is idempotent... doing another update should not change the
		// picture
		Response r2 = target("/lr/related/e7/8/e8/24").request().put(
				Entity.entity("{\"name\":\"aaa\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":24,\"name\":\"aaa\"}],\"total\":1}",
				r2.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e8").selectOne(context));
		assertEquals("aaa", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e8").selectOne(context));
		assertEquals(24, SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e8").selectOne(context).intValue());
		assertEquals(24,
				SQLSelect.scalarQuery(Integer.class, "SELECT e8_id FROM utest.e7 WHERE id = 8").selectOne(context)
						.intValue());
		assertNull(SQLSelect.scalarQuery(String.class, "SELECT e8_id FROM utest.e7 WHERE id = 7").selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_New_PropagatedId() {

		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e8 (id) values (7)"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e8 (id) values (8)"));

		Response r1 = target("/lr/related/e8/8/e9").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8}],\"total\":1}", r1.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e9").selectOne(context));
		assertEquals(8, SQLSelect.scalarQuery(Integer.class, "SELECT e8_id FROM utest.e9").selectOne(context)
				.intValue());

		// PUT is idempotent... doing another update should not change the
		// picture
		Response r2 = target("/lr/related/e8/8/e9").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8}],\"total\":1}", r2.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e9").selectOne(context));
		assertEquals(8, SQLSelect.scalarQuery(Integer.class, "SELECT e8_id FROM utest.e9").selectOne(context)
				.intValue());
	}
}
