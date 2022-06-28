package io.agrest.jpa;

import io.agrest.EntityDelete;
import io.agrest.SimpleResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E2;
import io.agrest.jpa.model.E3;
import io.agrest.jpa.model.E7;
import io.agrest.jpa.model.E8;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
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
    static final AgJpaTester tester = tester(E2Resource.class, E3Resource.class, E8Resource.class)
            .entities( E3.class, E7.class, E8.class,E2.class)
            .build();

    //TODO need to fix problem Column 'E8X0_.E8_ID' is either not in any table in the FROM list
    @Test
    public void testAll_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e8().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e7().insertColumns("ID", "NAME", "E8_ID")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e8/1/e7s").delete().wasOk().bodyEquals("{}");
        tester.e7().matcher().eq("E8_ID", 1).assertNoMatches();
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

        tester.target("/e2/1/e3s/9").delete().wasOk().bodyEquals("{}");

        tester.e3().matcher().eq("ID", 9).eq("E2_ID", null).assertOneMatch();
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

        tester.target("/e3/9/e2/1").delete().wasOk().bodyEquals("{}");

        tester.e3().matcher().eq("ID", 9).eq("E2_ID", null).assertOneMatch();
    }

    @Test
    public void testValidRel_ToOne_All() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("ID", "NAME")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("ID", "NAME", "E2_ID")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e3/9/e2").delete().wasOk().bodyEquals("{}");
        tester.e3().matcher().eq("ID", 9).eq("E2_ID", null).assertOneMatch();
    }

    @Test
    public void testInvalidRel() {
        tester.target("/e2/1/dummyRel/9")
                .delete()
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Invalid relationship: 'dummyRel'\"}");
    }

    @Test
    public void testNoSuchId_Source() {
        tester.target("/e2/22/e3s/9")
                .delete()
                .wasNotFound()
                .bodyEquals("{\"message\":\"No object for ID '22' and entity 'E2'\"}");
    }

    @Path("e2")
    public static class E2Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}")
        public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.delete(E2.class, config).byId(id).sync();
        }

        @Deprecated
        @DELETE
        public SimpleResponse deleteE2_Batch(Collection<EntityDelete<E2>> deleted, @Context UriInfo uriInfo) {
            return AgJaxrs.runtime(config).delete(E2.class, deleted);
        }

        @DELETE
        @Path("{id}/{rel}/{tid}")
        public SimpleResponse deleteToMany(
                @PathParam("id") int id,
                @PathParam("rel") String relationship,
                @PathParam("tid") int tid) {
            return AgJaxrs.unrelate(E2.class, config).sourceId(id).related(relationship, tid).sync();
        }
    }

    @Path("e3")
    public static class E3Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}/e2")
        public SimpleResponse deleteE2_Implicit(@PathParam("id") int id) {
            return AgJaxrs.unrelate(E3.class, config).sourceId(id).allRelated("e2").sync();
        }


        @DELETE
        @Path("{id}/e2/{tid}")
        public SimpleResponse deleteE2(@PathParam("id") int id, @PathParam("tid") int tid) {
            return AgJaxrs.unrelate(E3.class, config).sourceId(id).related("e2", tid).sync();
        }
    }

    @Path("e8")
    public static class E8Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}/e7s")
        public SimpleResponse deleteE7s(@PathParam("id") int id, String entityData) {
            return AgJaxrs.delete(E7.class, config).parent(E8.class, id, "e7s").sync();
        }
    }

}
