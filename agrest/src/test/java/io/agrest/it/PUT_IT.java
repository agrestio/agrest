package io.agrest.it;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.UpdateStage;
import io.agrest.encoder.Encoder;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E14;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E7;
import io.agrest.it.fixture.cayenne.E8;
import io.agrest.it.fixture.cayenne.E9;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PUT_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class, E7.class, E8.class, E9.class, E14.class, E17.class};
    }

    @Test
    public void test() {

        e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Response response = target("/e4/8").request().put(Entity.json("{\"id\":8,\"cVarchar\":\"zzz\"}"));

        onResponse(response).bodyEquals(1, "{\"id\":8," +
                "\"cBoolean\":null," +
                "\"cDate\":null," +
                "\"cDecimal\":null," +
                "\"cInt\":null," +
                "\"cTime\":null," +
                "\"cTimestamp\":null," +
                "\"cVarchar\":\"zzz\"}");

        e4().matcher().eq("id", 8).eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testExplicitCompoundId() {

        e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        Response response = target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .request().put(Entity.json("{\"name\":\"xxx\"}"));

        onSuccess(response).bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");
        e17().matcher().eq("id1", 1).eq("id2", 1).eq("name", "xxx").assertOneMatch();
    }

    @Test
    public void testToOne() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        Response response = target("/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":1}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ArraySyntax() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        Response response = target("/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":[1]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ToNull() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        Response response = target("/e3/3")
                .request()
                .put(Entity.json("{\"id\":3,\"e2\":null}"));

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        // TODO: can't use matcher until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Object[]> rows = e3().selectColumns("id_", "e2_id");
        assertEquals(1, rows.size());
        assertEquals(3, rows.get(0)[0]);
        assertNull(rows.get(0)[1]);
    }

    @Test
    public void testToOne_FromNull() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", null).exec();

        Entity<String> entity = Entity.json("{\"id\":3,\"e2\":8}");
        Response response = target("/e3/3").request().put(entity);

        onSuccess(response).bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");
        e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void testBulk() {

        e3().insertColumns("id_", "name")
                .values(5, "aaa")
                .values(4, "zzz")
                .values(2, "bbb")
                .values(6, "yyy").exec();

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
    public void testSingle_LongId_Small() {

        e14().insertColumns("long_id", "name").values(5L, "aaa").exec();

        Response response = target("/e14/5/")
                .queryParam("exclude", "id")
                .queryParam("include", E14.NAME.getName())
                .request()
                .put(Entity.json("[{\"id\":5,\"name\":\"bbb\"}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":5,\"name\":\"bbb\",\"prettyName\":\"bbb_pretty\"}");

        e14().matcher().assertOneMatch();
        e14().matcher().eq("long_id", 5).eq("name", "bbb").assertOneMatch();
    }

    @Test
    public void testBulk_LongId_Small() {

        e14().insertColumns("long_id", "name")
                .values(5L, "aaa")
                .values(4L, "zzz")
                .values(2L, "bbb")
                .values(6L, "yyy").exec();

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

        e14().matcher().assertMatches(4);

        // TODO: checking individual records one by one until "in()" becomes available in BQ 1.1 per
        //  https://github.com/bootique/bootique-jdbc/issues/92
        e14().matcher().eq("long_id", 2L).assertMatches(1);
        e14().matcher().eq("long_id", 4L).assertMatches(1);
        e14().matcher().eq("long_id", 5L).assertMatches(1);
        e14().matcher().eq("long_id", 5L).assertMatches(1);
    }

    @Test
    public void testBulk_LongId() {

        e14().insertColumns("long_id", "name")
                .values(8147483647L, "aaa")
                .values(8147483648L, "zzz")
                .values(8147483649L, "bbb")
                .values(3147483646L, "yyy").exec();

        Entity<String> putEntity = Entity.json("[{\"id\":3147483646,\"name\":\"yyy\"}"
                + ",{\"id\":8147483648,\"name\":\"zzz\"}"
                + ",{\"id\":8147483647,\"name\":\"111\"}"
                + ",{\"id\":8147483649,\"name\":\"333\"}]");

        Response r = target("/e14/")
                .request()
                .put(putEntity);

        onSuccess(r).bodyEquals(4,
                "{\"id\":3147483646,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
                "{\"id\":8147483648,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
                "{\"id\":8147483647,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
                "{\"id\":8147483649,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");

        e14().matcher().assertMatches(4);

        // TODO: checking individual records one by one until "in()" becomes available in BQ 1.1 per
        //  https://github.com/bootique/bootique-jdbc/issues/92
        e14().matcher().eq("long_id", 3147483646L).assertMatches(1);
        e14().matcher().eq("long_id", 8147483648L).assertMatches(1);
        e14().matcher().eq("long_id", 8147483647L).assertMatches(1);
        e14().matcher().eq("long_id", 8147483649L).assertMatches(1);
    }

    @Test
    public void testCustomEncoder() {

        Response r = target("/e7_custom_encoder")
                .request()
                .put(Entity.json("[{\"id\":4,\"name\":\"zzz\"}]"));

        onSuccess(r).bodyEquals("{\"encoder\":\"custom\"}");
    }

    @Test
    public void testBulk_ResponseAttributesFilter() {

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
    public void testBulk_ResponseToOneRelationshipFilter() {

        e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
        e9().insertColumns("e8_id").values(5).values(6).exec();

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
    public void testBulk_ResponseToManyRelationshipFilter() {

        e8().insertColumns("id", "name")
                .values(5, "aaa")
                .values(6, "ert").exec();

        e7().insertColumns("id", "name", "e8_id")
                .values(45, "me", 6)
                .values(78, "her", 5)
                .values(81, "him", 5).exec();

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
    public void testSingle_ResponseToOneRelationshipFilter() {

        e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
        e9().insertColumns("e8_id").values(5).values(6).exec();

        Response response = target("/e7/6")
                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
                .queryParam("exclude", E7.NAME.getName())
                .request()
                .put(Entity.json("[{\"name\":\"yyy\",\"e8\":6}]"));

        onSuccess(response).bodyEquals(1, "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}");
    }

    @Test
    public void testToMany() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        Response response = target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().put(Entity.json("{\"e3s\":[3,4,5]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");
        e3().matcher().eq("e2_id", 1).assertMatches(3);
    }

    @Test
    public void testToMany_UnrelateAll() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        Response response = target("/e2/8")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request().put(Entity.json("{\"e3s\":[]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":8,\"e3s\":[]}");

        // TODO: can't use matcher until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...
        // e3().matcher().eq("e2_id", null).assertMatches(3);
    }

    @Test
    public void testToMany_UnrelateOne() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        Response response = target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .request()
                .put(Entity.json("{\"e3s\":[4]}"));

        onSuccess(response).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":4}]}");

        e3().matcher().eq("e2_id", 1).eq("id_", 4).assertOneMatch();
        e3().matcher().eq("e2_id", 8).eq("id_", 5).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id, String entityData, @Context UriInfo uriInfo) {
            return Ag.idempotentCreateOrUpdate(E2.class, config).id(id).uri(uriInfo).syncAndSelect(entityData);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> syncE3(@Context UriInfo uriInfo, String requestBody) {
            return Ag.idempotentFullSync(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return Ag.update(E3.class, config).id(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e4/{id}")
        public DataResponse<E4> updateE4(@PathParam("id") int id, String requestBody) {
            return Ag.update(E4.class, config).id(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e7")
        public DataResponse<E7> syncE7(@Context UriInfo uriInfo, String data) {
            return Ag.idempotentFullSync(E7.class, config).uri(uriInfo).syncAndSelect(data);
        }

        @PUT
        @Path("e7_custom_encoder")
        public DataResponse<E7> syncE7_CustomEncoder(@Context UriInfo uriInfo, String data) {

            Encoder encoder = new Encoder() {
                @Override
                public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
                    out.writeStartObject();
                    out.writeObjectField("encoder", "custom");
                    out.writeEndObject();
                    return true;
                }

                @Override
                public boolean willEncode(String propertyName, Object object) {
                    return true;
                }
            };

            return Ag.idempotentFullSync(E7.class, config).uri(uriInfo)
                    .stage(UpdateStage.START, c -> c.setEncoder(encoder))
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e7/{id}")
        public DataResponse<E7> syncOneE7(@PathParam("id") int id, @Context UriInfo uriInfo, String data) {
            return Ag.idempotentFullSync(E7.class, config).id(id).uri(uriInfo).syncAndSelect(data);
        }

        @PUT
        @Path("e8")
        public DataResponse<E8> sync(@Context UriInfo uriInfo, String data) {
            return Ag.idempotentFullSync(E8.class, config).uri(uriInfo).syncAndSelect(data);
        }

        @PUT
        @Path("e14")
        public DataResponse<E14> sync(String data) {
            return Ag.idempotentFullSync(E14.class, config).syncAndSelect(data);
        }

        @PUT
        @Path("e14/{id}")
        public DataResponse<E14> update(@PathParam("id") int id, String data) {
            return Ag.update(E14.class, config).id(id).syncAndSelect(data);
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

            return Ag.update(E17.class, config).uri(uriInfo).id(ids).syncAndSelect(targetData);
        }
    }
}
