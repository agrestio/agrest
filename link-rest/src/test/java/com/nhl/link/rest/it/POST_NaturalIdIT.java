package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class POST_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPost_SingleId() {

        Response response1 = target("/single-id")
                .queryParam("exclude", "age", "description")
                .request()
                .post(Entity.json("{\"id\":\"John\"}"));
        assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

        E20 e20 = ObjectSelect.query(E20.class).selectFirst(newContext());
        assertNotNull(e20);
        assertEquals("John", e20.getName());

        assertEquals("{\"data\":[{\"id\":\"John\",\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        Response response2 = target("/single-id").queryParam("exclude", "age", "description").request().post(
                Entity.json("{\"id\":\"John\"}"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        assertTrue(response2.readEntity(String.class).contains("object already exists"));
    }

    @Test
    public void testPost_MultiId() {

        Response response1 = target("/multi-id").queryParam("exclude", "description").request().post(
                Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));
        assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());

        E21 e21 = ObjectSelect.query(E21.class).selectFirst(newContext());
        assertNotNull(e21);
        assertEquals(Integer.valueOf(18), e21.getAge());
        assertEquals("John", e21.getName());

        assertEquals("{\"data\":[{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        Response response2 = target("/multi-id").queryParam("exclude", "description").request().post(
                Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.getStatus());
        assertTrue(response2.readEntity(String.class).contains("object already exists"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("single-id")
        public DataResponse<E20> createE20(EntityUpdate<E20> update, @Context UriInfo uriInfo) {
            return LinkRest.create(E20.class, config).uri(uriInfo).syncAndSelect(update);
        }

        @POST
        @Path("multi-id")
        public DataResponse<E21> createE21(EntityUpdate<E21> update, @Context UriInfo uriInfo) {
            return LinkRest.create(E21.class, config).uri(uriInfo).syncAndSelect(update);
        }
    }

}
