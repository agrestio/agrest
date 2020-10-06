package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class POST_NaturalIdIT extends JerseyAndDerbyCase {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)

            .entities(E20.class, E21.class)
            .build();

    @Test
    public void testSingleId() {

        tester.target("/single-id")
                .queryParam("exclude", "age", "description")
                .post("{\"id\":\"John\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");

        tester.e20().matcher().eq("name_col", "John").assertOneMatch();

        tester.target("/single-id")
                .queryParam("exclude", "age", "description")
                .post("{\"id\":\"John\"}")
                .wasBadRequest()
                // TODO: DB columns exposed in the message
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E20' with id {name:John} - already exists\"}");
    }

    @Test
    public void testMultiId() {

        tester.target("/multi-id")
                .queryParam("exclude", "description")
                .post("{\"id\":{\"age\":18,\"name\":\"John\"}}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");

        tester.e21().matcher().eq("name", "John").eq("age", 18).assertOneMatch();

        tester.target("/multi-id").queryParam("exclude", "description")
                .post("{\"id\":{\"age\":18,\"name\":\"John\"}}")
                .statusEquals(Response.Status.BAD_REQUEST)
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E21' with id {name:John,age:18} - already exists\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("single-id")
        public DataResponse<E20> createE20(EntityUpdate<E20> update, @Context UriInfo uriInfo) {
            return Ag.create(E20.class, config).uri(uriInfo).syncAndSelect(update);
        }

        @POST
        @Path("multi-id")
        public DataResponse<E21> createE21(EntityUpdate<E21> update, @Context UriInfo uriInfo) {
            return Ag.create(E21.class, config).uri(uriInfo).syncAndSelect(update);
        }
    }

}
