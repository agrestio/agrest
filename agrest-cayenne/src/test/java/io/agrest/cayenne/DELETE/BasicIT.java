package io.agrest.cayenne.DELETE;

import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E24;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class BasicIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E17.class, E24.class)
            .build();

    @Test
    public void deleteAll() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(35, "zzz")
                .values(8, "yyy").exec();

        tester.target("/e4")
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e4().matcher().assertNoMatches();
    }

    @Test
    public void deleteAll_Empty() {

        tester.target("/e4")
                .delete()
                .wasOk()
                .bodyEquals("{}");
    }

    @Test
    public void deleteById() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e4/8")
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e4().matcher().assertOneMatch();
    }

    @Test
    public void deleteById_CompoundId() {

        tester.e17().insertColumns("id1", "id2", "name").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e17").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e17().matcher().assertOneMatch();
        tester.e17().matcher().eq("id2", 2).andEq("id2", 2).andEq("name", "bbb").assertOneMatch();
    }

    @Test
    public void deleteById_BadId() {

        tester.e4().insertColumns("id", "c_varchar").values(1, "xxx").exec();

        tester.target("/e4/7")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"message\":\"No object for ID '7' and entity 'E4'\"}");

        tester.e4().matcher().assertMatches(1);
    }

    @Test
    public void deleteTwice() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e4/8")
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.target("/e4/8")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"message\":\"No object for ID '8' and entity 'E4'\"}");
    }

    @Test
    public void delete_UpperCasePK() {

        tester.e24().insertColumns("TYPE", "name").values(1, "xyz").exec();

        tester.target("/e24/1")
                .delete()
                .wasOk()
                .bodyEquals("{}");
    }

    @Test
    public void delete_ByParentId() {

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
                .bodyEquals("{}");

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
            return AgJaxrs.delete(E3.class, config).parent(E2.class, e2Id, E2.E3S.getName()).sync();
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
            ids.put(E17.ID1.getName(), id1);
            ids.put(E17.ID2.getName(), id2);

            return AgJaxrs.delete(E17.class, config).byId(ids).sync();
        }

        @DELETE
        @Path("e24/{id}")
        public SimpleResponse deleteE24ById(@PathParam("id") int id) {
            return AgJaxrs.delete(E24.class, config).byId(id).sync();
        }
    }
}
