package io.agrest.it;

import io.agrest.AgREST;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E14;
import io.agrest.it.fixture.cayenne.E15;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E7;
import io.agrest.it.fixture.cayenne.E8;
import io.agrest.runtime.cayenne.ByKeyObjectMapperFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

public class PUT_Related_ByKey_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {


        insert("e8", "id, name", "15, 'xxx'");
        insert("e8", "id, name", "16, 'xxx'");

        insert("e7", "id, name, e8_id", "7, 'zzz', 16");
        insert("e7", "id, name, e8_id", "8, 'yyy', 15");
        insert("e7", "id, name, e8_id", "9, 'aaa', 15");

        Response r1 = target("/e8/bykey/15/e7s")
                .request()
                .put(Entity.json("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());

        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e7"));
        int id = intForQuery("SELECT id FROM utest.e7 WHERE name = 'newname'");
        assertEquals("{\"data\":[{\"id\":" + id + ",\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}],\"total\":2}",
                r1.readEntity(String.class));

        // testing idempotency

        Response r2 = target("/e8/bykey/15/e7s").request()
                .put(Entity.json("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals(
                "{\"data\":[" + "{\"id\":" + id + ",\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}],\"total\":2}",
                r2.readEntity(String.class));
        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e7"));
    }

    @Test
    public void testRelate_ToMany_PropertyMapper() {

        insert("e8", "id, name", "15, 'xxx'");
        insert("e8", "id, name", "16, 'xxx'");
        insert("e7", "id, e8_id, name", "7, 16, 'zzz'");
        insert("e7", "id, e8_id, name", "8, 15, 'yyy'");
        insert("e7", "id, e8_id, name", "9, 15, 'aaa'");

        Response r1 = target("/e8/bypropkey/15/e7s").request()
                .put(Entity.json("[  {\"name\":\"newname\"}, {\"name\":\"aaa\"} ]"));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());

        assertEquals(4, intForQuery("SELECT count(1) FROM utest.e7"));
        int id = intForQuery("SELECT id FROM utest.e7 WHERE name = 'newname'");
        assertEquals("{\"data\":[{\"id\":" + id + ",\"name\":\"newname\"},{\"id\":9,\"name\":\"aaa\"}],\"total\":2}",
                r1.readEntity(String.class));
    }

    @Test
    public void testPUT_ToMany_LongId() {

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e15 (long_id, name) values (5, 'aaa')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e15 (long_id, name) values (44, 'aaa')"));

        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (5, 5, 'aaa')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (4, 44, 'zzz')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (2, 44, 'bbb')"));
        newContext().performGenericQuery(
                new SQLTemplate(E3.class, "INSERT INTO utest.e14 (long_id, e15_id, name) values (6, 5, 'yyy')"));

        Response r1 = target("/e15/44/e14s").queryParam("exclude", "id").queryParam("include", E3.NAME.getName())
                .request().put(Entity.json("[{\"id\":4,\"name\":\"zzz\"},{\"id\":11,\"name\":\"new\"}]"));
        assertEquals(Status.OK.getStatusCode(), r1.getStatus());

        // update: ordering must be preserved...
        assertEquals(
                "{\"data\":[{\"id\":4,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}"
                        + ",{\"id\":11,\"name\":\"new\",\"prettyName\":\"new_pretty\"}],\"total\":2}",
                r1.readEntity(String.class));

        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e14 WHERE e15_id = 44"));
        assertEquals(2, intForQuery("SELECT count(1) FROM utest.e14 WHERE e15_id = 44 and long_id IN (4,11)"));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e8/bykey/{id}/e7s")
        public DataResponse<E7> e8CreateOrUpdateE7sByKey_Idempotent(@PathParam("id") int id, String entityData) {
            return AgREST.idempotentCreateOrUpdate(E7.class, config)
                    .mapper(ByKeyObjectMapperFactory.byKey(E7.NAME))
                    .toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/bypropkey/{id}/e7s")
        public DataResponse<E7> e8CreateOrUpdateE7sByPropKey_Idempotent(@PathParam("id") int id, String entityData) {
            return AgREST.idempotentCreateOrUpdate(E7.class, config).mapper(E7.NAME).toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}/e14s")
        // note that parent id is "int" here , but is BIGINT (long) in the DB. This
        // is intentional
        public DataResponse<E14> relateToOneExisting(@PathParam("id") int id, String data) {
            return AgREST.idempotentFullSync(E14.class, config).toManyParent(E15.class, id, E15.E14S).syncAndSelect(data);
        }
    }
}
