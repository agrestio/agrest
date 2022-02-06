package io.agrest.cayenne;

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

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

public class DELETE_AuthorizerIT extends DbTest {

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

        tester.target("/e2_stack_authorizer/1").delete().wasOk();

        tester.e2().matcher().assertMatches(1);
        tester.e2().matcher().eq("name", "b").assertOneMatch();
    }

    @Test
    public void testInStack_Blocked() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "dont_delete")
                .values(2, "b")
                .exec();

        tester.target("/e2_stack_authorizer/1")
                .delete()
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

        tester.target("/e2_request_and_stack_authorizer/1/can_delete")
                .delete()
                .wasOk();

        tester.e2().matcher().assertMatches(1);
        tester.e2().matcher().eq("name", "b").assertOneMatch();
    }

    @Test
    public void testInRequestAndStack_Blocked() {
        tester.e2().insertColumns("id_", "name")
                .values(1, "dont_delete_this_either")
                .values(2, "b")
                .exec();

        tester.target("/e2_request_and_stack_authorizer/1/dont_delete_this_either")
                .delete()
                .wasForbidden();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "dont_delete_this_either").assertOneMatch();
        tester.e2().matcher().eq("name", "b").assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @DELETE
        @Path("e2_stack_authorizer/{id}")
        public SimpleResponse putE2StackFilter(@PathParam("id") int id) {
            return AgJaxrs.delete(E2.class, config)
                    .byId(id)
                    .sync();
        }

        @DELETE
        @Path("e2_request_and_stack_authorizer/{id}/{name}")
        public SimpleResponse putE2RequestAndStackFilter(
                @PathParam("name") String name,
                @PathParam("id") int id) {

            return AgJaxrs.delete(E2.class, config)
                    .byId(id)
                    .authorizer(o -> !name.equals(o.getName()))
                    .sync();
        }
    }
}
