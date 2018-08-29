package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.swagger.api.v1.service.E20Resource;
import com.nhl.link.rest.swagger.api.v1.service.E21Resource;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class PUT_NaturalIdIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E20Resource.class);
        context.register(E21Resource.class);
    }

    @Test
    public void test_PUT_SingleId() {
        insert("e20", "name", "'John'");
        insert("e20", "name", "'Brian'");

        Response response = target("/v1/e20/John")
                .request()
                .put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(response).bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e20 WHERE age = 28 AND description = 'zzz'"));
    }

    @Test
    public void test_PUT_Single_Id_SeveralExistingObjects() {
        insert("e20", "name", "'John'");
        insert("e20", "name", "'John'");

        Response response = target("/v1/e20/John").request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}",
                response.readEntity(String.class));
    }

    @Test
    public void test_PUT_MultiId() {
        insert("e21", "age, name", "18, 'John'");
        insert("e21", "age, name", "27, 'Brian'");

        Response response = target("/v1/e21").queryParam("age", 18)
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

        Response response = target("/v1/e21").queryParam("age", 18).queryParam("name", "John")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}",
                response.readEntity(String.class));
    }

}
