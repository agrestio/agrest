package io.agrest.cayenne.POST;


import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E29;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E30;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RelateIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E2.class, E3.class, E29.class, E30.class)
            .build();

    @Test
    public void toOne() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        tester.target("/e3")
                .post("{\"e2\":8,\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"MM\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("e2_id", 8).andEq("name", "MM").assertOneMatch();
    }

    @Test
    public void toOne_Null() {

        tester.target("/e3")
                .post("{\"e2\":null,\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"MM\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("e2_id", null).assertOneMatch();
    }

    @Test
    public void toOne_CompoundId() {

        tester.e29().insertColumns("id1", "id2")
                .values(11, 21)
                .values(12, 22).exec();

        tester.target("/e30")
                .queryParam("include", "e29.id")
                .post("{\"e29\":{\"db:id1\":11,\"id2Prop\":21}}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"e29\":{\"id\":{\"db:id1\":11,\"id2Prop\":21}}}");

        tester.e30().matcher().assertOneMatch();
    }

    @Test
    public void toOne_BadFK() {

        tester.target("/e3")
                .post("{\"e2\":15,\"name\":\"MM\"}")
                .wasNotFound()
                .bodyEquals("{\"message\":\"Related object 'E2' with id of '15' is not found\"}");

        tester.e3().matcher().assertNoMatches();
    }

    @Test
    public void toMany() {

        tester.e3().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(8, "yyy").exec();

        Long id = tester.target("/e2")
                .queryParam("include", E2.E3S.getName())
                .queryParam("exclude", E2.ADDRESS.getName(), E2.E3S.dot(E3.NAME).getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .post("{\"e3s\":[1,8],\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"e3s\":[{\"id\":1},{\"id\":8}],\"name\":\"MM\"}")
                .getId();

        assertNotNull(id);

        tester.e3().matcher().eq("e2_id", id).assertMatches(2);
    }

    @Test
    // so while e29 -> e30 is a multi-column join, e30's own ID is single column
    public void toMany_OverMultiKeyRelationship() {

        tester.e30().insertColumns("id")
                .values(100)
                .values(101)
                .values(102).exec();

        tester.target("/e29")
                .queryParam("include", "e30s.id")
                .queryParam("exclude", "id")
                .post("{\"id2Prop\":54,\"e30s\":[100, 102]}")
                .wasCreated()
                .bodyEquals(1, "{\"e30s\":[{\"id\":100},{\"id\":102}],\"id2Prop\":54}");

        tester.e29().matcher().assertOneMatch();
    }

    @Test
    public void toMany_AsNewObjects() {

        tester.target("/e2")
                .queryParam("include", "name", "e3s.name")
                .post("{\"e3s\":[{\"name\":\"new_to_many1\"},{\"name\":\"new_to_many2\"}],\"name\":\"MM\"}")
                .wasCreated()
                .bodyEquals(1, "{\"e3s\":[],\"name\":\"MM\"}")
                .getId();

        tester.e2().matcher().assertOneMatch();
        tester.e3().matcher().assertNoMatches();
    }


    @Test
    public void toOne_AsNewObject() {

        // While Agrest does not yet support processing full related objects, it should not fail either.
        // Testing this condition here.

        tester.target("/e3")
                .queryParam("include", "name", "e2.id")
                .post("{\"e2\":{\"name\":\"new_to_one\"},\"name\":\"MM\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"e2\":null,\"name\":\"MM\"}");

        tester.e3().matcher().assertOneMatch();
        tester.e2().matcher().assertNoMatches();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E2.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(targetData);
        }

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.create(E3.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }

        @POST
        @Path("e29")
        public DataResponse<E29> createE29(String targetData, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E29.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .syncAndSelect(targetData);
        }

        @POST
        @Path("e30")
        public DataResponse<E30> createE30(String targetData, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E30.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .syncAndSelect(targetData);
        }
    }
}
