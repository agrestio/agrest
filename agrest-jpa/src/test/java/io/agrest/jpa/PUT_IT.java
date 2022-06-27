package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.UpdateStage;
import io.agrest.encoder.Encoder;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PUT_IT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(PUT_IT.Resource.class)
            .entities(E3.class, E2.class, E4.class, E14.class, E28.class) // E23.class ???
            .build();


    @Test
    public void testCreateOrUpdate_ById() {

        tester.target("/e23_create_or_update/8").put("{\"name\":\"zzz\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":8,\"exposedId\":8,\"name\":\"zzz\"}");
        tester.e23().matcher().eq("ID", 8).eq("NAME", "zzz").assertOneMatch();

        tester.target("/e23_create_or_update/8").put("{\"name\":\"aaa\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"exposedId\":8,\"name\":\"aaa\"}");
        tester.e23().matcher().eq("ID", 8).eq("NAME", "aaa").assertOneMatch();
    }

    @Test
    public void testUpdate() {

        tester.e4().insertColumns("ID", "C_VARCHAR", "C_DECIMAL")
                .values(1, "xxx", new BigDecimal("11.23"))
                .values(8, "yyy", new BigDecimal("-101.023")).exec();

        // TODO: BigDecimal failure is described here: https://github.com/agrestio/agrest/issues/494
        //   but we can't reproduce it until we
        tester.target("/e4/8").put("{\"id\":8,\"cVarchar\":\"zzz\",\"cDecimal\":12.99}")
                .wasOk()
                .bodyEquals(1, "{\"id\":8," +
                        "\"cBoolean\":null," +
                        "\"cDate\":null," +
                        "\"cDecimal\":12.99," +
                        "\"cInt\":null," +
                        "\"cTime\":null," +
                        "\"cTimestamp\":null," +
                        "\"cVarchar\":\"zzz\"}");

        // TODO: some kinda bug in TableMatcher - can't match on BigDecimal. So need to select the data and compare
        //   in memory
        Object[] data = tester.e4().selectColumns("C_VARCHAR", "C_DECIMAL")
                .where("ID", 8)
                .selectOne();

        Assertions.assertArrayEquals(new Object[]{"zzz", new BigDecimal("12.99")}, data);
    }

    @Test
    public void testExplicitCompoundId() {

        tester.e17().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .put("{\"name\":\"xxx\"}")
                .wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");

        tester.e17().matcher().eq("ID1", 1).eq("ID2", 1).eq("NAME", "xxx").assertOneMatch();
    }

    @Test
    public void testToOne() {
        tester.e3().deleteAll();
        tester.e2().deleteAll();

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":1}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("ID", 3).eq("E2_ID", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ArraySyntax() {
        tester.e3().deleteAll();
        tester.e2().deleteAll();

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":[1]}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("ID", 3).eq("E2_ID", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ToNull() {
        tester.e3().deleteAll();
        tester.e2().deleteAll();

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":null}")
                .wasOk().bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("ID", 3).eq("E2_ID", null).assertOneMatch();
    }

    @Test
    public void testToOne_FromNull() {
        tester.e3().deleteAll();
        tester.e2().deleteAll();

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("ID", "NAME", "E2_ID").values(3, "zzz", null).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2\":8}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("ID", 3).eq("E2_ID", 8).assertOneMatch();
    }

//    @Test
//    public void testBulk() {
//
//        tester.e3().insertColumns("id_", "name")
//                .values(5, "aaa")
//                .values(4, "zzz")
//                .values(2, "bbb")
//                .values(6, "yyy").exec();
//
//        String entity = "[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"},{\"id\":5,\"name\":\"111\"},{\"id\":2,\"name\":\"333\"}]";
//        tester.target("/e3/")
//                .queryParam("exclude", "id")
//                .queryParam("include", E3.NAME.getName())
//                .put(entity)
//                .wasOk()
//                // ordering must be preserved in response, so comparing with request entity
//                .bodyEquals(4,
//                        "{\"name\":\"yyy\"}",
//                        "{\"name\":\"zzz\"}",
//                        "{\"name\":\"111\"}",
//                        "{\"name\":\"333\"}");
//    }

