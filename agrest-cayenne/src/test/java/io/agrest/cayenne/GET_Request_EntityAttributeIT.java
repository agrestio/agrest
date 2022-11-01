package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_Request_EntityAttributeIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E3.class, E4.class)
            .build();

    @Test
    public void testRequest_Property() {

        tester.e4().insertColumns("id").values(1).values(2).exec();

        tester.target("/e4/calc_property")
                .queryParam("include", "id")
                .queryParam("include", "x")
                .queryParam("sort", "id")

                .get().wasOk().bodyEquals(2, "{\"id\":1,\"x\":\"y_1\"},{\"id\":2,\"x\":\"y_2\"}");
    }

    @Test
    public void testRequest_Property_Exclude() {

        tester.e4().insertColumns("id").values(1).values(2).exec();

        tester.target("/e4/calc_property")
                .queryParam("include", "id")
                .queryParam("sort", "id")

                .get().wasOk().bodyEquals(2, "{\"id\":1},{\"id\":2}");
    }

    @Test
    public void testRequest_ShadowProperty() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "x")
                .values(8, "y").exec();

        tester.target("/e3/custom_encoding")
                .queryParam("include", "name")
                .queryParam("sort", "id")

                .get().wasOk().bodyEquals(2, "{\"name\":\"_x_\"},{\"name\":\"_y_\"}");
    }

    @Test
    public void testRequest_ShadowProperty_Exclude() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "x")
                .values(2, "y").exec();

        tester.target("/e3/custom_encoding")
                .queryParam("include", "id")
                .queryParam("sort", "id")

                .get().wasOk().bodyEquals(2, "{\"id\":1},{\"id\":2}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4/calc_property")
        public DataResponse<E4> property_WithReader(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters())
                    .entityAttribute("x", String.class, o -> "y_" + Cayenne.intPKForObject(o))
                    .get();
        }

        @GET
        @Path("e3/custom_encoding")
        public DataResponse<E3> replaceProperty_WithReader(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters())
                    .entityAttribute(E3.NAME.getName(), String.class, o -> "_" + o.getName() + "_")
                    .get();
        }
    }
}
