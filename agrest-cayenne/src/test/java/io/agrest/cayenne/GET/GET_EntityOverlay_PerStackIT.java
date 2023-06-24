package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E22;
import io.agrest.cayenne.cayenne.main.E25;
import io.agrest.cayenne.cayenne.main.E3;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs2.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.AgRuntimeBuilder;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SelectById;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_EntityOverlay_PerStackIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E22.class, E25.class, E2.class, E3.class)
            .agCustomizer(GET_EntityOverlay_PerStackIT::addOverlay)
            .build();

    private static AgRuntimeBuilder addOverlay(AgRuntimeBuilder builder) {

        // creating an adhoc relationship between two persistent objects with a custom resolver
        AgEntityOverlay<E22> e22Overlay = AgEntity
                .overlay(E22.class)
                .toOne("overlayToOne", E25.class, GET_EntityOverlay_PerStackIT::findForParent);

        AgEntityOverlay<E2> e2Overlay = AgEntity
                .overlay(E2.class)
                .readablePropFilter(p -> p.property("id_", false));

        return builder.entityOverlay(e22Overlay).entityOverlay(e2Overlay);
    }

    private static E25 findForParent(E22 parent) {
        return SelectById
                .query(E25.class, Cayenne.intPKForObject(parent) * 2)
                .selectOne(parent.getObjectContext());
    }

    @Test
    public void redefineToOne() {

        tester.e22().insertColumns("id")
                .values(1)
                .values(2).exec();

        tester.e25().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4).exec();

        tester.target("/e22")
                .queryParam("include", "id")
                .queryParam("include", "overlayToOne")
                .queryParam("sort", "id")

                .get().wasOk().bodyEquals(2,
                "{\"id\":1,\"overlayToOne\":{\"id\":2}}",
                "{\"id\":2,\"overlayToOne\":{\"id\":4}}");
    }

    @Test
    public void overlay_HidingId() {

        tester.e2().insertColumns("id_", "name", "address").values(1, "N", "A").exec();
        tester.e3().insertColumns("id_", "name", "phone_number", "e2_id")
                .values(4, "N", "P", 1)
                .exec();

        tester.target("e2/noid")
                .queryParam("include", "name", "e3s.id")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"e3s\":[{\"id\":4}],\"name\":\"N\"}");
    }

    @Path("")
    public static final class Resource {
        @Context
        private Configuration config;

        @GET
        @Path("e22")
        public DataResponse<E22> getE22(@Context UriInfo uriInfo) {
            return AgJaxrs.select(E22.class, config).clientParams(uriInfo.getQueryParameters()).get();
        }

        @GET
        @Path("e2/noid")
        public DataResponse<E2> getE2_NoId(@Context UriInfo uriInfo) {

            return AgJaxrs.select(E2.class, config)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
