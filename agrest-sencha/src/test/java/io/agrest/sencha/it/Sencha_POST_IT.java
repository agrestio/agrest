package io.agrest.sencha.it;

import io.agrest.DataResponse;
import io.agrest.Ag;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E14;
import io.agrest.it.fixture.cayenne.E3;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Sencha_POST_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPost_ToOne() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response = target("/e3")
                .request()
                .post(Entity.json("{\"e2_id\":8,\"name\":\"MM\"}"));

        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

        E3 e3 = (E3) Cayenne.objectForQuery(newContext(), new SelectQuery<E3>(E3.class));
        int id = Cayenne.intPKForObject(e3);

        assertEquals(
                "{\"success\":true,\"data\":[{\"id\":" + id + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response.readEntity(String.class));

        newContext().invalidateObjects(e3);
        assertEquals("MM", e3.getName());
        assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
    }

    @Test
    public void testPost_ToOne_BadFK() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response = target("/e3").request()
                .post(Entity.json("{\"e2_id\":15,\"name\":\"MM\"}"));

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

        assertEquals(0, newContext().select(new SelectQuery<>(E3.class)).size());
    }

    @Test
    public void testPOST_Bulk_LongId() {

        Entity<String> entity = Entity.json(
                "[{\"id\":\"ext-record-6881\",\"name\":\"yyy\"}"
                        + ",{\"id\":\"ext-record-6882\",\"name\":\"zzz\"}]");
        Response response = target("/e14/").request().post(entity);
        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

        String data = response.readEntity(String.class);
        assertTrue(data.contains("\"total\":2"));

        assertEquals(2, countRows(E14.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @POST
        @Path("e14")
        public DataResponse<E14> post(String data) {
            return Ag.create(E14.class, config).syncAndSelect(data);
        }
    }
}
