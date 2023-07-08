package io.agrest.cayenne.GET;

import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

public class Exp_PropFilter_Overlay_RuntimeIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class)
            .agCustomizer(c -> c.entityOverlay(AgEntity.overlay(E2.class)
                    .readablePropFilter(b -> b.property("name", false))
                    .writablePropFilter(b -> b.property("name", false))))
            .build();

    // TODO: is this correct - we allow filtering on a non-readable attribute?
    @Test
    public void expOnHiddenProperty() {

        // testing condition described in https://github.com/agrestio/agrest/issues/641

        tester.e2()
                .insertColumns("id_", "name", "address")
                .values(1, "n1", "a1")
                .values(2, "n2", "a2")
                .exec();

        tester.target("")
                .queryParam("exp", "name = 'n1'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":\"a1\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        public DataResponse<E2> all(@QueryParam("exp") String exp) {
            AgRequest request = AgJaxrs.request(config).andExp(exp).build();
            return AgJaxrs.select(E2.class, config).request(request).get();
        }
    }
}
