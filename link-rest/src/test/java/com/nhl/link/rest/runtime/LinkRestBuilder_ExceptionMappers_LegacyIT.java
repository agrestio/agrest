package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

@Deprecated
public class LinkRestBuilder_ExceptionMappers_LegacyIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Override
    protected LinkRestBuilder doConfigure() {
        // this API is deprecated. Keeping the test around until it is fully removed.
        return super.doConfigure()
                .mapException(TestLrExceptionMapper.class)
                .mapException(TestExceptionMapper.class);
    }

    @Test
    public void testExceptionMapper() {

        // override standard mapper
        Response r1 = target("/lrexception").request().get();
        onResponse(r1)
                .statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("_lr__lr_exception_");

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
        @Path("lrexception")
        public DataResponse<E2> lrException(@Context UriInfo uriInfo) {
            throw new LinkRestException(Response.Status.FORBIDDEN, "_lr_exception_");
        }

        @GET
        @Path("testexception")
        public DataResponse<E2> testException(@Context UriInfo uriInfo) {
            throw new TestException("_test_exception_");
        }
    }

    public static class TestLrExceptionMapper implements ExceptionMapper<LinkRestException> {

        @Override
        public Response toResponse(LinkRestException exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("_lr_" + exception.getMessage())
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