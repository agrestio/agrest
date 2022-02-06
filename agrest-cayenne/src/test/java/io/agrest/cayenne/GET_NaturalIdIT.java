package io.agrest.cayenne;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E20;
import io.agrest.cayenne.cayenne.main.E21;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class GET_NaturalIdIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)

            .entities(E20.class, E21.class)
            .build();

    @Test
    public void test_SelectById() {

        tester.e20().insertColumns("name_col").values("John").exec();

        tester.target("/single-id/John").queryParam("exclude", "age", "description").get().wasOk().bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");

        tester.e20().insertColumns("name_col").values("John").exec();

        tester.target("/single-id/John").queryParam("exclude", "age", "description")
                .get()
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}");
    }

    @Test
    public void test_SelectById_MultiId() {

        tester.e21().insertColumns("age", "name").values(18, "John").exec();

        tester.target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description").get().wasOk().bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");


        tester.e21().insertColumns("age", "name").values(18, "John").exec();

        tester.target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .queryParam("exclude", "description")
                .get()
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("single-id/{id}")
        public DataResponse<E20> getE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
            return AgJaxrs.select(E20.class, config).clientParams(uriInfo.getQueryParameters()).byId(name).get();
        }

        @GET
        @Path("multi-id/byid")
        public DataResponse<E21> getE21ById(@QueryParam("age") int age,
                                            @QueryParam("name") String name,
                                            @Context UriInfo uriInfo) {
            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return AgJaxrs.select(E21.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).getOne();
        }
    }
}
