package io.agrest.jpa;


import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E20;
import io.agrest.jpa.model.E21;
import io.agrest.jpa.model.E29;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class POST_NaturalIdIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(POST_NaturalIdIT.class)
            .entities(E20.class, E21.class, E29.class)
            .build();

    @Test
    public void testSingleId() {

        tester.target("/single-id")
                .queryParam("exclude", "age", "description")
                .post("{\"id\":\"John\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":\"John\",\"name\":\"John\"}");

        tester.e20().matcher().eq("name_col", "John").assertOneMatch();

        tester.target("/single-id")
                .queryParam("exclude", "age", "description")
                .post("{\"id\":\"John\"}")
                .wasBadRequest()
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E20' with id {name:John} - already exists\"}");
    }

    @Test
    public void testMultiId() {

        tester.target("/multi-id")
                .queryParam("exclude", "description")
                .post("{\"id\":{\"age\":18,\"name\":\"John\"}}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"age\":18,\"name\":\"John\"},\"age\":18,\"name\":\"John\"}");

        tester.e21().matcher().eq("name", "John").eq("age", 18).assertOneMatch();

        tester.target("/multi-id").queryParam("exclude", "description")
                .post("{\"id\":{\"age\":18,\"name\":\"John\"}}")
                .wasBadRequest()
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E21' with id {name:John,age:18} - already exists\"}");
    }

    @Test
    public void testMultiId_MixedDbObj() {

        tester.target("/mixed-multi-id")
                .post("{\"id\":{\"db:id1\":18,\"id2Prop\":345}}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"db:id1\":18,\"id2Prop\":345},\"id2Prop\":345}");

        tester.e29().matcher().eq("id1", 18).eq("id2", 345).assertOneMatch();

        tester.target("/mixed-multi-id")
                .post("{\"id\":{\"db:id1\":18,\"id2Prop\":345}}")
                .wasBadRequest()
                .bodyEquals("{\"success\":false,\"message\":\"Can't create 'E29' with id {id2Prop:345,db:id1:18} - already exists\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;


        @POST
        @Path("single-id")
        public DataResponse<E20> createE20(EntityUpdate<E20> update, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E20.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(update);
        }

        @POST
        @Path("multi-id")
        public DataResponse<E21> createE21(EntityUpdate<E21> update, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E21.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(update);
        }

        @POST
        @Path("mixed-multi-id")
        public DataResponse<E29> createE29(EntityUpdate<E29> update, @Context UriInfo uriInfo) {
            return AgJaxrs.create(E29.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(update);
        }
    }

}
