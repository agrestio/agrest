package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.it.fixture.cayenne.E21;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class DELETE_NaturalIdIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)
            .entities(E20.class, E21.class)
            .build();

    @Test
    public void testSingleId() {

        tester.e20().insertColumns("name_col")
                .values("John")
                .values("Brian").exec();

        tester.target("/single-id/John").delete()
                .wasSuccess()
                .bodyEquals("{\"success\":true}");

        tester.e20().matcher().assertOneMatch();
        tester.e20().matcher().eq("name_col", "Brian").assertOneMatch();
    }

    @Test
    public void testMultiId() {

        tester.e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        tester.target("/multi-id")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .delete()
                .wasSuccess()
                .bodyEquals("{\"success\":true}");

        tester.e21().matcher().assertOneMatch();
        tester.e21().matcher().eq("name", "Brian").eq("age", 27).assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("multi-id")
        public SimpleResponse deleteE21ById(@QueryParam("age") int age, @QueryParam("name") String name) {
            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return Ag.service(config).delete(E21.class).id(id).delete();
        }

        @DELETE
        @Path("single-id/{id}")
        public SimpleResponse deleteE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return Ag.service(config).delete(E20.class, name);
        }
    }
}
