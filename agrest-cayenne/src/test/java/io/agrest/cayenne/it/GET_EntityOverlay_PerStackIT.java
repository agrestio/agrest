package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.cayenne.unit.CayenneAgTester;
import io.agrest.cayenne.unit.DbTest;
import io.agrest.it.fixture.cayenne.E22;
import io.agrest.it.fixture.cayenne.E25;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.AgBuilder;
import io.bootique.junit5.BQTestTool;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SelectById;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class GET_EntityOverlay_PerStackIT extends DbTest {

    @BQTestTool
    static final CayenneAgTester tester = tester(Resource.class)

            .entities(E22.class, E25.class)
            .agCustomizer(GET_EntityOverlay_PerStackIT::addOverlay)
            .build();

    private static AgBuilder addOverlay(AgBuilder builder) {

        // creating an adhoc relationship between two persistent objects with a custom resolver
        AgEntityOverlay<E22> e22Overlay = AgEntity
                .overlay(E22.class)
                .redefineToOne("overlayToOne", E25.class, GET_EntityOverlay_PerStackIT::findForParent);

        return builder.entityOverlay(e22Overlay);
    }

    private static E25 findForParent(E22 parent) {
        return SelectById
                .query(E25.class, Cayenne.intPKForObject(parent) * 2)
                .selectOne(parent.getObjectContext());
    }

    @Test
    public void testRedefineToOne_Meta() {
        String data = tester.target("/e22/meta").get().wasSuccess().getContentAsString();
        Assertions.assertTrue(
                data.contains("{\"name\":\"overlayToOne\",\"type\":\"E25\",\"relationship\":true}"),
                "Unexpected metadata: " + data);
    }

    @Test
    public void testRedefineToOne() {

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

                .get().wasSuccess().bodyEquals(2,
                "{\"id\":1,\"overlayToOne\":{\"id\":2}}",
                "{\"id\":2,\"overlayToOne\":{\"id\":4}}");
    }

    @Path("")
    public static final class Resource {
        @Context
        private Configuration config;

        @GET
        @Path("e22")
        public DataResponse<E22> getE22(@Context UriInfo uriInfo) {
            return Ag.service(config).select(E22.class).uri(uriInfo).get();
        }

        @GET
        @Path("e22/meta")
        public MetadataResponse<E22> getMetaE22(@Context UriInfo uriInfo) {
            return Ag.metadata(E22.class, config).forResource(Resource.class).uri(uriInfo).process();
        }
    }
}
