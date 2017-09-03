package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.annotation.LinkType;
import com.nhl.link.rest.annotation.LrResource;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GET_Metadata_CustomBase_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(R1.class);
    }

    @Override
    protected LinkRestBuilder doConfigure() {
        return super.doConfigure().baseUrl("https://example.org");
    }

    @Test
    public void testGetMetadataForResource() {

        Response r = target("/r1/meta").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());

        String json = r.readEntity(String.class);
        assertTrue(json.contains("{\"href\":\"https://example.org/r1/meta\",\"type\":\"metadata\",\"operations\":[{\"method\":\"GET\"}]}"));
    }

    @Path("r1")
    public static class R1 {

        @Context
        private Configuration config;

        @GET
        @Path("meta")
        @LrResource(entityClass = E5.class, type = LinkType.METADATA)
        public MetadataResponse<E5> getMetadata(@Context UriInfo uriInfo) {
            return LinkRest.metadata(E5.class, config).forResource(R1.class).uri(uriInfo).process();
        }
    }

}
