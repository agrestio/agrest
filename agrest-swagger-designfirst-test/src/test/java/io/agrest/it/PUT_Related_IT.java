package io.agrest.it;

import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.swagger.api.v1.service.E2Resource;
import io.agrest.swagger.api.v1.service.E3Resource;
import io.agrest.swagger.api.v1.service.E4Resource;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class PUT_Related_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
        context.register(E4Resource.class);
    }

    @Test
    public void testRelate_EmptyPutWithID() {

        insert("e2", "id, name", "24, 'xxx'");
        insert("e3", "id, name", "7, 'zzz'");
        insert("e3", "id, name", "8, 'yyy'");

        // POST with empty body ... how bad is that?
        Response r1 = target("/v1/e3/8/e2/24").request().put(Entity.json(""));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"xxx\"}],\"total\":1}",
                r1.readEntity(String.class));

        assertEquals("yyy", stringForQuery("SELECT name FROM utest.e3 WHERE e2_id = 24"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing() {

        insert("e2", "id, name", "24, 'xxx'");
        insert("e3", "id, name", "7, 'zzz'");
        insert("e3", "id, name", "8, 'yyy'");

        Response response = target("/v1/e3/8/e2/24").request().put(Entity.json("{}"));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"xxx\"}],\"total\":1}",
                response.readEntity(String.class));

        assertEquals("yyy", stringForQuery("SELECT name FROM utest.e3 WHERE e2_id = 24"));
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing_WithUpdate() {

        insert("e2", "id, name", "24, 'xxx'");
        insert("e3", "id, name", "7, 'zzz'");
        insert("e3", "id, name", "8, 'yyy'");

        Response r1 = target("/v1/e3/8/e2/24")
                .request()
                .put(Entity.json("{\"name\":\"123\"}"));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"123\"}],\"total\":1}",
                r1.readEntity(String.class));

        assertEquals(1, countRows(E2.class));
        assertEquals("yyy", stringForQuery("SELECT name FROM utest.e3 WHERE e2_id = 24"));
    }


    @Test
    public void testRelate_ValidRel_ToOne_New_AutogenId() {

        insert("e3", "id, name", "7, 'zzz'");
        insert("e3", "id, name", "8, 'yyy'");

        Response r1 = target("/v1/e3/8/e2/24")
                .request()
                .put(Entity.json("{\"name\":\"123\"}"));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Can't create 'E2' with fixed id\"}",
                r1.readEntity(String.class));

        assertEquals(0, intForQuery("SELECT count(1) FROM utest.e2"));
    }

    @Test
    public void testRelate_ToMany_NoIds() {

        insert("e2", "id, name", "15, 'xxx'");
        insert("e2", "id, name", "16, 'xxx'");

        insert("e3", "id, name, e2_id", "7, 'zzz', 16");
        insert("e3", "id, name, e2_id", "8, 'yyy', 15");
        insert("e3", "id, name, e2_id", "9, 'aaa', 15");

        // we can't PUT an object with generated ID , as the request is non-repeatable
        Response r1 = target("/v1/e2/15/e3s")
                .request()
                .put(Entity.json("[ {\"name\":\"newname\"} ]"));

        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Request is not idempotent.\"}", r1.readEntity(String.class));
        assertEquals(3, countRows(E3.class));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));
    }

}
