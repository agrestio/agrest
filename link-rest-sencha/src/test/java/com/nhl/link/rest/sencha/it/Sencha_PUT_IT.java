package com.nhl.link.rest.sencha.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SelectById;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Sencha_PUT_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testPut_ToOne_FromNull() {
        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', null");

        E3 e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
        newContext().invalidateObjects(e3);
        assertNull(e3.getE2());

        Response r = target("/e3/3").request()
                .put(Entity.json("{\"id\":3,\"e2_id\":8}"));
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
                r.readEntity(String.class));

        e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
        newContext().invalidateObjects(e3);
        assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
    }

    @Test
    public void testPut_ToOne_ToNull() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response1 = target("/e3/3").request()
                .put(Entity.json("{\"id\":3,\"e2_id\":null}"));

        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        E3 e3 = SelectById.query(E3.class, 3).prefetch(E3.E2.joint()).selectOne(newContext());
        assertNull(e3.getE2());
    }

    @Test
    public void testPut_ToOne() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        E3 e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
        newContext().invalidateObjects(e3);
        assertEquals(8, Cayenne.intPKForObject(e3.getE2()));

        Response response1 = target("/e3/3").request()
                .put(Entity.json("{\"id\":3,\"e2_id\":1}"));
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        e3 = Cayenne.objectForPK(newContext(), E3.class, 3);
        newContext().invalidateObjects(e3);
        assertEquals(1, Cayenne.intPKForObject(e3.getE2()));
    }

    @Test
    public void testPut_ToOne_Relationship_Name() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response1 = target("/e3/3").request()
                .put(Entity.json("{\"id\":3,\"e2\":1}"));
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true,\"data\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id  = 1"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return LinkRest.update(E3.class, config).id(id).syncAndSelect(requestBody);
        }
    }
}
