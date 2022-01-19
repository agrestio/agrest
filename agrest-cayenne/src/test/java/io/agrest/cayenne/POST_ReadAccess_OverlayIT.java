package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E8;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class POST_ReadAccess_OverlayIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E8.class)
            .build();

    @Test
    public void testWriteConstraints_Id_Allowed() {

        // endpoint constraint allows "name" and "id"

        tester.target("/e8/w/constrainedid/578")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .bodyEquals("{\"success\":true}");

        tester.e8().matcher().assertOneMatch();
        tester.e8().matcher().eq("id", 578).eq("name", "zzz").assertOneMatch();
    }

    @Test
    public void testWriteConstraints_Id_Blocked() {

        // endpoint constraint allows "name", but not "id"

        tester.target("/e8/w/constrainedidblocked/578")
                .post("{\"name\":\"zzz\"}")
                .wasBadRequest()
                .bodyEquals("{\"success\":false,\"message\":\"Setting ID explicitly is not allowed: {id=578}\"}");

        tester.e8().matcher().assertNoMatches();
    }

    @Test
    public void testWriteConstraints1() {

        tester.target("/e3/w/constrained")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");
    }

    @Test
    public void testWriteConstraints2() {

        tester.target("/e3/w/constrained")
                .post("{\"name\":\"zzz\",\"phoneNumber\":\"12345\"}")
                .wasCreated()
                .replaceId("RID")
                // writing phone number is not allowed, so it was ignored
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\",\"phoneNumber\":null}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("phone_number", null).assertOneMatch();
    }

    @Test
    public void testReadConstraints1() {

        tester.target("/e3/constrained")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\"}");
    }

    @Test
    public void testInclude_ReadConstraints() {

        // writing "phoneNumber" is allowed, but reading is not ... must be in DB, but not in response

        tester.target("/e3/constrained")
                .queryParam("include", "name")
                .queryParam("include", "phoneNumber")
                .post("{\"name\":\"zzz\",\"phoneNumber\":\"123456\"}")
                .wasCreated()
                .bodyEquals(1, "{\"name\":\"zzz\"}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("name", "zzz").eq("phone_number", "123456").assertOneMatch();
    }

    @Test
    public void testReadConstraints_DisallowRelated() {

        tester.target("/e3/constrained")
                .queryParam("include", E3.E2.getName())
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3/constrained")
        public DataResponse<E3> insertE3ReadConstrained(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo)
                    .readablePropFilter(E3.class, b -> b.idOnly().property("name", true))
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e3/w/constrained")
        public DataResponse<E3> insertE3WriteConstrained(@Context UriInfo uriInfo, String requestBody) {
            return Ag.create(E3.class, config).uri(uriInfo)
                    .writablePropFilter(E3.class, b -> b.idOnly().property("name", true))
                    .syncAndSelect(requestBody);
        }

        @POST
        @Path("e8/w/constrainedid/{id}")
        public SimpleResponse create_WriteConstrainedId(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {

            return Ag.create(E8.class, config).uri(uriInfo).id(id)
                    .writablePropFilter(E8.class, b -> b.idOnly().property("name", true))
                    .sync(requestBody);
        }

        @POST
        @Path("e8/w/constrainedidblocked/{id}")
        public SimpleResponse create_WriteConstrainedIdBlocked(
                @PathParam("id") int id,
                @Context UriInfo uriInfo,
                String requestBody) {
            return Ag.create(E8.class, config).uri(uriInfo).id(id)
                    .writablePropFilter(E8.class, b -> b.empty().property("name", true))
                    .sync(requestBody);
        }
    }
}
