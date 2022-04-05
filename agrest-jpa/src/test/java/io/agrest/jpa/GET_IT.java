package io.agrest.jpa;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E1;
import io.agrest.jpa.model.E4;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

class GET_IT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)
            .build();

    @Test
    public void testResponse() {

        tester.e4().insertColumns("ID", "C_VARCHAR", "C_INT").values(1, "xxx", 5).exec();

        tester.target("/e4")
                .get()
                .wasOk()
                .bodyEquals(1,
                        "{\"id\":1,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,"
                                + "\"cInt\":5,\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"xxx\"}");
    }

    @Test
    public void test() {
        tester.e1()
                .insertColumns("AGE", "DESCRIPTION", "NAME")
                .values(32, null, "test 1")
                .values(43, "description", "test 2")
                .exec();

        tester.target("/e1")
                .get()
                .wasOk()
                .totalEquals(2);
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private javax.ws.rs.core.Configuration config;

        @GET
        @Path("e1")
        public DataResponse<E1> getE1(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E1.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4/{id}")
        public DataResponse<E4> getE4_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).get();
        }
    }

}