package io.agrest.it;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
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

import static org.junit.Assert.*;

public class GET_AgRequestIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(Resource.class);
	}

	@Test
	public void test_CayenneExp_OverrideByAgRequest() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2_cayenneExp").queryParam("include", "name")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// returns 'xxx' instead of 'yyy' due to overriding cayenneExp by AgRequest
		assertEquals("{\"data\":[{\"name\":\"xxx\"}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_Includes_OverrideByAgRequest() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class,
						"INSERT INTO utest.e3 (id, e2_id, name) values (6, 3, 'yyy'),(8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response r1 = target("/e3_includes").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// returns names instead of id's due to overriding include by AgRequest
		assertEquals("{\"data\":[{\"name\":\"yyy\"},{\"name\":\"yyy\"}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void test_Excludes_OverrideByAgRequest() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class,
						"INSERT INTO utest.e3 (id, e2_id, name) values (6, 3, 'yyy'),(8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response r1 = target("/e3_excludes").queryParam("exclude", "name")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// returns 'name' and other fields except 'id' due to overriding exclude by AgRequest
		assertEquals("{\"data\":[{\"name\":\"yyy\",\"phoneNumber\":null},{\"name\":\"yyy\",\"phoneNumber\":null}],\"total\":2}", r1.readEntity(String.class));
	}

	@Test
	public void test_Sort_OverrideByAgRequest() {

		insert("e4", "id", "2");
		insert("e4", "id", "1");
		insert("e4", "id", "3");

		Response response = target("/e4_sort")
				.queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
				.queryParam("include", "id")
				.request()
				.get();
		// returns items in ascending order instead of descending due to overriding sort direction by AgRequest
		onSuccess(response).bodyEquals(3, "{\"id\":1},{\"id\":2},{\"id\":3}");
	}

	@Test
	public void test_MapBy_OverrideByAgRequest() {

		insert("e4", "c_varchar, c_int", "'xxx', 1");
		insert("e4", "c_varchar, c_int", "'yyy', 2");
		insert("e4", "c_varchar, c_int", "'zzz', 2");
		insert("e4", "c_varchar, c_int", "'xxx', 3");

		Response response = target("/e4_mapBy")
				.queryParam("mapBy", E4.C_INT.getName())
				.queryParam("include", E4.C_VARCHAR.getName())
				.request()
				.get();

		onSuccess(response).bodyEqualsMapBy(4,
				"\"xxx\":[{\"cVarchar\":\"xxx\"},{\"cVarchar\":\"xxx\"}]",
				"\"yyy\":[{\"cVarchar\":\"yyy\"}]",
				"\"zzz\":[{\"cVarchar\":\"zzz\"}]");
	}


	@Path("")
	public static class Resource {

		@Context
		private Configuration config;

		@GET
		@Path("e2_cayenneExp")
		public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
			CayenneExp cayenneExp = new CayenneExp("name = 'xxx'");
			AgRequest agRequest = Ag.request(config).cayenneExp(cayenneExp).build();

			return Ag.service(config).select(E2.class)
					.uri(uriInfo)
					.request(agRequest) // overrides parameters from uriInfo
					.get();
		}

        @GET
        @Path("e3_includes")
        public DataResponse<E3> getE3_includes(@Context UriInfo uriInfo) {

			AgRequest agRequest = Ag.request(config).addInclude("name").build();

			return Ag.service(config).select(E3.class)
					.uri(uriInfo)
					.request(agRequest) // overrides parameters from uriInfo
					.get();
        }

		@GET
		@Path("e3_excludes")
		public DataResponse<E3> getE3_excludes(@Context UriInfo uriInfo) {
			AgRequest agRequest = Ag.request(config).addExclude("id").build();

			return Ag.service(config).select(E3.class)
					.uri(uriInfo)
					.request(agRequest) // overrides parameters from uriInfo
					.get();
		}

		@GET
		@Path("e4_sort")
		public DataResponse<E4> getE4_sort(@Context UriInfo uriInfo) {
			AgRequest agRequest = Ag.request(config).sort(new Sort("id", Dir.ASC)).build();

			return Ag.service(config).select(E4.class)
					.uri(uriInfo)
					.request(agRequest) // overrides parameters from uriInfo
					.get();
		}

		@GET
		@Path("e4_mapBy")
		public DataResponse<E4> getE4_mapBy(@Context UriInfo uriInfo) {
			AgRequest agRequest = Ag.request(config).mapBy(E4.C_VARCHAR.getName()).build();

			return Ag.service(config).select(E4.class)
					.uri(uriInfo)
					.request(agRequest) // overrides parameters from uriInfo
					.get();
		}
	}
}
