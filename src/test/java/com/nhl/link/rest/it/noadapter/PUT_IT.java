package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E14Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;

public class PUT_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
		context.register(E4Resource.class);
		context.register(E14Resource.class);
	}

	@Test
	public void testPut() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		Response response1 = target("/e4/8").request().put(
				Entity.entity("{\"id\":8,\"cVarchar\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":8,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
						+ "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}],\"total\":1}",
				response1.readEntity(String.class));

		E4 e4 = Cayenne.objectForPK(runtime.newContext(), E4.class, 8);
		runtime.newContext().invalidateObjects(e4);
		assertEquals("zzz", e4.getCVarchar());
	}

	@Test
	public void testPut_ToOne() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 8)"));

		E3 e3 = Cayenne.objectForPK(runtime.newContext(), E3.class, 3);
		runtime.newContext().invalidateObjects(e3);
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));

		Response response1 = target("/e3/3").request().put(
				Entity.entity("{\"id\":3,\"e2\":1}", MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		e3 = Cayenne.objectForPK(runtime.newContext(), E3.class, 3);
		runtime.newContext().invalidateObjects(e3);
		assertEquals(1, Cayenne.intPKForObject(e3.getE2()));
	}

	@Test
	public void testPut_ToOne_ToNull() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', 8)"));

		E3 e3 = Cayenne.objectForPK(runtime.newContext(), E3.class, 3);
		runtime.newContext().invalidateObjects(e3);
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));

		Response response1 = target("/e3/3").request().put(
				Entity.entity("{\"id\":3,\"e2\":null}", MediaType.APPLICATION_JSON));

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		e3 = Cayenne.objectForPK(runtime.newContext(), E3.class, 3);
		runtime.newContext().invalidateObjects(e3);
		assertNull(e3.getE2());
	}

	@Test
	public void testPut_ToOne_FromNull() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e3 (id, name, e2_id) values (3, 'zzz', null)"));

		E3 e3 = Cayenne.objectForPK(runtime.newContext(), E3.class, 3);
		runtime.newContext().invalidateObjects(e3);
		assertNull(e3.getE2());

		Response response1 = target("/e3/3").request().put(
				Entity.entity("{\"id\":3,\"e2\":8}", MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		e3 = Cayenne.objectForPK(runtime.newContext(), E3.class, 3);
		runtime.newContext().invalidateObjects(e3);
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
	}

	@Test
	public void testPUT_Bulk() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (5, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (4, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (2, 'bbb')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (6, 'yyy')"));

		Response r2 = target("/e3/")
				.queryParam("exclude", "id")
				.queryParam("include", E3.NAME.getName())
				.request()
				.put(Entity
						.entity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]",
								MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), r2.getStatus());

		// update: ordering must be preserved...
		assertEquals(
				"{\"success\":true,\"data\":[{\"name\":\"yyy\"},{\"name\":\"zzz\"},{\"name\":\"111\"},{\"name\":\"333\"}],\"total\":4}",
				r2.readEntity(String.class));
	}

	@Test
	public void testPUT_Bulk_LongId_Small() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (5, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (4, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (2, 'bbb')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (6, 'yyy')"));

		Response r1 = target("/e14/")
				.queryParam("exclude", "id")
				.queryParam("include", E3.NAME.getName())
				.request()
				.put(Entity
						.entity("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]",
								MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), r1.getStatus());

		// update: ordering must be preserved...
		assertEquals(
				"{\"success\":true,\"data\":[{\"name\":\"yyy\"},{\"name\":\"zzz\"},{\"name\":\"111\"},{\"name\":\"333\"}],\"total\":4}",
				r1.readEntity(String.class));

		assertEquals(4, SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e14").selectOne(context)
				.intValue());
		assertEquals(4,
				SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e14 WHERE long_id IN (2,4,6,5)")
						.selectOne(context).intValue());
	}

	@Test
	public void testPUT_Bulk_LongId() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (8147483647, 'aaa')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (8147483648, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (8147483649, 'bbb')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, name) values (3147483646, 'yyy')"));

		Response r1 = target("/e14/")
				.queryParam("exclude", "id")
				.queryParam("include", E3.NAME.getName())
				.request()
				.put(Entity.entity("[{\"id\":3147483646,\"name\":\"yyy\"},{\"id\":8147483648,\"name\":\"zzz\"}"
						+ ",{\"id\":8147483647,\"name\":\"111\"},{\"id\":8147483649,\"name\":\"333\"}]",
						MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), r1.getStatus());

		// update: ordering must be preserved...
		assertEquals(
				"{\"success\":true,\"data\":[{\"name\":\"yyy\"},{\"name\":\"zzz\"},{\"name\":\"111\"},{\"name\":\"333\"}],\"total\":4}",
				r1.readEntity(String.class));

		assertEquals(4, SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e14").selectOne(context)
				.intValue());
		assertEquals(4, SQLSelect
				.scalarQuery(
						Integer.class,
						"SELECT count(1) FROM utest.e14 WHERE "
								+ "long_id IN (3147483646, 8147483648, 8147483647, 8147483649)").selectOne(context)
				.intValue());
	}
}
