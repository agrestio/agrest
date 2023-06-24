package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.inheritance.Ie1Sub1;
import io.agrest.cayenne.cayenne.inheritance.Ie1Super;
import io.agrest.cayenne.cayenne.inheritance.Ie2;
import io.agrest.cayenne.cayenne.inheritance.Ie3;
import io.agrest.cayenne.unit.inheritance.InheritanceDbTest;
import io.agrest.cayenne.unit.inheritance.InheritanceModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class ReadFilter_Overlay_Runtime_Inheritance4IT extends InheritanceDbTest {

    @BQTestTool
    static final InheritanceModelTester tester = tester(Resource.class)
            .entities(Ie1Super.class, Ie2.class, Ie3.class)
            .agCustomizer(c -> c
                    .entityOverlay(AgEntity.overlay(Ie1Super.class).readFilter(o -> o.getA0().startsWith("allowed")))

                    // this filter overrides the super filter
                    .entityOverlay(AgEntity.overlay(Ie1Sub1.class).readFilter(o -> true).ignoreSuperReadFilter()))
            .build();

    @Test
    public void super_FilterSuperAttributesAtSubIgnoreSuper() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(15, 1, "v01", "v12", null)
                .values(20, 2, "allowed_v02", null, "v21")
                .values(25, 2, "v02", null, "v22")
                .exec();

        tester.target("/")
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
        public DataResponse<Ie1Super> ie1super4(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
