package io.agrest.cayenne.PUT;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class PUT_EntityUpdateVsStringIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E3.class)
            .build();

    @ParameterizedTest
    @ValueSource(strings = {"full_sync_as_string", "full_sync_as_single_update", "full_sync_as_list_update"})
    public void testFullSync_EmptyBodyMustDelete(String path) {

        tester.e3().insertColumns("id_", "name").values(3, "aaa").exec();

        tester.target(path).put("")
                .wasOk()
                .bodyEquals("{}");
        tester.e3().matcher().assertNoMatches();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("full_sync_as_string")
        public SimpleResponse syncAsString(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.idempotentFullSync(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .sync(requestBody);
        }

        @PUT
        @Path("full_sync_as_single_update")
        public SimpleResponse syncAsSingleUpdate(@Context UriInfo uriInfo, EntityUpdate<E3> u) {
            return AgJaxrs.idempotentFullSync(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .sync(u);
        }

        @PUT
        @Path("full_sync_as_list_update")
        public SimpleResponse syncAsListUpdate(@Context UriInfo uriInfo, List<EntityUpdate<E3>> us) {
            return AgJaxrs.idempotentFullSync(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .sync(us);
        }

    }
}
