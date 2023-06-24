package io.agrest.cayenne.POST;


import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E8;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class WritablePropFilterIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E8.class)
            .build();

    @Test
    public void writeConstraints_Id_Allowed() {

        // endpoint constraint allows "name" and "id"

        tester.target("/e8/constrained-id/578")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .bodyEquals("{}");

        tester.e8().matcher().assertOneMatch();
        tester.e8().matcher().eq("id", 578).eq("name", "zzz").assertOneMatch();
    }

    @Test
    public void writeConstraints_Id_Blocked() {

        // endpoint constraint allows "name", but not "id"

        tester.target("/e8/constrained-id-blocked/578")
                .post("{\"name\":\"zzz\"}")
                .wasBadRequest()
                .bodyEquals("{\"message\":\"Setting ID explicitly is not allowed: {db:id=578}\"}");

        tester.e8().matcher().assertNoMatches();
    }

    @Test
    public void writeConstraints1() {

        tester.target("/e3/constrained")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");
    }

    @Test
    public void writeConstraints2() {

        tester.target("/e3/constrained")
                .post("{\"name\":\"zzz\",\"phoneNumber\":\"12345\"}")
                .wasCreated()
                .replaceId("RID")
                // writing phone number is not allowed, so it was ignored
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("phone_number", null).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3/constrained")
        public DataResponse<E3> constrained(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.create(E3.class, config).clientParams(uriInfo.getQueryParameters())
                    .writablePropFilter(E3.class, b -> b.idOnly().property("name", true))
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e8/constrained-id/{id}")
        public SimpleResponse constrainedId(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {

            return AgJaxrs.create(E8.class, config).clientParams(uriInfo.getQueryParameters()).byId(id)
                    .writablePropFilter(E8.class, b -> b.idOnly().property("name", true))
                    .sync(requestBody);
        }

        @POST
        @Path("e8/constrained-id-blocked/{id}")
        public SimpleResponse constrainedIdBlocked(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {
            return AgJaxrs.create(E8.class, config).clientParams(uriInfo.getQueryParameters()).byId(id)
                    .writablePropFilter(E8.class, b -> b.empty().property("name", true))
                    .sync(requestBody);
        }
    }
}
