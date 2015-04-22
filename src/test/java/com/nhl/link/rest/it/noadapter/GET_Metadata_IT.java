package com.nhl.link.rest.it.noadapter;

import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class GET_Metadata_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
    }

    @Test
    public void testGetMetadataForResource() {
        Response response1 = target("/e2/metadata").request().get();
		assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    }

}
