package io.agrest.cayenne;


import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.cayenne.cayenne.main.E17;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E31;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class POST_IT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E3.class, E4.class, E17.class, E31.class)
            .build();

    @Test
    public void test() {

        tester.target("/e4").post("{\"cVarchar\":\"zzz\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"zzz\"}");

        tester.e4().matcher().assertOneMatch();
        tester.e4().matcher().eq("c_varchar", "zzz").assertOneMatch();

        tester.target("/e4").post("{\"cVarchar\":\"TTTT\"}")
                .wasCreated()
                .replaceId("RID")
                .bodyEquals(1, "{\"id\":RID,\"cBoolean\":null,\"cDate\":null,\"cDecimal\":null,\"cInt\":null,"
                        + "\"cTime\":null,\"cTimestamp\":null,\"cVarchar\":\"TTTT\"}");

        tester.e4().matcher().assertMatches(2);
        tester.e4().matcher().eq("c_varchar", "TTTT").assertOneMatch();
    }

    @Test
    public void testIdCalledId() {

        tester.target("/e31").post("{\"id\":5,\"name\":\"31\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":5,\"name\":\"31\"}");

        tester.e31().matcher().assertOneMatch();
        tester.e31().matcher().eq("id", 5L).assertOneMatch();
    }

    @Test
    public void testCompoundId() {

        tester.target("/e17")
                .queryParam("id1", 1)
                .queryParam("id2", 1)
                .post("{\"name\":\"xxx\"}")
                .wasCreated()
                .bodyEquals(1, "{\"id\":{\"id1\":1,\"id2\":1},\"id1\":1,\"id2\":1,\"name\":\"xxx\"}");
    }

    @Test
    public void testSync_NoData() {

        tester.target("/e4/sync")
                .post("{\"cVarchar\":\"zzz\"}")
                .wasCreated()
                .bodyEquals("{}");

        tester.e4().matcher().assertOneMatch();
        tester.e4().matcher().eq("c_varchar", "zzz").assertOneMatch();
    }

    @Test
    public void testBulk() {

        tester.target("/e3/")
                .queryParam("exclude", "id")
                .queryParam("include", E3.NAME.getName())
                .post("[{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}]")
                .wasCreated()
                // ordering from request must be preserved...
                .bodyEquals(4, "{\"name\":\"aaa\"},{\"name\":\"zzz\"},{\"name\":\"bbb\"},{\"name\":\"yyy\"}");
    }


    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.create(E3.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4")
        public DataResponse<E4> createE4(String requestBody) {
            return AgJaxrs.create(E4.class, config).syncAndSelect(requestBody);
        }

        @POST
        @Path("e4/sync")
        public SimpleResponse createE4_DefaultData(String requestBody) {
            return AgJaxrs.create(E4.class, config).sync(requestBody);
        }

        @POST
        @Path("e17")
        public DataResponse<E17> createE17(
                @Context UriInfo uriInfo,
                @QueryParam("id1") Integer id1,
                @QueryParam("id2") Integer id2,
                String requestBody) {

            Map<String, Object> ids = new HashMap<>();
            ids.put(E17.ID1.getName(), id1);
            ids.put(E17.ID2.getName(), id2);

            return AgJaxrs.create(E17.class, config).byId(ids).syncAndSelect(requestBody);
        }

        @POST
        @Path("e31")
        public DataResponse<E31> createE31(@Context UriInfo uriInfo, String requestBody) {
            return AgJaxrs.create(E31.class, config).clientParams(uriInfo.getQueryParameters()).syncAndSelect(requestBody);
        }
    }
}
