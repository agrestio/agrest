package io.agrest.it;

import io.agrest.DataResponse;
import io.agrest.Ag;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E4;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

public class GET_SizeConstraintsIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    // TODO: unclear what server-side fetch offset protects? so not testing it here.

    @Test
    public void testNoClientLimit() {

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (1),(2),(3)");
        newContext().performGenericQuery(insert);

        Response r = target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .request()
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1},{\"id\":2}],\"total\":3}",
                r.readEntity(String.class));
    }

    @Test
    public void testClientLimitBelowServerLimit() {

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (1),(2),(3)");

        newContext().performGenericQuery(insert);

        Response r = target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .queryParam("limit", "1")
                .request()
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1}],\"total\":3}",
                r.readEntity(String.class));
    }

    @Test
    public void testClientLimitExceedsServerLimit() {

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e4 (id) values (1),(2),(3)");

        newContext().performGenericQuery(insert);

        Response r = target("/e4/limit")
                .queryParam("sort", "id")
                .queryParam("include", "id")
                .queryParam("limit", "5")
                .request()
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1},{\"id\":2}],\"total\":3}",
                r.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4/limit")
        public DataResponse<E4> limit(@Context UriInfo uriInfo) {
            return Ag.select(E4.class, config).uri(uriInfo)
                    .fetchLimit(2)
                    .get();
        }
    }
}
