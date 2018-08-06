package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Sort;
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
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CayenneExpProviderTest extends JerseyTestOnDerby {

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
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_List() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id").queryParam("cayenneExp", urlEnc("[\"name = 'yyy'\"]"))
				.request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_CayenneExp_List_Params() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "id")
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

		Response response1 = target("/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 in $ids\",\"params\":{\"ids\": [3, 4]}}")).request()
				.get();

		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":6}],\"total\":1}", response1.readEntity(String.class));
	}

//	@Test
//	public void test_Select_CayenneExp_NotIn_Array() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));
//
//		Response response1 = target("/e3").queryParam("include", "id")
//				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in $ids\",\"params\":{\"ids\": [1, 2]}}"))
//				.request().get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[],\"total\":0}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_Outer() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'A'),(9, null, 'B')"));
//
//		Response response1 = target("/e3").queryParam("include", "id")
//				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2+.name = null\"}")).request().get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_Outer_Relationship() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'A'),(9, null, 'B')"));
//
//		Response response1 = target("/e3").queryParam("include", "id")
//				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2+ = null\"}")).request().get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_Outer_To_Many_Relationship() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'A'),(9, null, 'B')"));
//
//		Response response1 = target("/e2").queryParam("include", "id")
//				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e3s+ = null\"}")).request().get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_In_TwoObjects() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, name) values (8, 'yyy'),(9, 'zzz')"));
//
//		Response response1 = target("/e3")
//				.queryParam("include", "id")
//				.queryParam("cayenneExp",
//						urlEnc("{\"exp\":\"name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}"))
//				.request().get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_In_TwoRelatedObjects() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));
//
//		Response response1 = target("/e3")
//				.queryParam("include", "id")
//				.queryParam("cayenneExp",
//						urlEnc("{\"exp\":\"e2.name in ($n1, $n2)\",\"params\":{\"n1\":\"zzz\",\"n2\":\"xxx\"}}"))
//				.request().get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":8}],\"total\":1}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_NotIn_ById() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));
//
//		Response response1 = target("/e3").queryParam("include", "id")
//				.queryParam("cayenneExp", urlEnc("{\"exp\":\"e2 not in ($id)\",\"params\":{\"id\":1}}")).request()
//				.get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
//	}
//
//	@Test
//	public void test_Select_CayenneExp_NotIn_By2Ids() {
//
//		newContext().performGenericQuery(
//				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy')"));
//		newContext().performGenericQuery(
//				new SQLTemplate(E3.class, "INSERT INTO utest.e3 (id, e2_id, name) values (8, 1, 'yyy'),(9, 2, 'zzz')"));
//
//		Response response1 = target("/e3")
//				.queryParam("include", "id")
//				.queryParam("cayenneExp",
//						urlEnc("{\"exp\":\"e2 not in ($id1, $id2)\",\"params\":{\"id1\":1,\"id2\":3}}")).request()
//				.get();
//
//		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
//		assertEquals("{\"data\":[{\"id\":9}],\"total\":1}", response1.readEntity(String.class));
//	}

	@Path("")
	public static class Resource {

		@Context
		private Configuration config;

		@GET
		@Path("e2")
		public DataResponse<E2> getE2(@QueryParam("include") List<Include> includes,
									  @QueryParam("exclude") List<Exclude> excludes,
									  @QueryParam("cayenneExp") CayenneExp cayenneExp) {

			LrRequest lrRequest = LrRequest.builder()
					.includes(includes)
					.excludes(excludes)
					.cayenneExp(cayenneExp)
					.build();

			assertNotNull(cayenneExp);
//			assertEquals();

			return null;
		}

		@GET
		@Path("e3")
		public DataResponse<E3> getE3(@QueryParam("sort") Sort sort,
									  @QueryParam("include") List<Include> includes,
									  @QueryParam("exclude") List<Exclude> excludes,
									  @QueryParam("cayenneExp") CayenneExp cayenneExp) {

			LrRequest lrRequest = LrRequest.builder()
					.sort(sort)
					.includes(includes)
					.excludes(excludes)
					.cayenneExp(cayenneExp)
					.build();

			return LinkRest.select(E3.class, config)
					.constraint(Constraint.excludeAll(E3.class).includeId().attributes("phoneNumber", "name")
							.path("e2",Constraint.excludeAll(E2.class).includeId().attributes("address", "name")
							)
							.path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")
							)
					)
//					.request(lrRequest) // overrides parameters from uriInfo
					.get();
		}
	}
}
