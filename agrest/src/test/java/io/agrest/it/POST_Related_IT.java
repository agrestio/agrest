package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.it.fixture.BQJerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E12;
import io.agrest.it.fixture.cayenne.E12E13;
import io.agrest.it.fixture.cayenne.E13;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E18;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class POST_Related_IT extends BQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E17.class, E18.class};
    }

    @Override
    protected Class<?>[] testEntitiesAndDependencies() {
        return new Class[]{E12.class, E13.class};
    }

    @Test
    public void testRelate_ToMany_New() {

        e2().insertColumns("id", "name").values(24, "xxx").exec();

        Response r = target("/e2/24/e3s")
                .request()
                .post(Entity.json("{\"name\":\"zzz\"}"));

        onSuccess(r)
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");

        e3().matcher().assertOneMatch();
        e3().matcher().eq("e2_id", 24).eq("name", "zzz").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_New_CompoundId() {

        e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa").exec();

        Response r = target("/e17/e18s")
                .matrixParam("parentId1", 1)
                .matrixParam("parentId2", 1)
                .request()
                .post(Entity.json("{\"name\":\"xxx\"}"));

        onSuccess(r)
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"xxx\"}");


        e18().matcher().assertOneMatch();
        e18().matcher().eq("e17_id1", 1).eq("e17_id2", 1).eq("name", "xxx").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        e2().insertColumns("id", "name")
                .values(15, "xxx")
                .values(16, "yyy").exec();

        e3().insertColumns("id", "name", "e2_id")
                .values(7, "zzz", 16)
                .values(8, "yyy", 15)
                .values(9, "aaa", 15).exec();

        Response r1 = target("/e2/15/e3s")
                .request()
                .post(Entity.json("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]"));

        onSuccess(r1)
                .bodyEquals(2, "{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
                        + "{\"id\":1,\"name\":\"newname\",\"phoneNumber\":null}");

        e3().matcher().assertMatches(4);
        e3().matcher().eq("e2_id", 15).assertMatches(3);

        // testing non-idempotency

        Response r2 = target("/e2/15/e3s")
                .request()
                .post(Entity.json("[ {\"id\":8,\"name\":\"123\"}, {\"name\":\"newname\"} ]"));

        onSuccess(r2)
                .bodyEquals(2, "{\"id\":8,\"name\":\"123\",\"phoneNumber\":null},"
                        + "{\"id\":2,\"name\":\"newname\",\"phoneNumber\":null}");

        e3().matcher().assertMatches(5);
        e3().matcher().eq("e2_id", 15).assertMatches(4);
    }

    @Test
    public void testToManyJoin() {

        e12().insertColumns("id")
                .values(11)
                .values(12).exec();

        e13().insertColumns("id")
                .values(14)
                .values(15)
                .values(16).exec();

        Response r = target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .request()
                .post(Entity.json("[{\"e13\":15},{\"e13\":14}]"));

        onResponse(r)
                .wasCreated()
                .bodyEquals(2, "{},{}");

        e12_13().matcher().assertMatches(2);
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        e12_13().matcher().eq("e12_id", 12).eq("e13_id", 15).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdateE3sOfE2(@PathParam("id") int id, String targetData) {
            return Ag.createOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S).syncAndSelect(targetData);
        }

        @POST
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> create_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return Ag.create(E12E13.class, config)
                    .toManyParent(E12.class, id, E12.E1213)
                    .uri(info)
                    .syncAndSelect(entityData);
        }

        @POST
        @Path("e17/e18s")
        public DataResponse<E18> createOrUpdateE18s(
                @Context UriInfo uriInfo,
                @MatrixParam("parentId1") Integer parentId1,
                @MatrixParam("parentId2") Integer parentId2,
                String targetData) {

            Map<String, Object> parentIds = new HashMap<>();
            parentIds.put(E17.ID1_PK_COLUMN, parentId1);
            parentIds.put(E17.ID2_PK_COLUMN, parentId2);

            return Ag.createOrUpdate(E18.class, config).toManyParent(E17.class, parentIds, E17.E18S).uri(uriInfo)
                    .syncAndSelect(targetData);
        }
    }

}
