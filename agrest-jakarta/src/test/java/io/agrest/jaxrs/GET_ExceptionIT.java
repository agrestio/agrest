package io.agrest.jaxrs;

import io.agrest.AgException;
import io.agrest.jaxrs.junit.AgPojoTester;
import io.agrest.jaxrs.junit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class GET_ExceptionIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void testNoData() {
        tester.target("/nodata").get()
                .wasNotFound()
                .bodyEquals("{\"success\":false,\"message\":\"request failed\"}");
    }

    @Test
    public void testNoData_WithThrowable() {
        tester.target("/nodata/th").get()
                .wasServerError()
                .mediaTypeEquals(MediaType.APPLICATION_JSON_TYPE)
                .bodyEquals("{\"success\":false,\"message\":\"request failed with th\"}");
    }

    @Path("nodata")
    public static class Resource {

        @GET
        public Response get() {
            throw AgException.notFound("request failed");
        }

        @GET
        @Path("th")
        public Response getTh() {
            try {
                throw new Throwable("Dummy");
            } catch (Throwable th) {
                throw AgException.internalServerError(th, "request failed with th");
            }
        }
    }

}
