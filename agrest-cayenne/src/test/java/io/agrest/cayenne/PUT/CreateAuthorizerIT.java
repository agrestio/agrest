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
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class CreateAuthorizerIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .agCustomizer(ab -> ab
                    .entityOverlay(AgEntity.overlay(E2.class).createAuthorizer(u -> !"blocked".equals(u.getValues().get("name"))))
            ).build();

    @Test
    public void inStack_Allowed() {

        tester.target("/e2_stack_authorizer")
                .put("[{\"name\":\"Bb\"},{\"name\":\"Aa\"}]")
                .wasCreated();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "Aa").assertOneMatch();
        tester.e2().matcher().eq("name", "Bb").assertOneMatch();
    }

    @Test
    public void inStack_Blocked() {

        tester.target("/e2_stack_authorizer")
                .put("[{\"name\":\"Bb\"},{\"name\":\"blocked\"}]")
                .wasForbidden();

        tester.e2().matcher().assertNoMatches();
    }

    @Test
    public void inRequestAndStack_Allowed() {

        tester.target("/e2_request_and_stack_authorizer/not_this")
                .put("[{\"name\":\"Bb\"},{\"name\":\"Aa\"}]")
                .wasCreated();

        tester.e2().matcher().assertMatches(2);
        tester.e2().matcher().eq("name", "Aa").assertOneMatch();
        tester.e2().matcher().eq("name", "Bb").assertOneMatch();
    }

    @Test
    public void inRequestAndStack_Blocked() {

        tester.target("/e2_request_and_stack_authorizer/not_this")
                .put("[{\"name\":\"Bb\"},{\"name\":\"blocked\"}]")
                .wasForbidden();

        tester.e2().matcher().assertNoMatches();

        tester.target("/e2_request_and_stack_authorizer/not_this")
                .put("[{\"name\":\"not_this\"},{\"name\":\"Aa\"}]")
                .wasForbidden();

        tester.e2().matcher().assertNoMatches();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2_stack_authorizer")
        public SimpleResponse putE2StackFilter(@Context UriInfo uriInfo, List<EntityUpdate<E2>> updates) {
            return AgJaxrs.createOrUpdate(E2.class, config).clientParams(uriInfo.getQueryParameters()).sync(updates);
        }

        @PUT
        @Path("e2_request_and_stack_authorizer/{name}")
        public SimpleResponse putE2RequestAndStackFilter(
                @Context UriInfo uriInfo,
                @PathParam("name") String name,
                List<EntityUpdate<E2>> updates) {

            return AgJaxrs.createOrUpdate(E2.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .createAuthorizer(E2.class, u -> !name.equals(u.getValues().get("name")))
                    .sync(updates);
        }
    }
}
