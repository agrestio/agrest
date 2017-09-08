package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E17Resource;
import com.nhl.link.rest.it.fixture.resource.E24Resource;
import com.nhl.link.rest.it.fixture.resource.E4Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DELETE_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E4Resource.class);
		context.register(E17Resource.class);
		context.register(E24Resource.class);
	}

	@Test
	public void testDelete() throws IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));

		Response response1 = target("/e4/8").request().delete();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		assertEquals(1l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));
	}

	@Test
	public void testDelete_CompoundId() {

		newContext().performGenericQuery(
				new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (1, 1, 'aaa')"));
		newContext().performGenericQuery(
				new SQLTemplate(E17.class, "INSERT INTO utest.e17 (id1, id2, name) values (2, 2, 'bbb')"));

		Response response1 = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request().delete();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		assertEquals(1l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E17 a")));
		assertEquals("bbb", Cayenne.objectForQuery(newContext(),
				new EJBQLQuery("select a.name from E17 a where a.id1 =2 and a.id2 = 2")));
	}

	@Test
	public void testDelete_BadID() throws IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));

		Response response1 = target("/e4/7").request().delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}",
				response1.readEntity(String.class));

		assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));
	}

	@Test
	public void testDelete_Twice() throws IOException {

		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		Response response1 = target("/e4/8").request().delete();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		Response response2 = target("/e4/8").request().delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
		assertEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}",
				response2.readEntity(String.class));
	}

	@Test
	public void test_Delete_UpperCasePK() {
		insert("e24", "TYPE, NAME", "1, 'xyz'");
		Response response1 = target("/e24/1").request().delete();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
	}
}
