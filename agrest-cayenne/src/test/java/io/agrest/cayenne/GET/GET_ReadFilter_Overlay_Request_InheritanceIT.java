package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Ie1Super;
import io.agrest.cayenne.cayenne.inheritance.Ie2;
import io.agrest.cayenne.cayenne.inheritance.Ie3;
import io.agrest.cayenne.unit.inheritance.InheritanceDbTest;
import io.agrest.cayenne.unit.inheritance.InheritanceModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public class GET_ReadFilter_Overlay_Request_InheritanceIT extends InheritanceDbTest {

    @BQTestTool
    static final InheritanceModelTester tester = tester(Resource.class)
            .entities(Ie1Super.class, Ie2.class, Ie3.class)
            .build();

    @Test
    public void super_FilterSuperAttributes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "allowed_v01", "v11", null)
                .values(20, 2, "v02", null, "v21")
                .values(30, 2, "allowed_v02", null, "v22")
                .exec();

        tester.target("/ie1super-filter-super-attributes")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":10,\"a0\":\"allowed_v01\",\"a1\":\"v11\",\"type\":1}",
                        "{\"id\":30,\"a0\":\"allowed_v02\",\"a2\":\"v22\",\"type\":2}");
    }

    @Test
    public void super_FilterSubAttributes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "allowed_v11", null)
                .values(15, 1, "v01", "v12", null)
                .values(20, 2, "v02", null, "v21")
                .exec();

        tester.target("/ie1super-filter-sub-attributes")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":10,\"a0\":\"v01\",\"a1\":\"allowed_v11\",\"type\":1}",
                        "{\"id\":20,\"a0\":\"v02\",\"a2\":\"v21\",\"type\":2}");
    }

    @Test
    public void super_FilterSuperAttributesAtSub() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(15, 1, "allowed_v01", "v12", null)
                .values(20, 2, "allowed_v02", null, "v21")
                .values(25, 2, "v02", null, "v22")
                .exec();

        tester.target("/ie1super-filter-super-attributes-at-sub")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":15,\"a0\":\"allowed_v01\",\"a1\":\"v12\",\"type\":1}",
                        "{\"id\":20,\"a0\":\"allowed_v02\",\"a2\":\"v21\",\"type\":2}");
    }

    @Test
    public void super_FilterSuperAttributesAtSubIgnoreSuper() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(15, 1, "v01", "v12", null)
                .values(20, 2, "allowed_v02", null, "v21")
                .values(25, 2, "v02", null, "v22")
                .exec();

        tester.target("/ie1super-filter-super-attributes-at-sub-ignore-super")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":10,\"a0\":\"v01\",\"a1\":\"v11\",\"type\":1}",
                        "{\"id\":15,\"a0\":\"v01\",\"a1\":\"v12\",\"type\":1}",
                        "{\"id\":20,\"a0\":\"allowed_v02\",\"a2\":\"v21\",\"type\":2}");
    }

    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("ie1super-filter-super-attributes")
        public DataResponse<Ie1Super> ie1super1(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .entityOverlay(AgEntity.overlay(Ie1Super.class).readFilter(o -> o.getA0().startsWith("allowed")))
                    .get();
        }

        @GET
        @Path("ie1super-filter-sub-attributes")
        public DataResponse<Ie1Super> ie1super2(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .entityOverlay(AgEntity.overlay(Ie1Sub1.class).readFilter(o -> o.getA1().startsWith("allowed")))
                    .get();
        }

        @GET
        @Path("ie1super-filter-super-attributes-at-sub")
        public DataResponse<Ie1Super> ie1super3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .entityOverlay(AgEntity.overlay(Ie1Super.class).readFilter(o -> o.getA0().startsWith("allowed")))

                    // this filter is combined with the super filter, and hence has no effect
                    .entityOverlay(AgEntity.overlay(Ie1Sub1.class).readFilter(o -> true))
                    .get();
        }

        @GET
        @Path("ie1super-filter-super-attributes-at-sub-ignore-super")
        public DataResponse<Ie1Super> ie1super4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .entityOverlay(AgEntity.overlay(Ie1Super.class).readFilter(o -> o.getA0().startsWith("allowed")))

                    // this filter overrides the super filter
                    .entityOverlay(AgEntity.overlay(Ie1Sub1.class).readFilter(o -> true).ignoreSuperReadFilter())
                    .get();
        }
    }
}
