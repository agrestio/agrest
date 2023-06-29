package io.agrest.cayenne.PUT;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E29;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E30;
import io.agrest.cayenne.cayenne.main.E7;
import io.agrest.cayenne.cayenne.main.E8;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

public class RelateIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E7.class, E29.class, E30.class)
            .build();

    @Test
    public void toOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":1}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).andEq("e2_id", 1).assertOneMatch();
    }

    @Test
    public void toOne_CompoundId() {

        tester.e29().insertColumns("id1", "id2")
                .values(11, 21)
                .values(12, 22).exec();

        tester.e30().insertColumns("id", "e29_id1", "e29_id2")
                .values(3, 11, 21).exec();

        tester.target("/e30/3")
                .queryParam("include", "e29.id")
                .put("{\"id\":3,\"e29\":{\"db:id1\":12,\"id2Prop\":22}}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"e29\":{\"id\":{\"db:id1\":12,\"id2Prop\":22}}}");

        tester.e30().matcher().eq("id", 3).andEq("e29_id1", 12).andEq("e29_id2", 22).assertOneMatch();
    }

    @Test
    public void toOne_ToNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", 8).exec();

        tester.target("/e3/3")
                .put("{\"id\":3,\"e2\":null}")
                .wasOk().bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("id_", 3).andEq("e2_id", null).assertOneMatch();
    }

    @Test
    public void toOne_FromNull() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();
        tester.e3().insertColumns("id_", "name", "e2_id").values(3, "zzz", null).exec();

        tester.target("/e3/3").put("{\"id\":3,\"e2\":8}")
                .wasOk()
                .bodyEquals(1, "{\"id\":3,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().eq("id_", 3).andEq("e2_id", 8).assertOneMatch();
    }

    @Test
    public void single_ResponseToOneRelationshipFilter() {

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
    public void toMany() {

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
    public void toMany_UnrelateAll() {

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
                .wasOk()
                .bodyEquals(1, "{\"id\":8,\"e3s\":[]}");

        tester.e3().matcher().eq("e2_id", null).assertMatches(3);
    }

    @Test
    public void toMany_UnrelateOne() {

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

        tester.e3().matcher().eq("e2_id", 1).andEq("id_", 4).assertOneMatch();
        tester.e3().matcher().eq("e2_id", 8).andEq("id_", 5).assertOneMatch();
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

        @PUT
        @Path("e30/{id}")
        public DataResponse<E30> updateE30(@PathParam("id") int id, @Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.update(E30.class, config)
                    .byId(id)
                    .clientParams(uriInfo.getQueryParameters())
                    .syncAndSelect(requestBody);
        }
    }
}
