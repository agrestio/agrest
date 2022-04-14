package io.agrest.jpa;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import io.agrest.SimpleResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E17;
import io.agrest.jpa.model.E2;
import io.agrest.jpa.model.E3;
import io.agrest.jpa.model.E4;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

public class DELETE_IT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .build();

    @Test
    public void testDeleteAll() {

        tester.e4().insertColumns("ID", "C_VARCHAR")
                .values(1, "xxx")
                .values(35, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e4")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e4().matcher().assertNoMatches();
    }

    @Test
    public void testDeleteAll_Empty() {

        tester.target("/e4")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");
    }

    @Test
    public void testDeleteById() {

        tester.e4().insertColumns("ID", "C_VARCHAR")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e4/8")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e4().matcher().assertOneMatch();
    }

    @Test
    public void testDeleteById_CompoundId() {

        tester.e17().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e17").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e17().matcher().assertOneMatch();
        tester.e17().matcher().eq("ID2", 2).eq("ID2", 2).eq("NAME", "bbb").assertOneMatch();
    }

    @Test
    public void testDeleteById_BadId() {

        tester.e4().insertColumns("ID", "C_VARCHAR").values(1, "xxx").exec();

        tester.target("/e4/7")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}");

        tester.e4().matcher().assertMatches(1);
    }

    @Test
    public void testDeleteTwice() {

        tester.e4().insertColumns("ID", "C_VARCHAR")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e4/8")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.target("/e4/8")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}");
    }

//    @Test
//    public void testDelete_UpperCasePK() {
//
//        tester.e24().insertColumns("TYPE", "name").values(1, "xyz").exec();
//
//        tester.target("/e24/1")
//                .delete()
//                .wasOk()
//                .bodyEquals("{\"success\":true}");
//    }

    @Test
    public void testDelete_ByParentId() {

        tester.e2().insertColumns("ID")
                .values(1)
                .values(2)
                .values(3).exec();

        tester.e3().insertColumns("ID", "E2_ID")
                .values(1, 1)
                .values(2, 2)
                .values(3, 2)
                .values(4, 3).exec();

        tester.target("/e2/2/e3s")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e3().matcher().assertMatches(2);
        tester.e3().matcher().eq("ID", 1).assertOneMatch();
        tester.e3().matcher().eq("ID", 4).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("e2/{e2_id}/e3s")
        public SimpleResponse deleteByParent(@PathParam("e2_id") int e2Id) {
            return AgJaxrs.delete(E3.class, config).parent(E2.class, e2Id, "e3s").sync();
        }

        @DELETE
        @Path("e4")
        public SimpleResponse deleteAll() {
            return AgJaxrs.delete(E4.class, config).sync();
        }

        @DELETE
        @Path("e4/{id}")
        public SimpleResponse deleteById(@PathParam("id") int id) {
            return AgJaxrs.delete(E4.class, config).byId(id).sync();
        }

        @DELETE
        @Path("e17")
        public SimpleResponse deleteByMultiId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("id1", id1);
            ids.put("id2", id2);

            return AgJaxrs.delete(E17.class, config).byId(ids).sync();
        }

//        @DELETE
//        @Path("e24/{id}")
//        public SimpleResponse deleteE24ById(@PathParam("id") int id) {
//            return AgJaxrs.delete(E24.class, config).byId(id).sync();
//        }
    }
}
