package com.nhl.link.rest.it;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E12E13;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E18;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GET_Related_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
        context.register(Resource.class);
    }

    @Test
    public void testGet_ToMany_Constrained() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");

        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        Response r1 = target("/e2/constraints/1/e3s").request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
    }

    @Test
    public void testGet_ToMany_CompoundId() {

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");
        insert("e18", "id, e17_id1, e17_id2, name", "1, 1, 1, 'xxx'");
        insert("e18", "id, e17_id1, e17_id2, name", "2, 1, 1, 'yyy'");
        insert("e18", "id, e17_id1, e17_id2, name", "3, 2, 2, 'zzz'");

        Response r1 = target("/e17/e18s").matrixParam("parentId1", 1).matrixParam("parentId2", 1).request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"name\":\"xxx\"},{\"id\":2,\"name\":\"yyy\"}],\"total\":2}",
                r1.readEntity(String.class));
    }

    @Test
    public void testGet_ValidRel_ToOne_CompoundId() {

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");
        insert("e18", "id, e17_id1, e17_id2, name", "1, 1, 1, 'xxx'");
        insert("e18", "id, e17_id1, e17_id2, name", "2, 1, 1, 'yyy'");
        insert("e18", "id, e17_id1, e17_id2, name", "3, 2, 2, 'zzz'");

        Response r1 = target("/e18/1").queryParam("include", E18.E17.getName()).request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals(
                "{\"data\":[{\"id\":1," + "\"e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"},"
                        + "\"name\":\"xxx\"}],\"total\":1}",
                r1.readEntity(String.class));
    }

    @Test
    public void testGet_CompoundId_UnmappedPk() {

        // remove a part of PK from the ObjEntity
        DataMap dataMap = DB_STACK.getCayenneStack().getChannel().getEntityResolver().getDataMap("datamap");
        ObjEntity E17 = dataMap.getObjEntity("E17");
        ObjAttribute unmappedAttribute = E17.getAttribute("id2");
        E17.removeAttribute("id2");

        insert("e17", "id1, id2, name", "1, 1, 'aaa'");
        insert("e17", "id1, id2, name", "2, 2, 'bbb'");
        insert("e18", "id, e17_id1, e17_id2, name", "1, 1, 1, 'xxx'");
        insert("e18", "id, e17_id1, e17_id2, name", "2, 1, 1, 'yyy'");
        insert("e18", "id, e17_id1, e17_id2, name", "3, 2, 2, 'zzz'");

        Response r1 = target("/e18/1").queryParam("include", E18.E17.getName()).request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":1,\"" + "e17\":{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"name\":\"aaa\"},"
                + "\"name\":\"xxx\"}],\"total\":1}", r1.readEntity(String.class));

        // restore initial state
        E17.addAttribute(unmappedAttribute);
    }

    @Test
    public void testGet_ValidRel_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        Response r1 = target("/e2/1/e3s").queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":8},{\"id\":9}],\"total\":2}", r1.readEntity(String.class));
    }

    @Test
    public void testGet_ValidRel_ToOne() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        insert("e2", "id, name", "1, 'xxx'");
        insert("e2", "id, name", "2, 'yyy'");
        insert("e3", "id, e2_id, name", "7, 2, 'zzz'");
        insert("e3", "id, e2_id, name", "8, 1, 'yyy'");
        insert("e3", "id, e2_id, name", "9, 1, 'zzz'");

        Response r1 = target("/e3/7/e2").queryParam("include", "id").request().get();

        assertEquals(Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{\"data\":[{\"id\":2}],\"total\":1}", r1.readEntity(String.class));
    }

    @Test
    public void testGet_InvalidRel() {
        Response r1 = target("/e2/1/dummyrel").request().get();

        assertEquals(Status.BAD_REQUEST.getStatusCode(), r1.getStatus());
        assertEquals("{\"success\":false,\"message\":\"Invalid relationship: 'dummyrel'\"}",
                r1.readEntity(String.class));
    }

    @Test
    public void testGET_ToManyJoin() {

        insert("e12", "id", "11");
        insert("e12", "id", "12");
        insert("e13", "id", "14");
        insert("e13", "id", "15");
        insert("e13", "id", "16");

        insert("e12_e13", "e12_id, e13_id", "11, 14");
        insert("e12_e13", "e12_id, e13_id", "12, 16");

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
        @Path("e12/{id}/e1213")
        public DataResponse<E12E13> get_Joins_NoId(@PathParam("id") int id, @Context UriInfo info) {
            return LinkRest.select(E12E13.class, config).toManyParent(E12.class, id, E12.E1213).uri(info).get();
        }

        @GET
        @Path("e18/{id}")
        public DataResponse<E18> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
            return LinkRest.select(E18.class, config).uri(uriInfo).byId(id).getOne();
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

            return LinkRest.select(E18.class, config).parent(E17.class, parentIds, E17.E18S.getName()).uri(uriInfo).get();
        }
    }
}
