package com.nhl.link.rest.incontainer;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.resource.E8Resource;

public class PUT_Related_ByKey_Test extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E8Resource.class);
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

		Response r1 = target("/e8/bykey/15/e7s").request().put(
				Entity.entity("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());

		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
		int id = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e7 WHERE name = 'newname'").selectOne(
				context);
		assertEquals("{\"success\":true,\"data\":[{\"id\":9,\"name\":\"aaa\"}," + "{\"id\":" + id
				+ ",\"name\":\"newname\"}],\"total\":2}", r1.readEntity(String.class));

		// testing idempotency

		Response r2 = target("/e8/bykey/15/e7s").request().put(
				Entity.entity("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":9,\"name\":\"aaa\"}," + "{\"id\":" + id
				+ ",\"name\":\"newname\"}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
	}

}
