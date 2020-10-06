package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.constraints.Constraint;
import io.agrest.it.fixture.cayenne.E12;
import io.agrest.it.fixture.cayenne.E12E13;
import io.agrest.it.fixture.cayenne.E13;
import io.agrest.it.fixture.cayenne.E17;
import io.agrest.it.fixture.cayenne.E18;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GET_Related_IT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E17.class, E18.class};
    }

    @Override
    protected Class<?>[] testEntitiesAndDependencies() {
        return new Class[]{E12.class, E13.class};
    }

    @Test
    public void testToMany_Constrained() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e2/constraints/1/e3s").request().get();
        onSuccess(r).bodyEquals(2, "{\"id\":8},{\"id\":9}");
    }

    @Test
    public void testToMany_CompoundId() {

        e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        e18().insertColumns("id", "e17_id1", "e17_id2", "name")
                .values(1, 1, 1, "xxx")
                .values(2, 1, 1, "yyy")
                .values(3, 2, 2, "zzz").exec();

        Response r = target("/e17/e18s")
                .matrixParam("parentId1", 1)
                .matrixParam("parentId2", 1)
                .request().get();

        onSuccess(r).bodyEquals(2, "{\"id\":1,\"name\":\"xxx\"},{\"id\":2,\"name\":\"yyy\"}");
    }

    @Test
    public void testValidRel_ToOne_CompoundId() {

        e17().insertColumns("id1", "id2", "name")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        e18().insertColumns("id", "e17_id1", "e17_id2", "name")
                .values(1, 1, 1, "xxx")
                .values(2, 1, 1, "yyy")
                .values(3, 2, 2, "zzz").exec();

        Response r = target("/e18/1").queryParam("include", E18.E17.getName()).request().get();
        onSuccess(r)
                .bodyEquals(1, "{\"id\":1,\"e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"},\"name\":\"xxx\"}");
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

        Response r = target("/e2/1/e3s").queryParam("include", "id").request().get();
        onSuccess(r).bodyEquals(2, "{\"id\":8},{\"id\":9}");
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

        Response r = target("/e3/7/e2").queryParam("include", "id").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testInvalidRel() {
        Response r = target("/e2/1/dummyrel").request().get();
        onResponse(r)
                .statusEquals(Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyrel'\"}");
    }

    @Test
    public void testToManyJoin() {

        e12().insertColumns("id")
                .values(11)
                .values(12).exec();

        e13().insertColumns("id")
                .values(14)
                .values(15)
                .values(16).exec();

        e12_13().insertColumns("e12_id", "e13_id")
                .values(11, 14)
                .values(12, 16)
                .exec();

        // excluding ID - can't render multi-column IDs yet
        Response r1 = target("/e12/12/e1213").queryParam("exclude", "id").queryParam("include", "e12")
                .queryParam("include", "e13").request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"e12\":{\"id\":12},\"e13\":{\"id\":16}}],\"total\":1}", r1.readEntity(String.class));
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2/{id}/dummyrel")
        public DataResponse<E3> getE2_Dummyrel(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E3.class, config).parent(E2.class, id, "dummyrel").uri(uriInfo).get();
        }

        @GET
        @Path("e2/{id}/e3s")
        public DataResponse<E3> getE2_E3s(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E3.class, config).parent(E2.class, id, "e3s").uri(uriInfo).get();
        }

        @GET
        @Path("e2/constraints/{id}/e3s")
        public DataResponse<E3> getE2_E3s_Constrained(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E3.class, config).parent(E2.class, id, "e3s").uri(uriInfo)
                    .constraint(Constraint.idOnly(E3.class)).get();
        }

        @GET
        @Path("e3/{id}/e2")
        public DataResponse<E2> getE2OfE3(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.select(E2.class, config).parent(E3.class, id, E3.E2).uri(uriInfo).get();
        }

        @GET
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> get_Joins_NoId(@PathParam("id") int id, @Context UriInfo info) {
            return Ag.select(E12E13.class, config).toManyParent(E12.class, id, E12.E1213).uri(info).get();
        }

        @GET
        @Path("e18/{id}")
        public DataResponse<E18> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
            return Ag.select(E18.class, config).uri(uriInfo).byId(id).getOne();
        }

        @GET
        @Path("e17/e18s")
        public DataResponse<E18> getChildren(
                @Context UriInfo uriInfo,
                @MatrixParam("parentId1") Integer parentId1,
                @MatrixParam("parentId2") Integer parentId2) {

            Map<String, Object> parentIds = new HashMap<>();
            parentIds.put(E17.ID1_PK_COLUMN, parentId1);
            parentIds.put(E17.ID2_PK_COLUMN, parentId2);

            return Ag.select(E18.class, config).parent(E17.class, parentIds, E17.E18S.getName()).uri(uriInfo).get();
        }
    }
}
