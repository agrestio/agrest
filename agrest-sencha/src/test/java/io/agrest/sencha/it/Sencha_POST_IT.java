package io.agrest.sencha.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E14;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class Sencha_POST_IT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E14.class};
    }

    @Test
    public void testPost_ToOne() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response response = target("/e3")
                .request()
                .post(Entity.json("{\"e2_id\":8,\"name\":\"MM\"}"));

        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

        Object[] row = e3().selectColumns("id", "name", "e2_id").get(0);

        assertEquals(
                "{\"success\":true,\"data\":[{\"id\":" + row[0] + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response.readEntity(String.class));

        assertEquals("MM", row[1]);
        assertEquals(8, row[2]);
    }

    @Test
    public void testPost_ToOne_BadFK() {

        e2().insertColumns("id", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response response = target("/e3").request()
                .post(Entity.json("{\"e2_id\":15,\"name\":\"MM\"}"));

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

        e3().matcher().assertNoMatches();
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

        e14().matcher().assertMatches(2);
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
