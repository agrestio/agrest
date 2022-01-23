package io.agrest.jaxrs;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.jaxrs.junit.AgPojoTester;
import io.agrest.jaxrs.junit.PojoTest;
import io.agrest.jaxrs.pojo.model.P1;
import io.agrest.jaxrs.pojo.model.P2;
import io.agrest.spi.AgExceptionMapper;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.di.Module;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class AgBuilder_ExceptionMappersIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class)
            .agCustomizer(b -> b.module(exceptionsModule()))
            .build();

    private static Module exceptionsModule() {
        return cb -> cb
                .bindMap(AgExceptionMapper.class)
                .put(AgException.class.getName(), TestAgExceptionMapper.class)
                .put(TestException.class.getName(), TestExceptionMapper.class);
    }

    @Test
    public void testExceptionMapper() {

        // override standard mapper
        tester.target("/agexception").get()
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"_ag__ag_exception_\"}");

        // install custom mapper
        tester.target("/testexception").get()
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"_test__test_exception_\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("agexception")
        public DataResponse<P1> agException(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P1.class, config)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                        throw AgException.forbidden("_ag_exception_");
                    })
                    .get();
        }

        @GET
        @Path("testexception")
        public DataResponse<P2> testException(@Context UriInfo uriInfo) {
            return AgJaxrs.select(P2.class, config)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                        throw new TestException("_test_exception_");
                    })
                    .get();
        }
    }

    public static class TestAgExceptionMapper implements AgExceptionMapper<AgException> {

        @Override
        public AgException toAgException(AgException e) {
            return AgException.internalServerError(e, "_ag_%s", e.getMessage());
        }
    }

    public static class TestExceptionMapper implements AgExceptionMapper<TestException> {

        @Override
        public AgException toAgException(TestException e) {
            return AgException.internalServerError(e, "_test_%s", e.getMessage());
        }
    }

    public static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }
}
