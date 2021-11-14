package io.agrest;

import io.agrest.unit.AgPojoTester;
import io.agrest.unit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GET_ExceptionIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = tester(Resource.class).build();

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
