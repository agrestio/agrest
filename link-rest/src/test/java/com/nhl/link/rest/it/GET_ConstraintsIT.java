package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E10;
import com.nhl.link.rest.it.fixture.cayenne.E4;
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

public class GET_ConstraintsIT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(Resource.class);
	}

	@Test
	public void test_Implicit() {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cInt\":5}],\"total\":1}", response1.readEntity(String.class));

	}

	@Test
	public void test_Explicit() {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").queryParam("include", E4.C_BOOLEAN.getName())
				.queryParam("include", E4.C_INT.getName()).request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"cInt\":5}],\"total\":1}", response1.readEntity(String.class));

	}

	@Test
	public void test_Explicit_QueryParam() {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").queryParam("include", E4.C_BOOLEAN.getName())
				.queryParam("limit", new Integer(10)).request().get();
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":false,\"message\":\"The query parameters set '{include=[cBoolean], limit=[10]}' is not supported\"}", response1.readEntity(String.class));

	}

	@Test
	public void test_Annotated() {

		insert("e10", "id, c_varchar, c_int, c_boolean, c_date", "1, 'xxx', 5, true, '2014-01-02'");

		Response response1 = target("/e10").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cBoolean\":true,\"cInt\":5}],\"total\":1}",
				response1.readEntity(String.class));

	}

	@Test
	public void test_Annotated_Relationship() {

		insert("e10", "id, c_varchar, c_int, c_boolean, c_date", "1, 'xxx', 5, true, '2014-01-02'");
		insert("e11", "id, e10_id, address, name", "15, 1, 'aaa', 'nnn'");

		Response response1 = target("/e10").queryParam("include", E10.E11S.getName()).request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"cBoolean\":true,\"cInt\":5,\"e11s\":{\"address\":\"aaa\"}}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Path("")
	public static class Resource {

		@Context
		private Configuration config;

        @GET
        @Path("e4/limit_attributes")
        public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {
            return LinkRest.select(E4.class, config).uri(uriInfo)
                    .constraint(Constraint.idOnly(E4.class)
							.attributes(E4.C_INT)
							.queryParams("include"))
                    .get();
        }

		@GET
        @Path("e10")
		public DataResponse<E10> get(@Context UriInfo uriInfo) {
			return LinkRest.select(E10.class, config).uri(uriInfo).get();
		}
	}
}
