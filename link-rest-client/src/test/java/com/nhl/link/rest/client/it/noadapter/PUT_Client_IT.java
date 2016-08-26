package com.nhl.link.rest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.ClientDataResponse;
import com.nhl.link.rest.client.LinkRestClient;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static com.nhl.link.rest.client.it.noadapter.EntityUtil.createE3;
import static org.junit.Assert.assertEquals;

public class PUT_Client_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E3Resource.class);
    }

    @Test
    public void testClient_Put() {

        // Create new entity
        ClientDataResponse<JsonNode> r1 = LinkRestClient.client(target("/e3"))
                .exclude(E3.PHONE_NUMBER.getName())
                .post(JsonNode.class, "{\"name\":\"ccc\"}");

        assertEquals(Response.Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int id = r1.getData().get(0).get(E3.ID_PK_COLUMN).asInt();
        JsonNode e3_before_update = createE3(id, "ccc");
        assertEquals(e3_before_update, r1.getData().get(0));

        // Update existing entity
        ClientDataResponse<JsonNode> r2 = LinkRestClient.client(target("/e3"))
                .exclude(E3.PHONE_NUMBER.getName())
                .put(JsonNode.class, "{\"id\":" + id + ",\"name\":\"ddd\"}");

        assertEquals(Response.Status.OK, r2.getStatus());
        assertEquals(1, r2.getTotal());

        JsonNode e3_after_update = createE3(id, "ddd");
        assertEquals(e3_after_update, r2.getData().get(0));
    }
}
