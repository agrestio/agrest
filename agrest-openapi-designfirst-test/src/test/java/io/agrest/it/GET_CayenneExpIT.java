package io.agrest.it;

import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.swagger.api.v1.service.E2Resource;
import io.agrest.swagger.api.v1.service.E3Resource;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

public class GET_CayenneExpIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E2Resource.class);
		context.register(E3Resource.class);
	}

	@Test
	public void test_CayenneExp_Map() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/v1/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_Map_Params() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/v1/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":1}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_Bare() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/v1/e2").queryParam("include", "id").queryParam("cayenneExp", urlEnc("name = 'yyy'"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_List() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/v1/e2").queryParam("include", "id").queryParam("cayenneExp", urlEnc("[\"name = 'yyy'\"]"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_List_Params() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/v1/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("[\"name = $b\", \"xxx\"]")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":1}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_In_Array() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class,
						"INSERT INTO utest.e3 (id, e2_id, name) values (6, 3, 'yyy'),(8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response response1 = target("/v1/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")).request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":6}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_NotIn_Array() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response response1 = target("/v1/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in $ids\",\"params\":{\"ids\": [1, 2]}}"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[],\"total\":0}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_Outer() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'A'),(9, null, 'B')"));

		Response response1 = target("/v1/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2+.name = null\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_Outer_Relationship() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'A'),(9, null, 'B')"));

		Response response1 = target("/v1/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2+ = null\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_Outer_To_Many_Relationship() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'A'),(9, null, 'B')"));

		Response response1 = target("/v1/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e3s+ = null\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_In_TwoObjects() {

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy'),(9, 'zzz')"));

		Response response1 = target("/v1/e3")
				.queryParam("include", "id")
				.queryParam("cayenneExp",
						urlEnc("{\"exp\":\"name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_In_TwoRelatedObjects() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response response1 = target("/v1/e3")
				.queryParam("include", "id")
				.queryParam("cayenneExp",
						urlEnc("{\"exp\":\"e2.name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":8}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_NotIn_ById() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response response1 = target("/v1/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in ($id)\",\"params\":{\"id\":1}}")).request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_NotIn_By2Ids() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response response1 = target("/v1/e3")
				.queryParam("include", "id")
				.queryParam("cayenneExp",
						urlEnc("{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")).request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
	}

}
