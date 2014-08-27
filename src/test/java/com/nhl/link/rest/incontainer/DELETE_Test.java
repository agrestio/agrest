package com.nhl.link.rest.incontainer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E4;

public class DELETE_Test extends JerseyTestOnDerby {

	@Before
	public void before() {
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E4"));
	}

	@Test
	public void testDelete() throws IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		assertEquals(2l, Cayenne.objectForQuery(runtime.newContext(), new EJBQLQuery("select count(a) from E4 a")));

		Response response1 = target("/lr/8").request().delete();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		assertEquals(1l, Cayenne.objectForQuery(runtime.newContext(), new EJBQLQuery("select count(a) from E4 a")));
	}

	@Test
	public void testDelete_BadID() throws IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		assertEquals(2l, Cayenne.objectForQuery(runtime.newContext(), new EJBQLQuery("select count(a) from E4 a")));

		Response response1 = target("/lr/7").request().delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}",
				response1.readEntity(String.class));

		assertEquals(2l, Cayenne.objectForQuery(runtime.newContext(), new EJBQLQuery("select count(a) from E4 a")));
	}

	@Test
	public void testDelete_Twice() throws IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id, c_varchar) values (8, 'yyy')"));

		Response response1 = target("/lr/8").request().delete();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true}", response1.readEntity(String.class));

		Response response2 = target("/lr/8").request().delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
		assertEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}",
				response2.readEntity(String.class));
	}

}
