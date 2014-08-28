package com.nhl.link.rest.incontainer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.unit.JerseyTestOnDerby;
import com.nhl.link.rest.unit.cayenne.E4;
import com.nhl.link.rest.unit.resource.E4Resource;

public class GET_Constraints_Test extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E4Resource.class);
	}

	@Test
	public void test_Implicit() throws WebApplicationException, IOException {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		runtime.newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"id\":1,\"cInt\":5}],\"total\":1}",
				response1.readEntity(String.class));

	}

	@Test
	public void test_Explicit() throws WebApplicationException, IOException {

		SQLTemplate insert = new SQLTemplate(E4.class,
				"INSERT INTO utest.e4 (id, c_varchar, c_int) values (1, 'xxx', 5)");
		runtime.newContext().performGenericQuery(insert);

		Response response1 = target("/e4/limit_attributes").queryParam("include", E4.C_BOOLEAN.getName())
				.queryParam("include", E4.C_INT.getName()).request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"success\":true,\"data\":[{\"cInt\":5}],\"total\":1}", response1.readEntity(String.class));

	}
}
