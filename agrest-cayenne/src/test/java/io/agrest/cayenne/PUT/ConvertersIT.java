package io.agrest.cayenne.PUT;

import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E28;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

public class ConvertersIT extends MainDbTest {
    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E28.class)
            .build();

    @Test
    public void json() {

        String e1 = "[{\"id\":5,\"json\":[1,2]},{\"id\":6,\"json\":{\"a\":1}},{\"id\":7,\"json\":5}]";
        tester.target("/e28/").put(e1).wasCreated();
        tester.e28().matcher().assertMatches(3);
        tester.e28().matcher().eq("id", 5).eq("json", "[1,2]").assertOneMatch();
        tester.e28().matcher().eq("id", 6).eq("json", "{\"a\":1}").assertOneMatch();
        tester.e28().matcher().eq("id", 7).eq("json", "5").assertOneMatch();

        // try updating
        String e2 = "[{\"id\":5,\"json\":[1,3]}]";
        tester.target("/e28/").put(e2).wasOk();
        tester.e28().matcher().assertMatches(3);
        tester.e28().matcher().eq("id", 5).eq("json", "[1,3]").assertOneMatch();
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e28")
        public SimpleResponse syncE28(String data) {
            return AgJaxrs.createOrUpdate(E28.class, config).sync(data);
        }
    }
}
