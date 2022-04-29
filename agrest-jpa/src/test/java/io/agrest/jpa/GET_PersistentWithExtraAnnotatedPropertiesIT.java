package io.agrest.jpa;

import io.agrest.DataResponse;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.jpa.model.E14;
import io.agrest.jpa.model.E15;
import io.agrest.jpa.unit.AgJpaTester;
import io.agrest.jpa.unit.DbTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_PersistentWithExtraAnnotatedPropertiesIT extends DbTest {

    @BQTestTool
    static final AgJpaTester tester = tester(Resource.class)

            .entities(E14.class, E15.class)
            .build();

    // TODO: each test is using the same dataset... if we could only do data cleanup once per class, then we can load
    //  the test data in constructor

    @Test
    public void testGET_Root() {

        tester.e15().insertColumns("LONG_ID", "NAME").values(1L, "xxx").exec();
        tester.e14().insertColumns("E15_ID", "LONG_ID", "NAME").values(1L, 8L, "yyy").exec();

        tester.target("/e14")
                .queryParam("include", "name", "prettyName")
                .get().wasOk().bodyEquals(1, "{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}");
    }

    @Test
    public void testIncludeRelationship() {

        tester.e15().insertColumns("LONG_ID", "NAME").values(1L, "xxx").exec();
        tester.e14().insertColumns("E15_ID", "LONG_ID", "NAME").values(1L, 8L, "yyy").exec();

        tester.target("/e14")
                .queryParam("include", "name", "p7")

                .get().wasOk().bodyEquals(1, "{\"name\":\"yyy\",\"p7\":{\"id\":800,\"string\":\"p7_yyy\"}}");
    }

    @Test
    public void testGET_Related() {

        tester.e15().insertColumns("LONG_ID", "NAME").values(1L, "xxx").exec();
        tester.e14().insertColumns("E15_ID", "LONG_ID", "NAME").values(1L, 8L, "yyy").exec();

        tester.target("/e15")
                .queryParam("include", "e14s.name", "e14s.prettyName")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":1,\"e14s\":[{\"name\":\"yyy\",\"prettyName\":\"yyy_pretty\"}],\"name\":\"xxx\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e15")
        public DataResponse<E15> getE15(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E15.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        //TODO @Path("e14")
//        @GET
//        @Path("e14")
//        public DataResponse<E14> getE14(@Context UriInfo uriInfo) {
//            return AgJaxrs.select(E14.class, config)
//                    .stage(SelectStage.FETCH_DATA, (SelectContext<E14> c) -> afterE14Fetched(c))
//                    .clientParams(uriInfo.getQueryParameters()).get();
//        }
//
//        void afterE14Fetched(SelectContext<E14> context) {
//            for (E14 e14 : context.getEntity().getData()) {
//                P7 p7 = new P7();
//                p7.setId(Cayenne.intPKForObject(e14) * 100);
//                p7.setString("p7_" + e14.getName());
//                e14.setP7(p7);
//            }
//        }
    }
}
