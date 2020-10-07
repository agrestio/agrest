package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E24;
import io.agrest.cayenne.cayenne.main.E4;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class DELETE_IT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E4.class, E17.class, E24.class)
            .build();

    @Test
    public void test() {

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
    public void testCompoundId() {

        tester.e17().insertColumns("id1", "id2", "name").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e17").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.e17().matcher().assertOneMatch();
        tester.e17().matcher().eq("id2", 2).eq("id2", 2).eq("name", "bbb").assertOneMatch();
    }

    @Test
    public void testBadId() {

        tester.e4().insertColumns("id", "c_varchar").values(1, "xxx").exec();

        tester.target("/e4/7")
                .delete()
                .statusEquals(Status.NOT_FOUND)
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '7' and entity 'E4'\"}");

        tester.e4().matcher().assertMatches(1);
    }

    @Test
    public void testTwice() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e4/8")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");

        tester.target("/e4/8")
                .delete()
                .statusEquals(Status.NOT_FOUND)
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '8' and entity 'E4'\"}");
    }

    @Test
    public void testUpperCasePK() {

        tester.e24().insertColumns("TYPE", "name").values(1, "xyz").exec();

        tester.target("/e24/1")
                .delete()
                .wasOk()
                .bodyEquals("{\"success\":true}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("e4/{id}")
        public SimpleResponse deleteById(@PathParam("id") int id) {
            return Ag.service(config).delete(E4.class, id);
        }

        @DELETE
        @Path("e17")
        public SimpleResponse deleteByMultiId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1_PK_COLUMN, id1);
            ids.put(E17.ID2_PK_COLUMN, id2);

            return Ag.service(config).delete(E17.class, ids);
        }

        @DELETE
        @Path("e24/{id}")
        public SimpleResponse deleteE24ById(@PathParam("id") int id) {
            return Ag.delete(E24.class, config).id(id).delete();
        }
    }
}
