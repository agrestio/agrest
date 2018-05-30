package com.nhl.link.rest.it;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.swagger.api.v1.service.E2Resource;
import com.nhl.link.rest.swagger.api.v1.service.E3Resource;
import com.nhl.link.rest.swagger.api.v1.service.E4Resource;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class PUT_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
        context.register(E4Resource.class);
    }

    @Test
    public void test_PUT() {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        Response response = target("/v1/e4/8").request().put(Entity.json("{\"id\":8,\"cVarchar\":\"zzz\"}"));

        onResponse(response).bodyEquals(1, "{\"id\":8," +
                "\"cBoolean\":null," +
                "\"cDate\":null," +
                "\"cDecimal\":null," +
                "\"cInt\":null," +
                "\"cTime\":null," +
                "\"cTimestamp\":null," +
                "\"cVarchar\":\"zzz\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e4 WHERE id = 8 AND c_varchar = 'zzz'"));
    }

    @Test
    public void testPut_ToOne() {
        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response = target("/v1/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":1}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
    }

    @Test
    public void testPut_ToOne_ArraySyntax() {
        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response = target("/v1/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":[1]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id = 1"));
    }

    @Test
    public void testPut_ToOne_ToNull() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response = target("/v1/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":null}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id IS NULL"));
    }

    @Test
    public void testPut_ToOne_FromNull() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', null");

        Entity<String> entity = Entity.json("{\"id\":3,\"e2\":8}");
        Response response = target("/v1/e3/3").request().put(entity);

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id  = 8"));
    }
}
