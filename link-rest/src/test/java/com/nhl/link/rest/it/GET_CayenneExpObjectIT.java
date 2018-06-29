package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.runtime.query.CayenneExp;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GET_CayenneExpObjectIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(Resource.class);
	}

	@Test
	public void test_CayenneExp_Map() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_Map_Params() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = $n\",\"params\":{\"n\":\"xxx\"}}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":1}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_Bare() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id").queryParam("cayenneExp", urlEnc("name = 'yyy'"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// TODO: Plain cayenneExp string parameter is not processed by CayenneExp object yet
		assertEquals("{\"data\":[{\"id\":1},{\"id\":2},{\"id\":3}],\"total\":3}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_List() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id").queryParam("cayenneExp", urlEnc("[\"name = 'yyy'\"]"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// TODO: Array cayenneExp parameter is not processed by CayenneExp object yet
		assertEquals("{\"data\":[{\"id\":1},{\"id\":2},{\"id\":3}],\"total\":3}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_List_Params() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("[\"name = $b\", \"xxx\"]")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// TODO: cayenneExp array parameter is not processed by CayenneExp object yet
		assertEquals("{\"data\":[{\"id\":1},{\"id\":2},{\"id\":3}],\"total\":3}", r1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_In_Array() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class,
						"INSERT INTO utest.e3 (id, e2_id, name) values (6, 3, 'yyy'),(8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response response1 = target("/e3").queryParam("include", "id")
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

		Response response1 = target("/e3").queryParam("include", "id")
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

		Response response1 = target("/e3").queryParam("include", "id")
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

		Response response1 = target("/e3").queryParam("include", "id")
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

		Response response1 = target("/e2").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e3s+ = null\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", response1.readEntity(String.class));
	}

	@Test
	public void test_Select_CayenneExp_In_TwoObjects() {

		newContext().performGenericQuery(
				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy'),(9, 'zzz')"));

		Response response1 = target("/e3")
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

		Response response1 = target("/e3")
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

		Response response1 = target("/e3").queryParam("include", "id")
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

		Response response1 = target("/e3")
				.queryParam("include", "id")
				.queryParam("cayenneExp",
						urlEnc("{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")).request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
	}

	@Path("")
	public static class Resource {

		@Context
		private Configuration config;

		@GET
		@Path("e2")
		public DataResponse<E2> getE2(@QueryParam("include") List<String> include,
									  @QueryParam("exclude") List<String> exclude,
									  @QueryParam("cayenneExp") CayenneExp cayenneExp) {

			return LinkRest.select(E2.class, config)
					.constraint(Constraint.excludeAll(E2.class).includeId().attributes("address", "name")
							.path("e3s",Constraint.excludeAll(E3.class).includeId().attributes("phoneNumber", "name"))
					)
					.include(include)
					.exclude(exclude)
					.cayenneExp(cayenneExp)
					.get();
		}

		@GET
		@Path("e3")
		public DataResponse<E3> getE3(@QueryParam("sort") String sort,
									   @QueryParam("include") List<String> include,
									   @QueryParam("exclude") List<String> exclude,
									   @QueryParam("cayenneExp") CayenneExp cayenneExp) {
			return LinkRest.select(E3.class, config)
					.constraint(Constraint.excludeAll(E3.class).includeId().attributes("phoneNumber", "name")
							.path("e2",Constraint.excludeAll(E2.class).includeId().attributes("address", "name")
							)
							.path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")
							)
					)
					.sort(sort)
					.include(include)
					.exclude(exclude)
					.cayenneExp(cayenneExp)
					.get();
		}
	}
}
