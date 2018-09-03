package io.agrest.provider;

import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import org.apache.cayenne.CayenneException;
import org.apache.cayenne.CayenneRuntimeException;
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

public class CayenneRuntimeExceptionMapperIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testException() {
        Response response = target("/g1").request().get();

        String cayenneVersion = CayenneException.getExceptionLabel();
        String expected = String
                .format("{\"success\":false,\"message\":\"CayenneRuntimeException %s_something_w_cayenne_\"}", cayenneVersion);
        onResponse(response)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals(expected);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("g1")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            throw new CayenneRuntimeException("_something_w_cayenne_");
        }
    }
}

