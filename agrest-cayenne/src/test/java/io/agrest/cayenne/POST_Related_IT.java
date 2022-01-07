package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.*;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class POST_Related_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E17.class, E18.class)
            .entitiesAndDependencies(E12.class, E13.class)
            .build();

    @Test
    public void testRelate_ToMany_New() {

        tester.e2().insertColumns("id_", "name").values(24, "xxx").exec();

        tester.target("/e2/24/e3s")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("e2_id", 24).eq("name", "zzz").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_New_CompoundId() {

        tester.e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa").exec();

        tester.target("/e17/e18s")
                .matrixParam("parentId1", 1)
                .matrixParam("parentId2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"xxx\"}");


        tester.e18().matcher().assertOneMatch();
        tester.e18().matcher().eq("e17_id1", 1).eq("e17_id2", 1).eq("name", "xxx").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        tester.e2().insertColumns("id_", "name")
                .values(15, "xxx")
                .values(16, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(12, "zzz", 16)
                .values(60, "yyy", 15)
                .values(90, "aaa", 15).exec();

        tester.target("/e2/15/e3s")
                // only including name in response, as IDs are generated and it is hard to assert them properly
                .queryParam("include", "name")
                .post("[ {\"id\":60,\"name\":\"123\"}, {\"name\":\"newname\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"name\":\"123\"}", "{\"name\":\"newname\"}");

        tester.e3().matcher().assertMatches(4);
        tester.e3().matcher().eq("e2_id", 15).assertMatches(3);

        // testing non-idempotency

        tester.target("/e2/15/e3s")
                .queryParam("include", "name")
                .post("[ {\"id\":60,\"name\":\"123\"}, {\"name\":\"newname\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"name\":\"123\"}", "{\"name\":\"newname\"}");

        tester.e3().matcher().assertMatches(5);
        tester.e3().matcher().eq("e2_id", 15).assertMatches(4);
    }

    @Test
    public void testToManyJoin() {

        tester.e12().insertColumns("id")
                .values(11)
                .values(12).exec();

        tester.e13().insertColumns("id")
                .values(14)
                .values(15)
                .values(16).exec();

        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .post("[{\"e13\":15},{\"e13\":14}]")
                .wasCreated()
                .bodyEquals(2, "{},{}");

        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 14).assertOneMatch();
        tester.e12_13().matcher().eq("e12_id", 12).eq("e13_id", 15).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdateE3sOfE2(@PathParam("id") int id, @Context UriInfo uri,  String targetData) {
            return Ag.createOrUpdate(E3.class, config).uri(uri)
                    .parent(E2.class, id, E2.E3S.getName())
                    .syncAndSelect(targetData);
        }

        @POST
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> create_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return Ag.create(E12E13.class, config)
                    .parent(E12.class, id, E12.E1213.getName())
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
            parentIds.put(E17.ID1.getName(), parentId1);
            parentIds.put(E17.ID2.getName(), parentId2);

            return Ag.createOrUpdate(E18.class, config)
                    .parent(E17.class, parentIds, E17.E18S.getName())
                    .uri(uriInfo)
                    .syncAndSelect(targetData);
        }
    }

}
