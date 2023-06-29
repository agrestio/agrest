package io.agrest.cayenne.POST;


import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E3;
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

public class ReadablePropFilterIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E3.class)
            .build();

    @Test
    public void readConstraints1() {

        tester.target("/e3/constrained")
                .post("{\"name\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"name\":\"zzz\"}");
    }

    @Test
    public void include_ReadConstraints() {

        // writing "phoneNumber" is allowed, but reading is not ... must be in DB, but not in response

        tester.target("/e3/constrained")
                .queryParam("include", "name")
                .queryParam("include", "phoneNumber")
                .post("{\"name\":\"zzz\",\"phoneNumber\":\"123456\"}")
                .wasCreated()
                .bodyEquals(1, "{\"name\":\"zzz\"}");

        tester.e3().matcher().assertOneMatch();
        tester.e3().matcher().eq("name", "zzz").andEq("phone_number", "123456").assertOneMatch();
    }

    @Test
    public void readConstraints_DisallowRelated() {

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
            return AgJaxrs.create(E3.class, config).clientParams(uriInfo.getQueryParameters())
                    .readablePropFilter(E3.class, b -> b.idOnly().property("name", true))
                    .syncAndSelect(requestBody);
        }
    }
}
