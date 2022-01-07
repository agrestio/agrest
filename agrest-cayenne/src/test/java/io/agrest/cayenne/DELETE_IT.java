package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E24;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class DELETE_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E17.class, E24.class)
            .build();

    @Test
    public void testDeleteAll() {

        tester.e4().insertColumns("id", "c_varchar")
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

        tester.e4().insertColumns("id", "c_varchar")
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

        tester.e17().insertColumns("id1", "id2", "name").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e17").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e17().matcher().assertOneMatch();
        tester.e17().matcher().eq("id2", 2).eq("id2", 2).eq("name", "bbb").assertOneMatch();
    }

    @Test
    public void testDeleteById_BadId() {

        tester.e4().insertColumns("id", "c_varchar").values(1, "xxx").exec();

        tester.target("/e4/7")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}");

        tester.e4().matcher().assertMatches(1);
    }

    @Test
    public void testDeleteTwice() {

        tester.e4().insertColumns("id", "c_varchar")
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

    @Test
    public void testDelete_UpperCasePK() {

        tester.e24().insertColumns("TYPE", "name").values(1, "xyz").exec();

        tester.target("/e24/1")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");
    }

    @Test
    public void testDelete_ByParentId() {

        tester.e2().insertColumns("id_")
                .values(1)
                .values(2)
                .values(3).exec();

        tester.e3().insertColumns("id_", "e2_id")
                .values(1, 1)
                .values(2, 2)
                .values(3, 2)
                .values(4, 3).exec();

        tester.target("/e2/2/e3s")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e3().matcher().assertMatches(2);
        tester.e3().matcher().eq("id_", 1).assertOneMatch();
        tester.e3().matcher().eq("id_", 4).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("e2/{e2_id}/e3s")
        public SimpleResponse deleteByParent(@PathParam("e2_id") int e2Id) {
            return Ag.delete(E3.class, config).parent(E2.class, e2Id, E2.E3S.getName()).sync();
        }

        @DELETE
        @Path("e4")
        public SimpleResponse deleteAll() {
            return Ag.delete(E4.class, config).sync();
        }

        @DELETE
        @Path("e4/{id}")
        public SimpleResponse deleteById(@PathParam("id") int id) {
            return Ag.service(config).delete(E4.class).id(id).sync();
        }

        @DELETE
        @Path("e17")
        public SimpleResponse deleteByMultiId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1.getName(), id1);
            ids.put(E17.ID2.getName(), id2);

            return Ag.service(config).delete(E17.class).id(ids).sync();
        }

        @DELETE
        @Path("e24/{id}")
        public SimpleResponse deleteE24ById(@PathParam("id") int id) {
            return Ag.delete(E24.class, config).id(id).sync();
        }
    }
}
