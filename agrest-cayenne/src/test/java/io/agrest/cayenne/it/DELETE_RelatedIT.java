package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.EntityDelete;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E7;
import io.agrest.it.fixture.cayenne.E8;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class DELETE_RelatedIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(E2Resource.class, E3Resource.class, E8Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E7.class, E8.class};
    }

    @Test
    public void testAll_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e8().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e7().insertColumns("id", "name", "e8_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e8/1/e7s").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        e7().matcher().eq("e8_id", 1).assertNoMatches();
    }

    @Test
    public void testValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e2/1/e3s/9").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Integer> ids1 = e3()
                .selectStatement(rs -> {
                    int i = rs.getInt(1);
                    return rs.wasNull() ? null : i;
                }).append("SELECT e2_id FROM utest.e3 WHERE id_ = 9")
                .select(100);
        assertEquals(1, ids1.size());
        assertNull(ids1.get(0));
    }

    @Test
    public void testValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e3/9/e2/1").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Integer> ids1 = e3()
                .selectStatement(rs -> {
                    int i = rs.getInt(1);
                    return rs.wasNull() ? null : i;
                }).append("SELECT e2_id FROM utest.e3 WHERE id_ = 9")
                .select(100);
        assertEquals(1, ids1.size());
        assertNull(ids1.get(0));
    }

    @Test
    public void testValidRel_ToOne_All() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e3/9/e2").request().delete();
        onSuccess(r).bodyEquals("{\"success\":true}");

        // TODO: can't use matcher for NULLs until BQ 1.1 upgrade (because of https://github.com/bootique/bootique-jdbc/issues/91 )
        //  so using select...

        List<Integer> ids1 = e3()
                .selectStatement(rs -> {
                    int i = rs.getInt(1);
                    return rs.wasNull() ? null : i;
                }).append("SELECT e2_id FROM utest.e3 WHERE id_ = 9")
                .select(100);
        assertEquals(1, ids1.size());
        assertNull(ids1.get(0));
    }

    @Test
    public void testInvalidRel() {
        Response r = target("/e2/1/dummyRel/9").request().delete();
        onResponse(r).statusEquals(Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyRel'\"}");
    }

    @Test
    public void testNoSuchId_Source() {
        Response r = target("/e2/22/e3s/9").request().delete();

        onResponse(r).statusEquals(Status.NOT_FOUND)
                .bodyEquals("{\"success\":false,\"message\":\"No object for ID '22' and entity 'E2'\"}");
    }

    @Path("e2")
    public static class E2Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}")
        public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.service(config).delete(E2.class, id);
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
            return Ag.service(config).unrelate(E3.class, id, E3.E2);
        }


        @DELETE
        @Path("{id}/e2/{tid}")
        public SimpleResponse deleteE2(@PathParam("id") int id, @PathParam("tid") int tid) {
            return Ag.service(config).unrelate(E3.class, id, E3.E2, tid);
        }
    }

    @Path("e8")
    public static class E8Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("{id}/e7s")
        public SimpleResponse deleteE7s(@PathParam("id") int id, String entityData) {
            return Ag.delete(E7.class, config).toManyParent(E8.class, id, E8.E7S).delete();
        }
    }

}
