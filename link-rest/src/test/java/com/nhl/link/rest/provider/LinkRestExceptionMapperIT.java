package com.nhl.link.rest.provider;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class LinkRestExceptionMapperIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testException() {
        Response response = target("/g1").request().get();
        onResponse(response)
                .statusEquals(Response.Status.FORBIDDEN)
                .bodyEquals("{\"success\":false,\"message\":\"_was_forbidden_\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("g1")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            throw new LinkRestException(Response.Status.FORBIDDEN, "_was_forbidden_");
        }
    }
}
