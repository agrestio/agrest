package io.agrest.it;

import io.agrest.DataResponse;
import io.agrest.LinkRest;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import io.agrest.it.fixture.cayenne.E4;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GET_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_SelectById() {

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e20 (name) values ('John')"));

        Response response1 = target("/single-id/John").queryParam("exclude", "age", "description").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":\"John\",\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e20 (name) values ('John')"));

        Response response2 = target("/single-id/John").queryParam("exclude", "age", "description").request().get();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response2.readEntity(String.class));
    }

    @Test
    public void test_SelectById_MultiId() {

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e21 (age, name) values (18, 'John')"));

        Response response1 = target("/multi-id/byid")
                .queryParam("age", 18).queryParam("name", "John")
                .queryParam("exclude", "description").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        assertEquals("{\"data\":[{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().performGenericQuery(new SQLTemplate(E4.class, "INSERT INTO utest.e21 (age, name) values (18, 'John')"));

        Response response2 = target("/multi-id/byid")
                .queryParam("age", 18).queryParam("name", "John")
                .queryParam("exclude", "description").request().get();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response2.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("single-id/{id}")
        public DataResponse<E20> getE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return LinkRest.service(config).selectById(E20.class, name, uriInfo);
        }

        @GET
        @Path("multi-id/byid")
        public DataResponse<E21> getE21ById(@QueryParam("age") int age, @QueryParam("name") String name,
                                            @Context UriInfo uriInfo) {
            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return LinkRest.service(config).select(E21.class).byId(id).uri(uriInfo).getOne();
        }
    }
}
