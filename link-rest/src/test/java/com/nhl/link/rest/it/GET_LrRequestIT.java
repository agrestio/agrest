package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Include;
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

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GET_LrRequestIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(Resource.class);
	}

	@Test
	public void test_CayenneExp_OverrideByLrRequest() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzz')"));

		Response r1 = target("/e2").queryParam("include", "name")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// returns 'xxx' instead of 'yyy' due to overriding cayenneExp by LrRequest
		assertEquals("{\"data\":[{\"name\":\"xxx\"}],\"total\":1}", r1.readEntity(String.class));
	}

	@Test
	public void test_Includes_OverrideByLrRequest() {

		newContext().performGenericQuery(
				new SQLTemplate(E2.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx'),(2, 'yyy'),(3, 'zzzz')"));
		newContext().performGenericQuery(
				new SQLTemplate(E3.class,
						"INSERT INTO utest.e3 (id, e2_id, name) values (6, 3, 'yyy'),(8, 1, 'yyy'),(9, 2, 'zzz')"));

		Response r1 = target("/e3").queryParam("include", "id")
				.queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}")).request().get();

		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		// returns names instead of id's due to overriding include by LrRequest
		assertEquals("{\"data\":[{\"name\":\"yyy\"},{\"name\":\"yyy\"}],\"total\":2}", r1.readEntity(String.class));
	}


	@Path("")
	public static class Resource {

		@Context
		private Configuration config;

		@GET
		@Path("e2")
		public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
			CayenneExp cayenneExp = new CayenneExp("name = 'xxx'");
			LrRequest lrRequest = LrRequest.builder().cayenneExp(cayenneExp).build();

			return LinkRest.service(config).select(E2.class)
					.uri(uriInfo)
					.request(lrRequest) // overrides parameters from uriInfo
					.get();
		}

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
			List<Include> includes = Collections.singletonList(new Include("name"));
			LrRequest lrRequest = LrRequest.builder().includes(includes).build();

			return LinkRest.service(config).select(E3.class)
					.uri(uriInfo)
					.request(lrRequest) // overrides parameters from uriInfo
					.get();
        }
	}
}
