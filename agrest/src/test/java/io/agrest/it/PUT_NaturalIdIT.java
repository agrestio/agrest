package io.agrest.it;

import io.agrest.AgREST;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PUT_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_PUT_SingleId() {
        insert("e20", "name", "'John'");
        insert("e20", "name", "'Brian'");

        Response response = target("/single-id/John")
                .request()
                .put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(response).bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e20 WHERE age = 28 AND description = 'zzz'"));
    }

    @Test
    public void test_PUT_Single_Id_SeveralExistingObjects() {
        insert("e20", "name", "'John'");
        insert("e20", "name", "'John'");

        Response response = target("/single-id/John").request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response.readEntity(String.class));
    }

    @Test
    public void test_PUT_MultiId() {
        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "27, 'Brian'");

        Response response = target("/multi-id/byid").queryParam("age", 18)
                .queryParam("name", "John")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(response).bodyEquals(1,
                "{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e21 WHERE age = 28 AND description = 'zzz'"));
    }

    @Test
    public void test_PUT_SeveralExistingObjects_MultiId() {
        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "18, 'John'");

        Response response = target("/multi-id/byid").queryParam("age", 18).queryParam("name", "John")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("single-id/{id}")
        public DataResponse<E20> createOrUpdate_E20(
                @PathParam("id") String name,
                EntityUpdate<E20> update,
                @Context UriInfo uriInfo) {

            return AgREST.idempotentCreateOrUpdate(E20.class, config).id(name).uri(uriInfo).syncAndSelect(update);
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
            return AgREST.idempotentCreateOrUpdate(E21.class, config).id(id).uri(uriInfo).syncAndSelect(update);
        }
    }

}
