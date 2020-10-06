package io.agrest.cayenne.it;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.UpdateStage;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.encoder.Encoder;
import io.agrest.it.fixture.cayenne.*;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PUT_IT extends JerseyAndDerbyCase {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E7.class, E8.class, E9.class, E14.class, E17.class)
            .build();

    @Test
    public void test() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e4/8").put("{\"id\":8,\"cVarchar\":\"zzz\"}")
                .wasSuccess()
                .bodyEquals(1, "{\"id\":8," +
                        "\"cBoolean\":null," +
                        "\"cDate\":null," +
                        "\"cDecimal\":null," +
                        "\"cInt\":null," +
                        "\"cTime\":null," +
                        "\"cTimestamp\":null," +
                        "\"cVarchar\":\"zzz\"}");

        tester.e4().matcher().eq("id", 8).eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testExplicitCompoundId() {

        tester.e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .put("{\"name\":\"xxx\"}")
                .wasSuccess().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");

        tester.e17().matcher().eq("id1", 1).eq("id2", 1).eq("name", "xxx").assertOneMatch();
    }

    @Test
    public void testToOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":1}")
                .wasSuccess()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ArraySyntax() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":[1]}")
                .wasSuccess()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ToNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":null}")
                .wasSuccess().bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        // TODO: can't use matcher until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Object[]> rows = tester.e3().selectColumns("id_", "e2_id");
        assertEquals(1, rows.size());
        assertEquals(3, rows.get(0)[0]);
        assertNull(rows.get(0)[1]);
    }

    @Test
    public void testToOne_FromNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", null).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2\":8}")
                .wasSuccess()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void testBulk() {

        tester.e3().insertColumns("id_", "name")
                .values(5, "aaa")
                .values(4, "zzz")
                .values(2, "bbb")
                .values(6, "yyy").exec();

        String entity = "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]";
        tester.target("/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", E3.NAME.getName())
                .put(entity)
                .wasSuccess()
                // ordering must be preserved in response, so comparing with request entity
                .bodyEquals(4,
                        "{\"name\":\"yyy\"}",
                        "{\"name\":\"zzz\"}",
                        "{\"name\":\"111\"}",
                        "{\"name\":\"333\"}");
    }

    @Test
    public void testSingle_LongId_Small() {

        tester.e14().insertColumns("long_id", "name").values(5L, "aaa").exec();

        tester.target("/e14/5/")
                .queryParam("exclude", "id")
                .queryParam("include", E14.NAME.getName())
                .put("[{\"id\":5,\"name\":\"bbb\"}]")
                .wasSuccess().bodyEquals(1, "{\"id\":5,\"name\":\"bbb\",\"prettyName\":\"bbb_pretty\"}");

        tester.e14().matcher().assertOneMatch();
        tester.e14().matcher().eq("long_id", 5).eq("name", "bbb").assertOneMatch();
    }

    @Test
    public void testBulk_LongId_Small() {

        tester.e14().insertColumns("long_id", "name")
                .values(5L, "aaa")
                .values(4L, "zzz")
                .values(2L, "bbb")
                .values(6L, "yyy").exec();

        String entity = "[{\"id\":6,\"name\":\"yyy\"}"
                + ",{\"id\":4,\"name\":\"zzz\"},"
                + "{\"id\":5,\"name\":\"111\"}"
                + ",{\"id\":2,\"name\":\"333\"}]";

        tester.target("/e14/")
                .queryParam("exclude", "id")
                .queryParam("include", E14.NAME.getName())
                .put(entity)
                .wasSuccess()
                .bodyEquals(4,
                        "{\"id\":6,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
                        "{\"id\":4,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
                        "{\"id\":5,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
                        "{\"id\":2,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");

        tester.e14().matcher().assertMatches(4);

        // TODO: checking individual records one by one until "in()" becomes available in BQ 1.1 per
        //  https://github.com/bootique/bootique-jdbc/issues/92
        tester.e14().matcher().eq("long_id", 2L).assertMatches(1);
        tester.e14().matcher().eq("long_id", 4L).assertMatches(1);
        tester.e14().matcher().eq("long_id", 5L).assertMatches(1);
        tester.e14().matcher().eq("long_id", 5L).assertMatches(1);
    }

    @Test
    public void testBulk_LongId() {

        tester.e14().insertColumns("long_id", "name")
                .values(8147483647L, "aaa")
                .values(8147483648L, "zzz")
                .values(8147483649L, "bbb")
                .values(3147483646L, "yyy").exec();

        String putEntity = "[{\"id\":3147483646,\"name\":\"yyy\"}"
                + ",{\"id\":8147483648,\"name\":\"zzz\"}"
                + ",{\"id\":8147483647,\"name\":\"111\"}"
                + ",{\"id\":8147483649,\"name\":\"333\"}]";

        tester.target("/e14/")
                .put(putEntity)
                .wasSuccess()
                .bodyEquals(4,
                        "{\"id\":3147483646,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
                        "{\"id\":8147483648,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
                        "{\"id\":8147483647,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
                        "{\"id\":8147483649,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");

        tester.e14().matcher().assertMatches(4);
        tester.e14().matcher().eq("long_id", 3147483646L).assertOneMatch();
        tester.e14().matcher().eq("long_id", 8147483648L).assertOneMatch();
        tester.e14().matcher().eq("long_id", 8147483647L).assertOneMatch();
        tester.e14().matcher().eq("long_id", 8147483649L).assertOneMatch();
    }

    @Test
    public void testCustomEncoder() {

        tester.target("/e7_custom_encoder")
                .put("[{\"id\":4,\"name\":\"zzz\"}]")
                .wasSuccess()
                .bodyEquals("{\"encoder\":\"custom\"}");
    }

    @Test
    public void testBulk_ResponseAttributesFilter() {

        tester.target("/e7")
                .queryParam("exclude", "id")
                .queryParam("include", E7.NAME.getName())
                .put("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]")
                .wasSuccess()
                .bodyEquals(2,
                        "{\"name\":\"yyy\"}",
                        "{\"name\":\"zzz\"}");

        tester.target("/e7")
                .queryParam("include", "id")
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"id\":6,\"name\":\"123\"},{\"id\":4}]")
                .wasSuccess()
                .bodyEquals(2, "{\"id\":6}", "{\"id\":4}");
    }

    @Test
    public void testBulk_ResponseToOneRelationshipFilter() {

        tester.e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
        tester.e9().insertColumns("e8_id").values(5).values(6).exec();

        tester.target("/e7")
                .queryParam("include", "id", E7.E8.getName())
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]")
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":6,\"e8\":null}",
                        "{\"id\":4,\"e8\":null}");

        tester.target("/e7")
                .queryParam("include", "id", E7.E8.getName())
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]")
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":6,\"e8\":{\"id\":6,\"name\":\"ert\"}}",
                        "{\"id\":4,\"e8\":{\"id\":5,\"name\":\"aaa\"}}");

        tester.target("/e7")
                .queryParam("include", "id", E7.E8.dot(E8.NAME).getName())
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]")
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":6,\"e8\":{\"name\":\"ert\"}}",
                        "{\"id\":4,\"e8\":{\"name\":\"aaa\"}}");

        tester.target("/e7")
                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]")
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}",
                        "{\"id\":4,\"e8\":{\"e9\":{\"id\":5}}}");
    }

    @Test
    public void testBulk_ResponseToManyRelationshipFilter() {

        tester.e8().insertColumns("id", "name")
                .values(5, "aaa")
                .values(6, "ert").exec();

        tester.e7().insertColumns("id", "name", "e8_id")
                .values(45, "me", 6)
                .values(78, "her", 5)
                .values(81, "him", 5).exec();

        tester.target("/e8")
                .queryParam("include", "id", E8.E7S.dot(E7.NAME).getName())
                .queryParam("exclude", E8.NAME.getName())
                .put("[{\"id\":6,\"name\":\"yyy\"},{\"id\":5,\"name\":\"zzz\"}]")
                .wasSuccess()
                .bodyEquals(2,
                        "{\"id\":6,\"e7s\":[{\"name\":\"me\"}]}",
                        "{\"id\":5,\"e7s\":[{\"name\":\"her\"},{\"name\":\"him\"}]}");
    }

    @Test
    public void testSingle_ResponseToOneRelationshipFilter() {

        tester.e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
        tester.e9().insertColumns("e8_id").values(5).values(6).exec();

        tester.target("/e7/6")
                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"name\":\"yyy\",\"e8\":6}]")
                .wasSuccess()
                .bodyEquals(1, "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}");
    }

    @Test
    public void testToMany() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .put("{\"e3s\":[3,4,5]}")
                .wasSuccess().bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");

        tester.e3().matcher().eq("e2_id", 1).assertMatches(3);
    }

    @Test
    public void testToMany_UnrelateAll() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/8")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .put("{\"e3s\":[]}")
                .wasSuccess().bodyEquals(1, "{\"id\":8,\"e3s\":[]}");

        tester.e3().matcher().eq("e2_id", null).assertMatches(3);
    }

    @Test
    public void testToMany_UnrelateOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .put("{\"e3s\":[4]}")
                .wasSuccess().bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":4}]}");

        tester.e3().matcher().eq("e2_id", 1).eq("id_", 4).assertOneMatch();
        tester.e3().matcher().eq("e2_id", 8).eq("id_", 5).assertOneMatch();
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
