package com.nhl.link.rest.sencha.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E14;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Sencha_POST_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E3Resource.class);
        context.register(Resource.class);
    }

    @Test
    public void testPost_ToOne() {

        performQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        performQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

        Response response1 = target("/e3").request()
                .post(Entity.entity("{\"e2_id\":8,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E3 e3 = (E3) Cayenne.objectForQuery(newContext(), new SelectQuery<E3>(E3.class));
        int id = Cayenne.intPKForObject(e3);

        assertEquals(
                "{\"success\":true,\"data\":[{\"id\":" + id + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().invalidateObjects(e3);
        assertEquals("MM", e3.getName());
        assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
    }

    @Test
    public void testPost_ToOne_BadFK() throws WebApplicationException, IOException {

        performQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (1, 'xxx')"));
        performQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e2 (id, name) values (8, 'yyy')"));

        Response response1 = target("/e3").request()
                .post(Entity.entity("{\"e2_id\":15,\"name\":\"MM\"}", MediaType.APPLICATION_JSON));

        assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());

        assertEquals(0, newContext().select(new SelectQuery<E3>(E3.class)).size());
    }

    @Test
    public void testPOST_Bulk_LongId() throws WebApplicationException, IOException {

        Entity<String> entity = jsonEntity(
                "[{\"id\":\"ext-record-6881\",\"name\":\"yyy\"}" + ",{\"id\":\"ext-record-6882\",\"name\":\"zzz\"}]");
        Response response = target("/e14/").request().post(entity);
        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

        String data = response.readEntity(String.class);
        assertTrue(data.contains("\"total\":2"));

        assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e14"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e14")
        public DataResponse<E14> post(String data) {
            return LinkRest.create(E14.class, config).syncAndSelect(data);
        }
    }
}
