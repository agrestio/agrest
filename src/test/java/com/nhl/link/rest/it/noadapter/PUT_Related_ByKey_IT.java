package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E15Resource;
import com.nhl.link.rest.it.fixture.resource.E8Resource;

public class PUT_Related_ByKey_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E8Resource.class);
		context.register(E15Resource.class);
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
		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id
				+ ",\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}],\"total\":2}", r1.readEntity(String.class));

		// testing idempotency

		Response r2 = target("/e8/bykey/15/e7s").request().put(
				Entity.entity("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("{\"success\":true,\"data\":[" + "{\"id\":" + id
				+ ",\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}],\"total\":2}", r2.readEntity(String.class));
		assertEquals(4, SQLSelect.scalarQuery(String.class, "SELECT count(1) FROM utest.e7").selectOne(context));
	}

	@Test
	public void testPUT_ToMany_LongId() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e15 (long_id, name) values (5, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e15 (long_id, name) values (44, 'aaa')"));

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (5, 5, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (4, 44, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (2, 44, 'bbb')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (6, 5, 'yyy')"));

		Response r1 = target("/e15/44/e14s")
				.queryParam("exclude", "id")
				.queryParam("include", E3.NAME.getName())
				.request()
				.put(Entity.entity("[{\"id\":4,\"name\":\"zzz\"},{\"id\":11,\"name\":\"new\"}]",
						MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), r1.getStatus());

		// update: ordering must be preserved...
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":4,\"name\":\"zzz\"},{\"id\":11,\"name\":\"new\"}],\"total\":2}",
				r1.readEntity(String.class));

		assertEquals(2, SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e14 WHERE e15_id = 44")
				.selectOne(context).intValue());
		assertEquals(
				2,
				SQLSelect
						.scalarQuery(Integer.class,
								"SELECT count(1) FROM utest.e14 WHERE e15_id = 44 and long_id IN (4,11)")
						.selectOne(context).intValue());
	}

}
