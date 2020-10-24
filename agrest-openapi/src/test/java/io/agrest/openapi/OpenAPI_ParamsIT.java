package io.agrest.openapi;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.pojo.model.P1;
import io.agrest.unit.AgPojoTester;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class OpenAPI_ParamsIT extends OpenAPITest {

    @BQTestTool
    static final AgPojoTester tester = tester(UriInfoResource.class).build();

    @Test
    public void testUriInfoParameters() {
        tester.target("/oapi").get()
                .wasOk()
                .bodyEquals("...");
    }

    @Path("uri-info")
    public static class UriInfoResource {

        @Context
        private Configuration config;

        @GET
        @Path("p1")
        public DataResponse<P1> get(@Context UriInfo uriInfo) {
            return Ag.select(P1.class, config).uri(uriInfo).get();
        }
    }
}
