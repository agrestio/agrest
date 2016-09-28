package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E23Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class GET_ExposedIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E23Resource.class);
    }

    @Test
    public void testGetById() {

        insert("e23", "id, name", "1, 'abc'");
        insert("e23", "id, name", "2, 'xyz'");

        Response r = target("/e23").path("1").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
		assertEquals("{\"data\":[{\"id\":1,\"exposedId\":1,\"name\":\"abc\"}],\"total\":1}", r.readEntity(String.class));
    }
}
