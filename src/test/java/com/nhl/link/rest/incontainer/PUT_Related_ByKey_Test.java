package com.nhl.link.rest.incontainer;

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

public class PUT_Related_ByKey_Test extends JerseyTestOnDerby {

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
	public void testRelate_ToMany_MixedCollection() {

		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (15, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E2.class, "INSERT INTO utest.e8 (id, name) values (16, 'xxx')"));

		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (7, 'zzz', 16)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (8, 'yyy', 15)"));
		context.performGenericQuery(new SQLTemplate(E3.class,
				"INSERT INTO utest.e7 (id, name, e8_id) values (9, 'aaa', 15)"));

		Response r1 = target("/lr/related/bykey/e8/15/e7s").request().put(
				Entity.entity("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());

		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
		int id = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e7 WHERE name = 'newname'").selectOne(
				context);
		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id + ",\"name\":\"newname\"},"
				+ "{\"id\":9,\"name\":\"aaa\"}],\"total\":2}", r1.readEntity(String.class));

		// testing idempotency

		Response r2 = target("/lr/related/bykey/e8/15/e7s").request().put(
				Entity.entity("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id + ",\"name\":\"newname\"},"
				+ "{\"id\":9,\"name\":\"aaa\"}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
	}

}
