package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.access.PropertyFilteringRulesBuilder;
import io.agrest.cayenne.cayenne.main.E14;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.cayenne.main.E4;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class PropFilter_OverlayIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E2.class, E3.class, E4.class, E14.class)
            .build();

    @Test
    public void implicit() {

        tester.e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        tester.target("/e4/limit_attributes")
                .get()
                .wasOk().bodyEquals(1, "{\"id\":1,\"cInt\":5}");
    }

    @Test
    public void explicit() {

        tester.e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        tester.target("/e4/limit_attributes")
                .queryParam("include", "cBoolean", "cInt")
                .get()
                .wasOk().bodyEquals(1, "{\"cInt\":5}");
    }

    @Test
    public void explicit_ToMany() {

        // make sure we have e3s for more than one e2 - this will help us
        // confirm that relationship queries are properly filtered.

        tester.e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        tester.e3().insertColumns("id_", "name", "e2_id")
                .values(7, "zzz", 2)
                .values(8, "yyy", 1)
                .values(9, "zzz", 1).exec();

        tester.target("/e2/constraints/1/e3s").get().wasOk().bodyEquals(2, "{\"id\":8},{\"id\":9}");
    }

    @Test
    public void id() {

        tester.e14().insertColumns("long_id", "name")
                .values(5L, "aaa")
                .values(4L, "zzz").exec();

        tester.target("/e14/overlay_id")
                .queryParam("sort", "name")
                .get()
                .wasOk().bodyEquals(2,
                        "{\"name\":\"aaa\",\"prettyName\":\"aaa_pretty\"}",
                        "{\"name\":\"zzz\",\"prettyName\":\"zzz_pretty\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e14/overlay_id")
        public DataResponse<E14> getObjects_OverlayId(@Context UriInfo uriInfo) {

            return AgJaxrs.select(E14.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(E14.class, r -> r.id(false))
                    .get();
        }

        @GET
        @Path("e4/limit_attributes")
        public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {

            return AgJaxrs.select(E4.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(E4.class, r -> r.empty().id(true).property("cInt", true))
                    .get();
        }

        @GET
        @Path("e2/constraints/{id}/e3s")
        public DataResponse<E3> getE2_E3s_Constrained(@PathParam("id") int id, @Context UriInfo uriInfo) {

            return AgJaxrs.select(E3.class, config)
                    .parent(E2.class, id, "e3s")
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(E3.class, PropertyFilteringRulesBuilder::idOnly)
                    .get();
        }
    }
}
