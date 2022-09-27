package io.agrest.jaxrs2;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.jaxrs2.junit.AgPojoTester;
import io.agrest.jaxrs2.junit.PojoTest;
import io.agrest.jaxrs2.pojo.model.P1;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GET_ExceptionIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void testNoData() {
        tester.target("/nodata").get()
                .wasNotFound()
                .bodyEquals("{\"message\":\"request failed\"}");
    }

    @Test
    public void testNoData_WithThrowable() {
        tester.target("/nodata/th").get()
                .wasServerError()
                .mediaTypeEquals(MediaType.APPLICATION_JSON_TYPE)
                .bodyEquals("{\"message\":\"request failed with th\"}");
    }

    @Test
    public void testNoData_WithWebApplicationException() {
        // must not be wrapped in AgException, and the status must be preserved
        tester.target("/nodata/wae").get()
                .wasForbidden()
                .mediaTypeEquals(MediaType.TEXT_HTML_TYPE.withCharset("iso-8859-1"));
    }

    @Test
    public void testNoData_WithWebApplicationException_InsidePipeline() {
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
