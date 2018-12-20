package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static io.agrest.property.PropertyBuilder.property;
import static org.junit.Assert.assertEquals;

public class GET_Props_EntityProperty_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testRequestEntityProperty() {

        newContext().performGenericQuery(new SQLTemplate(E3.class, "INSERT INTO utest.e4 (id) values (1), (2)"));

        Response r = target("/e4/calc_property")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .request()
                .get();

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1,\"x\":\"y_1\"},{\"id\":2,\"x\":\"y_2\"}],\"total\":2}",
                r.readEntity(String.class));
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
    }
}
