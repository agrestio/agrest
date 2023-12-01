package io.agrest.cayenne.DELETE;

import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E20;
import io.agrest.cayenne.cayenne.main.E21;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class NaturalIdIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E20.class, E21.class)
            .build();

    @Test
    public void singleId() {

        tester.e20().insertColumns("name_col")
                .values("John")
                .values("Brian").exec();

        tester.target("/single-id/John").delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e20().matcher().assertOneMatch();
        tester.e20().matcher().eq("name_col", "Brian").assertOneMatch();
    }

    @Test
    public void multiId() {

        tester.e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        tester.target("/multi-id")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .delete()
                .wasOk()
                .bodyEquals("{}");

        tester.e21().matcher().assertOneMatch();
        tester.e21().matcher().eq("name", "Brian").andEq("age", 27).assertOneMatch();
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
            return AgJaxrs.delete(E21.class, config).byMultiId(id).sync();
        }

        @DELETE
        @Path("single-id/{id}")
        public SimpleResponse deleteE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return AgJaxrs.delete(E20.class, config).byId(name).sync();
        }
    }
}
