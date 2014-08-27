package com.nhl.link.rest.incontainer.sencha;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.adapter.sencha.SenchaAdapter;
import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

public class Sencha_GET_Test extends JerseyTestOnDerby {

	@Before
	public void before() {
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E4"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E3"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E2"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E5"));
		runtime.newContext().performGenericQuery(new EJBQLQuery("delete from E6"));
	}

	@Override
	protected LinkRestBuilder doConfigure() {
		return super.doConfigure().adapter(new SenchaAdapter());
	}

	@Test
	public void test_SelectById_Prefetching() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response response1 = target("/lr/e3/8").queryParam("include", "e2.id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1,\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":1}",
				response1.readEntity(String.class));

		Response response2 = target("/lr/e3/8").queryParam("include", "e2.name").request().get();

		assertEquals(Status.OK.getStatusCode(), response2.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"e2_id\":1,\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":1}",
				response2.readEntity(String.class));

		Response response3 = target("/lr/e2/1").queryParam("include", "e3s.id").request().get();

		assertEquals(Status.OK.getStatusCode(), response3.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":1,\"address\":null,\"e3s\":"
				+ "[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}],\"total\":1}", response3.readEntity(String.class));
	}

	@Test
	public void test_Select_Prefetching() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));

		Response response1 = target("/lr/e3").queryParam("include", "id").queryParam("include", "e2.id")
				.queryParam("sort", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1},"
				+ "{\"id\":9,\"e2\":{\"id\":1},\"e2_id\":1}],\"total\":2}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_Prefetching_StartLimit() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, 1, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (10, 1, 'zzz')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (11, 1, 'zzz')"));

		Response response1 = target("/lr/e3").queryParam("include", "id").queryParam("include", "e2.id")
				.queryParam("sort", "id").queryParam("start", "1").queryParam("limit", "2").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":9,\"e2\":{\"id\":1},\"e2_id\":1},"
				+ "{\"id\":10,\"e2\":{\"id\":1},\"e2_id\":1}],\"total\":4}", response1.readEntity(String.class));
	}

	@Test
	public void test_SelectToOne_Null() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (9, null, 'zzz')"));

		Response response1 = target("/lr/e3").queryParam("include", "e2.id").queryParam("include", "id").request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1},{\"id\":9,\"e2\":null,\"e2_id\":null}],\"total\":2}",
				response1.readEntity(String.class));
	}
	
	@Test
	public void test_MapBy_ToOne() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));

		Response response1 = target("/lr/e3").queryParam("include", urlEnc("{\"path\":\"e2\",\"mapBy\":\"name\"}"))
				.queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		// no support for MapBy for to-one... simply ignoring it...
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"e2\":{"
				+ "\"id\":1,\"address\":null,\"name\":\"xxx\"},\"e2_id\":1}],\"total\":1}",
				response1.readEntity(String.class));
	}
	
	@Test
	public void test_ToMany_IncludeRelated() throws WebApplicationException, IOException {
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e5 (id,name) values (345, 'B'),(346, 'A')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, e5_id, name) "
						+ "values (8, 1, 345, 'a'),(9, 1, 345, 'z'),(7, 1, 346, 'm')"));

		Response response1 = target("/lr/e2").queryParam("include", urlEnc("{\"path\":\"e3s\"}")).queryParam("include", "id")
				.queryParam("exclude", "e3s.id").queryParam("exclude", "e3s.phoneNumber")
				.queryParam("include", "e3s.e5.name").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"e5_id\":345,\"name\":\"a\"},"
						+ "{\"e5\":{\"name\":\"B\"},\"e5_id\":345,\"name\":\"z\"},"
						+ "{\"e5\":{\"name\":\"A\"},\"e5_id\":346,\"name\":\"m\"}]}],\"total\":1}",
				response1.readEntity(String.class));
	}
	
	@Test
	public void test_PathRelationship() throws WebApplicationException, IOException {

		runtime.newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
		runtime.newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy')"));

		Response response1 = target("/lr/e3").queryParam("include", urlEnc("{\"path\":\"e2\"}"))
				.queryParam("include", "id").request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},"
				+ "\"e2_id\":1}],\"total\":1}", response1.readEntity(String.class));
	}

}
