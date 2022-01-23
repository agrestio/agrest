package io.agrest.cayenne;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.access.ReadFilter;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class PUT_ReadFilterIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class)
            .agCustomizer(ab -> ab
                    .entityOverlay(AgEntity.overlay(E2.class).readFilter(evenFilter()))
                    .entityOverlay(AgEntity.overlay(E3.class).readFilter(oddFilter()))
                    .entityOverlay(AgEntity.overlay(E4.class).readFilter(evenFilter()))
            )
            .build();

    static <T extends DataObject> ReadFilter<T> evenFilter() {
        return o -> Cayenne.intPKForObject(o) % 2 == 0;
    }

    static <T extends DataObject> ReadFilter<T> oddFilter() {
        return o -> Cayenne.intPKForObject(o) % 2 != 0;
    }

    @Test
    public void testInStack() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .values(4, "c")
                .exec();

        // all 3 updates should have been processed, but only the ones matching the read filter should be returned

        tester.target("/e2_stack_filter")
                .queryParam("include", "id", "name")
                .put("[{\"id\":2,\"name\":\"Bb\"},{\"id\":1,\"name\":\"Aa\"},{\"id\":4,\"name\":\"Cc\"}]")
                .wasOk().bodyEquals(2, "{\"id\":2,\"name\":\"Bb\"}", "{\"id\":4,\"name\":\"Cc\"}");

        tester.e2().matcher().assertMatches(3);
        tester.e2().matcher().eq("id_", 1).eq("name", "Aa").assertOneMatch();
        tester.e2().matcher().eq("id_", 2).eq("name", "Bb").assertOneMatch();
        tester.e2().matcher().eq("id_", 4).eq("name", "Cc").assertOneMatch();
    }

    @Test
    public void testInStack_Nested() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .exec();

        tester.e3().insertColumns("id_", "e2_id")
                .values(11, 1)
                .values(21, 2)
                .values(22, 2)
                .exec();

        // related entity rules must be applied just the same as for the root entity
        tester.target("/e2_stack_filter")
                .queryParam("include", "id", "e3s.id")
                .queryParam("sort", "id")
                .put("[{\"id\":2,\"name\":\"Bb\"},{\"id\":1,\"name\":\"Aa\"}]")
                .wasOk().bodyEquals(1, "{\"id\":2,\"e3s\":[{\"id\":21}]}");
    }

    @Test
    public void testInStackAndRequest() {

        tester.e2().insertColumns("id_", "name")
                .values(1, "a")
                .values(2, "b")
                .values(4, "c")
                .exec();

        // all 3 updates should have been processed, but only the one matching the read filter should be returned

        tester.target("/e2_request_and_stack_filter/Bb")
                .queryParam("include", "id", "name")
                .put("[{\"id\":2,\"name\":\"Bb\"},{\"id\":1,\"name\":\"Aa\"},{\"id\":4,\"name\":\"Cc\"}]")
                .wasOk().bodyEquals(1, "{\"id\":2,\"name\":\"Bb\"}");

        tester.e2().matcher().assertMatches(3);
        tester.e2().matcher().eq("id_", 1).eq("name", "Aa").assertOneMatch();
        tester.e2().matcher().eq("id_", 2).eq("name", "Bb").assertOneMatch();
        tester.e2().matcher().eq("id_", 4).eq("name", "Cc").assertOneMatch();
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("e2_stack_filter")
        public DataResponse<E2> putE2StackFilter(@Context UriInfo uriInfo, List<EntityUpdate<E2>> updates) {
            return AgJaxrs.createOrUpdate(E2.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(updates);
        }

        @PUT
        @Path("e2_request_and_stack_filter/{name}")
        public DataResponse<E2> putE2RequestAndStackFilter(
                @Context UriInfo uriInfo,
                @PathParam("name") String name,
                List<EntityUpdate<E2>> updates) {

            return AgJaxrs.createOrUpdate(E2.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .readableFilter(E2.class, e2 -> name.equals(e2.getName()))
                    .syncAndSelect(updates);
        }
    }
}
