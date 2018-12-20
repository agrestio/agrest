package io.agrest.it;

import io.agrest.DataResponse;
import io.agrest.Ag;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E22;
import io.agrest.it.fixture.cayenne.E25;
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

public class GET_DynamicAttributesIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E25Resource.class);
    }

    @Test
    public void testGet() {

        SQLTemplate insert = new SQLTemplate(E25.class, "INSERT INTO utest.e25 (id, name) values (1, 'xxx')");
        newContext().performGenericQuery(insert);

        Response r = target("/e25").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"name\":\"xxx\"}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void testGet_Rel() {
        newContext().performGenericQuery(
                new SQLTemplate(E22.class, "INSERT INTO utest.e22 (id, name) values (3, 'yyy')"));
        newContext().performGenericQuery(
                new SQLTemplate(E25.class, "INSERT INTO utest.e25 (id, name, e22_id) values (1, 'xxx', 3)"));

        Response r = target("/e25")
                .queryParam("include", "id")
                .queryParam("include", "e22.id")
                .request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"e22\":{\"id\":3}}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void testMapBy() {

        SQLTemplate insert = new SQLTemplate(E25.class, "INSERT INTO utest.e25 (id, name) values (1, 'xxx')");
        newContext().performGenericQuery(insert);

        Response r = target("/e25").queryParam("mapBy", "name").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":{\"xxx\":[{\"id\":1,\"name\":\"xxx\"}]},\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void testMapByRel() {

        newContext().performGenericQuery(
                new SQLTemplate(E22.class, "INSERT INTO utest.e22 (id, name) values (3, 'yyy')"));
        newContext().performGenericQuery(
                new SQLTemplate(E22.class, "INSERT INTO utest.e22 (id, name) values (4, 'zzzz')"));
        newContext().performGenericQuery(
                new SQLTemplate(E25.class, "INSERT INTO utest.e25 (id, name, e22_id) values (1, 'xxx', 3)"));
        newContext().performGenericQuery(
                new SQLTemplate(E25.class, "INSERT INTO utest.e25 (id, name, e22_id) values (2, 'yyy', 4)"));
        newContext().performGenericQuery(
                new SQLTemplate(E25.class, "INSERT INTO utest.e25 (id, name, e22_id) values (3, 'zzz', 3)"));

        Response r = target("/e25")
                .queryParam("mapBy", "e22.id")
                .queryParam("include", "id")
                .queryParam("include", "e22.id")
                .request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":{" +
                "\"3\":[{\"id\":1,\"e22\":{\"id\":3}},{\"id\":3,\"e22\":{\"id\":3}}]," +
                "\"4\":[{\"id\":2,\"e22\":{\"id\":4}}]" +
                "},\"total\":3}", r.readEntity(String.class));
    }

    @Path("e25")
    public static class E25Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<E25> getAll(@Context UriInfo uriInfo) {
            return Ag.select(E25.class, config).uri(uriInfo).get();
        }
    }
}
