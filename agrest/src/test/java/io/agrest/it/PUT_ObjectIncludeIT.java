package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;

public class PUT_ObjectIncludeIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }


    @Test
    public void testPut_Dummy() {
        insert("e5", "id, name, date", "45, 'T', '2013-01-03'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id, e5_id", "3, 'zzz', 8, 45");

        Response response = target("/e3/3")
                .queryParam("include", "e2")
                .queryParam("include", "e2.id")
                .queryParam("include", "e5.id")
                .queryParam("include", "e5")
                .request()
                .put(Entity.json("{\"id\":3}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8,\"address\":null,\"name\":\"yyy\"},\"e5\":{\"id\":45},\"name\":\"zzz\",\"phoneNumber\":null}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 8"));
    }

    @Test
    public void testPut_ToOne() {
        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response = target("/e3/3")
                .queryParam("include", E3.E2.getName())
                .queryParam("include", "e2.id")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":1}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"name\":\"zzz\",\"phoneNumber\":null}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
    }

    @Test
    public void testPut_ToOne2() {
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response = target("/e3/3")
                .queryParam("include", "e2.id")
                .request()
                .put(Entity.json("{\"id\":3}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"name\":\"zzz\",\"phoneNumber\":null}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
    }


    @Test
    public void testPut_ToMany() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        insert("e3", "id, name, e2_id", "3, 'zzz', null");
        insert("e3", "id, name, e2_id", "4, 'aaa', 8");
        insert("e3", "id, name, e2_id", "5, 'bbb', 8");

        Response response = target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("include", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().put(Entity.json("{\"e3s\":[3,4,5]}"));

        onSuccess(response).bodyEquals(1, "{\"address\":null,\"e3s\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null},{\"id\":4,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":5,\"name\":\"bbb\",\"phoneNumber\":null}],\"name\":\"xxx\"}");
        assertEquals(3L, countRows("e3", "WHERE e2_id = 1"));
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id, String entityData, @Context UriInfo uriInfo) {
            return Ag.idempotentCreateOrUpdate(E2.class, config).id(id).uri(uriInfo).syncAndSelect(entityData);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> syncE3(@Context UriInfo uriInfo, String requestBody) {
            return Ag.idempotentFullSync(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String data, @Context UriInfo uriInfo) {
            return Ag.update(E3.class, config).uri(uriInfo).id(id).syncAndSelect(data);
        }
    }
}
