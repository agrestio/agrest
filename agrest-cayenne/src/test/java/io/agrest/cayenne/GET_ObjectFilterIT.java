package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.filter.ObjectFilter;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_ObjectFilterIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E4.class)
            .agCustomizer(ab -> ab.entityOverlay(AgEntity.overlay(E4.class).readableObjectFilter(evenFilter())))
            .build();

    static ObjectFilter<E4> evenFilter() {
        return o -> Cayenne.intPKForObject(o) % 2 == 0;
    }

    @Test
    public void testRequestFilter() {

        tester.e4().insertColumns("id", "c_varchar")
                .values(1, "a")
                .values(2, "xyz")
                .values(3, "xyz")
                .values(4, "b")
                .values(5, "c")
                .values(6, "d")
                .exec();

        tester.target("/e4_by_prop/xyz")
                .queryParam("include", "id", "cVarchar")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2,\"cVarchar\":\"xyz\"}");
    }

    @Test
    public void testSharedFilter() {

        tester.e4().insertColumns("id").values(1).values(2).exec();

        tester.target("/e4_stack_filter")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2}");
    }

    @Test
    public void testFilteredPagination1() {

        tester.e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4)
                .values(5)
                .values(6)
                .values(7)
                .values(8)
                .values(9)
                .values(10).exec();

        tester.target("/e4_stack_filter")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "0")
                .queryParam("limit", "2")
                .get()
                .wasOk().bodyEquals(5, "{\"id\":2},{\"id\":4}");
    }

    @Test
    public void testFilteredPagination2() {

        tester.e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4)
                .values(5)
                .values(6)
                .values(7)
                .values(8)
                .values(9)
                .values(10).exec();

        tester.target("/e4_stack_filter")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "3")
                .get()
                .wasOk().bodyEquals(5, "{\"id\":6},{\"id\":8},{\"id\":10}");
    }

    @Test
    public void testFilteredPagination3() {

        tester.e4().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4)
                .values(5)
                .values(6)
                .values(7)
                .values(8)
                .values(9)
                .values(10).exec();

        tester.target("/e4_stack_filter")
                .queryParam("include", "id")
                .queryParam("sort", "id")
                .queryParam("start", "2")
                .queryParam("limit", "10")
                .get()
                .wasOk().bodyEquals(5, "{\"id\":6},{\"id\":8},{\"id\":10}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4_stack_filter")
        public DataResponse<E4> get(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E4.class).uri(uriInfo).get();
        }

        @GET
        @Path("e4_by_prop/{cVarchar}")
        public DataResponse<E4> getWithRequestEncoder(@Context UriInfo uriInfo, @PathParam("cVarchar") String cVarchar) {

            return Ag.service(config)
                    .select(E4.class)
                    .objectFilter(E4.class, e4 -> cVarchar.equals(e4.getCVarchar()))
                    .uri(uriInfo)
                    .get();
        }
    }
}
