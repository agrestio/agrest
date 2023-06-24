package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Ie1Super;
import io.agrest.cayenne.cayenne.inheritance.Ie2;
import io.agrest.cayenne.cayenne.inheritance.Ie3;
import io.agrest.cayenne.unit.inheritance.InheritanceDbTest;
import io.agrest.cayenne.unit.inheritance.InheritanceModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_PropFilter_Overlay_Request_InheritanceIT extends InheritanceDbTest {

    @BQTestTool
    static final InheritanceModelTester tester = tester(Resource.class)
            .entities(Ie1Super.class, Ie2.class, Ie3.class)
            .build();

    @Test
    public void testSuper_ExcludeSuperAttributes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(20, 2, "v02", null, "v21")
                .exec();

        tester.target("/ie1super-exclude-super-attributes")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":10,\"a1\":\"v11\",\"type\":1}",
                        "{\"id\":20,\"a2\":\"v21\",\"type\":2}");
    }

    @Test
    public void testSuper_ExcludeSubAttributes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(20, 2, "v02", null, "v21")
                .exec();

        tester.target("/ie1super-exclude-sub-attributes")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":10,\"a0\":\"v01\",\"type\":1}",
                        "{\"id\":20,\"a0\":\"v02\",\"a2\":\"v21\",\"type\":2}");
    }

    @Test
    public void testSuper_ReincludeSuperAttributes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(20, 2, "v02", null, "v21")
                .exec();

        tester.target("/ie1super-reinclude-super-attributes")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\",\"type\":1}",
                        "{\"id\":20,\"a2\":\"v21\",\"type\":2}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("ie1super-exclude-super-attributes")
        public DataResponse<Ie1Super> ie1super1(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(Ie1Super.class, r -> r.attributes(true).property("a0", false))
                    .get();
        }

        @GET
        @Path("ie1super-exclude-sub-attributes")
        public DataResponse<Ie1Super> ie1super2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(Ie1Sub1.class, r -> r.attributes(true).property("a1", false))
                    .get();
        }

        @GET
        @Path("ie1super-reinclude-super-attributes")
        public DataResponse<Ie1Super> ie1super3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .propFilter(Ie1Super.class, r -> r.attributes(true).property("a0", false))
                    .propFilter(Ie1Sub1.class, r -> r.attributes(true).property("a0", true))
                    .get();
        }
    }
}
