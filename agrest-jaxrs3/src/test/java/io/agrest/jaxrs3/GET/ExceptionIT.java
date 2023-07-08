package io.agrest.jaxrs3.GET;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.agrest.jaxrs3.junit.pojo.P1;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class ExceptionIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void noData() {
        tester.target("/nodata").get()
                .wasNotFound()
                .bodyEquals("{\"message\":\"request failed\"}");
    }

    @Test
    public void noData_WithThrowable() {
        tester.target("/nodata/th").get()
                .wasServerError()
                .mediaTypeEquals(MediaType.APPLICATION_JSON_TYPE)
                .bodyEquals("{\"message\":\"request failed with th\"}");
    }

    @Test
    public void noData_WithWebApplicationException() {
        // must not be wrapped in AgException, and the status must be preserved
        tester.target("/nodata/wae").get()
                .wasForbidden()
                .mediaTypeEquals(MediaType.TEXT_HTML_TYPE.withCharset("iso-8859-1"));
    }

    @Test
    public void noData_WithWebApplicationException_InsidePipeline() {
        // AgException must be unwrapped
        tester.target("/nodata/wae_inside_ag").get()
                .wasForbidden()
                .mediaTypeEquals(MediaType.TEXT_HTML_TYPE.withCharset("iso-8859-1"));
    }


    @Path("nodata")
    public static class Resource {

        @Context
        Configuration config;

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

        @GET
        @Path("wae")
        public Response getWae() {
            throw new WebApplicationException("Was forbidden", 403);
        }

        @GET
        @Path("wae_inside_ag")
        public DataResponse<P1> getWaeInsideAg() {
            return AgJaxrs.select(P1.class, config)
                    .stage(SelectStage.START, c -> {
                        throw new WebApplicationException("Was forbidden inside pipeline", 403);
                    }).get();
        }
    }

}
