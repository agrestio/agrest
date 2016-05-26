package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import com.nhl.link.rest.it.fixture.resource.E20Resource;
import com.nhl.link.rest.it.fixture.resource.E21Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class POST_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E20Resource.class);
		context.register(E21Resource.class);
    }

    @Test
	public void testPost() throws WebApplicationException, IOException {

		Response response1 = target("/e20").queryParam("exclude", "age", "description").request().post(
				Entity.entity("{\"id\":\"John\"}", MediaType.APPLICATION_JSON));
		assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

		E20 e20 = ObjectSelect.query(E20.class).selectFirst(context);
		assertNotNull(e20);
		assertEquals("John", e20.getName());

		assertEquals("{\"data\":[{\"id\":\"John\",\"name\":\"John\"}],\"total\":1}",
				response1.readEntity(String.class));

		Response response2 = target("/e20").queryParam("exclude", "age", "description").request().post(
				Entity.entity("{\"id\":\"John\"}", MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        assertTrue(response2.readEntity(String.class).contains("object already exists"));
	}

	@Test
	public void testPost_MultiId() throws WebApplicationException, IOException {

		Response response1 = target("/e21").queryParam("exclude", "description").request().post(
				Entity.entity("{\"id\":{\"age\":18,\"name\":\"John\"}}", MediaType.APPLICATION_JSON));
		assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

		E21 e21 = ObjectSelect.query(E21.class).selectFirst(context);
		assertNotNull(e21);
		assertEquals(Integer.valueOf(18), e21.getAge());
		assertEquals("John", e21.getName());

		assertEquals("{\"data\":[{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}],\"total\":1}",
				response1.readEntity(String.class));

		Response response2 = target("/e21").queryParam("exclude", "description").request().post(
				Entity.entity("{\"id\":{\"age\":18,\"name\":\"John\"}}", MediaType.APPLICATION_JSON));

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        assertTrue(response2.readEntity(String.class).contains("object already exists"));
	}
}
