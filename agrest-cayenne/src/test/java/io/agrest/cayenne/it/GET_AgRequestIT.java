package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.base.protocol.CayenneExp;
import io.agrest.base.protocol.Sort;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_AgRequestIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E4.class};
    }

    @Test
    public void test_CayenneExp_OverrideByAgRequest() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        Response r = target("/e2_cayenneExp")
                .queryParam("include", "name")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}"))
                .request().get();

        // returns 'xxx' instead of 'yyy' due to overriding cayenneExp by AgRequest
        onSuccess(r).bodyEquals(1, "{\"name\":\"xxx\"}");
    }

    @Test
    public void testIncludes_OverrideByAgRequest() {

        e3().insertColumns("id_", "name")
                .values(6, "yyy")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        Response r = target("/e3_includes")
                .queryParam("include", "id")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}"))
                .request().get();

        // returns names instead of id's due to overriding include by AgRequest
        onSuccess(r).bodyEquals(2, "{\"name\":\"yyy\"},{\"name\":\"yyy\"}");
    }

    @Test
    public void testExcludes_OverrideByAgRequest() {

        e3().insertColumns("id_", "name")
                .values(6, "yyy")
                .values(8, "yyy")
                .values(9, "zzz").exec();

        Response r = target("/e3_excludes")
                .queryParam("exclude", "name")
                .queryParam("cayenneExp", urlEnc("{\"exp\":\"name = 'yyy'\"}"))
                .request().get();

        // returns 'name' and other fields except 'id' due to overriding exclude by AgRequest
        onSuccess(r).bodyEquals(2, "{\"name\":\"yyy\",\"phoneNumber\":null},{\"name\":\"yyy\",\"phoneNumber\":null}");
    }

    @Test
    public void test_Sort_OverrideByAgRequest() {

        e4().insertColumns("id")
                .values(2)
                .values(1)
                .values(3).exec();

        Response response = target("/e4_sort")
                .queryParam("sort", urlEnc("[{\"property\":\"id\",\"direction\":\"DESC\"}]"))
                .queryParam("include", "id")
                .request()
                .get();

        // returns items in ascending order instead of descending due to overriding sort direction by AgRequest
        onSuccess(response).bodyEquals(3, "{\"id\":1},{\"id\":2},{\"id\":3}");
    }

    @Test
    public void test_MapBy_OverrideByAgRequest() {

        e4().insertColumns("c_varchar", "c_int")
                .values("xxx", 1)
                .values("yyy", 2)
                .values("zzz", 2)
                .values("xxx", 3).exec();

        Response r = target("/e4_mapBy")
                .queryParam("mapBy", E4.C_INT.getName())
                .queryParam("include", E4.C_VARCHAR.getName())
                .request()
                .get();

        onSuccess(r).bodyEqualsMapBy(4,
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
            CayenneExp cayenneExp = new CayenneExp("name = 'xxx'");
            AgRequest agRequest = Ag.request(config).cayenneExp(cayenneExp).build();

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
