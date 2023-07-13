package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.SelectBuilder;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class MapBy_MaxPathDepthIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class)
            .build();

    @BeforeEach
    void insertTestData() {
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();
    }

    @Test
    public void depth100_Default() {
        tester.target("/e3")
                .queryParam("include", "id")
                .queryParam("mapBy", "e2.id")
                .get()
                .wasOk()
                .bodyEquals("{\"data\":{\"8\":[{\"id\":3}]},\"total\":1}");
    }

    @Test
    public void depth0() {
        tester.target("/e3")
                .queryParam("depth", 0)
                .queryParam("include", "id")
                .queryParam("mapBy", "e2.id")
                .get()
                .wasBadRequest();
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e3")
        public DataResponse<E3> getE3(
                @Context UriInfo uriInfo,

                // This is for test only. Don't do that at home. Max include depth must not be
                // controlled by the client
                @QueryParam("depth") Integer depth) {

            SelectBuilder<E3> builder = AgJaxrs
                    .select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters());

            if (depth != null) {
                builder.maxPathDepth(depth);
            }

            return builder.get();
        }
    }
}
