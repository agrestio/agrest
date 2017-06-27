package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E25Resource;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GET_DynamicAttributesIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E25Resource.class);
    }

    @Test
    public void testGet() throws WebApplicationException, IOException {

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e25 (id, name) values (1, 'xxx')");
        newContext().performGenericQuery(insert);

        Response r = target("/e25").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"name\":\"xxx\"}],\"total\":1}", r.readEntity(String.class));
    }

    @Test
    public void testMapBy() throws WebApplicationException, IOException {

        SQLTemplate insert = new SQLTemplate(E4.class, "INSERT INTO utest.e25 (id, name) values (1, 'xxx')");
        newContext().performGenericQuery(insert);

        Response r = target("/e25").queryParam("mapBy", "name").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"data\":{\"xxx\":[{\"id\":1,\"name\":\"xxx\"}]},\"total\":1}", r.readEntity(String.class));
    }
}
