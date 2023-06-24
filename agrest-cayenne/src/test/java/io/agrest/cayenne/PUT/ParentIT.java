package io.agrest.cayenne.PUT;


import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.cayenne.main.E12;
import io.agrest.cayenne.cayenne.main.E12E13;
import io.agrest.cayenne.cayenne.main.E13;
import io.agrest.cayenne.cayenne.main.E15;
import io.agrest.cayenne.cayenne.main.E15E1;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.cayenne.main.E7;
import io.agrest.cayenne.cayenne.main.E8;
import io.agrest.cayenne.cayenne.main.E9;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class ParentIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E1.class, E2.class, E3.class, E9.class)
            .entitiesAndDependencies(E5.class, E7.class, E8.class, E12.class, E13.class, E15.class)
            .build();

    @Test
    public void relate_EmptyPutWithID() {

        tester.e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        // POST with empty body ... how bad is that?
        tester.target("/e3/8/e2/24").put("")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        tester.e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void relate_ValidRel_ToOne_Existing() {
        tester.e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e3/8/e2/24").put("{}")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        tester.e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void relate_ValidRel_ToOne_Existing_WithUpdate() {

        tester.e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        tester.e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e3/8/e2/24")
                .put("{\"name\":\"123\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"123\"}");

        tester.e2().matcher().assertOneMatch();
        tester.e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void relate_ToMany_MixedCollection() {

        tester.e8().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e7().insertColumns("id", "name", "e8_id")
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
    public void toMany_CreateUpdateDelete() {

        tester.e8().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "zzz", 15).exec();

        // this must add E7 with id=1, update - with id=8, delete - with id=9
        tester.target("/e8/15/e7s")
                .put("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        tester.e7().matcher().eq("e8_id", 15).assertMatches(2);
        tester.e7().matcher().eq("id", 9).assertNoMatches();

        // testing idempotency

        tester.target("/e8/15/e7s")
                .put("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        tester.e7().matcher().eq("e8_id", 15).assertMatches(2);
        tester.e7().matcher().eq("id", 9).assertNoMatches();
    }

    @Test
    public void relate_ValidRel_ToOne_New_AutogenId() {

        tester.e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e3/8/e2/24")
                .put("{\"name\":\"123\"}")
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Can't create 'E2' with fixed id\"}");

        tester.e2().matcher().assertNoMatches();
    }

    @Test
    public void relate_ValidRel_ToOne_New_DefaultId() {

        tester.e7().insertColumns("id")
                .values(7)
                .values(8).exec();

        tester.target("/e7/8/e8/24")
                .put("{\"name\":\"aaa\"}")
                .wasCreated().bodyEquals(1, "{\"id\":24,\"name\":\"aaa\"}");

        tester.e8().matcher().assertOneMatch();
        tester.e8().matcher().eq("name", "aaa").assertOneMatch();
        tester.e8().matcher().eq("id", 24).assertOneMatch();

        tester.e7().matcher().eq("id", 8).eq("e8_id", 24).assertOneMatch();
        tester.e7().matcher().eq("id", 7).eq("e8_id", null).assertOneMatch();

        // PUT is idempotent... doing another update should not change the picture
        tester.target("/e7/8/e8/24")
                .put("{\"name\":\"aaa\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":24,\"name\":\"aaa\"}");

        tester.e8().matcher().assertOneMatch();
        tester.e8().matcher().eq("name", "aaa").assertOneMatch();
        tester.e8().matcher().eq("id", 24).assertOneMatch();
        tester.e7().matcher().eq("id", 8).eq("e8_id", 24).assertOneMatch();
        tester.e7().matcher().eq("id", 7).eq("e8_id", null).assertOneMatch();
    }

    @Test
    public void relate_ValidRel_ToOne_New_PropagatedId() {

        tester.e8().insertColumns("id")
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
        tester.e9().matcher().eq("e8_id", 8).assertOneMatch();
    }

    @Test
    public void relate_ToMany_NoIds() {

        tester.e2().insertColumns("id_", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        // we can't PUT an object with generated ID , as the request is non-repeatable
        tester.target("/e2/15/e3s")
                .put("[ {\"name\":\"newname\"} ]")
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Request is not idempotent.\"}");

        tester.e3().matcher().assertMatches(3);
        tester.e3().matcher().eq("e2_id", 15).assertMatches(2);
    }

    @Test
    public void toMany_Join() {

        tester.e12().insertColumns("id").values(11).values(12).exec();
        tester.e13().insertColumns("id").values(14).values(15).values(16).exec();

        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .put("[{\"e13\":15},{\"e13\":14}]")
                .wasCreated()
                .bodyEquals(2, "{},{}");


        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 15).assertOneMatch();

        // testing idempotency
        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .put("[{\"e13\":15},{\"e13\":14}]")
                .wasOk().bodyEquals(2, "{},{}");


        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 15).assertOneMatch();

        // add one and delete another record
        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .put("[{\"e13\":16},{\"e13\":14}]")
                .wasOk().bodyEquals(2, "{},{}");

        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 16).assertOneMatch();
    }

    @Test
    public void toMany_DifferentIdTypes() {

        tester.e1().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();


        tester.e15().insertColumns("long_id", "name")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        tester.e15_1().insertColumns("e15_id", "e1_id")
                .values(14, 1).exec();

        tester.target("/e15/14/e15e1")
                .queryParam("exclude", "id")
                .put("[{\"e1\":1}]").wasOk();

        tester.e15_1().matcher().assertOneMatch();
        tester.e15_1().matcher().eq("e15_id", 14).eq("e1_id", 1).assertOneMatch();

        tester.e1().matcher().assertMatches(2);
        tester.e15().matcher().assertMatches(3);
    }

    @Test
    public void toMany_Flattened_DifferentIdTypes() {

        tester.e5().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e15().insertColumns("long_id", "name")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        tester.e15_5().insertColumns("e15_id", "e5_id").values(14, 1).exec();

        tester.target("/e15/14").put("{\"e5s\":[1]}").wasOk();

        tester.e15_5().matcher().assertOneMatch();
        tester.e5().matcher().assertMatches(2);
        tester.e15().matcher().assertMatches(3);
        tester.e15_5().matcher().eq("e15_id", 14).eq("e5_id", 1).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E3.class, config)
                    .parent(E2.class, id, E2.E3S.getName())
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
                    .parent(E3.class, parentId, E3.E2.getName())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e7/{id}/e8/{tid}")
        public DataResponse<E8> relateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
            return AgJaxrs.idempotentCreateOrUpdate(E8.class, config)
                    .byId(id)
                    .parent(E7.class, parentId, E7.E8.getName())
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e8/{id}/e9")
        public DataResponse<E9> relateToOneDependent(@PathParam("id") int id, String entityData) {
            // this will test support for ID propagation in a 1..1
            return AgJaxrs.idempotentCreateOrUpdate(E9.class, config)
                    .parent(E8.class, id, E8.E9.getName())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/createorupdate/{id}/e7s")
        public DataResponse<E7> createOrUpdateE7s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E7.class, config)
                    .parent(E8.class, id, E8.E7S.getName())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/{id}/e7s")
        public DataResponse<E7> fullSyncE7s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.idempotentFullSync(E7.class, config)
                    .parent(E8.class, id, E8.E7S.getName())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> fullSync_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return AgJaxrs.idempotentFullSync(E12E13.class, config)
                    .parent(E12.class, id, E12.E1213.getName())
                    .clientParams(info.getQueryParameters())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}/e15e1")
        public DataResponse<E15E1> createOrUpdate_Joins(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return AgJaxrs.createOrUpdate(E15E1.class, config)
                    .parent(E15.class, id, E15.E15E1.getName())
                    .clientParams(info.getQueryParameters())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}")
        public DataResponse<E15> createOrUpdate_Joins_FlattenedRel(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return AgJaxrs.createOrUpdate(E15.class, config)
                    .byId(id)
                    .clientParams(info.getQueryParameters())
                    .syncAndSelect(entityData);
        }
    }
}
