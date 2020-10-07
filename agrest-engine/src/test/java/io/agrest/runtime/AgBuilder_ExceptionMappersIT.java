package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.pojo.model.P1;
import io.agrest.pojo.model.P2;
import io.agrest.unit.AgPojoTester;
import io.agrest.unit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.di.Module;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;

public class AgBuilder_ExceptionMappersIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = tester(Resource.class)
            .agCustomizer(b -> b.module(exceptionsModule()))
            .build();

    private static Module exceptionsModule() {
        return cb -> cb
                .bindMap(ExceptionMapper.class)
                .put(AgException.class.getName(), TestAgExceptionMapper.class)
                .put(TestException.class.getName(), TestExceptionMapper.class);
    }

    @Test
    public void testExceptionMapper() {

        // override standard mapper
        tester.target("/agexception").get()
                .wasServerError()
                .bodyEquals("_ag__ag_exception_");

        // install custom mapper
        tester.target("/testexception").get()
                .wasServerError()
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
