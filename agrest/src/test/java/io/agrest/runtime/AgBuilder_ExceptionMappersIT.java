package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.it.fixture.BQJerseyTestOnPojo;
import io.agrest.it.fixture.pojo.model.P1;
import io.agrest.it.fixture.pojo.model.P2;
import org.apache.cayenne.di.Module;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.function.UnaryOperator;

public class AgBuilder_ExceptionMappersIT extends BQJerseyTestOnPojo {

    @BeforeClass
    public static void startTestRuntime() {
        UnaryOperator<AgBuilder> customizer = b -> b.module(exceptionsModule());
        startTestRuntime(customizer, Resource.class);
    }

    private static Module exceptionsModule() {
        return cb -> cb.bindMap(ExceptionMapper.class)
                // TODO: Bootique-like extender API
                .put(AgException.class.getName(), TestAgExceptionMapper.class)
                .put(TestException.class.getName(), TestExceptionMapper.class);
    }

    @Test
    public void testExceptionMapper() {

        // override standard mapper
        Response r1 = target("/agexception").request().get();
        onResponse(r1)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("_ag__ag_exception_");

        // install custom mapper
        Response r2 = target("/testexception").request().get();
        onResponse(r2)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("_test__test_exception_");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("agexception")
        public DataResponse<P1> agException(@Context UriInfo uriInfo) {
            throw new AgException(Response.Status.FORBIDDEN, "_ag_exception_");
        }

        @GET
        @Path("testexception")
        public DataResponse<P2> testException(@Context UriInfo uriInfo) {
            throw new TestException("_test_exception_");
        }
    }

    public static class TestAgExceptionMapper implements ExceptionMapper<AgException> {

        @Override
        public Response toResponse(AgException exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("_ag_" + exception.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    public static class TestExceptionMapper implements ExceptionMapper<TestException> {

        @Override
        public Response toResponse(TestException exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("_test_" + exception.getMessage())
                    .type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    public static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }
}
