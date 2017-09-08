package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E14Resource;
import com.nhl.link.rest.it.fixture.resource.E15Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class GET_Props_PersistentWithExtraAnnotatedProps_IT extends JerseyTestOnDerby {

	@Override
	protected void doAddResources(FeatureContext context) {
		context.register(E14Resource.class);
		context.register(E15Resource.class);
	}

	@Test
	public void testGET_Root() {
		insert("e15", "long_id, name", "1, 'xxx'");
		insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

		Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "prettyName").request()
				.get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals("{\"data\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testGET_PrefetchPojoRel() {
		insert("e15", "long_id, name", "1, 'xxx'");
		insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

		Response response1 = target("/e14").queryParam("include", "name").queryParam("include", "p7").request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());

		assertEquals("{\"data\":[{\"name\":\"yyy\",\"p7\":{\"id\":800,\"string\":\"p7_yyy\"}}],\"total\":1}",
				response1.readEntity(String.class));
	}

	@Test
	public void testGET_Related() {
		insert("e15", "long_id, name", "1, 'xxx'");
		insert("e14", "e15_id, long_id, name", "1, 8, 'yyy'");

		Response response1 = target("/e15").queryParam("include", "e14s.name").queryParam("include", "e14s.prettyName")
				.request().get();
		assertEquals(Status.OK.getStatusCode(), response1.getStatus());
		assertEquals(
				"{\"data\":"
						+ "[{\"id\":1,\"e14s\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"name\":\"xxx\"}],\"total\":1}",
				response1.readEntity(String.class));
	}
}
