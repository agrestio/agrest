package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class POST_NaturalIdIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E20.class, E21.class};
    }

    @Test
    public void testSingleId() {

        Response r1 = target("/single-id")
                .queryParam("exclude", "age", "description")
                .request()
                .post(Entity.json("{\"id\":\"John\"}"));

        onResponse(r1)
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");

        e20().matcher().eq("name", "John").assertOneMatch();

        Response r2 = target("/single-id")
                .queryParam("exclude", "age", "description")
                .request()
                .post(Entity.json("{\"id\":\"John\"}"));

        onResponse(r2)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E20' with id {name:John} - already exists\"}");
    }

    @Test
    public void testMultiId() {

        Response r1 = target("/multi-id")
                .queryParam("exclude", "description")
                .request().post(Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));

        onResponse(r1)
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");

        e21().matcher().eq("name", "John").eq("age", 18).assertOneMatch();

        Response r2 = target("/multi-id").queryParam("exclude", "description")
                .request()
                .post(Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));

        onResponse(r2)
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E21' with id {name:John,age:18} - already exists\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("single-id")
        public DataResponse<E20> createE20(EntityUpdate<E20> update, @Context UriInfo uriInfo) {
            return Ag.create(E20.class, config).uri(uriInfo).syncAndSelect(update);
        }

        @POST
        @Path("multi-id")
        public DataResponse<E21> createE21(EntityUpdate<E21> update, @Context UriInfo uriInfo) {
            return Ag.create(E21.class, config).uri(uriInfo).syncAndSelect(update);
        }
    }

}
