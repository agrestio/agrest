package com.nhl.link.rest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.ClientDataResponse;
import com.nhl.link.rest.client.ClientSimpleResponse;
import com.nhl.link.rest.client.LinkRestClient;
import com.nhl.link.rest.client.LinkRestClientException;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;

import static com.nhl.link.rest.client.it.noadapter.EntityUtil.createE2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DELETE_Client_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
    }

    @Test
    public void testClient_Delete() {

        // Create new entity
        ClientDataResponse<JsonNode> r1 = LinkRestClient.client(target("/e2"))
                .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                .post(JsonNode.class, "{\"name\":\"xxx\"}");

        assertEquals(Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int id = r1.getData().get(0).get(E2.ID_PK_COLUMN).asInt();
        JsonNode e2 = createE2(id, "xxx");
        assertEquals(e2, r1.getData().get(0));

        // Delete existing entity
        ClientSimpleResponse r2 = LinkRestClient.client(target("/e2/" + id))
                .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                .delete();

        assertEquals(Status.OK, r2.getStatus());

        // Try to fetch deleted entity
        LinkRestClientException e = null;
        try {
            LinkRestClient.client(target("/e2/" + id))
                    .exclude(E2.ADDRESS.getName(), E2.E3S.getName())
                    .get(JsonNode.class);
        } catch (LinkRestClientException e1) {
            e = e1;
        }
        assertNotNull(e);
        assertTrue(e.getMessage().startsWith("Server returned 404 (Not Found)"));
    }
}
