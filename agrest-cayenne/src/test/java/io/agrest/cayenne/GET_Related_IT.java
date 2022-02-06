package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E12;
import io.agrest.cayenne.cayenne.main.E12E13;
import io.agrest.cayenne.cayenne.main.E13;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E18;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E29;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E30;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class GET_Related_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E17.class, E18.class)
            .entitiesAndDependencies(E12.class, E13.class, E29.class)
            .build();

    @Test
    public void testToMany_CompoundId() {

        tester.e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.e18().insertColumns("id", "e17_id1", "e17_id2", "name")
                .values(1, 1, 1, "xxx")
                .values(2, 1, 1, "yyy")
                .values(3, 2, 2, "zzz").exec();

        tester.target("/e17/e18s")
                .matrixParam("parentId1", 1)
                .matrixParam("parentId2", 1)
                .get().wasOk().bodyEquals(2, "{\"id\":1,\"name\":\"xxx\"},{\"id\":2,\"name\":\"yyy\"}");
    }

    @Test
    public void testValidRel_ToOne_CompoundId() {

        tester.e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.e18().insertColumns("id", "e17_id1", "e17_id2", "name")
                .values(1, 1, 1, "xxx")
                .values(2, 1, 1, "yyy")
                .values(3, 2, 2, "zzz").exec();

        tester.target("/e18/1").queryParam("include", E18.E17.getName()).get().wasOk()
                .bodyEquals(1, "{\"id\":1,\"e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"},\"name\":\"xxx\"}");
    }

    @Test
    public void testValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e2/1/e3s").queryParam("include", "id").get().wasOk().bodyEquals(2, "{\"id\":8},{\"id\":9}");
    }

    @Test
    public void testValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3/7/e2").queryParam("include", "id").get().wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testInvalidRel() {
        tester.target("/e2/1/dummyrel").get()
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"Invalid parent relationship: 'dummyrel'\"}");
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

        tester.e12_13().insertColumns("e12_id", "e13_id")
                .values(11, 14)
                .values(12, 16)
                .exec();

        tester.target("/e12/12/e1213")
                .queryParam("include", "e12", "e13")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":{\"db:e12_id\":12,\"db:e13_id\":16},\"e12\":{\"id\":12},\"e13\":{\"id\":16}}");
    }

    @Test
    public void testByParentCompoundDbId() {

        tester.e29().insertColumns("id1", "id2")
                .values(1, 15)
                .values(2, 35).exec();

        tester.e30().insertColumns("id", "e29_id1", "e29_id2")
                .values(1, 1, 15)
                .values(2, 2, 35)
                .values(3, 1, 15).exec();

        tester.target("/e30_compound_db/1/15")
                .get().wasOk()
                .bodyEquals(2, "{\"id\":1}", "{\"id\":3}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2/{id}/dummyrel")
        public DataResponse<E3> getE2_Dummyrel(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).parent(E2.class, id, "dummyrel").clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e2/{id}/e3s")
        public DataResponse<E3> getE2_E3s(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).parent(E2.class, id, "e3s").clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e3/{id}/e2")
        public DataResponse<E2> getE2OfE3(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E2.class, config).parent(E3.class, id, E3.E2.getName()).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> get_Joins_NoId(@PathParam("id") int id, @Context UriInfo info) {
            return AgJaxrs.select(E12E13.class, config)
                    .parent(E12.class, id, E12.E1213.getName())
                    .clientParams(info.getQueryParameters()).get();
        }

        @GET
        @Path("e18/{id}")
        public DataResponse<E18> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
            return AgJaxrs.select(E18.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).getOne();
        }

        @GET
        @Path("e17/e18s")
        public DataResponse<E18> getChildren(
                @Context UriInfo uriInfo,
                @MatrixParam("parentId1") Integer parentId1,
                @MatrixParam("parentId2") Integer parentId2) {

            Map<String, Object> parentIds = new HashMap<>();
            parentIds.put(E17.ID1.getName(), parentId1);
            parentIds.put(E17.ID2.getName(), parentId2);

            return AgJaxrs.select(E18.class, config).parent(E17.class, parentIds, E17.E18S.getName()).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e30_compound_db/{parent_id1}/{parent_id2}")
        public DataResponse<E30> getByParentCompoundDbId(
                @Context UriInfo uriInfo,
                @PathParam("parent_id1") Integer parentId1,
                @PathParam("parent_id2") Integer parentId2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("db:" + E29.ID1_PK_COLUMN, parentId1);
            ids.put(E29.ID2PROP.getName(), parentId2);

            return AgJaxrs.select(E30.class, config).clientParams(uriInfo.getQueryParameters()).parent(E29.class, ids, E29.E30S.getName()).get();
        }
    }
}
