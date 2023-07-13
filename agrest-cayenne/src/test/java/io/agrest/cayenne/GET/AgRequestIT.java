package io.agrest.cayenne.GET;

import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class AgRequestIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .build();

    @Test
    public void exp_OverrideByAgRequest() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        tester.target("/e2_exp")
                .queryParam("include", "name")
                .queryParam("exp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                .wasOk()
                // returns 'xxx' instead of 'yyy' due to overriding exp by AgRequest
                .bodyEquals(1, "{\"name\":\"xxx\"}");
    }

    @Test
    public void includes_OverrideByAgRequest() {

        tester.e3().insertColumns("id_", "name")
                .values(6, "yyy")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        tester.target("/e3_includes")
                .queryParam("include", "id")
                .queryParam("exp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                // returns names instead of id's due to overriding include by AgRequest
                .wasOk().bodyEquals(2, "{\"name\":\"yyy\"}", "{\"name\":\"yyy\"}");
    }

    @Test
    public void excludes_OverrideByAgRequest() {

        tester.e3().insertColumns("id_", "name")
                .values(6, "yyy")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        tester.target("/e3_excludes")
                .queryParam("exclude", "name")
                .queryParam("exp", "{\"exp\":\"name = 'yyy'\"}")
                .get()
                // returns 'name' and other fields except 'id' due to overriding exclude by AgRequest
                .wasOk()
                .bodyEquals(2,
                        "{\"name\":\"yyy\",\"phoneNumber\":null}",
                        "{\"name\":\"yyy\",\"phoneNumber\":null}");
    }

    @Test
    public void sort_OverrideByAgRequest() {

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
    public void mapBy_OverrideByAgRequest() {

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
        @Path("e2_exp")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            Exp exp = Exp.from("name = 'xxx'");
            AgRequest agRequest = AgJaxrs.request(config).andExp(exp).build();

            return AgJaxrs.select(E2.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e3_includes")
        public DataResponse<E3> getE3_includes(@Context UriInfo uriInfo) {

            AgRequest agRequest = AgJaxrs.request(config).addInclude("name").build();

            return AgJaxrs.select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e3_excludes")
        public DataResponse<E3> getE3_excludes(@Context UriInfo uriInfo) {
            AgRequest agRequest = AgJaxrs.request(config).addExclude("id").build();

            return AgJaxrs.select(E3.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e4_sort")
        public DataResponse<E4> getE4_sort(@Context UriInfo uriInfo) {

            AgRequest agRequest = AgJaxrs.request(config).addSort(new Sort("id")).build();

            return AgJaxrs.select(E4.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }

        @GET
        @Path("e4_mapBy")
        public DataResponse<E4> getE4_mapBy(@Context UriInfo uriInfo) {
            AgRequest agRequest = AgJaxrs.request(config).mapBy(E4.C_VARCHAR.getName()).build();

            return AgJaxrs.select(E4.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .request(agRequest) // overrides parameters from uriInfo
                    .get();
        }
    }
}
