package io.agrest.it;

import io.agrest.Ag;
import io.agrest.EntityDelete;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E7;
import io.agrest.it.fixture.cayenne.E8;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class DELETE_RelatedIT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
        context.register(E8Resource.class);
    }

    @Test
    public void testDelete_All_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e8", "id, name", "1, 'xxx'");
        insert("e8", "id, name", "2, 'yyy'");
        insert("e7", "id, e8_id, name", "7, 2, 'zzz'");
        insert("e7", "id, e8_id, name", "8, 1, 'yyy'");
        insert("e7", "id, e8_id, name", "9, 1, 'zzz'");

        assertEquals(2, intForQuery("SELECT COUNT(1) FROM utest.e7 WHERE e8_id = 1"));
        Response r1 = target("/e8/1/e7s").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(0, intForQuery("SELECT COUNT(1) FROM utest.e7 WHERE e8_id = 1"));
    }

    @Test
    public void testDelete_ValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

        Response r1 = target("/e2/1/e3s/9").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
    }

    @Test
    public void testDelete_ValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

        Response r1 = target("/e3/9/e2/1").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
    }

    @Test
    public void testDelete_ValidRel_ToOne_All() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        assertEquals(1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));

        Response r1 = target("/e3/9/e2").request().delete();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":true}", r1.readEntity(String.class));

        assertEquals(-1, intForQuery("SELECT e2_id FROM utest.e3 WHERE id = 9"));
    }

    @Test
    public void testDelete_InvalidRel() {
        Response r1 = target("/e2/1/dummyRel/9").request().delete();

        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyRel'\"}",
                r1.readEntity(String.class));
    }

    @Test
    public void testDelete_NoSuchId_Source() {
        Response r1 = target("/e2/22/e3s/9").request().delete();
        assertEquals(Status.NOT_FOUND.getStatusCode(), r1.getStatus());

        String responseEntity = r1.readEntity(String.class).replaceFirst("\\'[\\d]+\\'", "''");
        assertEquals("{\"success\":false,\"message\":\"No object for ID '' and entity 'E2'\"}", responseEntity);
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
            return Ag.delete(E7.class, config).toManyParent(E8.class, id, E8.E7S.getName()).delete();
        }
    }

}
