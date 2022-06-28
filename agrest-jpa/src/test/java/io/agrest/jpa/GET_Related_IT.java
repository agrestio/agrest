package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
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
    static final AgJpaTester tester = tester(Resource.class)
            .entities(E3.class, E2.class, E18.class, E17.class, E12E13.class, E13.class, E12.class, E30.class, E29.class)
            .build();

    @Test
    public void testToMany_CompoundId() {

        tester.e17().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.e18().insertColumns("ID", "E17_ID1", "E17_ID2", "NAME")
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

        tester.e17().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.e18().insertColumns("ID", "E17_ID1", "E17_ID2", "NAME")
                .values(1, 1, 1, "xxx")
                .values(2, 1, 1, "yyy")
                .values(3, 2, 2, "zzz").exec();

        tester.target("/e18/1").queryParam("include", E18.E17).get().wasOk()
                .bodyEquals(1, "{\"id\":1,\"e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"},\"name\":\"xxx\"}");
    }

    @Test
    public void testValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e2/1/e3s").queryParam("include", "id")
                .get().wasOk()
                .bodyEquals(2, "{\"id\":8},{\"id\":9}");
    }

    @Test
    public void testValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3/7/e2").queryParam("include", "id").get().wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testInvalidRel() {
        tester.target("/e2/1/dummyrel").get()
                .wasServerError()
                .bodyEquals("{\"message\":\"Invalid parent relationship: 'dummyrel'\"}");
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

        tester.e12_13().insertColumns("E12_ID", "E13_ID")
                .values(11, 14)
                .values(12, 16)
                .exec();

        tester.target("/e12/12/e1213s")
                .queryParam("include", "e12", "e13")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":{\"db:e12_id\":12,\"db:e13_id\":16},\"e12\":{\"id\":12},\"e13\":{\"id\":16}}");
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
            return AgJaxrs.select(E2.class, config).parent(E3.class, id, E3.E2).clientParams(uriInfo.getQueryParameters()).get();
        }


        @GET
        @Path("e12/{id}/e1213s")
        public DataResponse<E12E13> get_Joins_NoId(@PathParam("id") int id, @Context UriInfo info) {
            return AgJaxrs.select(E12E13.class, config)
                    .parent(E12.class, id, E12.E1213S)
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
            parentIds.put(E17.ID1, parentId1);
            parentIds.put(E17.ID2, parentId2);

            return AgJaxrs.select(E18.class, config).parent(E17.class, parentIds, E17.E18S).clientParams(uriInfo.getQueryParameters()).get();
        }
    }
}
