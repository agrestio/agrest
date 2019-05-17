package io.agrest.it;

import io.agrest.Ag;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import io.agrest.it.fixture.cayenne.E24;
import io.agrest.it.fixture.cayenne.E4;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class DELETE_NaturalIdIT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class, E17.class, E24.class};
    }

    @Test
    public void testDelete_SingleId() {

        e20().insertColumns("name")
                .values("John")
                .values("Brian").exec();

        Response r = target("/single-id/John").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        e20().matcher().assertOneMatch();
        e20().matcher().eq("name", "Brian").assertOneMatch();
    }

    @Test
    public void testDelete_MultiId() {

        e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        Response r = target("/multi-id")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .request()
                .delete();

        onSuccess(r).bodyEquals("{\"success\":true}");

        e21().matcher().assertOneMatch();
        e21().matcher().eq("name", "Brian").eq("age", 27).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("multi-id")
        public SimpleResponse deleteE21ById(@QueryParam("age") int age, @QueryParam("name") String name) {
            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return Ag.service(config).delete(E21.class).id(id).delete();
        }

        @DELETE
        @Path("single-id/{id}")
        public SimpleResponse deleteE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return Ag.service(config).delete(E20.class, name);
        }
    }
}
