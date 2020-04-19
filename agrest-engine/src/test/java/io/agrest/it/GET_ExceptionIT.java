package io.agrest.it;

import io.agrest.AgException;
import io.agrest.it.fixture.JerseyAndPojoCase;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class GET_ExceptionIT extends JerseyAndPojoCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Test
    public void testNoData() {
        Response r = target("/nodata").request().get();
        onResponse(r).statusEquals(Status.NOT_FOUND).bodyEquals("{\"success\":false,\"message\":\"request failed\"}");
    }

    @Test
    public void testNoData_WithThrowable() {
        Response r = target("/nodata/th").request().get();

        onResponse(r).statusEquals(Status.INTERNAL_SERVER_ERROR)
                .mediaTypeEquals(MediaType.APPLICATION_JSON_TYPE)
                .bodyEquals("{\"success\":false,\"message\":\"request failed with th\"}");
    }

    @Path("nodata")
    public static class Resource {

        @GET
        public Response get() {
            throw new AgException(Status.NOT_FOUND, "request failed");
        }

        @GET
        @Path("th")
        public Response getTh() {
            try {
                throw new Throwable("Dummy");
            } catch (Throwable th) {
                throw new AgException(Status.INTERNAL_SERVER_ERROR, "request failed with th", th);
            }
        }
    }

}
