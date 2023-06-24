package io.agrest.cayenne.POST;

import io.agrest.AgException;
import io.agrest.AgResponse;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.apache.cayenne.Cayenne;
import org.junit.jupiter.api.Test;

public class NestedIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class)
            .build();

    @Test
    public void toOne_AsNewObject() {

        tester.target("/e3")
                .queryParam("include", "name", "e2.id")
                .post("{\"e2\":{\"name\":\"new_to_one\"},\"name\":\"MM\"}")
                .wasCreated()
                .bodyEquals(1, "{\"e2\":null,\"name\":\"MM\"}");

        tester.e3().matcher().assertOneMatch();
        tester.e2().matcher().assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, EntityUpdate<E3> update) {

            // While Agrest does not yet support processing full related objects,
            // we should be able to manually save related objects

            DataResponse<E3> e3Response = AgJaxrs.create(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .syncAndSelect(update);
            checkCreated(e3Response, "Can't create e3");

            int e2Id = Cayenne.intPKForObject(e3Response.getData().get(0));

            EntityUpdate<E2> e2Update = update.getRelatedUpdate(E3.E2.getName());
            if (e2Update != null) {
                SimpleResponse e2Response = AgJaxrs
                        .create(E2.class, config)
                        .parent(E3.class, e2Id, E3.E2.getName())
                        .sync(e2Update);
                checkCreated(e2Response, "Can't create e2 related to e3");
            }

            return e3Response;
        }

        void checkCreated(AgResponse response, String message, Object... messageParams) {
            if (response.getStatus() != 201) {
                throw AgException.of(response.getStatus(), message, messageParams);
            }
        }
    }
}