//    @Test
//    public void testSingle_LongId_Small() {
//
//        tester.e14().insertColumns("long_id", "name").values(5L, "aaa").exec();
//
//        tester.target("/e14/5/")
//                .queryParam("exclude", "id")
//                .queryParam("include", E14.NAME.getName())
//                .put("[{\"id\":5,\"name\":\"bbb\"}]")
//                .wasOk().bodyEquals(1, "{\"id\":5,\"name\":\"bbb\",\"prettyName\":\"bbb_pretty\"}");
//
//        tester.e14().matcher().assertOneMatch();
//        tester.e14().matcher().eq("long_id", 5).eq("name", "bbb").assertOneMatch();
//    }

//    @Test
//    public void testBulk_LongId_Small() {
//
//        tester.e14().insertColumns("long_id", "name")
//                .values(5L, "aaa")
//                .values(4L, "zzz")
//                .values(2L, "bbb")
//                .values(6L, "yyy").exec();
//
//        String entity = "[{\"id\":6,\"name\":\"yyy\"}"
//                + ",{\"id\":4,\"name\":\"zzz\"},"
//                + "{\"id\":5,\"name\":\"111\"}"
//                + ",{\"id\":2,\"name\":\"333\"}]";
//
//        tester.target("/e14/")
//                .queryParam("exclude", "id")
//                .queryParam("include", E14.NAME.getName())
//                .put(entity)
//                .wasOk()
//                .bodyEquals(4,
//                        "{\"id\":6,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
//                        "{\"id\":4,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
//                        "{\"id\":5,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
//                        "{\"id\":2,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");
//
//        tester.e14().matcher().assertMatches(4);
//
//        // TODO: checking individual records one by one until "in()" becomes available in BQ 1.1 per
//        //  https://github.com/bootique/bootique-jdbc/issues/92
//        tester.e14().matcher().eq("long_id", 2L).assertMatches(1);
//        tester.e14().matcher().eq("long_id", 4L).assertMatches(1);
//        tester.e14().matcher().eq("long_id", 5L).assertMatches(1);
//        tester.e14().matcher().eq("long_id", 5L).assertMatches(1);
//    }

//    @Test
//    public void testBulk_LongId() {
//
//        tester.e14().insertColumns("long_id", "name")
//                .values(8147483647L, "aaa")
//                .values(8147483648L, "zzz")
//                .values(8147483649L, "bbb")
//                .values(3147483646L, "yyy").exec();
//
//        String putEntity = "[{\"id\":3147483646,\"name\":\"yyy\"}"
//                + ",{\"id\":8147483648,\"name\":\"zzz\"}"
//                + ",{\"id\":8147483647,\"name\":\"111\"}"
//                + ",{\"id\":8147483649,\"name\":\"333\"}]";
//
//        tester.target("/e14/")
//                .put(putEntity)
//                .wasOk()
//                .bodyEquals(4,
//                        "{\"id\":3147483646,\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}",
//                        "{\"id\":8147483648,\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}",
//                        "{\"id\":8147483647,\"name\":\"111\",\"prettyName\":\"111_pretty\"}",
//                        "{\"id\":8147483649,\"name\":\"333\",\"prettyName\":\"333_pretty\"}");
//
//        tester.e14().matcher().assertMatches(4);
//        tester.e14().matcher().eq("long_id", 3147483646L).assertOneMatch();
//        tester.e14().matcher().eq("long_id", 8147483648L).assertOneMatch();
//        tester.e14().matcher().eq("long_id", 8147483647L).assertOneMatch();
//        tester.e14().matcher().eq("long_id", 8147483649L).assertOneMatch();
//    }

