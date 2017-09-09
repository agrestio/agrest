package com.nhl.link.rest.it;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E24;
import com.nhl.link.rest.it.fixture.cayenne.E4;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DELETE_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testDelete() throws IOException {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));

        Response response1 = target("/e4/8").request().delete();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true}", response1.readEntity(String.class));

        assertEquals(1l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));
    }

    @Test
    public void testDelete_CompoundId() {

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");

        Response response1 = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request().delete();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true}", response1.readEntity(String.class));

        assertEquals(1l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E17 a")));
        assertEquals("bbb", Cayenne.objectForQuery(newContext(),
                new EJBQLQuery("select a.name from E17 a where a.id1 =2 and a.id2 = 2")));
    }

    @Test
    public void testDelete_BadID() throws IOException {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));

        Response response1 = target("/e4/7").request().delete();
        assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}",
                response1.readEntity(String.class));

        assertEquals(2l, Cayenne.objectForQuery(newContext(), new EJBQLQuery("select count(a) from E4 a")));
    }

    @Test
    public void testDelete_Twice() throws IOException {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        Response response1 = target("/e4/8").request().delete();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"success\":true}", response1.readEntity(String.class));

        Response response2 = target("/e4/8").request().delete();
        assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}",
                response2.readEntity(String.class));
    }

    @Test
    public void test_Delete_UpperCasePK() {

        insert("e24", "TYPE, NAME", "1, 'xyz'");

        Response response1 = target("/e24/1").request().delete();
        assertEquals(Status.OK.getStatusCode(), response1.getStatus());
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("e4/{id}")
        public SimpleResponse deleteById(@PathParam("id") int id) {
            return LinkRest.service(config).delete(E4.class, id);
        }

        @DELETE
        @Path("e17")
        public SimpleResponse deleteByMultiId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1_PK_COLUMN, id1);
            ids.put(E17.ID2_PK_COLUMN, id2);

            return LinkRest.service(config).delete(E17.class, ids);
        }

        @DELETE
        @Path("e24/{id}")
        public SimpleResponse deleteE24ById(@PathParam("id") int id) {
            return LinkRest.delete(E24.class, config).id(id).delete();
        }
    }
}
