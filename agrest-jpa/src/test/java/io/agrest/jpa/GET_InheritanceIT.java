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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;

class GET_InheritanceIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .entities(E33.class,E34.class)
            .build();

    @Test
    public void testResponse() {

        tester.e34().insertColumns("ID","FIRSTNAME","LASTNAME")
                .values(2,"Pablo","Picasso")
                .exec();

        tester.target("/e34")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2,\"firstName\":\"Pablo\"," + "\"lastName\":\"Picasso\"}");
    }


    @Test
    public void testById() {
        tester.e34().insertColumns("ID","FIRSTNAME","LASTNAME")
                .values(2,"Pablo","Picasso")
                .exec();

        tester.target("/e34/2")
                .get().wasOk()
                .bodyEquals(1, "{\"id\":2,\"firstName\":\"Pablo\"," + "\"lastName\":\"Picasso\"}");
    }


    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private javax.ws.rs.core.Configuration config;

        @GET
        @Path("e34")
        public DataResponse<E34> getE34(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E34.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e34/{id}")
        public DataResponse<E34> getE34BuId(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E34.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }


    }


}