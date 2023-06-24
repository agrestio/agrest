package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E7;
import io.agrest.cayenne.cayenne.main.E8;
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

public class PUT_Relate_IT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E7.class)
            .build();

    @Test
    public void testToOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":1}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ArraySyntax() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":[1]}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void testToOne_ToNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":null}")
                .wasOk().bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("id_", 3).eq("e2_id", null).assertOneMatch();
    }

    @Test
    public void testToOne_FromNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", null).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2\":8}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).eq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void testSingle_ResponseToOneRelationshipFilter() {

        tester.e8().insertColumns("id", "name").values(5, "aaa").values(6, "ert").exec();
        tester.e9().insertColumns("e8_id").values(5).values(6).exec();

        tester.target("/e7/6")
                .queryParam("include", "id", E7.E8.dot(E8.E9).getName())
                .queryParam("exclude", E7.NAME.getName())
                .put("[{\"name\":\"yyy\",\"e8\":6}]")
                .wasCreated()
                .bodyEquals(1, "{\"id\":6,\"e8\":{\"e9\":{\"id\":6}}}");
    }

    @Test
    public void testToMany() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .put("{\"e3s\":[3,4,5]}")
                .wasOk().bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":3},{\"id\":4},{\"id\":5}]}");

        tester.e3().matcher().eq("e2_id", 1).assertMatches(3);
    }

    @Test
    public void testToMany_UnrelateAll() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/8")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .put("{\"e3s\":[]}")
                .wasOk().bodyEquals(1, "{\"id\":8,\"e3s\":[]}");

        tester.e3().matcher().eq("e2_id", null).assertMatches(3);
    }

    @Test
    public void testToMany_UnrelateOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(3, "zzz", null)
                .values(4, "aaa", 8)
                .values(5, "bbb", 8).exec();

        tester.target("/e2/1")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.NAME.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .put("{\"e3s\":[4]}")
                .wasOk().bodyEquals(1, "{\"id\":1,\"e3s\":[{\"id\":4}]}");

        tester.e3().matcher().eq("e2_id", 1).eq("id_", 4).assertOneMatch();
        tester.e3().matcher().eq("e2_id", 8).eq("id_", 5).assertOneMatch();
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
        @Path("e3/{id}")
        public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
            return AgJaxrs.update(E3.class, config).byId(id).syncAndSelect(requestBody);
        }

        @PUT
        @Path("e7/{id}")
        public DataResponse<E7> syncOneE7(@PathParam("id") int id, @Context UriInfo uriInfo, String data) {
            return AgJaxrs.idempotentFullSync(E7.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).syncAndSelect(data);
        }
    }
}
