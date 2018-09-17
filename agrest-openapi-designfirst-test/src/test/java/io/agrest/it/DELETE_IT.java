package io.agrest.it;

import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.swagger.api.v1.service.E4Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.EJBQLQuery;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class DELETE_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E4Resource.class);
    }

    @Test
    public void testDelete() {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        Response response = target("/v1/e4/8").request().delete();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":true}", response.readEntity(String.class));

        assertEquals(1L, countRows(E4.class));
    }

    @Test
    public void testDelete_BadID() {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));

        Response response1 = target("/v1/e4/7").request().delete();
        assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}",
                response1.readEntity(String.class));

        assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));
    }

    @Test
    public void testDelete_Twice() {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        Response response1 = target("/v1/e4/8").request().delete();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true}", response1.readEntity(String.class));

        Response response2 = target("/v1/e4/8").request().delete();
        assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}",
                response2.readEntity(String.class));
    }
}
