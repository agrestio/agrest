package io.agrest.cayenne;

import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
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

public class PUT_DeleteAuthorizerIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .agCustomizer(ab -> ab
                    .entityOverlay(AgEntity.overlay(E2.class).deleteAuthorizer(o -> !"dont_delete".equals(o.getName())))
            ).build();

    @Test
    public void testInStack_Allowed() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.target("/e2_stack_authorizer")
                .put("[{\"id\":2,\"name\":\"Bb\"}]")
                .wasOk();

        tester.e2().matcher().assertMatches(1);
        tester.e2().matcher().eq("name", "Bb").assertOneMatch();
    }

    @Test
    public void testInStack_Blocked() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "dont_delete")
                .values(2, "b")
                .exec();

        tester.target("/e2_stack_authorizer")
                .put("[{\"id\":2,\"name\":\"Bb\"}]")
                .wasForbidden();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "dont_delete").assertOneMatch();
        tester.e2().matcher().eq("name", "b").assertOneMatch();
    }

    @Test
    public void testInRequestAndStack_Allowed() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.target("/e2_request_and_stack_authorizer/not_this")
                .put("[{\"id\":2,\"name\":\"Bb\"}]")
                .wasOk();

        tester.e2().matcher().assertMatches(1);
        tester.e2().matcher().eq("name", "Bb").assertOneMatch();
    }

    @Test
    public void testInRequestAndStack_Blocked() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "dont_delete_this_either")
                .values(2, "b")
                .exec();

        tester.target("/e2_request_and_stack_authorizer/dont_delete_this_either")
                .put("[{\"id\":2,\"name\":\"Bb\"}]")
                .wasForbidden();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "dont_delete_this_either").assertOneMatch();
        tester.e2().matcher().eq("name", "b").assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2_stack_authorizer")
        public SimpleResponse putE2StackFilter(List<EntityUpdate<E2>> updates) {
            return AgJaxrs.idempotentFullSync(E2.class, config).sync(updates);
        }

        @PUT
        @Path("e2_request_and_stack_authorizer/{name}")
        public SimpleResponse putE2RequestAndStackFilter(
                @PathParam("name") String name,
                List<EntityUpdate<E2>> updates) {

            return AgJaxrs.idempotentFullSync(E2.class, config)
                    .deleteAuthorizer(E2.class, o -> !name.equals(o.getName()))
                    .sync(updates);
        }
    }
}
