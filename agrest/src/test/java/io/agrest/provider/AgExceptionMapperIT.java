package io.agrest.provider;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndPojoCase;
import io.agrest.it.fixture.pojo.model.P1;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class AgExceptionMapperIT extends JerseyAndPojoCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Test
    public void testException() {
        Response response = target("/p1").request().get();
        onResponse(response)
                .statusEquals(Response.Status.FORBIDDEN)
                .bodyEquals("{\"success\":false,\"message\":\"_was_forbidden_\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @GET
        @Path("p1")
        public DataResponse<P1> getE2(@Context UriInfo uriInfo) {
            throw new AgException(Response.Status.FORBIDDEN, "_was_forbidden_");
        }
    }
}
