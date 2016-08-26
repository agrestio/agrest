package com.nhl.link.rest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.ClientDataResponse;
import com.nhl.link.rest.client.LinkRestClient;
import com.nhl.link.rest.client.protocol.Include;
import com.nhl.link.rest.it.fixture.JerseyTestOnDerby;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.resource.E2Resource;
import com.nhl.link.rest.it.fixture.resource.E3Resource;
import org.junit.Test;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;

import static com.nhl.link.rest.client.it.noadapter.EntityUtil.createE2;
import static com.nhl.link.rest.client.it.noadapter.EntityUtil.createE3;
import static org.junit.Assert.assertEquals;

public class POST_Client_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(E2Resource.class);
        context.register(E3Resource.class);
    }

    @Test
    public void testClient_Post() {

        // Create related entity
        ClientDataResponse<JsonNode> r1 = LinkRestClient.client(target("/e3"))
                .exclude(E3.PHONE_NUMBER.getName())
                .post(JsonNode.class, "{\"name\":\"ccc\"}");

        assertEquals(Status.CREATED, r1.getStatus());
        assertEquals(1, r1.getTotal());

        int e3_id = r1.getData().get(0).get(E3.ID_PK_COLUMN).asInt();
        JsonNode e3 = createE3(e3_id, "ccc");
        assertEquals(e3, r1.getData().get(0));

        // Create parent entity
        ClientDataResponse<JsonNode> r2 = LinkRestClient.client(target("/e2"))
                .exclude(E2.ADDRESS.getName(), E2.E3S.dot(E3.PHONE_NUMBER).getName())
                .include(Include.path(E2.E3S.getName()))
                .post(JsonNode.class, "{\"name\":\"xxx\",\"address\":\"yyy\",\"e3s\":[1]}");

        assertEquals(Status.CREATED, r2.getStatus());
        assertEquals(1, r2.getTotal());

        int e2_id = r2.getData().get(0).get(E2.ID_PK_COLUMN).asInt();
        assertEquals(createE2(e2_id, "xxx", e3), r2.getData().get(0));
    }
}
