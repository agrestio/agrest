package io.agrest.cayenne.PUT;


import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E5;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class ObjectIncludeIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E5.class)
            .build();

    @Test
    public void overlap() {
        tester.e5().insertColumns("id", "name", "date").values(45, "T", "2013-01-03").exec();
        tester.e2().insertColumns("id_", "name").values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id", "e5_id").values(3, "zzz", 8, 45).exec();

        tester.target("/e3/3")
                .queryParam("include", "e2", "e2.id", "e5.id", "e5")
                .put("{\"id\":3}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":8},\"e5\":{\"id\":45},\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void toOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .queryParam("include", "e2")
                .put("{\"id\":3,\"e2\":1}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"e2\":{\"id\":1,\"address\":null,\"name\":\"xxx\"},\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void toMany() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/1")
                .queryParam("include", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.getName())
                .put("{\"e3s\":[3,4,5]}")
                .wasOk()
                .bodyEquals(1, "{\"address\":null,\"e3s\":[{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null},{\"id\":4,\"name\":\"aaa\",\"phoneNumber\":null},{\"id\":5,\"name\":\"bbb\",\"phoneNumber\":null}],\"name\":\"xxx\"}");

        tester.e3().matcher().eq("e2_id", 1).assertMatches(3);
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2/{id}")
        public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id, String entityData, @Context UriInfo uriInfo) {
            return AgJaxrs.idempotentCreateOrUpdate(E2.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).syncAndSelect(entityData);
        }

        @PUT
        @Path("e3")
        public DataResponse<E3> syncE3(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.idempotentFullSync(E3.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String data, @Context UriInfo uriInfo) {
            return AgJaxrs.update(E3.class, config).clientParams(uriInfo.getQueryParameters()).byId(id).syncAndSelect(data);
        }
    }
}
