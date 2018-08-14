package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.swagger.api.v1.service.E2Resource;
import com.nhl.link.rest.swagger.api.v1.service.E3Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class GET_Related_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
    }

    @Test
    public void testGet_ValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        Response r1 = target("/v1/e2/1/e3s").queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
    }

    @Test
    public void testGet_ValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        Response r1 = target("/v1/e3/7/e2").queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
    }

}
