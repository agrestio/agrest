package com.nhl.link.rest.it.noadapter;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E4Resource;

public class GET_CustomProperties_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E4Resource.class);
	}

	@Test
	public void testRootExtrasEncoder() {

		runtime.newContext()
				.performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) values (1), (2)"));

		Response response1 = target("/e4/calc_property").queryParam("include", "id").queryParam("sort", "id").request()
				.get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"success\":true,\"data\":[{\"id\":1,\"x\":\"y_1\"}," + "{\"id\":2,\"x\":\"y_2\"}],\"total\":2}",
				response1.readEntity(String.class));
	}
}
