package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class PUT_Related_IT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .entities(E3.class, E2.class, E9.class, E7.class, E8.class, E12.class,
                    E13.class, E15E1.class, E15E5.class, E14.class, E15.class, E1.class, E5.class)
            .build();

    @Test
    public void testRelate_EmptyPutWithID() {

        tester.e2().insertColumns("ID", "NAME")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        // POST with empty body ... how bad is that?
        tester.target("/e3/8/e2/24").put("")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        tester.e3().matcher().eq("E2_ID", 24).eq("NAME", "yyy").assertOneMatch();
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing() {
        tester.e2().insertColumns("ID", "NAME")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e3/8/e2/24").put("{}")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        tester.e3().matcher().eq("E2_ID", 24).eq("NAME", "yyy").assertOneMatch();
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing_WithUpdate() {

        tester.e2().insertColumns("ID", "NAME")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e3/8/e2/24")
                .put("{\"name\":\"123\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"123\"}");

        tester.e2().matcher().assertOneMatch();
        tester.e3().matcher().eq("E2_ID", 24).eq("NAME", "yyy").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        tester.e8().insertColumns("ID", "NAME")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e7().insertColumns("ID", "NAME", "E8_ID")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        tester.target("/e8/createorupdate/15/e7s")
                .put("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]")
                .wasOk().bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        tester.e7().matcher().assertMatches(4);

        // testing idempotency

        tester.target("/e8/createorupdate/15/e7s")
                .put("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"id\":1,\"name\":\"newname\"}," + "{\"id\":8,\"name\":\"123\"}");

        tester.e7().matcher().assertMatches(4);
    }

    @Test
    public void test_ToMany_CreateUpdateDelete() {

        tester.e8().insertColumns("ID", "NAME")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e7().insertColumns("ID", "NAME", "E8_ID")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "zzz", 15).exec();

        // this must add E7 with id=1, update - with id=8, delete - with id=9
        tester.target("/e8/15/e7s")
                .put("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        tester.e7().matcher().eq("E8_ID", 15).assertMatches(2);
        tester.e7().matcher().eq("ID", 9).assertNoMatches();

        // testing idempotency

        tester.target("/e8/15/e7s")
                .put("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        tester.e7().matcher().eq("E8_ID", 15).assertMatches(2);
        tester.e7().matcher().eq("ID", 9).assertNoMatches();
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_AutogenId() {

        tester.e3().insertColumns("ID", "NAME")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e3/8/e2/24")
                .put("{\"name\":\"123\"}")
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Can't create 'E2' with fixed id\"}");

        tester.e2().matcher().assertNoMatches();
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_DefaultId() {

        tester.e7().insertColumns("ID")
                .values(7)
                .values(8).exec();

        tester.target("/e7/8/e8/24")
                .put("{\"name\":\"aaa\"}")
                .wasCreated().bodyEquals(1, "{\"id\":24,\"name\":\"aaa\"}");

        tester.e8().matcher().assertOneMatch();
        tester.e8().matcher().eq("NAME", "aaa").assertOneMatch();
        tester.e8().matcher().eq("ID", 24).assertOneMatch();

        tester.e7().matcher().eq("ID", 8).eq("E8_ID", 24).assertOneMatch();
        tester.e7().matcher().eq("ID", 7).eq("E8_ID", null).assertOneMatch();

        // PUT is idempotent... doing another update should not change the picture
        tester.target("/e7/8/e8/24")
                .put("{\"name\":\"aaa\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"name\":\"aaa\"}");

        tester.e8().matcher().assertOneMatch();
        tester.e8().matcher().eq("NAME", "aaa").assertOneMatch();
        tester.e8().matcher().eq("ID", 24).assertOneMatch();
        tester.e7().matcher().eq("ID", 8).eq("E8_ID", 24).assertOneMatch();
        tester.e7().matcher().eq("ID", 7).eq("E8_ID", null).assertOneMatch();
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_PropagatedId() {

        tester.e8().insertColumns("ID")
                .values(7)
                .values(8).exec();

        tester.target("/e8/8/e9").put("{}")
                .wasCreated().bodyEquals(1, "{\"id\":8}");

        tester.e9().matcher().assertOneMatch();
        tester.e9().matcher().eq("e8_id", 8).assertOneMatch();

        // PUT is idempotent... doing another update should not change the picture
        tester.target("/e8/8/e9").put("{}")
                .wasOk().bodyEquals(1, "{\"id\":8}");

        tester.e9().matcher().assertOneMatch();
        tester.e9().matcher().eq("E8_ID", 8).assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_NoIds() {

        tester.e2().insertColumns("ID", "NAME")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        // we can't PUT an object with generated ID , as the request is non-repeatable
        tester.target("/e2/15/e3s")
                .put("[ {\"name\":\"newname\"} ]")
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Request is not idempotent.\"}");

        tester.e3().matcher().assertMatches(3);
        tester.e3().matcher().eq("E2_ID", 15).assertMatches(2);
    }

    @Test
    public void testToMany_Join() {

        tester.e12().insertColumns("ID").values(11).values(12).exec();
        tester.e13().insertColumns("ID").values(14).values(15).values(16).exec();

        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .put("[{\"e13\":15},{\"e13\":14}]")
                .wasCreated()
                .bodyEquals(2, "{},{}");


        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 14).assertOneMatch();
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 15).assertOneMatch();

        // testing idempotency
        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .put("[{\"e13\":15},{\"e13\":14}]")
                .wasOk().bodyEquals(2, "{},{}");


        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 14).assertOneMatch();
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 15).assertOneMatch();

        // add one and delete another record
        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .put("[{\"e13\":16},{\"e13\":14}]")
                .wasOk().bodyEquals(2, "{},{}");

        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 14).assertOneMatch();
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 16).assertOneMatch();
    }

    @Test
    public void testToMany_DifferentIdTypes() {

        tester.e1().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(2, "yyy").exec();


        tester.e15().insertColumns("LONG_ID", "NAME")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        tester.e15_1().insertColumns("E15_ID", "E1_ID")
                .values(14, 1).exec();

        tester.target("/e15/14/e15e1")
                .queryParam("exclude", "id")
                .put("[{\"e1\":1}]").wasOk();

        tester.e15_1().matcher().assertOneMatch();
        tester.e15_1().matcher().eq("E15_ID", 14).eq("E1_ID", 1).assertOneMatch();

        tester.e1().matcher().assertMatches(2);
        tester.e15().matcher().assertMatches(3);
    }

    @Test
    public void testToMany_Flattened_DifferentIdTypes() {

        tester.e5().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e15().insertColumns("LONG_ID", "NAME")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        tester.e15_5().insertColumns("E15_ID", "E5_ID").values(14, 1).exec();

        tester.target("/e15/14").put("{\"e5s\":[1]}").wasOk();

        tester.e15_5().matcher().assertOneMatch();
        tester.e5().matcher().assertMatches(2);
        tester.e15().matcher().assertMatches(3);
        tester.e15_5().matcher().eq("E15_ID", 14).eq("E5_ID", 1).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E3.class, config)
                    .parent(E2.class, id, E2.E3S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e3/{id}/e2/{tid}")
        public DataResponse<E2> createOrUpdate_Idempotent_E2ViaE3(
                @PathParam("id") int parentId,
                @PathParam("tid") int id,
                String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E2.class, config)
                    .byId(id)
                    .parent(E3.class, parentId, E3.E2)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e7/{id}/e8/{tid}")
        public DataResponse<E8> relateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
            return AgJaxrs.idempotentCreateOrUpdate(E8.class, config)
                    .byId(id)
                    .parent(E7.class, parentId, E7.E8)
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e8/{id}/e9")
        public DataResponse<E9> relateToOneDependent(@PathParam("id") int id, String entityData) {
            // this will test support for ID propagation in a 1..1
            return AgJaxrs.idempotentCreateOrUpdate(E9.class, config)
                    .parent(E8.class, id, E8.E9)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/createorupdate/{id}/e7s")
        public DataResponse<E7> createOrUpdateE7s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E7.class, config)
                    .parent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/{id}/e7s")
        public DataResponse<E7> fullSyncE7s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.idempotentFullSync(E7.class, config)
                    .parent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> fullSync_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return AgJaxrs.idempotentFullSync(E12E13.class, config)
                    .parent(E12.class, id, E12.E1213S)
                    .clientParams(info.getQueryParameters())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}/e15e1")
        public DataResponse<E15E1> createOrUpdate_Joins(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return AgJaxrs.createOrUpdate(E15E1.class, config)
                    .parent(E15.class, id, E15.E15E1)
                    .clientParams(info.getQueryParameters())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}")
        public DataResponse<E15> createOrUpdate_Joins_FlattenedRel(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return AgJaxrs.createOrUpdate(E15.class, config).byId(id).clientParams(info.getQueryParameters()).syncAndSelect(entityData);
        }
    }
}
