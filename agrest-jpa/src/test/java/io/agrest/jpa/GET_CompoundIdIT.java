package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.converter.jsonvalue.UtcDateConverter;
import io.agrest.encoder.DateTimeFormatters;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.*;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class GET_CompoundIdIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .entities(E17.class,E31.class,E32.class).build();


    @Test
    public void testByCompoundId() {
        tester.e17().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").exec();
        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .get().wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"}");
    }

    @Test
    public void testByCompoundIdWithIdClass() {
        tester.e31().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").exec();
        tester.target("/e31")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .get().wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"}");
    }

    @Test
    public void testByCompoundIdWithEmbeddedIdClass() {
        tester.e32().insertColumns("ID1", "ID2", "NAME").values(1, 1, "aaa").exec();
        tester.target("/e32")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .get().wasOk().bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"aaa\"}");
    }


    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private javax.ws.rs.core.Configuration config;



        @GET
        @Path("e17")
        public DataResponse<E17> getByCompoundId(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            Map<String, Object> ids = new HashMap<>();
            ids.put("id1", id1);
            ids.put("id2", id2);

            return AgJaxrs.select(E17.class, config).clientParams(uriInfo.getQueryParameters()).byId(ids).getOne();
        }

        @GET
        @Path("e31")
        public DataResponse<E31> getByCompoundIdWithIdClass(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

                E31IdClass e31IdClass = new E31IdClass(id1, id2);

                return AgJaxrs.select(E31.class, config).clientParams(uriInfo.getQueryParameters()).byId(e31IdClass).getOne();
        }


        @GET
        @Path("e32")
        public DataResponse<E32> getByCompoundIdWithEmbeddedIdClass(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2) {

            E32EmbeddedIdClass e32EmbeddedIdClass = new E32EmbeddedIdClass(id1, id2);

            return AgJaxrs.select(E32.class, config).clientParams(uriInfo.getQueryParameters()).byId(e32EmbeddedIdClass).getOne();
        }
    }


}