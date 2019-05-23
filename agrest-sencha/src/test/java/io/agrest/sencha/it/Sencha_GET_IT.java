package io.agrest.sencha.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.sencha.SenchaOps;
import io.agrest.sencha.it.fixture.SenchaBQJerseyTestOnDerby;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class Sencha_GET_IT extends SenchaBQJerseyTestOnDerby {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    @Test
    public void testIncludeRelationships_ById() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response response1 = target("/e3/8").queryParam("include", "e2.id").request().get();
        onSuccess(response1).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1,\"name\":\"yyy\",\"phoneNumber\":null}");

        Response response2 = target("/e3/8").queryParam("include", "e2.name").request().get();
        onSuccess(response2).bodyEquals(1, "{\"id\":8,\"e2\":{\"name\":\"xxx\"},\"e2_id\":1,\"name\":\"yyy\",\"phoneNumber\":null}");

        Response response3 = target("/e2/1").queryParam("include", "e3s.id").request().get();
        onSuccess(response3).bodyEquals(1, "{\"id\":1,\"address\":null,\"e3s\":[{\"id\":8},{\"id\":9}],\"name\":\"xxx\"}");
    }

    @Test
    public void testIncludeRelationships() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("sort", "id").request().get();

        onSuccess(r).bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1},{\"id\":9,\"e2\":{\"id\":1},\"e2_id\":1}");
    }

    @Test
    public void testIncludeRelationships_StartLimit() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", 1)
                .values(10, "zzz", 1)
                .values(11, "zzz", 1).exec();

        Response r = target("/e3")
                .queryParam("include", "id")
                .queryParam("include", "e2.id")
                .queryParam("sort", "id")
                .queryParam("start", "1")
                .queryParam("limit", "2").request().get();

        onSuccess(r).bodyEquals(4, "{\"id\":9,\"e2\":{\"id\":1},\"e2_id\":1},{\"id\":10,\"e2\":{\"id\":1},\"e2_id\":1}");
    }

    @Test
    public void testToOne_Null() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1)
                .values(9, "zzz", null).exec();

        Response r = target("/e3").queryParam("include", "e2.id").queryParam("include", "id").request().get();
        onSuccess(r).bodyEquals(2, "{\"id\":8,\"e2\":{\"id\":1},\"e2_id\":1},{\"id\":9,\"e2\":null,\"e2_id\":null}");
    }

    @Test
    public void testMapBy_ToOne() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id")
                .values(8, "yyy", 1).exec();

        Response r = target("/e3")
                .queryParam("include", urlEnc("{\"path\":\"e2\",\"mapBy\":\"name\"}"))
                .queryParam("include", "id").request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"e2_id\":1}");
    }

    @Test
    public void testToMany_IncludeRelated() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e5().insertColumns("id", "name").values(345, "B").values(346, "A").exec();
        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(8, "a", 1, 345)
                .values(9, "z", 1, 345)
                .values(7, "m", 1, 346).exec();

        Response r = target("/e2")
                .queryParam("include", urlEnc("{\"path\":\"e3s\"}"))
                .queryParam("include", "id")
                .queryParam("exclude", "e3s.id")
                .queryParam("exclude", "e3s.phoneNumber")
                .queryParam("include", "e3s.e5.name").request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1,\"e3s\":[{\"e5\":{\"name\":\"B\"},\"e5_id\":345,\"name\":\"a\"},"
                + "{\"e5\":{\"name\":\"B\"},\"e5_id\":345,\"name\":\"z\"},"
                + "{\"e5\":{\"name\":\"A\"},\"e5_id\":346,\"name\":\"m\"}]}");
    }

    @Test
    public void testIncludePathRelationship() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();
        e3().insertColumns("id_", "name", "e2_id").values(8, "yyy", 1).exec();

        Response r = target("/e3")
                .queryParam("include", urlEnc("{\"path\":\"e2\"}"))
                .queryParam("include", "id").request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":8,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"e2_id\":1}");
    }

    @Test
    public void testFilter_ById() {

        e2().insertColumns("id_", "name").values(1, "xxx").exec();

        Response r = target("/e2")
                .queryParam("include", "id")
                .queryParam("filter", urlEnc("[{\"exactMatch\":true,\"disabled\":false,\"property\":\"id\",\"operator\":\"=\",\"value\":1}]"))
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"id\":1}");
    }

    @Test
    public void testStartsWith_AfterParseRequest() {

        e2().insertColumns("id_", "name")
                .values(1, "Axx")
                .values(2, "Bxx")
                .values(3, "cxx").exec();

        Response r1 = target("/e2_startwith_pr")
                .queryParam("include", "id")
                .queryParam("query", "a")
                .queryParam("sort", "id").request().get();

        onSuccess(r1).bodyEquals(1, "{\"id\":1}");


        Response r2 = target("/e2_startwith_pr")
                .queryParam("include", "id")
                .queryParam("query", "C")
                .queryParam("sort", "id").request().get();

        onSuccess(r2).bodyEquals(1, "{\"id\":3}");
    }

    @Test
    public void testStartsWith_AfterAssembleQuery() {

        e2().insertColumns("id_", "name")
                .values(1, "Axx")
                .values(2, "Bxx")
                .values(3, "cxx").exec();

        Response r1 = target("/e2_startwith_aq")
                .queryParam("include", "id")
                .queryParam("query", "a")
                .queryParam("sort", "id").request().get();

        onSuccess(r1).bodyEquals(1, "{\"id\":1}");

        Response r2 = target("/e2_startwith_aq")
                .queryParam("include", "id")
                .queryParam("query", "C")
                .queryParam("sort", "id").request().get();

        onSuccess(r2).bodyEquals(1, "{\"id\":3}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e2")
        public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E2.class).uri(uriInfo).get();
        }

        @GET
        @Path("e2/{id}")
        public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.service(config).selectById(E2.class, id, uriInfo);
        }

        @GET
        @Path("e3")
        public DataResponse<E3> get(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E3.class).uri(uriInfo).get();
        }

        @GET
        @Path("e3/{id}")
        public DataResponse<E3> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            return Ag.service(config).selectById(E3.class, id, uriInfo);
        }

        @GET
        @Path("e2_startwith_pr")
        public DataResponse<E2> getE2_StartsWith_ParseRequest(@Context UriInfo uriInfo) {
            return Ag
                    .service(config)
                    .select(E2.class)
                    .stage(SelectStage.CREATE_ENTITY, SenchaOps.startsWithFilter(E2.NAME, uriInfo))
                    .uri(uriInfo).get();
        }

        @GET
        @Path("e2_startwith_aq")
        public DataResponse<E2> getE2_StartsWith_AssembleQuery(@Context UriInfo uriInfo) {
            return Ag
                    .service(config)
                    .select(E2.class)
                    .stage(SelectStage.ASSEMBLE_QUERY, SenchaOps.startsWithFilter(E2.NAME, uriInfo))
                    .uri(uriInfo).get();
        }
    }
}
