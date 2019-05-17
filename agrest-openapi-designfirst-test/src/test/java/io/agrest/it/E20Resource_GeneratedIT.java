package io.agrest.it;

import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.swagger.api.v1.service.E20Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class E20Resource_GeneratedIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E20Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E20.class};
    }

    @Test
    public void testGET_ById_Exclude() {

        e20().insertColumns("name").values("John").exec();

        Response r = target("/v1/e20/John")
                .queryParam("exclude", "age", "description").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");
    }

    @Test
    public void testPOST_Exclude() {

        Response r = target("/v1/e20")
                .queryParam("exclude", "age", "description")
                .request()
                .post(Entity.json("{\"id\":\"John\"}"));

        onResponse(r).statusEquals(Response.Status.CREATED).bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");
        e20().matcher().assertOneMatch();
    }

    @Test
    public void testPUT_ByName() {

        e20().insertColumns("name")
                .values("John")
                .values("Brian").exec();

        Response r = target("/v1/e20/John")
                .request()
                .put(Entity.json("{\"age\":28,\"description\":\"zzz\"}"));

        onSuccess(r).bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        e20().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testDELETE_ByName() {

        e20().insertColumns("name")
                .values("John")
                .values("Brian").exec();

        Response r = target("/v1/e20/John").request().delete();
        onResponse(r).statusEquals(Response.Status.OK).bodyEquals("{\"success\":true}");

        e20().matcher().assertOneMatch();
        e20().matcher().eq("name", "Brian").assertOneMatch();
    }
}
