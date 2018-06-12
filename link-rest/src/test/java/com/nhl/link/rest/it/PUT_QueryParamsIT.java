package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;
import com.nhl.link.rest.it.fixture.cayenne.E3;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PUT_QueryParamsIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_PUT_SingleId() {
        insert("e20", "name", "'John'");
        insert("e20", "name", "'Brian'");

        Response response = target("/single-id/John")
                .queryParam("exclude", "description")
                .request()
                .put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(response).bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"name\":\"John\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e20 WHERE age = 28 AND description = 'zzz'"));
    }

    @Test
    public void test_PUT_Single_Id_SeveralExistingObjects() {
        insert("e20", "name", "'John'");
        insert("e20", "name", "'John'");

        Response response = target("/single-id/John")
                .queryParam("exclude", "description")
                .request()
                .put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response.readEntity(String.class));
    }

    @Test
    public void test_PUT_MultiId() {
        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "27, 'Brian'");

        Response response = target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(response).bodyEquals(1,
                "{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"name\":\"John\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e21 WHERE age = 28 AND description = 'zzz'"));
    }

    @Test
    public void test_PUT_SeveralExistingObjects_MultiId() {
        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "18, 'John'");

        Response response = target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response.readEntity(String.class));
    }

    @Test
    public void testPUT_Bulk() {

        insert("e3", "id, name", "5, 'aaa'");
        insert("e3", "id, name", "4, 'zzz'");
        insert("e3", "id, name", "2, 'bbb'");
        insert("e3", "id, name", "6, 'yyy'");

        Entity<String> entity = Entity.json(
                "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]");
        Response response = target("/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", E3.NAME.getName())
                .request()
                .put(entity);

        // ordering must be preserved in response, so comparing with request entity
        onSuccess(response).bodyEquals(4,
                "{\"name\":\"yyy\"}",
                "{\"name\":\"zzz\"}",
                "{\"name\":\"111\"}",
                "{\"name\":\"333\"}");
    }

    @Test
    public void testPut_ToMany_UnrelateOne() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', null");
        insert("e3", "id, name, e2_id", "4, 'aaa', 8");
        insert("e3", "id, name, e2_id", "5, 'bbb', 8");

        Response response = target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request()
                .put(Entity.json("{\"e3s\":[4]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":4}]}");

        assertEquals(1L, countRows("e3", "WHERE e2_id = 1 AND id = 4"));
        assertEquals(1L, countRows("e3", "WHERE e2_id = 8 AND id = 5"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("single-id/{id}")
        public DataResponse<E20> createOrUpdate_E20(
                @PathParam("id") String name,
                @QueryParam("exclude") List<String> exclude,
                EntityUpdate<E20> update) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("exclude",  exclude);

            return LinkRest.idempotentCreateOrUpdate(E20.class, config).id(name).queryParams(queryParams).syncAndSelect(update);
        }

        @PUT
        @Path("multi-id/byid")
        public DataResponse<E21> createOrUpdate_E21(
                @QueryParam("age") int age,
                @QueryParam("name") String name,
                @QueryParam("exclude") List<String> exclude,
                EntityUpdate<E21> update) {

            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("age", Arrays.asList("" + age));
            queryParams.put("name", Arrays.asList(name));
            queryParams.put("exclude",  exclude);

            return LinkRest.idempotentCreateOrUpdate(E21.class, config).id(id).queryParams(queryParams).syncAndSelect(update);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> syncE3(@QueryParam("include") List<String> include,
                                       @QueryParam("exclude") List<String> exclude,
                                       String requestBody) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("include",  include);
            queryParams.put("exclude",  exclude);

            return LinkRest.idempotentFullSync(E3.class, config).queryParams(queryParams).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id,
                                                  @QueryParam("include") List<String> include,
                                                  @QueryParam("exclude") List<String> exclude,
                                                  String entityData) {

            Map<String, List<String>>  queryParams = new HashMap<>();
            queryParams.put("include",  include);
            queryParams.put("exclude",  exclude);

            return LinkRest.idempotentCreateOrUpdate(E2.class, config).id(id).queryParams(queryParams).syncAndSelect(entityData);
        }
    }

}
