package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.inheritance.Aie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Aie1Super;
import io.agrest.cayenne.cayenne.inheritance.Ie2;
import io.agrest.cayenne.cayenne.inheritance.Ie3;
import io.agrest.cayenne.unit.inheritance.InheritanceDbTest;
import io.agrest.cayenne.unit.inheritance.InheritanceModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class PropFilter_Annotations_InheritanceIT extends InheritanceDbTest {

    @BQTestTool
    static final InheritanceModelTester tester = tester(Resource.class)
            .entities(Aie1Super.class, Ie2.class, Ie3.class)
            .build();

    @Test
    public void aie1super() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3")
                .values(10, 1, "v01", "v11", null, null)
                .values(15, 3, "v01", "v12", null, "v31")
                .values(20, 2, "v02", null, "v21", null)
                .exec();

        tester.target("/aie1super")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\",\"type\":1}",
                        "{\"id\":15,\"a0\":\"v01\",\"a1\":\"v12\",\"a3\":\"v31\",\"type\":3}",
                        "{\"id\":20,\"a2\":\"v21\",\"type\":2}");
    }

    @Test
    public void aie1super_RequestOverlay() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2", "a3")
                .values(10, 1, "v01", "v11", null, null)
                .values(15, 3, "v01", "v12", null, "v31")
                .values(20, 2, "v02", null, "v21", null)
                .exec();

        tester.target("/aie1super-request-overlay")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":10,\"a0\":\"v01\",\"type\":1}",
                        "{\"id\":15,\"a0\":\"v01\",\"a3\":\"v31\",\"type\":3}",
                        "{\"id\":20,\"a2\":\"v21\",\"type\":2}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("aie1super")
        public DataResponse<Aie1Super> ie1super1(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Aie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("aie1super-request-overlay")
        public DataResponse<Aie1Super> ie1super2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Aie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(Aie1Sub1.class, p -> p.property("a1", false))
                    .get();
        }
    }
}
