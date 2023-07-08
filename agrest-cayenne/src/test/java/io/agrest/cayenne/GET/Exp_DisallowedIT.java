package io.agrest.cayenne.GET;

import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

public class Exp_DisallowedIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class)
            .build();

    @Test
    public void dbExp() {

        tester.e2()
                .insertColumns("id_", "name", "address")
                .values(1, "n1", "a1")
                .values(2, "n2", "a2")
                .exec();

        tester.target("")
                .queryParam("exp", "db:id_ = 1")
                .get()
                .wasBadRequest();
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
