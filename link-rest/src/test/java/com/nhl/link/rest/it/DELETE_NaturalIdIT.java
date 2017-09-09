package com.nhl.link.rest.it;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DELETE_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testDelete_SingleId() {

        insert("e20", "name", "'John'");
        insert("e20", "name", "'Brian'");

        Response response = target("/single-id/John").request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":true}", response.readEntity(String.class));

        assertEquals(1L, Cayenne.objectForQuery(newContext(),
                new EJBQLQuery("select count(a) from E20 a WHERE a.name = 'Brian'")));
    }

    @Test
    public void testDelete_MultiId() {

        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "27, 'Brian'");

        Response response = target("/multi-id")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .request()
                .delete();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":true}", response.readEntity(String.class));

        assertEquals(1L, countRows(E21.class, E21.AGE.eq(27).andExp(E21.NAME.eq("Brian"))));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("multi-id")
        public SimpleResponse deleteE21ById(@QueryParam("age") int age, @QueryParam("name") String name) {
            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return LinkRest.service(config).delete(E21.class).id(id).delete();
        }

        @DELETE
        @Path("single-id/{id}")
        public SimpleResponse deleteE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return LinkRest.service(config).delete(E20.class, name);
        }
    }
}
