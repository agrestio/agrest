package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.runtime.AgBuilder;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

/**
 * @deprecated since 2.10, as the API being tested was deprecated.
 */
public class GET_Props_TransientProperty_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Override
    protected AgBuilder doConfigure() {
        return super.doConfigure().transientProperty(E4.class, "derived");
    }

    @Test
    public void testTransientAttribute() {

        insert("e4", "id, c_varchar", "1, 'x'");
        insert("e4", "id, c_varchar", "2, 'y'");

        Response r = target("/e4")
                .queryParam("include", "derived")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"derived\":\"x$\"},{\"derived\":\"y$\"}],\"total\":2}",
                r.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4")
        public DataResponse<E4> get(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }
    }
}
