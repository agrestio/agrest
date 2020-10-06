package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import io.agrest.it.fixture.cayenne.E23;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

public class PUT_NaturalIdIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E20.class, E21.class, E23.class};
    }

    @Test
    public void testSingleId() {

        e20().insertColumns("name_col")
                .values("John")
                .values("Brian").exec();

        Response r = target("/single-id/John")
                .request()
                .put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(r).bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");
        e20().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testSingle_Id_SeveralExistingObjects() {
        e20().insertColumns("name_col")
                .values("John")
                .values("John").exec();

        Response r = target("/single-id/John").request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));
        onResponse(r).statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}");
    }

    @Test
    public void testMultiId() {

        e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        Response r = target("/multi-id/byid").queryParam("age", 18)
                .queryParam("name", "John")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(r).bodyEquals(1,
                "{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");
        e21().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testSeveralExistingObjects_MultiId() {
        e21().insertColumns("age", "name")
                .values(18, "John")
                .values(18, "John").exec();

        Response r = target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onResponse(r).statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}");
    }

    @Test
    public void testNaturalIdInPayload() {

        e23().insertColumns("id", "name").values(12, "John").exec();

        Response r = target("/natural-id-in-payload")
                .request()
                .put(Entity.json("[{\"exposedId\":12,\"name\":\"Joe\"}, {\"exposedId\":10,\"name\":\"Ana\"}]"));

        onSuccess(r).bodyEquals("{\"success\":true}");
        e23().matcher().assertMatches(2);
    }

    @Test
    public void testNaturalIdInPayload_MascaradingAsId() {

        e23().insertColumns("id", "name").values(12, "John").exec();

        Response r = target("/natural-id-in-payload")
                .request()
                .put(Entity.json("[{\"id\":12,\"name\":\"Joe\"}, {\"id\":10,\"name\":\"Ana\"}]"));

        onSuccess(r).bodyEquals("{\"success\":true}");
        e23().matcher().assertMatches(2);
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("natural-id-in-payload")
        public SimpleResponse createOrUpdate_E23(
                Collection<EntityUpdate<E23>> update,
                @Context UriInfo uriInfo) {

            return Ag.idempotentCreateOrUpdate(E23.class, config).uri(uriInfo).sync(update);
        }

        @PUT
        @Path("single-id/{id}")
        public DataResponse<E20> createOrUpdate_E20(
                @PathParam("id") String name,
                EntityUpdate<E20> update,
                @Context UriInfo uriInfo) {

            return Ag.idempotentCreateOrUpdate(E20.class, config).id(name).uri(uriInfo).syncAndSelect(update);
        }

        @PUT
        @Path("multi-id/byid")
        public DataResponse<E21> createOrUpdate_E21(
                @QueryParam("age") int age,
                @QueryParam("name") String name,
                EntityUpdate<E21> update,
                @Context UriInfo uriInfo) {

            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return Ag.idempotentCreateOrUpdate(E21.class, config).id(id).uri(uriInfo).syncAndSelect(update);
        }
    }

}
