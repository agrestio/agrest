package io.agrest.cayenne.POST;


import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class POST_Relate_IT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class)
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
        tester.e3().matcher().eq("e2_id", 8).eq("name", "MM").assertOneMatch();
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
    public void toOne_BadFK() {

        tester.target("/e3")
                .post("{\"e2\":15,\"name\":\"MM\"}")
                .wasNotFound()
                .bodyEquals("{\"message\":\"Related object 'E2' with ID '[15]' is not found\"}");

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
    }
}
