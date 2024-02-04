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

import java.sql.Date;
import java.time.LocalDate;

public class Exp_PropFilter_Overlay_RequestIT extends MainDbTest {

    // testing condition described in https://github.com/agrestio/agrest/issues/641
    // running in "TEST_METHOD" scope to reset the runtime from possible cache poisoning

    @BQTestTool
    final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class)
            .build();

    @Test
    public void noNameThanName() {

        tester.e2()
                .insertColumns("id_", "name", "address")
                .values(1, "n1", "a1")
                .values(2, "n2", "a2")
                .exec();

        tester.target("e2-sans-name")
                // TODO: is this correct - we allow filtering on a non-readable attribute
                .queryParam("exp", "name = 'n1'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":\"a1\"}");

        tester.target("e2-all")
                .queryParam("exp", "name = 'n1'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":\"a1\",\"name\":\"n1\"}");
    }

    @Test
    public void nameAsDateThenAsString() {

        tester.e2()
                .insertColumns("id_", "name", "address")
                .values(1, "2021-01-01", "a1")
                .values(2, "2021-01-02", "a2")
                .exec();

        tester.target("e2-name-as-date")
                // TODO: is this correct - filtering on a redefined attribute?
                .queryParam("exp", "name = '2021-01-01'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":\"a1\",\"name\":\"2021-01-01\"}");

        tester.target("e2-all")
                .queryParam("exp", "name = '2021-01-01'")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"address\":\"a1\",\"name\":\"2021-01-01\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2-all")
        public DataResponse<E2> all(@QueryParam("exp") String exp) {
            AgRequest request = AgJaxrs.request(config).andExp(exp).build();
            return AgJaxrs.select(E2.class, config).request(request).get();
        }

        @GET
        @Path("e2-sans-name")
        public DataResponse<E2> sansName(@QueryParam("exp") String exp) {
            AgRequest request = AgJaxrs.request(config).andExp(exp).build();
            return AgJaxrs.select(E2.class, config)
                    .request(request)
                    .propFilter(E2.class, b -> b.property("name", false))
                    .get();
        }

        @GET
        @Path("e2-name-as-date")
        public DataResponse<E2> nameAsDate(@QueryParam("exp") String exp) {
            AgRequest request = AgJaxrs.request(config)
                    .andExp(exp)
                    .build();

            return AgJaxrs.select(E2.class, config)
                    .request(request)
                    .entityOverlay(AgEntity.overlay(E2.class).attribute(
                            "name",
                            // using SQL date for tests to match an existing converter in CayenneExpPostProcessor
                            Date.class,
                            e2 -> Date.valueOf(LocalDate.parse(e2.getName()))))
                    .get();
        }
    }
}
