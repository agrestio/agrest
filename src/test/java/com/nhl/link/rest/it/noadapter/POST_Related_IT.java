package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataRow;
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

public class POST_Related_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E12Resource.class);
	}

	@Test
	public void testRelate_ToMany_New() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));

		Response r1 = target("/e2/24/e3s").request().post(
				Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{REPLACED_ID,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				r1.readEntity(String.class).replaceFirst("\"id\":[\\d]+", "REPLACED_ID"));

		assertEquals(1, SQLSelect.scalarQuery(E3.class, "SELECT count(1) FROM utest.e3").selectOne(context));

		DataRow row = SQLSelect.dataRowQuery("SELECT e2_id, name FROM utest.e3").lowerColumnNames().selectOne(context);
		assertEquals("zzz", row.get("name"));
		assertEquals(24, row.get("e2_id"));
	}

	@Test
	public void testRelate_ToMany_MixedCollection() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (15, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (16, 'xxx')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (7, 'zzz', 16)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (8, 'yyy', 15)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e3 (id, name, e2_id) values (9, 'aaa', 15)"));

		Response r1 = target("/e2/15/e3s").request().post(
				Entity.entity("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
				+ "{\"id\":1,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}", r1.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3").selectOne(context));
		assertEquals(3, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3 WHERE e2_id = 15")
				.selectOne(context));

		// testing non-idempotency

		Response r2 = target("/e2/15/e3s").request().post(
				Entity.entity("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
				+ "{\"id\":2,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(5, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3").selectOne(context));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3 WHERE e2_id = 15")
				.selectOne(context));
	}

	@Test
	public void testPOST_ToManyJoin() {

		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (11)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e12 (id) values (12)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (14)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (15)"));
		context.performGenericQuery(new SQLTemplate(E12.class, "INSERT INTO utest.e13 (id) values (16)"));

		Response r1 = target("/e12/12/e1213").queryParam("exclude", "id").request()
				.post(Entity.entity("[{\"e13\":15},{\"e13\":14}]", MediaType.APPLICATION_JSON));

		assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{},{}],\"total\":2}",
				r1.readEntity(String.class));

		assertEquals(2, SQLSelect.scalarQuery(E12E13.class, "SELECT count(1) FROM utest.e12_e13").selectOne(context));
		assertEquals(1, SQLSelect.scalarQuery(E12E13.class,
				"SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 14").selectOne(context));
		assertEquals(1, SQLSelect.scalarQuery(E12E13.class,
				"SELECT count(1) FROM utest.e12_e13 " + "WHERE e12_id = 12 AND e13_id = 15").selectOne(context));
	}

}
