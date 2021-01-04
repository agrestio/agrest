package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.base.protocol.CayenneExp;
import io.agrest.base.protocol.Sort;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_AgRequestIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .build();

    @Test
    public void test_CayenneExp_OverrideByAgRequest() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2_cayenneExp")
                .queryParam("include", "name")
                .queryParam("cayenneExp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                .wasOk()
                // returns 'xxx' instead of 'yyy' due to overriding cayenneExp by AgRequest
                .bodyEquals(1, "{\"name\":\"xxx\"}");
    }

    @Test
    public void testIncludes_OverrideByAgRequest() {

        tester.e3().insertColumns("id_", "name")
                .values(6, "yyy")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        tester.target("/e3_includes")
                .queryParam("include", "id")
                .queryParam("cayenneExp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                // returns names instead of id's due to overriding include by AgRequest
                .wasOk().bodyEquals(2, "{\"name\":\"yyy\"}", "{\"name\":\"yyy\"}");
    }

    @Test
    public void testExcludes_OverrideByAgRequest() {

        tester.e3().insertColumns("id_", "name")
                .values(6, "yyy")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        tester.target("/e3_excludes")
                .queryParam("exclude", "name")
                .queryParam("cayenneExp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                // returns 'name' and other fields except 'id' due to overriding exclude by AgRequest
                .wasOk()
                .bodyEquals(2,
                        "{\"name\":\"yyy\",\"phoneNumber\":null}",
                        "{\"name\":\"yyy\",\"phoneNumber\":null}");
    }

    @Test
    public void test_Sort_OverrideByAgRequest() {

        tester.e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        tester.target("/e4_sort")
                .queryParam("sort", "[{\"property\":\"id\",\"direction\":\"DESC\"}]")
                .queryParam("include", "id")
                .get()
                // returns items in ascending order instead of descending due to overriding sort direction by AgRequest
                .wasOk()
                .bodyEquals(3, "{\"id\":1},{\"id\":2},{\"id\":3}");
    }

    @Test
    public void test_MapBy_OverrideByAgRequest() {

        tester.e4().insertColumns("c_varchar", "c_int")
                .values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2)
                .values("xxx", 3).exec();

        tester.target("/e4_mapBy")
                .queryParam("mapBy", E4.C_INT.getName())
                .queryParam("include", E4.C_VARCHAR.getName())
                .get()
                .wasOk()
                .bodyEqualsMapBy(4,
                        "\"xxx\":[{\"cVarchar\":\"xxx\"},{\"cVarchar\":\"xxx\"}]",
                        "\"yyy\":[{\"cVarchar\":\"yyy\"}]",
                        "\"zzz\":[{\"cVarchar\":\"zzz\"}]");
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2_cayenneExp")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            CayenneExp exp = CayenneExp.simple("name = 'xxx'");
            AgRequest agRequest = Ag.request(config).cayenneExp(exp).build();

            return Ag.service(config).select(E2.class)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e3_includes")
        public DataResponse<E3> getE3_includes(@Context UriInfo uriInfo) {

            AgRequest agRequest = Ag.request(config).addInclude("name").build();

            return Ag.service(config).select(E3.class)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e3_excludes")
        public DataResponse<E3> getE3_excludes(@Context UriInfo uriInfo) {
            AgRequest agRequest = Ag.request(config).addExclude("id").build();

            return Ag.service(config).select(E3.class)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e4_sort")
        public DataResponse<E4> getE4_sort(@Context UriInfo uriInfo) {

            AgRequest agRequest = Ag.request(config).addOrdering(new Sort("id")).build();

            return Ag.service(config).select(E4.class)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e4_mapBy")
        public DataResponse<E4> getE4_mapBy(@Context UriInfo uriInfo) {
            AgRequest agRequest = Ag.request(config).mapBy(E4.C_VARCHAR.getName()).build();

            return Ag.service(config).select(E4.class)
                    .uri(uriInfo)
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }
    }
}
