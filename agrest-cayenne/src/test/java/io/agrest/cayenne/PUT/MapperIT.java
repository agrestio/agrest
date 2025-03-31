package io.agrest.cayenne.PUT;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E1;
import io.agrest.cayenne.cayenne.main.E23;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MapperIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entitiesAndDependencies(E1.class, E23.class)
            .build();


    @Test
    public void implicitIdMapper() {

        tester.e23().insertColumns("id", "name")
                .values(56, "N1")
                .values(54, "N2").exec();

        tester.target("/id")
                .queryParam("include", "exposedId", "name")
                .put("[  {\"exposedId\":58,\"name\":\"N4\"}, {\"exposedId\":56,\"name\":\"N3\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"exposedId\":58,\"name\":\"N4\"},{\"exposedId\":56,\"name\":\"N3\"}");

        tester.e23().matcher().assertMatches(3);
        tester.e23().matcher().eq("id", 58).andEq("name", "N4").assertOneMatch();
        tester.e23().matcher().eq("id", 56).andEq("name", "N3").assertOneMatch();
    }

    @Test
    public void propertyMapper() {

        tester.e1().insertColumns("id", "name", "description")
                .values(56, "N1", "D1")
                .values(54, "N2", "D2").exec();

        tester.target("/prop")
                .queryParam("include", "name", "description")
                .put("[  {\"name\":\"N4\",\"description\":\"D4\"}, {\"name\":\"N1\",\"description\":\"D3\"} ]")
                .wasOk()
                .bodyEquals(2, "{\"description\":\"D4\",\"name\":\"N4\"},{\"description\":\"D3\",\"name\":\"N1\"}");

        tester.e1().matcher().assertMatches(3);
        tester.e1().matcher().eq("name", "N1").andEq("description", "D3").assertOneMatch();
        tester.e1().matcher().eq("name", "N2").andEq("description", "D2").assertOneMatch();
        tester.e1().matcher().eq("name", "N4").andEq("description", "D4").assertOneMatch();
    }

    @Test
    public void propertiesMapper() {

        tester.e1().insertColumns("id", "name", "age", "description")
                .values(56, "N1", 1, "D1")
                .values(55, "N1", 2, "D2")
                .values(54, "N2", 2, "D3").exec();

        tester.target("/props")
                .queryParam("include", "name", "age", "description")
                .put("[{\"age\":3,\"description\":\"D4\",\"name\":\"N1\"}, {\"age\":2,\"description\":\"D5\",\"name\":\"N1\"}]")
                .wasOk()
                .bodyEquals(2, "{\"age\":3,\"description\":\"D4\",\"name\":\"N1\"},{\"age\":2,\"description\":\"D5\",\"name\":\"N1\"}");

        tester.e1().matcher().assertMatches(4);
        tester.e1().matcher().eq("name", "N1").andEq("age", 1).andEq("description", "D1").assertOneMatch();
        tester.e1().matcher().eq("name", "N1").andEq("age", 2).andEq("description", "D5").assertOneMatch();
        tester.e1().matcher().eq("name", "N2").andEq("age", 2).andEq("description", "D3").assertOneMatch();
        tester.e1().matcher().eq("name", "N1").andEq("age", 3).andEq("description", "D4").assertOneMatch();

    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("id")
        public DataResponse<E23> implicitIdMapper(@QueryParam("include") List<String> includes, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E23.class, config)
                    .request(AgJaxrs.request(config).addIncludes(includes).build())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("prop")
        public DataResponse<E1> propertyMapper(@QueryParam("include") List<String> includes, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E1.class, config)
                    .mapper(E1.NAME.getName())
                    .request(AgJaxrs.request(config).addIncludes(includes).build())
                    .syncAndSelect(entityData);
        }

        @PUT
        @Path("props")
        public DataResponse<E1> propertiesMapper(@QueryParam("include") List<String> includes, String entityData) {
            return AgJaxrs.idempotentCreateOrUpdate(E1.class, config)
                    .mapper(E1.NAME.getName(), E1.AGE.getName())
                    .request(AgJaxrs.request(config).addIncludes(includes).build())
                    .syncAndSelect(entityData);
        }
    }
}
