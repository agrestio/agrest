package io.agrest.it;

import io.agrest.AgREST;
import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E14;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E7;
import io.agrest.it.fixture.cayenne.E8;
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

public class PUT_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
    }

    @Test
    public void test_PUT() {

        insert("e4", "id, c_varchar", "1, 'xxx'");
        insert("e4", "id, c_varchar", "8, 'yyy'");

        Response response = target("/e4/8").request().put(Entity.json("{\"id\":8,\"cVarchar\":\"zzz\"}"));

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
    public void test_PUT_ExplicitCompoundId() {
        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");

        Response response = target("/e17").queryParam("id1", 1).queryParam("id2", 1).request()
                .put(Entity.json("{\"name\":\"xxx\"}"));

        onSuccess(response).bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e17 WHERE id1 = 1 AND id2 = 1 AND name = 'xxx'"));
    }

    @Test
    public void testPut_ToOne() {
        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', 8");

        Response response = target("/e3/3")
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

        Response response = target("/e3/3")
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

        Response response = target("/e3/3")
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
        Response response = target("/e3/3").request().put(entity);

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e3 WHERE id = 3 AND e2_id  = 8"));
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
    public void testPUT_Single_LongId_Small() {

        insert("e14", "long_id, name", "5, 'aaa'");

        Response response = target("/e14/5/")
                .queryParam("exclude", "id")
                .queryParam("include", E14.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":5,\"name\":\"bbb\"}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":5,\"name\":\"bbb\",\"prettyName\":\"bbb_pretty\"}");

        assertEquals(1L, countRows(E14.class));
        assertEquals(1, intForQuery("SELECT COUNT(1) FROM utest.e14 WHERE long_id = 5 AND NAME = 'bbb'"));
    }

    @Test
    public void testPUT_Bulk_LongId_Small() {

        insert("e14", "long_id, name", "5, 'aaa'");
        insert("e14", "long_id, name", "4, 'zzz'");
        insert("e14", "long_id, name", "2, 'bbb'");
        insert("e14", "long_id, name", "6, 'yyy'");

        Entity<String> entity = Entity.json("[{\"id\":6,\"name\":\"yyy\"}"
                + ",{\"id\":4,\"name\":\"zzz\"},"
                + "{\"id\":5,\"name\":\"111\"}"
                + ",{\"id\":2,\"name\":\"333\"}]");

        Response response = target("/e14/")
                .queryParam("exclude", "id")
                .queryParam("include", E14.NAME.getName())
                .request()
                .put(entity);

        onSuccess(response).bodyEquals(4,
                "{\"id\":6,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
                "{\"id\":4,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
                "{\"id\":5,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
                "{\"id\":2,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");

        assertEquals(4L, countRows(E14.class));
        assertEquals(4, intForQuery("SELECT COUNT(1) FROM utest.e14 WHERE long_id IN (2,4,6,5)"));
    }

    @Test
    public void testPUT_Bulk_LongId() {

        insert("e14", "long_id, name", "8147483647, 'aaa'");
        insert("e14", "long_id, name", "8147483648, 'zzz'");
        insert("e14", "long_id, name", "8147483649, 'bbb'");
        insert("e14", "long_id, name", "3147483646, 'yyy'");

        Entity<String> putEntity = Entity.json("[{\"id\":3147483646,\"name\":\"yyy\"}"
                + ",{\"id\":8147483648,\"name\":\"zzz\"}"
                + ",{\"id\":8147483647,\"name\":\"111\"}"
                + ",{\"id\":8147483649,\"name\":\"333\"}]");

        Response response = target("/e14/")
                .request()
                .put(putEntity);

        onSuccess(response).bodyEquals(4,
                "{\"id\":3147483646,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
                "{\"id\":8147483648,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
                "{\"id\":8147483647,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
                "{\"id\":8147483649,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");

        assertEquals(4L, countRows("e14"));
        assertEquals(4, countRows("e14", "WHERE long_id IN (3147483646, 8147483648, 8147483647, 8147483649)"));
    }

    @Test
    public void testPUT_Bulk_ResponseAttributesFilter() {

        Response response1 = target("/e7")
                .queryParam("exclude", "id")
                .queryParam("include", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]"));

        onSuccess(response1).bodyEquals(2,
                "{\"name\":\"yyy\"}",
                "{\"name\":\"zzz\"}");

        Response response2 = target("/e7")
                .queryParam("include", "id")
                .queryParam("exclude", E7.NAME.getName())
                .request().put(Entity.json("[{\"id\":6,\"name\":\"123\"},{\"id\":4}]"));

        onSuccess(response2).bodyEquals(2, "{\"id\":6}", "{\"id\":4}");
    }

    @Test
    public void testPUT_Bulk_ResponseToOneRelationshipFilter() {

        insert("e8", "id, name", "5, 'aaa'");
        insert("e8", "id, name", "6, 'ert'");

        insert("e9", "e8_id", "5");
        insert("e9", "e8_id", "6");

        Response response1 = target("/e7")
                .queryParam("include", "id", E7.E8.getName())
                .queryParam("exclude", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]"));

        onSuccess(response1).bodyEquals(2,
                "{\"id\":6,\"e8\":null}",
                "{\"id\":4,\"e8\":null}");

        Response response2 = target("/e7")
                .queryParam("include", "id", E7.E8.getName())
                .queryParam("exclude", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]"));

        onSuccess(response2).bodyEquals(2,
                "{\"id\":6,\"e8\":{\"id\":6,\"name\":\"ert\"}}",
                "{\"id\":4,\"e8\":{\"id\":5,\"name\":\"aaa\"}}");

        Response response3 = target("/e7")
                .queryParam("include", "id", E7.E8.dot(E8.NAME).getName())
                .queryParam("exclude", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]"));

        onSuccess(response3).bodyEquals(2,
                "{\"id\":6,\"e8\":{\"name\":\"ert\"}}",
                "{\"id\":4,\"e8\":{\"name\":\"aaa\"}}");

        Response response4 = target("/e7")
                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
                .queryParam("exclude", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]"));

        onSuccess(response4).bodyEquals(2,
                "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}",
                "{\"id\":4,\"e8\":{\"e9\":{\"id\":5}}}");
    }

    @Test
    public void testPUT_Bulk_ResponseToManyRelationshipFilter() {

        insert("e8", "id, name", "5, 'aaa'");
        insert("e8", "id, name", "6, 'ert'");

        insert("e7", "id, e8_id, name", "45, 6, 'me'");
        insert("e7", "id, e8_id, name", "78, 5, 'her'");
        insert("e7", "id, e8_id, name", "81, 5, 'him'");

        Response response = target("/e8")
                .queryParam("include", "id", E8.E7S.dot(E7.NAME).getName())
                .queryParam("exclude", E8.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":6,\"name\":\"yyy\"},{\"id\":5,\"name\":\"zzz\"}]"));

        onSuccess(response).bodyEquals(2,
                "{\"id\":6,\"e7s\":[{\"name\":\"me\"}]}",
                "{\"id\":5,\"e7s\":[{\"name\":\"her\"},{\"name\":\"him\"}]}");
    }

    @Test
    public void testPUT_Single_ResponseToOneRelationshipFilter() {

        insert("e8", "id, name", "5, 'aaa'");
        insert("e8", "id, name", "6, 'ert'");

        insert("e9", "e8_id", "5");
        insert("e9", "e8_id", "6");

        Response response = target("/e7/6")
                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
                .queryParam("exclude", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"name\":\"yyy\",\"e8\":6}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}");
    }

    @Test
    public void testPut_ToMany() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', null");
        insert("e3", "id, name, e2_id", "4, 'aaa', 8");
        insert("e3", "id, name, e2_id", "5, 'bbb', 8");

        Response response = target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().put(Entity.json("{\"e3s\":[3,4,5]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");
        assertEquals(3L, countRows("e3", "WHERE e2_id = 1"));
    }

    @Test
    public void testPut_ToMany_UnrelateAll() {

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "8, 'yyy'");
        insert("e3", "id, name, e2_id", "3, 'zzz', null");
        insert("e3", "id, name, e2_id", "4, 'aaa', 8");
        insert("e3", "id, name, e2_id", "5, 'bbb', 8");

        Response response = target("/e2/8")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().put(Entity.json("{\"e3s\":[]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":8,\"e3s\":[]}");
        assertEquals(3, countRows("e3", "WHERE e2_id IS NULL"));
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
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id, String entityData, @Context UriInfo uriInfo) {
            return AgREST.idempotentCreateOrUpdate(E2.class, config).id(id).uri(uriInfo).syncAndSelect(entityData);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> syncE3(@Context UriInfo uriInfo, String requestBody) {
            return AgREST.idempotentFullSync(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return AgREST.update(E3.class, config).id(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e4/{id}")
        public DataResponse<E4> updateE4(@PathParam("id") int id, String requestBody) {
            return AgREST.update(E4.class, config).id(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e7")
        public DataResponse<E7> syncE7(@Context UriInfo uriInfo, String data) {
            return AgREST.idempotentFullSync(E7.class, config).uri(uriInfo).syncAndSelect(data);
        }

        @PUT
        @Path("e7/{id}")
        public DataResponse<E7> syncOneE7(@PathParam("id") int id, @Context UriInfo uriInfo, String data) {
            return AgREST.idempotentFullSync(E7.class, config).id(id).uri(uriInfo).syncAndSelect(data);
        }

        @PUT
        @Path("e8")
        public DataResponse<E8> sync(@Context UriInfo uriInfo, String data) {
            return AgREST.idempotentFullSync(E8.class, config).uri(uriInfo).syncAndSelect(data);
        }

        @PUT
        @Path("e14")
        public DataResponse<E14> sync(String data) {
            return AgREST.idempotentFullSync(E14.class, config).syncAndSelect(data);
        }

        @PUT
        @Path("e14/{id}")
        public DataResponse<E14> update(@PathParam("id") int id, String data) {
            return AgREST.update(E14.class, config).id(id).syncAndSelect(data);
        }

        @PUT
        @Path("e17")
        public DataResponse<E17> updateById(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String targetData) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1_PK_COLUMN, id1);
            ids.put(E17.ID2_PK_COLUMN, id2);

            return AgREST.update(E17.class, config).uri(uriInfo).id(ids).syncAndSelect(targetData);
        }
    }
}
