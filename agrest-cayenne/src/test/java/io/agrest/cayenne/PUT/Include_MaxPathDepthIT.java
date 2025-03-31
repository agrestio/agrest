package io.agrest.cayenne.PUT;

import io.agrest.DataResponse;
import io.agrest.UpdateBuilder;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class Include_MaxPathDepthIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E5.class)
            .build();

    @BeforeEach
    void insertTestData() {
        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "A").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();
    }

    @Test
    public void depth100_Default() {

        tester.target("/e2/8")
                .queryParam("include", "name", "e3s.id", "e3s.e5.id")
                .put("{\"name\":\"B\"}")
                .wasOk()
                .bodyEquals(1, "{\"e3s\":[{\"id\":3,\"e5\":{\"id\":45}}],\"name\":\"B\"}");
    }

    @Test
    public void depth0() {

        tester.target("/e2/8")
                .queryParam("depth", 0)
                .queryParam("include", "name", "e3s.id", "e3s.e5.id")
                .put("{\"name\":\"B\"}")
                .wasOk()
                .bodyEquals(1, "{\"name\":\"B\"}");
    }

    @Test
    public void depth1() {

        tester.target("/e2/8")
                .queryParam("depth", 1)
                .queryParam("include", "name", "e3s.id", "e3s.e5.id")
                .put("{\"name\":\"B\"}")
                .wasOk()
                .bodyEquals(1, "{\"e3s\":[{\"id\":3}],\"name\":\"B\"}");
    }

    @Test
    public void depth2() {

        tester.target("/e2/8")
                .queryParam("depth", 2)
                .queryParam("include", "name", "e3s.id", "e3s.e5.id")
                .put("{\"name\":\"B\"}")
                .wasOk()
                .bodyEquals(1, "{\"e3s\":[{\"id\":3,\"e5\":{\"id\":45}}],\"name\":\"B\"}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(
                @PathParam("id") int id, String entityData,
                @Context UriInfo uriInfo,

                // This is for test only. Don't do that at home. Max include depth must not be
                // controlled by the client
                @QueryParam("depth") Integer depth) {
            UpdateBuilder<E2> builder = AgJaxrs.idempotentCreateOrUpdate(E2.class, config).byId(id)
                    .clientParams(uriInfo.getQueryParameters());

            if (depth != null) {
                builder.maxPathDepth(depth);
            }

            return builder.syncAndSelect(entityData);
        }
    }
}
