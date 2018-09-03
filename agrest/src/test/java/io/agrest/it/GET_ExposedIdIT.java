package io.agrest.it;

import io.agrest.DataResponse;
import io.agrest.LinkRest;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E23;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

    @Path("e23")
    public static class E23Resource {

        @Context
        private Configuration config;

        @GET
        @Path("{id}")
        public DataResponse<E23> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return LinkRest.select(E23.class, config).byId(id).uri(uriInfo).getOne();
        }
    }
}
