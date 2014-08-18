package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

public class LinkRestService_InContainer_POST_Related_Test extends JerseyTestOnDerby {

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
	public void testRelate_ToMany_New() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (24, 'xxx')"));

		Response r1 = target("/lr/related/e2/24/e3s").request().post(
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

		Response r1 = target("/lr/related/e2/15/e3s").request().post(
				Entity.entity("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
				+ "{\"id\":1,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}", r1.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3").selectOne(context));
		assertEquals(3, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3 WHERE e2_id = 15")
				.selectOne(context));

		// testing non-idempotency

		Response r2 = target("/lr/related/e2/15/e3s").request().post(
				Entity.entity("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
				+ "{\"id\":2,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(5, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3").selectOne(context));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e3 WHERE e2_id = 15")
				.selectOne(context));
	}

}
