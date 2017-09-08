package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.runtime.LinkRestBuilder;
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
    protected LinkRestBuilder doConfigure() {
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
            return LinkRest.service(config).select(E4.class).uri(uriInfo).get();
        }
    }
}
