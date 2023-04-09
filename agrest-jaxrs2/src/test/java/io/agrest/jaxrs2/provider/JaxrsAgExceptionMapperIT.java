package io.agrest.jaxrs2.provider;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.jaxrs2.junit.AgPojoTester;
import io.agrest.jaxrs2.junit.PojoTest;
import io.agrest.jaxrs2.junit.pojo.P1;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class JaxrsAgExceptionMapperIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void testException() {
        tester.target("/p1").get()
                .wasForbidden()
                .bodyEquals("{\"message\":\"_was_forbidden_\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @GET
        @Path("p1")
        public DataResponse<P1> getE2(@Context UriInfo uriInfo) {
            throw AgException.forbidden("_was_forbidden_");
        }
    }
}
