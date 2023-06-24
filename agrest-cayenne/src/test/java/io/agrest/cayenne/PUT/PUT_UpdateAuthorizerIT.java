package io.agrest.cayenne.PUT;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import java.util.List;

public class PUT_UpdateAuthorizerIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .agCustomizer(ab -> ab
                    .entityOverlay(AgEntity.overlay(E2.class).updateAuthorizer((o, u) -> o.getName().equals(u.getValues().get("name"))))
            ).build();

    @Test
    public void inStack_Allowed() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.target("/e2_stack_authorizer")
                .put("[{\"id\":2,\"name\":\"b\",\"address\":\"Bb\"},{\"id\":1,\"name\":\"a\",\"address\":\"Aa\"}]")
                .wasOk();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "a").eq("address", "Aa").assertOneMatch();
        tester.e2().matcher().eq("name", "b").eq("address", "Bb").assertOneMatch();
    }

    @Test
    public void inStack_Blocked() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.target("/e2_stack_authorizer")
                .put("[{\"id\":2,\"name\":\"b\",\"address\":\"Bb\"},{\"id\":1,\"name\":\"Aa\",\"address\":\"Aa\"}]")
                .wasForbidden();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "a").eq("address", null).assertOneMatch();
        tester.e2().matcher().eq("name", "b").eq("address", null).assertOneMatch();
    }

    @Test
    public void inRequestAndStack_Allowed() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.target("/e2_request_and_stack_authorizer/not_this")
                .put("[{\"id\":2,\"name\":\"b\",\"address\":\"Bb\"},{\"id\":1,\"name\":\"a\",\"address\":\"Aa\"}]")
                .wasOk();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "a").eq("address", "Aa").assertOneMatch();
        tester.e2().matcher().eq("name", "b").eq("address", "Bb").assertOneMatch();
    }

    @Test
    public void inRequestAndStack_Blocked() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.target("/e2_request_and_stack_authorizer/b")
                .put("[{\"id\":2,\"name\":\"b\",\"address\":\"Bb\"},{\"id\":1,\"name\":\"a\",\"address\":\"Aa\"}]")
                .wasForbidden();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "a").eq("address", null).assertOneMatch();
        tester.e2().matcher().eq("name", "b").eq("address", null).assertOneMatch();
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2_stack_authorizer")
        public SimpleResponse putE2StackFilter(List<EntityUpdate<E2>> updates) {
            return AgJaxrs.createOrUpdate(E2.class, config).sync(updates);
        }

        @PUT
        @Path("e2_request_and_stack_authorizer/{name}")
        public SimpleResponse putE2RequestAndStackFilter(
                @PathParam("name") String name,
                List<EntityUpdate<E2>> updates) {

            return AgJaxrs
                    .createOrUpdate(E2.class, config)
                    .updateAuthorizer(E2.class, (o, u) -> !name.equals(u.getValues().get("name")))
                    .sync(updates);
        }
    }
}
