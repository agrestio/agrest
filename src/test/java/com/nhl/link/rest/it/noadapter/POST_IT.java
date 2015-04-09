package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.it.fixture.resource.E16Resource;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import com.nhl.link.rest.it.fixture.resource.E8Resource;

public class POST_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E3Resource.class);
		context.register(E4Resource.class);
		context.register(E8Resource.class);
		context.register(E16Resource.class);
	}

	@Test
	public void testPost() throws WebApplicationException, IOException {

		Response response1 = target("/e4").request().post(
				Entity.entity("{\"cVarchar\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

		E4 e41 = ObjectSelect.query(E4.class).selectFirst(context);
		assertNotNull(e41);
		assertEquals("zzz", e41.getCVarchar());
		int id1 = Cayenne.intPKForObject(e41);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id1
				+ ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
				+ "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}],\"total\":1}",
				response1.readEntity(String.class));

		Response response2 = target("/e4").request().post(
				Entity.entity("{\"cVarchar\":\"TTTT\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.CREATED.getStatusCode(), response2.getStatus());

		List<E4> e4s = context.select(new SelectQuery<E4>(E4.class));
		assertEquals(2, e4s.size());
		assertTrue(e4s.remove(e41));

		E4 e42 = e4s.get(0);
		assertEquals("TTTT", e42.getCVarchar());
		int id2 = Cayenne.intPKForObject(e42);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id2
				+ ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
				+ "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"TTTT\"}],\"total\":1}",
				response2.readEntity(String.class));
	}

	@Test
	public void testPost_DateTime() {
		Response r1 = target("e16").request().post(
				Entity.entity(
						"{\"cDate\":\"2015-03-14\", \"cTime\":\"T19:00:00\", \"cTimestamp\":\"2015-03-14T19:00:00.000\"}",
						MediaType.APPLICATION_JSON
				)
		);
		assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());
	}

	@Test
	public void testPost_Default_NoData() throws WebApplicationException, IOException {

		Response response1 = target("/e4/defaultdata").request().post(
				Entity.entity("{\"cVarchar\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		E4 e41 = (E4) Cayenne.objectForQuery(context, new SelectQuery<E4>(E4.class));
		assertEquals("zzz", e41.getCVarchar());
	}

	@Test
	public void testPost_WriteConstraints_Id_Allowed() throws WebApplicationException, IOException {

		Response r1 = target("/e8/w/constrainedid/578").request().post(
				Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());

		assertEquals(Integer.valueOf(1), SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e8")
				.selectOne(context));
		assertEquals("zzz", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e8").selectOne(context));
		int id1 = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e8").selectOne(context);
		assertEquals(578, id1);
	}

	@Test
	public void testPost_WriteConstraints_Id_Blocked() throws WebApplicationException, IOException {

		Response r1 = target("/e8/w/constrainedidblocked/578").request().post(
				Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());

		assertEquals(Integer.valueOf(0), SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e8")
				.selectOne(context));
	}

	@Test
	public void testPost_WriteConstraints1() throws WebApplicationException, IOException {

		Response r1 = target("/e3/w/constrained").request().post(
				Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());

		assertEquals(Integer.valueOf(1), SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e3")
				.selectOne(context));
		assertEquals("zzz", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e3").selectOne(context));
		int id1 = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e3").selectOne(context);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id1
				+ ",\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void testPost_WriteConstraints2() throws WebApplicationException, IOException {

		Response r2 = target("/e3/w/constrained").request().post(
				Entity.entity("{\"name\":\"yyy\",\"phoneNumber\":\"12345\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

		assertEquals(
				Integer.valueOf(1),
				SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e3 WHERE name = 'yyy'").selectOne(
						context));
		int id1 = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e3  WHERE name = 'yyy'")
				.selectOne(context);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id1
				+ ",\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":1}", r2.readEntity(String.class));
	}

	@Test
	public void testPost_ReadConstraints1() throws WebApplicationException, IOException {

		Response r1 = target("/e3/constrained").request().post(
				Entity.entity("{\"name\":\"zzz\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r1.getStatus());

		assertEquals(Integer.valueOf(1), SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e3")
				.selectOne(context));
		assertEquals("zzz", SQLSelect.scalarQuery(String.class, "SELECT name FROM utest.e3").selectOne(context));
		int id1 = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e3").selectOne(context);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id1 + ",\"name\":\"zzz\"}],\"total\":1}",
				r1.readEntity(String.class));
	}

	@Test
	public void testPost_ReadConstraints2() throws WebApplicationException, IOException {

		Response r2 = target("/e3/constrained").queryParam("include", "name").queryParam("include", "phoneNumber")
				.request().post(Entity.entity("{\"name\":\"yyy\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

		assertEquals(
				Integer.valueOf(1),
				SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e3 WHERE name = 'yyy'").selectOne(
						context));

		assertEquals("{\"success\":true,\"data\":[{\"name\":\"yyy\"}],\"total\":1}", r2.readEntity(String.class));
	}

	@Test
	public void testPost_ReadConstraints3() throws WebApplicationException, IOException {

		Response r2 = target("/e3/constrained").queryParam("include", E3.E2.getName()).request()
				.post(Entity.entity("{\"name\":\"yyy\"}", MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

		assertEquals(
				Integer.valueOf(1),
				SQLSelect.scalarQuery(Integer.class, "SELECT count(1) FROM utest.e3 WHERE name = 'yyy'").selectOne(
						context));
		int id2 = SQLSelect.scalarQuery(Integer.class, "SELECT id FROM utest.e3 WHERE name = 'yyy'").selectOne(context);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id2 + ",\"name\":\"yyy\"}],\"total\":1}",
				r2.readEntity(String.class));
	}

	@Test
	public void testPost_ToOne() throws WebApplicationException, IOException {

		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

		Response response1 = target("/e3").request().post(
				Entity.entity("{\"e2\":8,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

		E3 e3 = (E3) Cayenne.objectForQuery(context, new SelectQuery<E3>(E3.class));
		int id = Cayenne.intPKForObject(e3);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id
				+ ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}", response1.readEntity(String.class));

		runtime.newContext().invalidateObjects(e3);
		assertEquals("MM", e3.getName());
		assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
	}

	@Test
	public void testPost_ToOne_Null() throws WebApplicationException, IOException {

		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

		Response response1 = target("/e3").request().post(
				Entity.entity("{\"e2_id\":null,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

		E3 e3 = (E3) Cayenne.objectForQuery(context, new SelectQuery<E3>(E3.class));
		int id = Cayenne.intPKForObject(e3);

		assertEquals("{\"success\":true,\"data\":[{\"id\":" + id
				+ ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}", response1.readEntity(String.class));

		runtime.newContext().invalidateObjects(e3);
		assertEquals("MM", e3.getName());
		assertNull(e3.getE2());
	}

	@Test
	public void testPost_ToOne_BadFK() throws WebApplicationException, IOException {

		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		context.performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

		Response response1 = target("/e3").request().post(
				Entity.entity("{\"e2\":15,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

		assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());

		assertEquals(0, context.select(new SelectQuery<E3>(E3.class)).size());
	}

	@Test
	public void testPost_Bulk() throws WebApplicationException, IOException {

		Response r2 = target("/e3/")
				.queryParam("exclude", "id")
				.queryParam("include", E3.NAME.getName())
				.request()
				.post(Entity.entity("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]",
						MediaType.APPLICATION_JSON));
		assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

		// ordering must be preserved...
		assertEquals(
				"{\"success\":true,\"data\":[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}],\"total\":4}",
				r2.readEntity(String.class));
	}
}
