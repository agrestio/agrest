package io.agrest.jpa;


import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
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
    static final AgJpaTester tester = tester(Resource.class)

            .entities(E3.class, E2.class, E17.class, E18.class, E12.class, E13.class)
            .build();

    @Test
    public void testRelate_ToMany_New() {

        tester.e2().insertColumns("ID", "NAME").values(24, "xxx").exec();

        tester.target("/e2/24/e3s")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("E2_ID", 24).eq("NAME", "zzz").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_New_CompoundId() {

        tester.e17().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa").exec();

        tester.target("/e17/e18s")
                .matrixParam("parentId1", 1)
                .matrixParam("parentId2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"xxx\"}");


        tester.e18().matcher().assertOneMatch();
        tester.e18().matcher().eq("E17_ID1", 1).eq("E17_ID2", 1).eq("NAME", "xxx").assertOneMatch();
    }

    @Test
    public void testRelate_ToMany_MixedCollection() {

        tester.e2().insertColumns("ID", "NAME")
                .values(15, "xxx")
                .values(16, "yyy").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
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
        tester.e3().matcher().eq("E2_ID", 15).assertMatches(3);

        // testing non-idempotency

        tester.target("/e2/15/e3s")
                .queryParam("include", "name")
                .post("[ {\"id\":60,\"name\":\"123\"}, {\"name\":\"newname\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"name\":\"123\"}", "{\"name\":\"newname\"}");

        tester.e3().matcher().assertMatches(5);
        tester.e3().matcher().eq("E2_ID", 15).assertMatches(4);
    }

    @Test
    public void testToManyJoin() {

        tester.e12().insertColumns("ID")
                .values(11)
                .values(12).exec();

        tester.e13().insertColumns("ID")
                .values(14)
                .values(15)
                .values(16).exec();

        tester.target("/e12/12/e1213")
                .queryParam("exclude", "id")
                .post("[{\"e13\":15},{\"e13\":14}]")
                .wasCreated()
                .bodyEquals(2, "{},{}");

        tester.e12_13().matcher().assertMatches(2);
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 14).assertOneMatch();
        tester.e12_13().matcher().eq("E12_ID", 12).eq("E13_ID", 15).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2/{id}/e3s")
        public DataResponse<E3> createOrUpdateE3sOfE2(@PathParam("id") int id, @Context UriInfo uri, String targetData) {
            return AgJaxrs.createOrUpdate(E3.class, config).clientParams(uri.getQueryParameters())
                    .parent(E2.class, id, E2.E3S)
                    .syncAndSelect(targetData);
        }

        @POST
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> create_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
            return AgJaxrs.create(E12E13.class, config)
                    .parent(E12.class, id, E12.E1213)
                    .clientParams(info.getQueryParameters())
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
            parentIds.put(E17.ID1, parentId1);
            parentIds.put(E17.ID2, parentId2);

            return AgJaxrs.createOrUpdate(E18.class, config)
                    .parent(E17.class, parentIds, E17.E18S)
                    .clientParams(uriInfo.getQueryParameters())
                    .syncAndSelect(targetData);
        }
    }

}
