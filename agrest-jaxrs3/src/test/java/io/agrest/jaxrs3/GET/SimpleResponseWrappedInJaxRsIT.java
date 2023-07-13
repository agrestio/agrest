package io.agrest.jaxrs3.GET;

import io.agrest.HttpStatus;
import io.agrest.SimpleResponse;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class SimpleResponseWrappedInJaxRsIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void basic() {
        tester.target("/wrapped-data-response")
                .get()
                .wasOk()
                .bodyEquals("{\"message\":\"Hi!\"}");
    }

    @Test
    public void explicitDataResponseStatusIgnored() {
        tester.target("/wrapped-data-response/simple-response-status-ignored")
                .get()
                .wasStatus(Response.Status.PARTIAL_CONTENT.getStatusCode())
                .bodyEquals("{\"message\":\"Hi!\"}");
    }

    @Path("wrapped-data-response")
    public static class Resource {

        @GET
        public Response basic() {
            return Response.ok(SimpleResponse.of(200, "Hi!")).build();
        }

        @GET
        @Path("simple-response-status-ignored")
        public Response simpleResponseStatusIgnored() {

            return Response
                    // explicit SimpleResponse status is ignored, JAX-RS status is used
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .entity(SimpleResponse.of(200, "Hi!"))
                    .build();
        }
    }
}