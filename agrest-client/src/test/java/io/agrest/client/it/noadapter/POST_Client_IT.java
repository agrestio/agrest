package io.agrest.client.it.noadapter;

import com.fasterxml.jackson.databind.JsonNode;

import io.agrest.DataResponse;
import io.agrest.LinkRest;
import io.agrest.client.ClientDataResponse;
import io.agrest.client.LinkRestClient;
import io.agrest.client.protocol.Include;
import io.agrest.it.fixture.JerseyTestOnDerby;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static io.agrest.client.it.noadapter.EntityUtil.createE2;
import static io.agrest.client.it.noadapter.EntityUtil.createE3;
import static org.junit.Assert.assertEquals;

public class POST_Client_IT extends JerseyTestOnDerby {

    @Override
    protected void doAddResources(FeatureContext context) {
        context.register(Resource.class);
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

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @POST
        @Path("e2")
        public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
            return LinkRest.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
        }

        @POST
        @Path("e3")
        public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
            return LinkRest.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
        }
    }
}
