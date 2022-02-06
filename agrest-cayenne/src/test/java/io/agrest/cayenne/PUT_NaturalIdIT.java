package io.agrest.cayenne;


import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E20;
import io.agrest.cayenne.cayenne.main.E21;
import io.agrest.cayenne.cayenne.main.E23;
import io.agrest.cayenne.cayenne.main.E29;
import io.agrest.cayenne.unit.AgCayenneTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PUT_NaturalIdIT extends DbTest {

    @BQTestTool
    static final AgCayenneTester tester = tester(Resource.class)
            .entitiesAndDependencies(E20.class, E21.class, E23.class, E29.class)
            .build();

    @Test
    public void testSingleId() {

        tester.e20().insertColumns("name_col")
                .values("John")
                .values("Brian").exec();

        tester.target("/single-id/John")

                .put("{\"age\":28,\"description\":\"zzz\"}")
                .wasOk()
                .bodyEquals(1, "{\"id\":\"John\",\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        tester.e20().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testSingle_Id_SeveralExistingObjects() {
        tester.e20().insertColumns("name_col")
                .values("John")
                .values("John").exec();

        tester.target("/single-id/John").put("{\"age\":28,\"description\":\"zzz\"}")
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID 'John' and entity 'E20'\"}");
    }

    @Test
    public void testMultiId() {

        tester.e21().insertColumns("age", "name")
                .values(18, "John")
                .values(27, "Brian").exec();

        tester.target("/multi-id/byid").queryParam("age", 18)
                .queryParam("name", "John")
                .put("{\"age\":28,\"description\":\"zzz\"}")
                .wasOk()
                .bodyEquals(1,
                        "{\"id\":{\"age\":28,\"name\":\"John\"},\"age\":28,\"description\":\"zzz\",\"name\":\"John\"}");

        tester.e21().matcher().eq("age", 28).eq("description", "zzz").assertOneMatch();
    }

    @Test
    public void testSeveralExistingObjects_MultiId() {
        tester.e21().insertColumns("age", "name")
                .values(18, "John")
                .values(18, "John").exec();

        tester.target("/multi-id/byid")
                .queryParam("age", 18)
                .queryParam("name", "John")
                .put("{\"age\":28,\"description\":\"zzz\"}")
                .wasServerError()
                .bodyEquals("{\"success\":false,\"message\":\"Found more than one object for ID '{name:John,age:18}' and entity 'E21'\"}");
    }

    @Test
    public void testNaturalIdInPayload() {

        tester.e23().insertColumns("id", "name").values(12, "John").exec();

        tester.target("/natural-id-in-payload")
                .put("[{\"exposedId\":12,\"name\":\"Joe\"}, {\"exposedId\":10,\"name\":\"Ana\"}]")
                .wasOk().bodyEquals("{\"success\":true}");

        tester.e23().matcher().assertMatches(2);
    }

    @Test
    public void testNaturalIdInPayload_MasqueradingAsId() {

        tester.e23().insertColumns("id", "name").values(12, "John").exec();

        tester.target("/natural-id-in-payload")
                .put("[{\"id\":12,\"name\":\"Joe\"}, {\"id\":10,\"name\":\"Ana\"}]")
                .wasOk().bodyEquals("{\"success\":true}");

        tester.e23().matcher().assertMatches(2);
    }

    @Test
    public void testMultiId_MixedDbObj() {

        tester.target("/mixed-multi-id")
                .put("{\"id\":{\"db:id1\":18,\"id2Prop\":345}}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"db:id1\":18,\"id2Prop\":345},\"id2Prop\":345}");

        tester.e29().matcher().eq("id1", 18).eq("id2", 345).assertOneMatch();

        tester.target("/mixed-multi-id")
                .put("{\"id\":{\"db:id1\":18,\"id2Prop\":345}}")
                .wasOk()
                .bodyEquals(1, "{\"id\":{\"db:id1\":18,\"id2Prop\":345},\"id2Prop\":345}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @PUT
        @Path("natural-id-in-payload")
        public SimpleResponse createOrUpdate_E23(
                Collection<EntityUpdate<E23>> update,
                @Context UriInfo uriInfo) {

            return AgJaxrs.idempotentCreateOrUpdate(E23.class, config).clientParams(uriInfo.getQueryParameters()).sync(update);
        }

        @PUT
        @Path("single-id/{id}")
        public DataResponse<E20> createOrUpdate_E20(
                @PathParam("id") String name,
                EntityUpdate<E20> update,
                @Context UriInfo uriInfo) {

            return AgJaxrs.idempotentCreateOrUpdate(E20.class, config).byId(name).clientParams(uriInfo.getQueryParameters()).syncAndSelect(update);
        }

        @PUT
        @Path("multi-id/byid")
        public DataResponse<E21> createOrUpdate_E21(
                @QueryParam("age") int age,
                @QueryParam("name") String name,
                EntityUpdate<E21> update,
                @Context UriInfo uriInfo) {

            Map<String, Object> id = new HashMap<>(3);
            id.put("age", age);
            id.put("name", name);
            return AgJaxrs.idempotentCreateOrUpdate(E21.class, config).byId(id).clientParams(uriInfo.getQueryParameters()).syncAndSelect(update);
        }

        @PUT
        @Path("mixed-multi-id")
        public DataResponse<E29> createE29(EntityUpdate<E29> update, @Context UriInfo uriInfo) {
            return AgJaxrs.idempotentCreateOrUpdate(E29.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(update);
        }
    }

}
