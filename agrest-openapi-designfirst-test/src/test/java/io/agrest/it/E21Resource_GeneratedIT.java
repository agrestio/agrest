package io.agrest.it;

import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E21;
import io.agrest.swagger.api.v1.service.E21Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class E21Resource_GeneratedIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E21Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E21.class};
    }

    @Test
    public void test_SelectById_MultiId() {

        e21().insertColumns("age", "name")
                .values(18, "John").exec();

        Response r = target("/v1/e21")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description")
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");
    }


    @Test
    public void testPOST_Exclude() {

        Response r = target("/v1/e21")
                .queryParam("exclude", "description")
                .request()
                .post(Entity.json("{\"id\":{\"age\":18,\"name\":\"John\"}}"));

        onResponse(r).statusEquals(Response.Status.CREATED)
                .bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");
        e21().matcher().assertOneMatch();
    }

    @Test
    public void testPUT() {
        e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        Response r = target("/v1/e21").queryParam("age", 18)
                .queryParam("name", "John")
                .request().put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(r).bodyEquals(1,
                "{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        e21().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testDELETE_MultiId() {

        e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        Response r = target("/v1/e21")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .request()
                .delete();

        onSuccess(r).bodyEquals("{\"success\":true}");

        e21().matcher().assertOneMatch();
        e21().matcher().eq("name", "Brian").eq("age", 27).assertOneMatch();
    }
}
