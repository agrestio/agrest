package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E12E13;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E12Resource;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E7Resource;
import com.nhl.link.rest.it.fixture.resource.E8Resource;

public class PUT_Related_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
		context.register(E7Resource.class);
		context.register(E8Resource.class);
		context.register(E12Resource.class);
	}

	@Test
	public void testRelate_EmptyPutWithID() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		// POST with empty body ... how bad is that?
		Response r1 = target("/e3/8/e2/24").request().put(Entity.entity("", MediaType.APPLICATION_JSON));

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

		Response r1 = target("/e3/8/e2/24").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

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

		Response r1 = target("/e3/8/e2/24").request().put(
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

		Response r1 = target("/e8/createorupdate/15/e7s").request().put(
				Entity.entity("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]",
						MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":["
				+ "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}],\"total\":2}",
				r1.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));

		// testing idempotency

		Response r2 = target("/e8/createorupdate/15/e7s").request().put(
				Entity.entity("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]",
						MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":1,\"name\":\"newname\"},"
				+ "{\"id\":8,\"name\":\"123\"}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
	}

	@Test
	public void test_ToMany_CreateUpdateDelete() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (15, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (16, 'xxx')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (7, 'zzz', 16)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (8, 'yyy', 15)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (9, 'aaa', 15)"));

		// this must add E7 with id=1, update - with id=8, delete - with id=9
		Response r1 = target("/e8/15/e7s").request().put(
				Entity.entity("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]",
						MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":["
				+ "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}],\"total\":2}",
				r1.readEntity(String.class));
		assertEquals(2, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7 WHERE e8_id = 15")
				.selectOne(context));
		assertEquals(0,
				SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7 WHERE id = 9").selectOne(context));

		// testing idempotency

		Response r2 = target("/e8/15/e7s").request().put(
				Entity.entity("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]",
						MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":["
				+ "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}],\"total\":2}",
				r2.readEntity(String.class));
		assertEquals(2, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7 WHERE e8_id = 15")
				.selectOne(context));
		assertEquals(0,
				SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7 WHERE id = 9").selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_New_AutogenId() {

		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (7, 'zzz')"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy')"));

		Response r1 = target("/e3/8/e2/24").request().put(
				Entity.entity("{\"name\":\"123\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Can't create 'E2' with fixed id\"}", r1.readEntity(String.class));

		assertEquals(0, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e2").selectOne(context));
	}

	@Test
	public void testRelate_ValidRel_ToOne_New_DefaultId() {

		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id) values (7)"));
		context.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e7 (id) values (8)"));

		Response r1 = target("/e7/8/e8/24").request().put(
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
		Response r2 = target("/e7/8/e8/24").request().put(
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

		Response r1 = target("/e8/8/e9").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8}],\"total\":1}", r1.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e9").selectOne(context));
		assertEquals(8, SQLSelect.scalarQuery(Integer.class, "SELECT e8_id FROM utest.e9").selectOne(context)
				.intValue());

		// PUT is idempotent... doing another update should not change the
		// picture
		Response r2 = target("/e8/8/e9").request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8}],\"total\":1}", r2.readEntity(String.class));

		assertEquals(1, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e9").selectOne(context));
		assertEquals(8, SQLSelect.scalarQuery(Integer.class, "SELECT e8_id FROM utest.e9").selectOne(context)
				.intValue());
	}

	@Test
	public void testRelate_ToMany_NoIds() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (15, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (16, 'xxx')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (7, 'zzz', 16)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (8, 'yyy', 15)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (9, 'aaa', 15)"));

		assertEquals(3, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3").selectOne(context));
		assertEquals(2, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3 WHERE e2_id = 15")
				.selectOne(context));

		// we can't PUT an object with generated ID , as the request is
		// non-repeatable
		Response r1 = target("/e2/15/e3s").request().put(
				Entity.entity("[ {\"name\":\"newname\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"Request is not idempotent.\"}", r1.readEntity(String.class));
		assertEquals(3, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3").selectOne(context));
		assertEquals(2, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3 WHERE e2_id = 15")
				.selectOne(context));
	}

	@Test
	public void testPUT_ToMany_Join() {

		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (11)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (12)"));

		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (14)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (15)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (16)"));

		Response r1 = target("/e12/12/e1213").queryParam("exclude", "id").request()
				.put(Entity.entity("[{\"e13\":15},{\"e13\":14}]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{},{}],\"total\":2}", r1.readEntity(String.class));

		assertEquals(2, SQLSelect.scalarQuery(E12E13.class, "SELECT count(1) FROM utest.e12_e13").selectOne(context));
		assertEquals(1, SQLSelect.scalarQuery(E12E13.class,
				"SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 14").selectOne(context));
		assertEquals(1, SQLSelect.scalarQuery(E12E13.class,
				"SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 15").selectOne(context));

		// testing idempotency
		Response r2 = target("/e12/12/e1213").queryParam("exclude", "id").request()
				.put(Entity.entity("[{\"e13\":15},{\"e13\":14}]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{},{}],\"total\":2}", r2.readEntity(String.class));

		assertEquals(2, SQLSelect.scalarQuery(E12E13.class, "SELECT count(1) FROM utest.e12_e13").selectOne(context));
		assertEquals(
				1,
				SQLSelect.scalarQuery(E12E13.class,
						"SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 14").selectOne(context));
		assertEquals(
				1,
				SQLSelect.scalarQuery(E12E13.class,
						"SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 15").selectOne(context));

		// add one and delete another record
		Response r3 = target("/e12/12/e1213").queryParam("exclude", "id").request()
				.put(Entity.entity("[{\"e13\":16},{\"e13\":14}]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r3.getStatus());
		assertEquals("{\"success\":true,\"data\":[{},{}],\"total\":2}", r3.readEntity(String.class));

		assertEquals(2, SQLSelect.scalarQuery(E12E13.class, "SELECT count(1) FROM utest.e12_e13").selectOne(context));
		assertEquals(
				1,
				SQLSelect.scalarQuery(E12E13.class,
						"SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 14").selectOne(context));
		assertEquals(
				1,
				SQLSelect.scalarQuery(E12E13.class,
						"SELECT count(1) FROM utest.e12_e13 WHERE e12_id = 12 AND e13_id = 16").selectOne(context));
	}
}
