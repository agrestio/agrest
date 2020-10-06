package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class GET_NaturalIdIT extends JerseyAndDerbyCase {


    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E20.class, E21.class};
    }

    @Test
    public void test_SelectById() {

        e20().insertColumns("name_col").values("John").exec();

        Response r1 = target("/single-id/John").queryParam("exclude", "age", "description").request().get();
        onSuccess(r1).bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");

        e20().insertColumns("name_col").values("John").exec();

        Response r2 = target("/single-id/John").queryParam("exclude", "age", "description").request().get();
        onResponse(r2).statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}");
    }

    @Test
    public void test_SelectById_MultiId() {

        e21().insertColumns("age", "name").values(18, "John").exec();

        Response r1 = target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description").request().get();
        onSuccess(r1).bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");


        e21().insertColumns("age", "name").values(18, "John").exec();

        Response r2 = target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description").request().get();
        onResponse(r2).statusEquals(Response.Status.INTERNAL_SERVER_ERROR)
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("single-id/{id}")
        public DataResponse<E20> getE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return Ag.service(config).selectById(E20.class, name, uriInfo);
        }

        @GET
        @Path("multi-id/byid")
        public DataResponse<E21> getE21ById(@QueryParam("age") int age,
                                            @QueryParam("name") String name,
                                            @Context UriInfo uriInfo) {
            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return Ag.service(config).select(E21.class).byId(id).uri(uriInfo).getOne();
        }
    }
}
