package io.agrest.cayenne.provider;

import io.agrest.DataResponse;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.CayenneException;
import org.apache.cayenne.CayenneRuntimeException;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

public class CayenneRuntimeExceptionMapperIT extends JerseyAndDerbyCase {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)

            .build();

    @Test
    public void testException() {
        String cayenneVersion = CayenneException.getExceptionLabel();
        String expected = String.format(
                "{\"success\":false,\"message\":\"CayenneRuntimeException %s_something_w_cayenne_\"}",
                cayenneVersion);

        tester.target("/g1").get()
                .wasServerError()
                .bodyEquals(expected);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("g1")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            throw new CayenneRuntimeException("_something_w_cayenne_");
        }
    }
}