//    @Test
//    public void testCustomEncoder() {
//
//        tester.target("/e7_custom_encoder")
//                .put("[{\"id\":4,\"name\":\"zzz\"}]")
//                .wasCreated()
//                .bodyEquals("{\"encoder\":\"custom\"}");
//    }
//
//    @Test
//    public void testBulk_ResponseAttributesFilter() {
//
//        tester.target("/e7")
//                .queryParam("exclude", "id")
//                .queryParam("include", E7.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]")
//                .wasCreated()
//                .bodyEquals(2,
//                        "{\"name\":\"yyy\"}",
//                        "{\"name\":\"zzz\"}");
//
//        tester.target("/e7")
//                .queryParam("include", "id")
//                .queryParam("exclude", E7.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"123\"},{\"id\":4}]")
//                .wasOk()
//                .bodyEquals(2, "{\"id\":6}", "{\"id\":4}");
//    }
//
//    @Test
//    public void testBulk_ResponseToOneRelationshipFilter() {
//
//        tester.e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
//        tester.e9().insertColumns("e8_id").values(5).values(6).exec();
//
//        tester.target("/e7")
//                .queryParam("include", "id", E7.E8.getName())
//                .queryParam("exclude", E7.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"yyy\"},{\"id\":4,\"name\":\"zzz\"}]")
//                .wasCreated()
//                .bodyEquals(2,
//                        "{\"id\":6,\"e8\":null}",
//                        "{\"id\":4,\"e8\":null}");
//
//        tester.target("/e7")
//                .queryParam("include", "id", E7.E8.getName())
//                .queryParam("exclude", E7.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]")
//                .wasOk()
//                .bodyEquals(2,
//                        "{\"id\":6,\"e8\":{\"id\":6,\"name\":\"ert\"}}",
//                        "{\"id\":4,\"e8\":{\"id\":5,\"name\":\"aaa\"}}");
//
//        tester.target("/e7")
//                .queryParam("include", "id", E7.E8.dot(E8.NAME).getName())
//                .queryParam("exclude", E7.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]")
//                .wasOk()
//                .bodyEquals(2,
//                        "{\"id\":6,\"e8\":{\"name\":\"ert\"}}",
//                        "{\"id\":4,\"e8\":{\"name\":\"aaa\"}}");
//
//        tester.target("/e7")
//                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
//                .queryParam("exclude", E7.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"123\",\"e8\":6},{\"id\":4,\"name\":\"zzz\",\"e8\":5}]")
//                .wasOk()
//                .bodyEquals(2,
//                        "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}",
//                        "{\"id\":4,\"e8\":{\"e9\":{\"id\":5}}}");
//    }
//
//    @Test
//    public void testBulk_ResponseToManyRelationshipFilter() {
//
//        tester.e8().insertColumns("id", "name")
//                .values(5, "aaa")
//                .values(6, "ert").exec();
//
//        tester.e7().insertColumns("id", "name", "e8_id")
//                .values(45, "me", 6)
//                .values(78, "her", 5)
//                .values(81, "him", 5).exec();
//
//        tester.target("/e8")
//                .queryParam("include", "id", E8.E7S.dot(E7.NAME).getName())
//                .queryParam("exclude", E8.NAME.getName())
//                .put("[{\"id\":6,\"name\":\"yyy\"},{\"id\":5,\"name\":\"zzz\"}]")
//                .wasOk()
//                .bodyEquals(2,
//                        "{\"id\":6,\"e7s\":[{\"name\":\"me\"}]}",
//                        "{\"id\":5,\"e7s\":[{\"name\":\"her\"},{\"name\":\"him\"}]}");
//    }
//
//    @Test
//    public void testSingle_ResponseToOneRelationshipFilter() {
//
//        tester.e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
//        tester.e9().insertColumns("e8_id").values(5).values(6).exec();
//
//        tester.target("/e7/6")
//                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
//                .queryParam("exclude", E7.NAME.getName())
//                .put("[{\"name\":\"yyy\",\"e8\":6}]")
//                .wasCreated()
//                .bodyEquals(1, "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}");
//    }
//
//    @Test
//    public void testToMany() {
//
//        tester.e2().insertColumns("id_", "name")
//                .values(1, "xxx")
//                .values(8, "yyy").exec();
//        tester.e3().insertColumns("id_", "name", "e2_id")
//                .values(3, "zzz", null)
//                .values(4, "aaa", 8)
//                .values(5, "bbb", 8).exec();
//
//        tester.target("/e2/1")
//                .queryParam("include", E2.E3S.getName())
//                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
//                .put("{\"e3s\":[3,4,5]}")
//                .wasOk().bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");
//
//        tester.e3().matcher().eq("e2_id", 1).assertMatches(3);
//    }
//
//    @Test
//    public void testToMany_UnrelateAll() {
//
//        tester.e2().insertColumns("id_", "name")
//                .values(1, "xxx")
//                .values(8, "yyy").exec();
//        tester.e3().insertColumns("id_", "name", "e2_id")
//                .values(3, "zzz", null)
//                .values(4, "aaa", 8)
//                .values(5, "bbb", 8).exec();
//
//        tester.target("/e2/8")
//                .queryParam("include", E2.E3S.getName())
//                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
//                .put("{\"e3s\":[]}")
//                .wasOk().bodyEquals(1, "{\"id\":8,\"e3s\":[]}");
//
//        tester.e3().matcher().eq("e2_id", null).assertMatches(3);
//    }
//
//    @Test
//    public void testToMany_UnrelateOne() {
//
//        tester.e2().insertColumns("id_", "name")
//                .values(1, "xxx")
//                .values(8, "yyy").exec();
//        tester.e3().insertColumns("id_", "name", "e2_id")
//                .values(3, "zzz", null)
//                .values(4, "aaa", 8)
//                .values(5, "bbb", 8).exec();
//
//        tester.target("/e2/1")
//                .queryParam("include", E2.E3S.getName())
//                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
//                .put("{\"e3s\":[4]}")
//                .wasOk().bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":4}]}");
//
//        tester.e3().matcher().eq("e2_id", 1).eq("id_", 4).assertOneMatch();
//        tester.e3().matcher().eq("e2_id", 8).eq("id_", 5).assertOneMatch();
//    }

    @Test
    @Disabled
    public void testJson() {

        String e1 = "[{\"id\":5,\"json\":[1,2]},{\"id\":6,\"json\":{\"a\":1}},{\"id\":7,\"json\":5}]";
        tester.target("/e28/").put(e1).wasCreated();
        tester.e28().matcher().assertMatches(3);
        tester.e28().matcher().eq("id", 5).eq("json", "[1,2]").assertOneMatch();
        tester.e28().matcher().eq("id", 6).eq("json", "{\"a\":1}").assertOneMatch();
        tester.e28().matcher().eq("id", 7).eq("json", "5").assertOneMatch();

        // try updating
        String e2 = "[{\"id\":5,\"json\":[1,3]}]";
        tester.target("/e28/").put(e2).wasOk();
        tester.e28().matcher().assertMatches(3);
        tester.e28().matcher().eq("id", 5).eq("json", "[1,3]").assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id, String entityData, @Context UriInfo uriInfo) {
            return AgJaxrs.idempotentCreateOrUpdate(E2.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).syncAndSelect(entityData);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> syncE3(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.idempotentFullSync(E3.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return AgJaxrs.update(E3.class, config).byId(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e4/{id}")
        public DataResponse<E4> updateE4(@PathParam("id") int id, String requestBody) {
            return AgJaxrs.update(E4.class, config).byId(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e7")
        public DataResponse<E7> syncE7(@Context UriInfo uriInfo, String data) {
            return AgJaxrs.idempotentFullSync(E7.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @PUT
        @Path("e7_custom_encoder")
        public DataResponse<E7> syncE7_CustomEncoder(@Context UriInfo uriInfo, String data) {

            Encoder encoder = (propertyName, object, out) -> {
                out.writeStartObject();
                out.writeObjectField("encoder", "custom");
                out.writeEndObject();
            };

            return AgJaxrs.idempotentFullSync(E7.class, config).clientParams(uriInfo.getQueryParameters())
                    .stage(UpdateStage.START, c -> c.setEncoder(encoder))
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e7/{id}")
        public DataResponse<E7> syncOneE7(@PathParam("id") int id, @Context UriInfo uriInfo, String data) {
            return AgJaxrs.idempotentFullSync(E7.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @PUT
        @Path("e8")
        public DataResponse<E8> sync(@Context UriInfo uriInfo, String data) {
            return AgJaxrs.idempotentFullSync(E8.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }

        @PUT
        @Path("e14")
        public DataResponse<E14> sync(String data) {
            return AgJaxrs.idempotentFullSync(E14.class, config).syncAndSelect(data);
        }

        @PUT
        @Path("e14/{id}")
        public DataResponse<E14> update(@PathParam("id") int id, String data) {
            return AgJaxrs.update(E14.class, config).byId(id).syncAndSelect(data);
        }

        @PUT
        @Path("e17")
        public DataResponse<E17> updateById(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String targetData) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("id1", id1);
            ids.put("id2", id2);

            return AgJaxrs.update(E17.class, config).clientParams(uriInfo.getQueryParameters()).byId(ids).syncAndSelect(targetData);
        }

        @PUT
        @Path("e23_create_or_update/{id}")
        public DataResponse<E23> createOrUpdateE4(@PathParam("id") int id, String requestBody) {
            return AgJaxrs.createOrUpdate(E23.class, config).byId(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e28")
        public SimpleResponse syncE28(String data) {
            return AgJaxrs.createOrUpdate(E28.class, config).sync(data);
        }
    }
}
