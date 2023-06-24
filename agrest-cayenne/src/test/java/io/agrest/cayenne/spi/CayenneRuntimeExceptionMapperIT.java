package io.agrest.cayenne.spi;


import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.CayenneRuntimeException;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class CayenneRuntimeExceptionMapperIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class).build();

    @Test
    public void exception() {
        String cayenneVersion = CayenneRuntimeException.getExceptionLabel();
        String expected = String.format(
                "{\"message\":\"CayenneRuntimeException %s_something_w_cayenne_\"}",
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

