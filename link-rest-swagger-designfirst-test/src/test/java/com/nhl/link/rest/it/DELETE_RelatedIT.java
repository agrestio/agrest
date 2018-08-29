package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.swagger.api.v1.service.E2Resource;
import com.nhl.link.rest.swagger.api.v1.service.E3Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class DELETE_RelatedIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
    }


    @Test
    public void testDelete_ValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

        Response r1 = target("/v1/e2/1/e3s/9").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
    }

    @Test
    public void testDelete_ValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

        Response r1 = target("/v1/e3/9/e2/1").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
    }

    @Test
    public void testDelete_ValidRel_ToOne_All() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

        Response r1 = target("/v1/e3/9/e2").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
    }

    @Test
    public void testDelete_NoSuchId_Source() {
        Response r1 = target("/v1/e2/22/e3s/9").request().delete();
        assertEquals(Status.NOT_FOUND.getStatusCode(), r1.getStatus());

        String responseEntity = r1.readEntity(String.class).replaceFirst("\\'[\\d]+\\'", "''");
        assertEquals("{\"success\":false,\"message\":\"No object for ID '' and entity 'E2'\"}", responseEntity);
    }

}
