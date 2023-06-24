package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E23;
import io.agrest.cayenne.cayenne.main.E26;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_ExposedIdIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E23.class, E26.class)
            .build();

    @Test
    public void byId() {

        tester.e23().insertColumns("id", "name")
                .values(1, "abc")
                .values(2, "xyz").exec();

        tester.target("/e23/1").get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"exposedId\":1,\"name\":\"abc\"}");
    }

    @Test
    public void includeFrom() {

        tester.e23().insertColumns("id", "name").values(1, "abc").exec();
        tester.e26().insertColumns("id", "e23_id").values(41, 1).exec();

        tester.target("/e23")
                .queryParam("include", "id", "e26s.id")
                .get().wasOk().bodyEquals(1, "{\"id\":1,\"e26s\":[{\"id\":41}]}");

        tester.assertQueryCount(2);
    }

    @Test
    public void includeTo() {

        tester.e23().insertColumns("id", "name").values(1, "abc").exec();
        tester.e26().insertColumns("id", "e23_id").values(41, 1).exec();

        tester.target("/e26")
                .queryParam("include", "id", "e23.id")
                .get().wasOk().bodyEquals(1, "{\"id\":41,\"e23\":{\"id\":1}}");

        tester.assertQueryCount(2);
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e26")
        public DataResponse<E26> getAllE26s(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E26.class, config).clientParams(uriInfo.getQueryParameters()).getOne();
        }

        @GET
        @Path("e23")
        public DataResponse<E23> getAllE23s(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E23.class, config).clientParams(uriInfo.getQueryParameters()).getOne();
        }

        @GET
        @Path("e23/{id}")
        public DataResponse<E23> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E23.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).getOne();
        }
    }
}
