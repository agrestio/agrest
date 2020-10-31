package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.EntityDelete;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E7;
import io.agrest.cayenne.cayenne.main.E8;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

public class DELETE_RelatedIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(E2Resource.class, E3Resource.class, E8Resource.class)
            .entities(E2.class, E3.class, E7.class, E8.class)
            .build();

    @Test
    public void testAll_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e8().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e8/1/e7s").delete().wasOk().bodyEquals("{\"success\":true}");
        tester.e7().matcher().eq("e8_id", 1).assertNoMatches();
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

        tester.target("/e2/1/e3s/9").delete().wasOk().bodyEquals("{\"success\":true}");

        tester.e3().matcher().eq("id_", 9).eq("e2_id", null).assertOneMatch();
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

        tester.target("/e3/9/e2/1").delete().wasOk().bodyEquals("{\"success\":true}");

        tester.e3().matcher().eq("id_", 9).eq("e2_id", null).assertOneMatch();
    }

    @Test
    public void testValidRel_ToOne_All() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3/9/e2").delete().wasOk().bodyEquals("{\"success\":true}");
        tester.e3().matcher().eq("id_", 9).eq("e2_id", null).assertOneMatch();
    }

    @Test
    public void testInvalidRel() {
        tester.target("/e2/1/dummyRel/9")
                .delete()
                .wasBadRequest()
                .bodyEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyRel'\"}");
    }

    @Test
    public void testNoSuchId_Source() {
        tester.target("/e2/22/e3s/9")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '22' and entity 'E2'\"}");
    }

    @Path("e2")
    public static class E2Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}")
        public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.delete(E2.class, config).id(id).delete();
        }

        @DELETE
        public SimpleResponse deleteE2_Batch(Collection<EntityDelete<E2>> deleted, @Context UriInfo uriInfo) {
            return Ag.service(config).delete(E2.class, deleted);
        }

        @DELETE
        @Path("{id}/{rel}/{tid}")
        public SimpleResponse deleteToMany(@PathParam("id") int id, @PathParam("rel") String relationship,
                                           @PathParam("tid") int tid) {
            return Ag.service(config).unrelate(E2.class, id, relationship, tid);
        }
    }

    @Path("e3")
    public static class E3Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}/e2")
        public SimpleResponse deleteE2_Implicit(@PathParam("id") int id) {
            return Ag.service(config).unrelate(E3.class, id, E3.E2.getName());
        }


        @DELETE
        @Path("{id}/e2/{tid}")
        public SimpleResponse deleteE2(@PathParam("id") int id, @PathParam("tid") int tid) {
            return Ag.service(config).unrelate(E3.class, id, E3.E2.getName(), tid);
        }
    }

    @Path("e8")
    public static class E8Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}/e7s")
        public SimpleResponse deleteE7s(@PathParam("id") int id, String entityData) {
            return Ag.delete(E7.class, config).parent(E8.class, id, E8.E7S.getName()).delete();
        }
    }

}
