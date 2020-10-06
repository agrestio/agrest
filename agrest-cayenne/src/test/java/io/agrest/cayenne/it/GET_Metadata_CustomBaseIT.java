package io.agrest.cayenne.it;

import io.agrest.Ag;
import io.agrest.MetadataResponse;
import io.agrest.annotation.AgResource;
import io.agrest.annotation.LinkType;
import io.agrest.cayenne.unit.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E5;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.*;

public class GET_Metadata_CustomBaseIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(ab -> ab.baseUrl("https://example.org"), Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[0];
    }

    @Test
    public void testGetMetadataForResource() {

        Response r = target("/r1/meta").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        assertTrue(json.contains("{\"href\":\"https://example.org/r1/meta\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}"));
    }

    @Path("r1")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("meta")
        @AgResource(entityClass = E5.class, type = LinkType.METADATA)
        public MetadataResponse<E5> getMetadata(@Context UriInfo uriInfo) {
            return Ag.metadata(E5.class, config).forResource(Resource.class).uri(uriInfo).process();
        }
    }

}
