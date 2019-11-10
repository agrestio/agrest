package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E22;
import io.agrest.it.fixture.cayenne.E25;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.query.SelectById;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.*;

public class GET_EntityOverlay_PerStack_PersistentToPersistentIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {

        // creating an adhoc relationship between two persistent objects with a custom resolver
        AgEntityOverlay<E22> e22Overlay = AgEntity
                .overlay(E22.class)
                .redefineToOne("overlayToOne", E25.class, p -> findForParent(p));

        startTestRuntime(
                ab -> ab.entityOverlay(e22Overlay),
                Resource.class);
    }

    private static E25 findForParent(E22 parent) {
        return SelectById
                .query(E25.class, Cayenne.intPKForObject(parent) * 2)
                .selectOne(parent.getObjectContext());
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E22.class, E25.class};
    }

    @Test
    public void testRedefineToOne_Meta() {
        Response r = target("/e22/meta").request().get();
        String data = onSuccess(r).getContentAsString();
        assertTrue("Unexpected metadata: " + data,
                data.contains("{\"name\":\"overlayToOne\",\"type\":\"E25\",\"relationship\":true}"));
    }

    @Test
    public void testRedefineToOne() {

        e22().insertColumns("id")
                .values(1)
                .values(2).exec();

        e25().insertColumns("id")
                .values(1)
                .values(2)
                .values(3)
                .values(4).exec();

        Response r = target("/e22")
                .queryParam("include", "id")
                .queryParam("include", "overlayToOne")
                .queryParam("sort", "id")
                .request()
                .get();

        onSuccess(r).bodyEquals(2,
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
