package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E17;
import io.agrest.jpa.model.E31;
import io.agrest.jpa.model.E31IdClass;
import io.agrest.jpa.model.E32;
import io.agrest.jpa.model.E32EmbeddedIdClass;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class PUT_CompoundIdIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(PUT_CompoundIdIT.Resource.class)
            .entities(E17.class,E31.class,E32.class)
            .build();

    @Test
    public void testExplicitCompoundId() {

        tester.e17().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .put("{\"name\":\"xxx\"}")
                .wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");

        tester.e17().matcher().eq("ID1", 1).eq("ID2", 1).eq("NAME", "xxx").assertOneMatch();
    }

    @Test
    public void testExplicitByCompoundIdWithIdClass() {

        tester.e31().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.target("/e31")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .put("{\"name\":\"xxx\"}")
                .wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");

        tester.e31().matcher().eq("ID1", 1).eq("ID2", 1).eq("NAME", "xxx").assertOneMatch();
    }

    @Test
    public void testExplicitCompoundIdWithEmbeddedIdClass() {

        tester.e32().insertColumns("ID1", "ID2", "NAME")
                .values(1, 1, "aaa")
                .values(2, 2, "bbb").exec();

        tester.target("/e32")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .put("{\"name\":\"xxx\"}")
                .wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");

        tester.e32().matcher().eq("ID1", 1).eq("ID2", 1).eq("NAME", "xxx").assertOneMatch();
    }



    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e17")
        public DataResponse<E17> updateById(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String targetData) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("id1", id1);
            ids.put("id2", id2);
            return AgJaxrs.update(E17.class, config).clientParams(uriInfo.getQueryParameters()).byId(ids).syncAndSelect(targetData);
        }

        @DELETE
        @Path("e31")
        public DataResponse<E31> updateByCompoundIdWithIdClass(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String targetData) {

            E31IdClass e31IdClass = new E31IdClass(id1, id2);

            return AgJaxrs.update(E31.class, config).clientParams(uriInfo.getQueryParameters()).byId(e31IdClass).syncAndSelect(targetData);
        }

        @DELETE
        @Path("e32")
        public DataResponse<E32> updateByCompoundIdWithEmbeddedIdClass(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String targetData) {

            E32EmbeddedIdClass e32EmbeddedIdClass = new E32EmbeddedIdClass(id1, id2);

            return AgJaxrs.update(E32.class, config).clientParams(uriInfo.getQueryParameters()).byId(e32EmbeddedIdClass).syncAndSelect(targetData);
        }

    }
}
