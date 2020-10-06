package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E12;
import io.agrest.it.fixture.cayenne.E12E13;
import io.agrest.it.fixture.cayenne.E13;
import io.agrest.it.fixture.cayenne.E15;
import io.agrest.it.fixture.cayenne.E15E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E7;
import io.agrest.it.fixture.cayenne.E8;
import io.agrest.it.fixture.cayenne.E9;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PUT_Related_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E1.class, E2.class, E3.class, E7.class, E8.class, E9.class};
    }

    @Override
    protected Class<?>[] testEntitiesAndDependencies() {
        return new Class[]{E12.class, E13.class, E15.class};
    }

    @Test
    public void testRelate_EmptyPutWithID() {

        e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        // POST with empty body ... how bad is that?
        Response r = target("/e3/8/e2/24").request().put(Entity.json(""));

        onSuccess(r).bodyEquals(1, "{\"id\":24,\"address\":null,\"name\":\"xxx\"}");

        e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing() {
        e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        Response response = target("/e3/8/e2/24").request().put(Entity.json("{}"));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"xxx\"}],\"total\":1}",
                response.readEntity(String.class));

        e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testRelate_ValidRel_ToOne_Existing_WithUpdate() {

        e2().insertColumns("id_", "name")
                .values(24, "xxx").exec();

        e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        Response r1 = target("/e3/8/e2/24")
                .request()
                .put(Entity.json("{\"name\":\"123\"}"));

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"address\":null,\"name\":\"123\"}],\"total\":1}",
                r1.readEntity(String.class));

        e2().matcher().assertOneMatch();
        e3().matcher().eq("e2_id", 24).eq("name", "yyy").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        e8().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        Response r1 = target("/e8/createorupdate/15/e7s")
                .request()
                .put(Entity.json("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]"));

        onSuccess(r1).bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");
        e7().matcher().assertMatches(4);

        // testing idempotency

        Response r2 = target("/e8/createorupdate/15/e7s")
                .request()
                .put(Entity.json("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]"));

        onSuccess(r2).bodyEquals(2, "{\"id\":1,\"name\":\"newname\"}," + "{\"id\":8,\"name\":\"123\"}");
        e7().matcher().assertMatches(4);
    }

    @Test
    public void test_ToMany_CreateUpdateDelete() {

        e8().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "zzz", 15).exec();

        // this must add E7 with id=1, update - with id=8, delete - with id=9
        Response r1 = target("/e8/15/e7s")
                .request()
                .put(Entity.json("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]"));
        onSuccess(r1).bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        e7().matcher().eq("e8_id", 15).assertMatches(2);
        e7().matcher().eq("id", 9).assertNoMatches();

        // testing idempotency

        Response r2 = target("/e8/15/e7s")
                .request()
                .put(Entity.json("[  {\"id\":1,\"name\":\"newname\"}, {\"id\":8,\"name\":\"123\"} ]"));

        onSuccess(r2).bodyEquals(2, "{\"id\":1,\"name\":\"newname\"},{\"id\":8,\"name\":\"123\"}");

        e7().matcher().eq("e8_id", 15).assertMatches(2);
        e7().matcher().eq("id", 9).assertNoMatches();
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_AutogenId() {

        e3().insertColumns("id_", "name")
                .values(7, "zzz")
                .values(8, "yyy").exec();

        Response r = target("/e3/8/e2/24")
                .request()
                .put(Entity.json("{\"name\":\"123\"}"));

        onResponse(r).statusEquals(Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E2' with fixed id\"}");

        e2().matcher().assertNoMatches();
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_DefaultId() {

        e7().insertColumns("id")
                .values(7)
                .values(8).exec();

        Response r1 = target("/e7/8/e8/24")
                .request()
                .put(Entity.json("{\"name\":\"aaa\"}"));

        onSuccess(r1).bodyEquals(1, "{\"id\":24,\"name\":\"aaa\"}");

        e8().matcher().assertOneMatch();
        e8().matcher().eq("name", "aaa").assertOneMatch();
        e8().matcher().eq("id", 24).assertOneMatch();

        e7().matcher().eq("id", 8).eq("e8_id", 24).assertOneMatch();

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Integer> ids1 = e7()
                .selectStatement(rs -> {
                    int i = rs.getInt(1);
                    return rs.wasNull() ? null : i;
                }).append("SELECT e8_id FROM utest.e7 WHERE id = 7")
                .select(100);
        assertEquals(1, ids1.size());
        assertNull(ids1.get(0));

        // PUT is idempotent... doing another update should not change the picture
        Response r2 = target("/e7/8/e8/24")
                .request()
                .put(Entity.json("{\"name\":\"aaa\"}"));

        assertEquals(Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("{\"data\":[{\"id\":24,\"name\":\"aaa\"}],\"total\":1}", r2.readEntity(String.class));

        e8().matcher().assertOneMatch();
        e8().matcher().eq("name", "aaa").assertOneMatch();
        e8().matcher().eq("id", 24).assertOneMatch();
        e7().matcher().eq("id", 8).eq("e8_id", 24).assertOneMatch();

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Integer> ids2 = e7()
                .selectStatement(rs -> {
                    int i = rs.getInt(1);
                    return rs.wasNull() ? null : i;
                }).append("SELECT e8_id FROM utest.e7 WHERE id = 7")
                .select(100);
        assertEquals(1, ids2.size());
        assertNull(ids2.get(0));
    }

    @Test
    public void testRelate_ValidRel_ToOne_New_PropagatedId() {

        e8().insertColumns("id")
                .values(7)
                .values(8).exec();

        Response r1 = target("/e8/8/e9").request().put(Entity.json("{}"));
        onSuccess(r1).bodyEquals(1, "{\"id\":8}");

        e9().matcher().assertOneMatch();
        e9().matcher().eq("e8_id", 8).assertOneMatch();

        // PUT is idempotent... doing another update should not change the picture
        Response r2 = target("/e8/8/e9").request().put(Entity.json("{}"));
        onSuccess(r2).bodyEquals(1, "{\"id\":8}");

        e9().matcher().assertOneMatch();
        e9().matcher().eq("e8_id", 8).assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_NoIds() {

        e2().insertColumns("id_", "name")
                .values(15, "xxx")
                .values(16, "xxx").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        // we can't PUT an object with generated ID , as the request is non-repeatable
        Response r = target("/e2/15/e3s")
                .request()
                .put(Entity.json("[ {\"name\":\"newname\"} ]"));

        onResponse(r).statusEquals(Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Request is not idempotent.\"}");

        e3().matcher().assertMatches(3);
        e3().matcher().eq("e2_id", 15).assertMatches(2);
    }

    @Test
    public void testToMany_Join() {

        e12().insertColumns("id").values(11).values(12).exec();
        e13().insertColumns("id").values(14).values(15).values(16).exec();

        Response r1 = target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .request()
                .put(Entity.json("[{\"e13\":15},{\"e13\":14}]"));

        onSuccess(r1).bodyEquals(2, "{},{}");


        e12_13().matcher().assertMatches(2);
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 15).assertOneMatch();

        // testing idempotency
        Response r2 = target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .request()
                .put(Entity.json("[{\"e13\":15},{\"e13\":14}]"));

        onSuccess(r2).bodyEquals(2, "{},{}");


        e12_13().matcher().assertMatches(2);
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 15).assertOneMatch();

        // add one and delete another record
        Response r3 = target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .request()
                .put(Entity.json("[{\"e13\":16},{\"e13\":14}]"));

        onSuccess(r3).bodyEquals(2, "{},{}");

        e12_13().matcher().assertMatches(2);
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 16).assertOneMatch();
    }

    @Test
    public void testToMany_DifferentIdTypes() {

        e1().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();


        e15().insertColumns("long_id", "name")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        e15_1().insertColumns("e15_id", "e1_id")
                .values(14, 1).exec();

        Response r = target("/e15/14/e15e1")
                .queryParam("exclude", "id")
                .request()
                .put(Entity.json("[{\"e1\":1}]"));

        onSuccess(r);

        e15_1().matcher().assertOneMatch();
        e15_1().matcher().eq("e15_id", 14).eq("e1_id", 1).assertOneMatch();

        e1().matcher().assertMatches(2);
        e15().matcher().assertMatches(3);
    }

    @Test
    public void testToMany_Flattened_DifferentIdTypes() {

        e5().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e15().insertColumns("long_id", "name")
                .values(14L, "aaa")
                .values(15L, "bbb")
                .values(16L, "ccc").exec();

        e15_5().insertColumns("e15_id", "e5_id").values(14, 1).exec();


        Response r = target("/e15/14").request().put(Entity.json("{\"e5s\":[1]}"));
        onSuccess(r);

        e15_5().matcher().assertOneMatch();
        e5().matcher().assertMatches(2);
        e15().matcher().assertMatches(3);
        e15_5().matcher().eq("e15_id", 14).eq("e5_id", 1).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
            return Ag.idempotentCreateOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e3/{id}/e2/{tid}")
        public DataResponse<E2> createOrUpdate_Idempotent_E2ViaE3(
                @PathParam("id") int parentId,
                @PathParam("tid") int id,
                String entityData) {
            return Ag.idempotentCreateOrUpdate(E2.class, config)
                    .id(id)
                    .parent(E3.class, parentId, E3.E2)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e7/{id}/e8/{tid}")
        public DataResponse<E8> relateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
            return Ag.idempotentCreateOrUpdate(E8.class, config).id(id).parent(E7.class, parentId, E7.E8)
                    .syncAndSelect(data);
        }

        @PUT
        @Path("e8/{id}/e9")
        public DataResponse<E9> relateToOneDependent(@PathParam("id") int id, String entityData) {
            // this will test support for ID propagation in a 1..1
            return Ag.idempotentCreateOrUpdate(E9.class, config).parent(E8.class, id, E8.E9)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/createorupdate/{id}/e7s")
        public DataResponse<E7> createOrUpdateE7s(@PathParam("id") int id, String entityData) {
            return Ag.idempotentCreateOrUpdate(E7.class, config).toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e8/{id}/e7s")
        public DataResponse<E7> fullSyncE7s(@PathParam("id") int id, String entityData) {
            return Ag.idempotentFullSync(E7.class, config).toManyParent(E8.class, id, E8.E7S)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> fullSync_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return Ag.idempotentFullSync(E12E13.class, config).toManyParent(E12.class, id, E12.E1213).uri(info)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}/e15e1")
        public DataResponse<E15E1> createOrUpdate_Joins(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return Ag.createOrUpdate(E15E1.class, config).toManyParent(E15.class, id, E15.E15E1).uri(info)
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("e15/{id}")
        public DataResponse<E15> createOrUpdate_Joins_FlattenedRel(@PathParam("id") long id, @Context UriInfo info, String entityData) {
            return Ag.createOrUpdate(E15.class, config).id(id).uri(info).syncAndSelect(entityData);
        }
    }
}
