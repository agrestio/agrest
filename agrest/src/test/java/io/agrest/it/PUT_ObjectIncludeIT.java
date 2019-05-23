package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class PUT_ObjectIncludeIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    @Test
    public void testOverlap() {
        e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        e2().insertColumns("id_", "name").values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        Response response = target("/e3/3")
                .queryParam("include", "e2")
                .queryParam("include", "e2.id")
                .queryParam("include", "e5.id")
                .queryParam("include", "e5")
                .request()
                .put(Entity.json("{\"id\":3}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8},\"e5\":{\"id\":45},\"name\":\"zzz\",\"phoneNumber\":null}");

        e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void testToOne() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        Response response = target("/e3/3")
                .queryParam("include", "e2")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":1}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"name\":\"zzz\",\"phoneNumber\":null}");
        e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToMany() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        Response response = target("/e2/1")
                .queryParam("include", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.getName())
                .request().put(Entity.json("{\"e3s\":[3,4,5]}"));

        onSuccess(response).bodyEquals(1, "{\"address\":null,\"e3s\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null},{\"id\":4,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":5,\"name\":\"bbb\",\"phoneNumber\":null}],\"name\":\"xxx\"}");
        e3().matcher().eq("e2_id", 1).assertMatches(3);
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
