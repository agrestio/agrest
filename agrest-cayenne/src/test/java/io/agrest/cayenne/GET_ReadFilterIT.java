package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.access.ReadFilter;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_ReadFilterIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .agCustomizer(ab -> ab
                    .entityOverlay(AgEntity.overlay(E2.class).readFilter(evenFilter()))
                    .entityOverlay(AgEntity.overlay(E3.class).readFilter(oddFilter()))
                    .entityOverlay(AgEntity.overlay(E4.class).readFilter(evenFilter()))
            )
            .build();

    static <T extends DataObject> ReadFilter<T> evenFilter() {
        return o -> Cayenne.intPKForObject(o) % 2 == 0;
    }

    static <T extends DataObject> ReadFilter<T> oddFilter() {
        return o -> Cayenne.intPKForObject(o) % 2 != 0;
    }

    @Test
    public void testRootFilter_InRequest() {

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
    public void testNestedFilter_ToOne() {

        tester.e2().insertColumns("id_")
                .values(2)
                .values(3)
                .exec();

        tester.e3().insertColumns("id_", "e2_id")
                .values(1, 2)
                .values(2, 2)
                .values(3, 3)
                .values(4, 3)
                .exec();

        tester.target("/e3_nested_filter")
                .queryParam("include", "id", "e2.id")
                .queryParam("sort", "id")
                .get()
                .wasOk().bodyEquals(2, "{\"id\":1,\"e2\":{\"id\":2}}", "{\"id\":3,\"e2\":null}");
    }

    @Test
    public void testNestedFilter_ToMany() {

        tester.e2().insertColumns("id_")
                .values(2)
                .values(3)
                .exec();

        tester.e3().insertColumns("id_", "e2_id")
                .values(1, 2)
                .values(2, 2)
                .values(3, 3)
                .values(4, 3)
                .exec();

        tester.target("/e2_nested_filter")
                .queryParam("include", "id", "e3s.id")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":2,\"e3s\":[{\"id\":1}]}");
    }

    @Test
    public void testFilter_InStack() {

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
        @Path("e2_nested_filter")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E2.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e3_nested_filter")
        public DataResponse<E3> getE3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E3.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4_stack_filter")
        public DataResponse<E4> get(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E4.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e4_by_prop/{cVarchar}")
        public DataResponse<E4> getWithRequestEncoder(@Context UriInfo uriInfo, @PathParam("cVarchar") String cVarchar) {

            return AgJaxrs
                    .select(E4.class, config)
                    .filter(E4.class, e4 -> cVarchar.equals(e4.getCVarchar()))
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
