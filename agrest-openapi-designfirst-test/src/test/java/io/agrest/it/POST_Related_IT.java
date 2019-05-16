package io.agrest.it;

import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.swagger.api.v1.service.E2Resource;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.query.SQLSelect;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class POST_Related_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
    }



    @Test
    public void testRelate_ToMany_MixedCollection() {

        insert("e2", "id, name", "15, 'xxx'");
        insert("e2", "id, name", "16, 'xxx'");

        insert("e3", "id, name, e2_id", "7, 'zzz', 16");
        insert("e3", "id, name, e2_id", "8, 'yyy', 15");
        insert("e3", "id, name, e2_id", "9, 'aaa', 15");

        Response r1 = target("/v1/e2/15/e3s")
                .request()
                .post(Entity.json("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
                        + "{\"id\":1,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}",
                r1.readEntity(String.class));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals(3, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));

        // testing non-idempotency

        Response r2 = target("/v1/e2/15/e3s")
                .request()
                .post(Entity.json("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
                        + "{\"id\":2,\"name\":\"newname\",\"phoneNumber\":null}],\"total\":2}",
                r2.readEntity(String.class));
        assertEquals(5, intForQuery("SELECT count(1) FROM utest.e3"));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e3 WHERE e2_id = 15"));
    }

}
