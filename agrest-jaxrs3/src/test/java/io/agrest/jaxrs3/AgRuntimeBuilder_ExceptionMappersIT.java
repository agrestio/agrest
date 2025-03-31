package io.agrest.jaxrs3;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.jaxrs3.junit.AgPojoTester;
import io.agrest.jaxrs3.junit.PojoTest;
import io.agrest.jaxrs3.junit.pojo.P1;
import io.agrest.jaxrs3.junit.pojo.P2;
import io.agrest.spi.AgExceptionMapper;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.di.Module;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class AgRuntimeBuilder_ExceptionMappersIT extends PojoTest {

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
    public void exceptionMapper() {

        // override standard mapper
        tester.target("/agexception").get()
                .wasServerError()
                .bodyEquals("{\"message\":\"_ag__ag_exception_\"}");

        // install custom mapper
        tester.target("/testexception").get()
                .wasServerError()
                .bodyEquals("{\"message\":\"_test__test_exception_\"}");
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
