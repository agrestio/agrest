package io.agrest.jpa;

import io.agrest.SimpleResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
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

public class DELETE_CompoundIdIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .entities(E17.class, E31.class, E32.class)
            .build();


    @Test
    public void testDeleteById_CompoundId() {

        tester.e17().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e17").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e17().matcher().assertOneMatch();
        tester.e17().matcher().eq("ID2", 2).eq("ID2", 2).eq("NAME", "bbb").assertOneMatch();
    }

    @Test
    public void testDeleteByCompoundIdWithIdClass() {

        tester.e31().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e31").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e31().matcher().assertOneMatch();
        tester.e31().matcher().eq("ID2", 2).eq("ID2", 2).eq("NAME", "bbb").assertOneMatch();
    }

    @Test
    public void testDeleteByCompoundIdWithEmbeddedIdClass() {

        tester.e32().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").values(2, 2, "bbb").exec();

        tester.target("/e32").queryParam("id1", 1).queryParam("id2", 1)
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e32().matcher().assertOneMatch();
        tester.e32().matcher().eq("ID2", 2).eq("ID2", 2).eq("NAME", "bbb").assertOneMatch();
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


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

        @DELETE
        @Path("e31")
        public SimpleResponse deleteByCompoundIdWithIdClass(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("id1", id1);
            ids.put("id2", id2);

            return AgJaxrs.delete(E31.class, config).byId(ids).sync();
        }

        @DELETE
        @Path("e32")
        public SimpleResponse deleteByCompoundIdWithEmbeddedIdClass(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            E32EmbeddedIdClass e32EmbeddedIdClass = new E32EmbeddedIdClass(id1, id2);

            return AgJaxrs.delete(E32.class, config).byId(e32EmbeddedIdClass).sync();
        }


    }
}
