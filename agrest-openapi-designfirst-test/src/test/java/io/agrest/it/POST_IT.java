package io.agrest.it;

import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.swagger.api.v1.service.E2Resource;
import io.agrest.swagger.api.v1.service.E3Resource;
import io.agrest.swagger.api.v1.service.E4Resource;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SelectQuery;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class POST_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
        context.register(E4Resource.class);
    }

    @Test
    public void testPost() {

        ObjectContext context = newContext();

        Response response1 = target("/v1/e4").request()
                .post(Entity.json("{\"cVarchar\":\"zzz\"}"));
        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E4 e41 = ObjectSelect.query(E4.class).selectFirst(context);
        assertNotNull(e41);
        Assert.assertEquals("zzz", e41.getCVarchar());
        int id1 = Cayenne.intPKForObject(e41);

        assertEquals(
                "{\"data\":[{\"id\":" + id1 + ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}],\"total\":1}",
                response1.readEntity(String.class));

        Response response2 = target("/v1/e4").request()
                .post(Entity.json("{\"cVarchar\":\"TTTT\"}"));

        assertEquals(Status.CREATED.getStatusCode(), response2.getStatus());

        List<E4> e4s = context.select(new SelectQuery<E4>(E4.class));
        assertEquals(2, e4s.size());
        assertTrue(e4s.remove(e41));

        E4 e42 = e4s.get(0);
        Assert.assertEquals("TTTT", e42.getCVarchar());
        int id2 = Cayenne.intPKForObject(e42);

        assertEquals(
                "{\"data\":[{\"id\":" + id2 + ",\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"TTTT\"}],\"total\":1}",
                response2.readEntity(String.class));
    }

    @Test
    public void testPost_ToOne() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response1 = target("/v1/e3").request()
                .post(Entity.json("{\"e2\":8,\"name\":\"MM\"}"));

        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E3 e3 = (E3) Cayenne.objectForQuery(newContext(), new SelectQuery<>(E3.class));
        int id = Cayenne.intPKForObject(e3);

        assertEquals("{\"data\":[{\"id\":" + id + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().invalidateObjects(e3);
        Assert.assertEquals("MM", e3.getName());
        assertEquals(8, Cayenne.intPKForObject(e3.getE2()));
    }

    @Test
    public void testPost_ToOne_Null() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response1 = target("/v1/e3").request()
                .post(Entity.json("{\"e2_id\":null,\"name\":\"MM\"}"));

        assertEquals(Status.CREATED.getStatusCode(), response1.getStatus());

        E3 e3 = (E3) Cayenne.objectForQuery(newContext(), new SelectQuery<E3>(E3.class));
        int id = Cayenne.intPKForObject(e3);

        assertEquals("{\"data\":[{\"id\":" + id + ",\"name\":\"MM\",\"phoneNumber\":null}],\"total\":1}",
                response1.readEntity(String.class));

        newContext().invalidateObjects(e3);
        Assert.assertEquals("MM", e3.getName());
        assertNull(e3.getE2());
    }

    @Test
    public void testPost_ToOne_BadFK() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");

        Response response1 = target("/v1/e3").request()
                .post(Entity.json("{\"e2\":15,\"name\":\"MM\"}"));

        assertEquals(Status.NOT_FOUND.getStatusCode(), response1.getStatus());

        assertEquals(0, newContext().select(new SelectQuery<E3>(E3.class)).size());
    }

    @Test
    public void testPost_Bulk() {

        Response r2 = target("/v1/e3/").queryParam("exclude", "id").queryParam("include", E3.NAME.getName()).request()
                .post(Entity.json("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]"));
        assertEquals(Status.CREATED.getStatusCode(), r2.getStatus());

        // ordering must be preserved...
        assertEquals(
                "{\"data\":[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}],\"total\":4}",
                r2.readEntity(String.class));
    }

    @Test
    public void testPost_ToMany() {

        insert("e3", "id, name", "1, 'xxx'");
        insert("e3", "id, name", "8, 'yyy'");

        Response response = target("/v1/e2")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request()
                .post(Entity.json("{\"e3s\":[1,8],\"name\":\"MM\"}"));

        E2 e2 = (E2) Cayenne.objectForQuery(newContext(), new SelectQuery<>(E2.class));
        int id = Cayenne.intPKForObject(e2);

        onResponse(response)
                .statusEquals(Status.CREATED)
                .bodyEquals(1, "{\"id\":" + id + ",\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}");

        assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE e2_id = " + id));
    }

}
