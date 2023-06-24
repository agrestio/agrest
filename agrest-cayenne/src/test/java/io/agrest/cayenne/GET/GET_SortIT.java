package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_SortIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E4.class)
            .build();


    @Test
    public void testSort_ById() {

        tester.e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/e4")
                .queryParam("sort", "[{\"property\":\"id\",\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get()
                .wasOk()
                .bodyEquals(3, "{\"id\":3}", "{\"id\":2}", "{\"id\":1}");
    }

    @Test
    public void testSort_Invalid() {

        tester.target("/e4")
                .queryParam("sort", "[{\"property\":\"xyz\",\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get()
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Invalid path 'xyz' for 'E4'\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4")
        public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }
    }

}
