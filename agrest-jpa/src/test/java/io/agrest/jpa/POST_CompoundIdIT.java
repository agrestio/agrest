package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class POST_CompoundIdIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(POST_CompoundIdIT.Resource.class)
            .entities(E17.class, E31.class, E32.class)
            .build();


    @Test
    public void testCompoundId() {

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");
    }

    @Test
    public void testCompoundIdWithIdClass() {

        tester.target("/e31")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");
    }

    @Test
    public void testCompoundIdWithEmbeddedIdClass() {

        tester.target("/e32")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"name\":\"xxx\"}");
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("e17")
        public DataResponse<E17> createE17(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String requestBody) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1, id1);
            ids.put(E17.ID2, id2);

            return AgJaxrs.create(E17.class, config).byId(ids).syncAndSelect(requestBody);
        }

        @POST
        @Path("e31")
        public DataResponse<E31> createE31(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String requestBody) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E31.ID1, id1);
            ids.put(E31.ID2, id2);

            return AgJaxrs.create(E31.class, config).byId(ids).syncAndSelect(requestBody);
        }

        @POST
        @Path("e32")
        public DataResponse<E32> createE32(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String requestBody) {

            E32EmbeddedIdClass e32EmbeddedIdClass = new E32EmbeddedIdClass(id1, id2);

            return AgJaxrs.create(E32.class, config).byId(e32EmbeddedIdClass).syncAndSelect(requestBody);
        }

    }
}
