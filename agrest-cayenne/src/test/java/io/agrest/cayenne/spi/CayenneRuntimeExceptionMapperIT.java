package io.agrest.cayenne.spi;


import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.CayenneRuntimeException;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class CayenneRuntimeExceptionMapperIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class).build();

    @Test
    public void testException() {
        String cayenneVersion = CayenneRuntimeException.getExceptionLabel();
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
            // must be thrown within Ag chain
            return AgJaxrs.select(E2.class, config)
                    .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
                        throw new CayenneRuntimeException("_something_w_cayenne_");
                    })
                    .get();
        }
    }
}

