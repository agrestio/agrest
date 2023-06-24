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

public class GET_PropFilter_Overlay_Runtime_Inheritance3IT extends InheritanceDbTest {

    @BQTestTool
    static final InheritanceModelTester tester = tester(Resource.class)
            .entities(Ie1Super.class, Ie2.class, Ie3.class)
            .agCustomizer(b -> b
                    .entityOverlay(AgEntity.overlay(Ie1Super.class).readablePropFilter(r -> r.attributes(true).property("a0", false)))
                    .entityOverlay(AgEntity.overlay(Ie1Sub1.class).readablePropFilter(r -> r.attributes(true).property("a0", true))))
            .build();

    @Test
    public void testSuper_ReincludeSuperAttributes() {

        tester.ie1().insertColumns("id", "type", "a0", "a1", "a2")
                .values(10, 1, "v01", "v11", null)
                .values(20, 2, "v02", null, "v21")
                .exec();

        tester.target("/")
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
        public DataResponse<Ie1Super> ie1super3(@Context UriInfo uriInfo) {
            return AgJaxrs.select(Ie1Super.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
