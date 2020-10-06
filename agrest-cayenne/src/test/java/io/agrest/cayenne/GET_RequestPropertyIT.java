package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.property.PropertyReader;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static io.agrest.property.PropertyBuilder.property;

public class GET_RequestPropertyIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)
            .entities(E3.class, E4.class)
            .build();

    @Test
    public void testRequest_Property() {

        tester.e4().insertColumns("id").values(1).values(2).exec();

        tester.target("/e4/calc_property")
                .queryParam("include", "id")
                .queryParam("include", "x")
                .queryParam("sort", "id")

                .get().wasSuccess().bodyEquals(2, "{\"id\":1,\"x\":\"y_1\"},{\"id\":2,\"x\":\"y_2\"}");
    }

    @Test
    public void testRequest_Property_Exclude() {

        tester.e4().insertColumns("id").values(1).values(2).exec();

        tester.target("/e4/calc_property")
                .queryParam("include", "id")
                .queryParam("sort", "id")

                .get().wasSuccess().bodyEquals(2, "{\"id\":1},{\"id\":2}");
    }

    @Test
    public void testRequest_ShadowProperty() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "x")
                .values(8, "y").exec();

        tester.target("/e3/custom_encoding")
                .queryParam("include", "name")
                .queryParam("sort", "id")

                .get().wasSuccess().bodyEquals(2, "{\"name\":\"_x_\"},{\"name\":\"_y_\"}");
    }

    @Test
    public void testRequest_ShadowProperty_Exclude() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "x")
                .values(2, "y").exec();

        tester.target("/e3/custom_encoding")
                .queryParam("include", "id")
                .queryParam("sort", "id")

                .get().wasSuccess().bodyEquals(2, "{\"id\":1},{\"id\":2}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4/calc_property")
        public DataResponse<E4> property_WithReader(@Context UriInfo uriInfo) {
            PropertyReader xReader = (root, name) -> "y_" + Cayenne.intPKForObject((DataObject) root);
            return Ag.select(E4.class, config).uri(uriInfo).property("x", property(xReader)).get();
        }

        @GET
        @Path("e3/custom_encoding")
        public DataResponse<E3> replaceProperty_WithReader(@Context UriInfo uriInfo) {

            // use case: custom encoder for the existing property...
            PropertyReader xReader = (o, name) -> "_" + ((E3) o).getName() + "_";
            return Ag.select(E3.class, config).uri(uriInfo).property(E3.NAME.getName(), property(xReader)).get();
        }
    }
}
